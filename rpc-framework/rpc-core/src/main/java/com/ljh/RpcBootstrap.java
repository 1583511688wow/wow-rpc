package com.ljh;

import com.ljh.discovery.Registry;
import com.ljh.discovery.RegistryConfig;
import com.ljh.discovery.impl.Op;
import com.ljh.untils.network.NetUtils;
import com.ljh.untils.zookeeper.ZookeeperNode;
import com.ljh.untils.zookeeper.ZookeeperUtils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ljh
 * rpc 启动器
 */

public class RpcBootstrap {

    private static final Logger log = LoggerFactory.getLogger(RpcBootstrap.class);

    // 定义相关的一些基础配置
    private String applicationName = "default";
    private ProtocolConfig protocolConfig;
    private int port = 8088;

    private Registry registry ;

    public final static Map<InetSocketAddress, Channel> CHANNEL_CACHE = new ConcurrentHashMap<>(16);
    //维护已经发布暴露的服务列表 k -> 接口全限定名  value -> ServiceConfig
    private static Map<String, ServiceConfig<?>> serviceList = new HashMap<>();


    private static RpcBootstrap rpcBootstrap = new RpcBootstrap();

    public RpcBootstrap() {
    }


    public static RpcBootstrap getInstance() {


        return rpcBootstrap;
    }


    /**
     * 定义程序的名字
     * @param appName
     * @return
     */
    public RpcBootstrap application(String appName) {

        this.applicationName = appName;
        return this;
    }

    /**
     * 配置一个注册中心
     * @param registryConfig
     * @return
     */
    public RpcBootstrap registry(RegistryConfig registryConfig) {


        Registry registry = registryConfig.getRegistry();
        this.registry = registry;
        return this;
    }

    /**
     * 配置当前暴露的服务使用的协议
     * @param protocolConfig
     * @return
     */
    public RpcBootstrap protocol(ProtocolConfig protocolConfig){

        this.protocolConfig = protocolConfig;

        if (log.isDebugEnabled()){

        log.info("当前工程使用了jdk");

        }

        return this;
    }

    /**
     * 单个发布服务
     * @param service
     * @return
     */
    public RpcBootstrap publish(ServiceConfig<?> service) {


        registry.register(service);

        serviceList.put(service.getInterface().getName(), service);

        return this;
    }

    /**
     * 批量发布服务
     * @param services
     * @return
     */
    public RpcBootstrap publish(List<ServiceConfig> services) {

        return this;
    }

    /**
     * 启动 netty 服务
     */
    public void start() {



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
                            socketChannel.pipeline().addLast(new SimpleChannelInboundHandler() {
                                @Override
                                protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object msg) throws Exception {
                                    ByteBuf byteBuf = (ByteBuf) msg;
                                    log.info("byteBuf--->{}", byteBuf.toString(Charset.defaultCharset()));
                                    System.out.println(byteBuf.toString(Charset.defaultCharset()));


                                    channelHandlerContext.channel().writeAndFlush(Unpooled.copiedBuffer("你好我来了".getBytes()));

                                }
                            })

                            ;
                        }
                    });

            // 4、绑定端口
            ChannelFuture channelFuture = serverBootstrap.bind(port).sync();

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

        reference.setRegistry(registry);
        return this;
    }
}
