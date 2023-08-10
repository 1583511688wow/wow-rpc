package com.ljh.proxy;

import com.ljh.ReferenceConfig;
import com.ljh.RpcBootstrap;
import com.ljh.compress.CompressorFactory;
import com.ljh.discovery.Registry;
import com.ljh.exceptions.DiscoveryException;
import com.ljh.exceptions.NetworkException;
import com.ljh.netty.NettyBootstrapInitializer;
import com.ljh.serialize.SerializerFactory;
import com.ljh.transport.message.RequestPayload;
import com.ljh.transport.message.RpcRequest;
import com.ljh.untils.id.IdGenerator;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 封装了客户端通信的基础逻辑，通过代理对象进行远程调用
 * @author ljh
 */
public class RpcConsumerInvocationHandler implements InvocationHandler {


    private static final Logger log = LoggerFactory.getLogger(RpcConsumerInvocationHandler.class);

    private final Registry registry;
    private final Class<?> interfaceRef;


    public RpcConsumerInvocationHandler(Registry registry, Class<?> interfaceRef) {
        this.registry = registry;
        this.interfaceRef = interfaceRef;
    }



    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {



        //1.封装报文
        RequestPayload requestPayload = RequestPayload.builder()
                .interfaceName(interfaceRef.getName())
                .methodName(method.getName())
                .parametersType(method.getParameterTypes())
                .parametersValue(args)
                .returnType(method.getReturnType()).build();


        RpcRequest rpcRequest = RpcRequest.builder()
                .requestId(RpcBootstrap.idGenerator.getId())
                .compressType(CompressorFactory.getCompressor(RpcBootstrap.COMPRESS_TYPE).getCode())
                .requestType((byte) 1)
                .serializeType(SerializerFactory.getSerializer(RpcBootstrap.serializeType).getCode())
                .timeStamp(new Date().getTime())
                .requestPayload(requestPayload).build();


        RpcBootstrap.REQUEST_THREAD_LOCAL.set(rpcRequest);


        //2.发现服务,从注册中心拉取列表，并通过负载均衡的到一个可用的服务
        InetSocketAddress inetSocketAddress = RpcBootstrap.LOAD_BALANCER.selectServiceAddress(interfaceRef.getName());


        //3.建立连接获取通道channel
        Channel channel = getAvailableChannel(inetSocketAddress);


        //心跳请求判断


        //生成future
        CompletableFuture<Object> future = new CompletableFuture<>();
        //todo: 需要拿到返回值 将CompletableFuture暴露出去
        //放进全局map
        RpcBootstrap.PENDING_REQUEST.put(rpcRequest.getRequestId(), future);

        //向服务端发送消息 报文进入pipeline执行出站
        channel.writeAndFlush(rpcRequest).addListener((ChannelFutureListener) promise -> {
            //promise返回 writeAndFlush的结果 异常就捕获
            if (!promise.isSuccess()){

                future.completeExceptionally(promise.cause());
            }
        });



        RpcBootstrap.REQUEST_THREAD_LOCAL.remove();
        //返回等待netty收到的结果，根据全局future
        return future.get(30, TimeUnit.SECONDS);
    }

    /**
     * 根据 IP地址获取一个可用的通道 channel
     * @param inetSocketAddress
     * @return
     */
    private Channel getAvailableChannel(InetSocketAddress inetSocketAddress) {

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
            try {

                channel = future.get(3, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {

                log.error("获取通道时发生异常", e);
                throw new DiscoveryException(e);
            }

            //再缓存channel
            RpcBootstrap.CHANNEL_CACHE.put(inetSocketAddress, channel);
        }

        if (channel == null){
            log.error("获取与【{}】的通道 channel发生异常", inetSocketAddress);
            throw new NetworkException("获取通道channel发生异常");

        }

        log.info("已经获取了与【{}】的连接通道 channel", inetSocketAddress);

        return channel;


    }
}
