package com.ljh;

import com.ljh.discovery.Registry;
import com.ljh.discovery.RegistryConfig;
import com.ljh.exceptions.NetworkException;
import com.ljh.untils.netty.NettyBootstrapInitializer;
import com.ljh.untils.zookeeper.ZookeeperUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author ljh
 * @param <T>
 */
public class ReferenceConfig<T> {

    private static final Logger log = LoggerFactory.getLogger(ReferenceConfig.class);

    private Class<T> interfaceRef;


    private Registry registry;

    public Registry getRegistry() {
        return registry;
    }

    public Class<T> getInterfaceRef() {
        return interfaceRef;
    }

    public void setInterfaceRef(Class<T> interfaceRef) {
        this.interfaceRef = interfaceRef;
    }

    public void setRegistry(Registry registry) {
        this.registry = registry;
    }

    /**
     * 生成代理对象调用方法
     * @return
     */
    public T get() {

        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        Class[] calsses = new Class[]{interfaceRef};
        Object result = Proxy.newProxyInstance(contextClassLoader, calsses, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

                //发现服务，从注册中心，寻找一个可用的服务
                //传入服务的名字，返回 IP + 端口
                //todo: 我们每次调用相关方法的时候都要去注册中心取拉取列表
                InetSocketAddress inetSocketAddress = registry.lookUp(interfaceRef.getName());

                //使用netty连接服务器，发送调用的服务名字 + 方法名字 + 参数列表，得到结果

                //从全局缓存中得一个通道
                Channel channel = RpcBootstrap.CHANNEL_CACHE.get(inetSocketAddress);
                if (channel == null){

                    //生成channel，异步拿到channel
                    CompletableFuture<Channel> future = new CompletableFuture<>();
                    NettyBootstrapInitializer.getBootstrap()
                            .connect(inetSocketAddress)
                            .addListener((ChannelFutureListener) promise -> {
                                if (!promise.isSuccess()){
                                    //存放异常
                                    System.out.println("异常");
                                    future.completeExceptionally(promise.cause());
                                }
                                log.info("已经和【{}】成功建立了连接", inetSocketAddress);
                                future.complete(promise.channel());
                            });

                    //获取结果 channel
                     channel = future.get(3, TimeUnit.SECONDS);

                    //再缓存channel
                    RpcBootstrap.CHANNEL_CACHE.put(inetSocketAddress, channel);
                }

                if (channel == null){
                    throw new NetworkException("获取通道channel发生异常");

                }

                CompletableFuture<Object> future = new CompletableFuture<>();
                //todo: 需要拿到返回值 将CompletableFuture暴露出去  1
                channel.writeAndFlush(Unpooled.copiedBuffer("6666666".getBytes())).addListener((ChannelFutureListener) promise -> {
                    //promise返回 writeAndFlush的结果
                 if (!promise.isSuccess()){

                     future.completeExceptionally(promise.cause());
                 }

                });

             //   Object o = future.get(3, TimeUnit.SECONDS);
                return null;
            }
        });

        return (T)result;
    }
}
