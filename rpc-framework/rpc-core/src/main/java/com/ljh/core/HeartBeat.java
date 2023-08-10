package com.ljh.core;

import com.ljh.RpcBootstrap;
import com.ljh.compress.CompressorFactory;
import com.ljh.discovery.Registry;
import com.ljh.enumeration.RequestType;
import com.ljh.netty.NettyBootstrapInitializer;
import com.ljh.serialize.SerializerFactory;
import com.ljh.transport.message.RpcRequest;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * 心跳检测
 */
public class HeartBeat {

    private static final Logger log = LoggerFactory.getLogger(HeartBeat.class);

    public static void detectHeartbeat(String serviceName){

        //从注册中心拉取服务列表
        Registry registry = RpcBootstrap.getInstance().getRegistry();
        List<InetSocketAddress> inetSocketAddresses = registry.lookUp(serviceName);

        // 将连接进行缓存
        for (InetSocketAddress address : inetSocketAddresses) {
            try {
                if (!RpcBootstrap.CHANNEL_CACHE.containsKey(address)) {
                    Channel channel = NettyBootstrapInitializer.getBootstrap().connect(address).sync().channel();
                    RpcBootstrap.CHANNEL_CACHE.put(address, channel);
                }

            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }


        // 3、任务，定期发送消息
        Thread thread = new Thread(() ->
                new Timer().scheduleAtFixedRate(new MyTimerTask(), 0, 2000)
                , "rpc-HeartbeatDetector-thread");
        thread.setDaemon(true);
        thread.start();

    }


    private static class MyTimerTask extends TimerTask {
        @Override
        public void run() {

            //将响应时长的channel清空
            RpcBootstrap.ANSWER_TIME.clear();

            Map<InetSocketAddress, Channel> channelCache = RpcBootstrap.CHANNEL_CACHE;
            for (Map.Entry<InetSocketAddress, Channel> entry : channelCache.entrySet()){

                Channel channel = entry.getValue();



                long start = System.currentTimeMillis();

                // 构建一个心跳请求
                RpcRequest rpcRequest = RpcRequest.builder()
                        .requestId(RpcBootstrap.idGenerator.getId())
                        .compressType(CompressorFactory.getCompressor(RpcBootstrap.COMPRESS_TYPE).getCode())
                        .requestType(RequestType.HEART_BEAT.getId())
                        .serializeType(SerializerFactory.getSerializer(RpcBootstrap.serializeType).getCode())
                        .timeStamp(start)
                        .build();



                CompletableFuture<Object> future = new CompletableFuture<>();
                //放进全局map
                RpcBootstrap.PENDING_REQUEST.put(rpcRequest.getRequestId(), future);


                channel.writeAndFlush(rpcRequest).addListener((ChannelFutureListener) promise -> {
                    //promise返回 writeAndFlush的结果 异常就捕获
                    if (!promise.isSuccess()){

                        future.completeExceptionally(promise.cause());
                    }
                });

                Long endTime = null;
                try {
                   future.get();
                    endTime = System.currentTimeMillis();
                } catch (InterruptedException | ExecutionException e) {

                    e.printStackTrace();
                }

                Long time = endTime - start;

                //使用treemap进行缓存
                RpcBootstrap.ANSWER_TIME.put(time, channel);
                log.info("和[{}]服务器的响应时间是[{}].", entry.getKey(), time);


            }


        }
    }
}
