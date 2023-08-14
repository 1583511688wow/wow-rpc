package com.ljh;

import com.ljh.annotation.WowApi;
import com.ljh.channelHandler.handler.MethodCallHandler;
import com.ljh.channelHandler.handler.RpcRequestDecoder;
import com.ljh.channelHandler.handler.RpcResponseEncoder;
import com.ljh.config.Configuration;
import com.ljh.core.HeartBeat;
import com.ljh.core.RpcShutdownHook;
import com.ljh.discovery.RegistryConfig;
import com.ljh.loadbalancer.LoadBalancer;
import com.ljh.transport.message.RpcRequest;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author ljh
 * rpc 启动器
 */

public class RpcBootstrap {
    //日志
    private static final Logger log = LoggerFactory.getLogger(RpcBootstrap.class);
    private static final RpcBootstrap rpcBootstrap = new RpcBootstrap();


    //全局的配置中心
    private Configuration configuration;


    public final static Map<InetSocketAddress, Channel> CHANNEL_CACHE = new ConcurrentHashMap<>(16);
    //维护已经发布暴露的服务列表 k -> 接口全限定名  value -> ServiceConfig
    public static Map<String, ServiceConfig<?>> serviceList = new ConcurrentHashMap<>();

    //定义全局的 completableFuture
    public final static Map<Long, CompletableFuture<Object>> PENDING_REQUEST = new ConcurrentHashMap<>();
    public final static TreeMap<Long, Channel> ANSWER_TIME = new TreeMap<>();

    // 保存request对象，可以到当前线程中随时获取
    public static final ThreadLocal<RpcRequest> REQUEST_THREAD_LOCAL = new ThreadLocal<>();



    public RpcBootstrap() {

         configuration = new Configuration();
    }


    public static RpcBootstrap getInstance() {


        return rpcBootstrap;
    }

    public Configuration getConfiguration() {
        return configuration;
    }


    /**
     * 定义程序的名字
     * @param appName
     * @return
     */
    public RpcBootstrap application(String appName) {

        configuration.setAppName(appName);
        return this;
    }

    /**
     * 配置一个注册中心
     * @param registryConfig
     * @return
     */
    public RpcBootstrap registry(RegistryConfig registryConfig) {

        configuration.setRegistryConfig(registryConfig);
        return this;
    }


    /**
     * 配置负载均衡策略
     * @param loadBalancer 注册中心
     * @return this当前实例
     */
    public RpcBootstrap loadBalancer(LoadBalancer loadBalancer) {
        configuration.setLoadBalancer(loadBalancer);
        return this;
    }






    /**
     * 单个发布服务
     * @param service
     * @return
     */
    public RpcBootstrap publish(ServiceConfig<?> service) {


        configuration.getRegistryConfig().getRegistry().register(service);

        serviceList.put(service.getInterface().getName(), service);

        return this;
    }

    /**
     * 批量发布服务
     * @param services
     * @return
     */
    public RpcBootstrap publish(List<ServiceConfig> services) {
        for (ServiceConfig<?> service : services) {
            this.publish(service);
        }
        return this;
    }

    /**
     * 启动 netty 服务
     */
    public void start() {


        //注册程序关闭钩子函数
        Runtime.getRuntime().addShutdownHook(new RpcShutdownHook());

        // 1、创建eventLoop，老板只负责处理请求，之后会将请求分发至worker
        EventLoopGroup boss = new NioEventLoopGroup(2);
        EventLoopGroup worker = new NioEventLoopGroup(10);
        try {

            // 2、需要一个服务器引导程序
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            // 3、配置服务器
            serverBootstrap = serverBootstrap.group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            // 是核心，我们需要添加很多入站和出站的handler
                            socketChannel.pipeline()
                                    .addLast(new LoggingHandler())
                                    //解码器
                                    .addLast(new RpcRequestDecoder())
                                    .addLast(new MethodCallHandler())
                                    .addLast(new RpcResponseEncoder())

                            ;
                        }
                    });

            // 4、绑定端口
            ChannelFuture channelFuture = serverBootstrap.bind(configuration.getPort()).sync();

            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                boss.shutdownGracefully().sync();
                worker.shutdownGracefully().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public RpcBootstrap reference(ReferenceConfig<?> reference) {

        HeartBeat.detectHeartbeat(reference.getInterfaceRef().getName());

        reference.setRegistry(configuration.getRegistryConfig().getRegistry());
        reference.setGroup(this.getConfiguration().getGroup());
        return this;
    }

    //配置序列化的方式
    public RpcBootstrap serialize(String serialize) {


        configuration.setSerializeType(serialize);
        log.info("配置的的使用序列化方式为【{}】", serialize);
        return this;

    }

    //配置序列化的方式
    public RpcBootstrap compress(String compressType) {

        configuration.setCompressType(compressType);
        log.info("配置的的使用压缩方式为【{}】", compressType);
        return this;

    }


    public RpcBootstrap scan(String packageName) {

        // 1、需要通过packageName获取其下的所有的类的权限定名称
        List<String> classNames = getAllClassNames(packageName);


        // 2、通过反射获取他的接口，构建具体实现
        List<Class<?>> classes = classNames.stream()
                .map(className -> {
                    try {
                        return Class.forName(className);
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }).filter(clazz -> clazz.getAnnotation(WowApi.class) != null)
                .collect(Collectors.toList());

        for (Class<?> clazz : classes) {
            // 获取他的接口
            Class<?>[] interfaces = clazz.getInterfaces();
            Object instance = null;
            try {
                instance = clazz.getConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                    NoSuchMethodException e) {
                throw new RuntimeException(e);
            }

            // 获取分组信息
            WowApi wowApi = clazz.getAnnotation(WowApi.class);
            String group = wowApi.group();

            for (Class<?> anInterface : interfaces) {
                ServiceConfig<?> serviceConfig = new ServiceConfig<>();
                serviceConfig.setInterface(anInterface);
                serviceConfig.setRef(instance);
                serviceConfig.setGroup(group);
                    log.debug("---->已经通过包扫描，将服务【{}】准备发布.",anInterface);
                // 3、发布
                publish(serviceConfig);
                log.debug("---->已经通过包扫描，将服务【{}】发布.",anInterface);
            }

        }
        return this;

    }

    private List<String> getAllClassNames(String packageName) {

        // 1、通过packageName获得绝对路径
        // com.ljhclass.xxx.yyy -> E://xxx/xww/sss/com/ydlclass/xxx/yyy
        String basePath = packageName.replaceAll("\\.","/");
        URL url = ClassLoader.getSystemClassLoader().getResource(basePath);
        if(url == null){
            throw new RuntimeException("包扫描时，发现路径不存在.");
        }
        String absolutePath = url.getPath();
        //
        List<String> classNames = new ArrayList<>();
        classNames = recursionFile(absolutePath,classNames,basePath);

        return classNames;


    }

    private List<String> recursionFile(String absolutePath, List<String> classNames, String basePath) {

        // 获取文件
        File file = new File(absolutePath);
        // 判断文件是否是文件夹
        if (file.isDirectory()){
            // 找到文件夹的所有的文件
            File[] children = file.listFiles(pathname -> pathname.isDirectory() || pathname.getPath().contains(".class"));
            if(children == null || children.length == 0){
                return classNames;
            }
            for (File child : children) {
                if(child.isDirectory()){
                    // 递归调用
                    recursionFile(child.getAbsolutePath(),classNames,basePath);
                } else {
                    // 文件 --> 类的权限定名称
                    String className = getClassNameByAbsolutePath(child.getAbsolutePath(),basePath);
                    classNames.add(className);
                }
            }

        } else {
            // 文件 --> 类的权限定名称
            String className = getClassNameByAbsolutePath(absolutePath,basePath);
            classNames.add(className);
        }
        return classNames;

    }

    private String getClassNameByAbsolutePath(String absolutePath,String basePath) {
        // E:\project\ydlclass-yrpc\yrpc-framework\yrpc-core\target\classes\com\ydlclass\serialize\Serializer.class
        // com\ydlclass\serialize\Serializer.class --> com.ljh.serialize.Serializer
        String fileName = absolutePath
                .substring(absolutePath.indexOf(basePath.replaceAll("/","\\\\")))
                .replaceAll("\\\\",".");

        fileName = fileName.substring(0,fileName.indexOf(".class"));
        return fileName;
    }

    public RpcBootstrap group(String primary) {

        this.getConfiguration().setGroup(primary);



        return this;

    }
}
