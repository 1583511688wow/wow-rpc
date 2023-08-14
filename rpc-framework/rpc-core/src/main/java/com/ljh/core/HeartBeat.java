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
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 心跳检测
 */
public class HeartBeat {

    private static final Logger log = LoggerFactory.getLogger(HeartBeat.class);

    public static void detectHeartbeat(String serviceName){

        //从注册中心拉取服务列表
        Registry registry = RpcBootstrap.getInstance().getConfiguration().getRegistryConfig().getRegistry();
        List<InetSocketAddress> inetSocketAddresses = registry.lookUp(serviceName, RpcBootstrap.getInstance().getConfiguration().getGroup());

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
                , "Rpc-HeartbeatDetector-thread");
        thread.setDaemon(true);
        thread.start();

    }


    private static class MyTimerTask extends TimerTask {
        @Override
        public void run() {

            // 将响应时长的map清空
            RpcBootstrap.ANSWER_TIME.clear();

            // 遍历所有的channel
            Map<InetSocketAddress, Channel> cache = RpcBootstrap.CHANNEL_CACHE;
            for (Map.Entry<InetSocketAddress, Channel> entry : cache.entrySet()) {
                // 定义一个重试的次数
                int tryTimes = 3;
                while (tryTimes > 0) {
                    // 通过心跳检测处理每一个channel
                    Channel channel = entry.getValue();

                    long start = System.currentTimeMillis();
                    // 构建一个心跳请求
                    RpcRequest rpcRequest = RpcRequest.builder()
                            .requestId(RpcBootstrap.getInstance().getConfiguration().idGenerator.getId())
                            .compressType(CompressorFactory.getCompressor(RpcBootstrap.getInstance().getConfiguration().getCompressType()).getCode())
                            .requestType(RequestType.HEART_BEAT.getId())
                            .serializeType(SerializerFactory.getSerializer(RpcBootstrap.getInstance().getConfiguration().getSerializeType()).getCode())
                            .timeStamp(start)
                            .build();

                    // 4、写出报文
                    CompletableFuture<Object> completableFuture = new CompletableFuture<>();
                    // 将 completableFuture 暴露出去
                    RpcBootstrap.PENDING_REQUEST.put(rpcRequest.getRequestId(), completableFuture);

                    channel.writeAndFlush(rpcRequest).addListener((ChannelFutureListener) promise -> {
                        if (!promise.isSuccess()) {
                            completableFuture.completeExceptionally(promise.cause());
                        }
                    });

                    Long endTime = 0L;
                    try {
                        // 阻塞方法，get()方法如果得不到结果，就会一直阻塞
                        // 我们想不一直阻塞可以添加参数
                        completableFuture.get(1, TimeUnit.SECONDS);
                        endTime = System.currentTimeMillis();
                    } catch (InterruptedException | ExecutionException | TimeoutException e) {
                        // 一旦发生问题，需要优先重试
                        tryTimes --;
                        log.error("和地址为【{}】的主机连接发生异常.正在进行第【{}】次重试......",
                                channel.remoteAddress(), 3 - tryTimes);

                        // 将重试的机会用尽，将失效的地址移出服务列表
                        if(tryTimes == 0){
                            RpcBootstrap.CHANNEL_CACHE.remove(entry.getKey());
                        }

                        // 尝试等到一段时间后重试
                        try {
                            Thread.sleep(10*(new Random().nextInt(5)));
                        } catch (InterruptedException ex) {
                            throw new RuntimeException(ex);
                        }

                        continue;
                    }
                    Long time = endTime - start;

                    // 使用treemap进行缓存
                    RpcBootstrap.ANSWER_TIME.put(time, channel);
                    log.debug("和[{}]服务器的响应时间是[{}].", entry.getKey(), time);
                    break;
                }
            }

            log.info("-----------------------响应时间的treemap----------------------");
            for (Map.Entry<Long, Channel> entry : RpcBootstrap.ANSWER_TIME.entrySet()) {
                if (log.isDebugEnabled()) {
                    log.debug("[{}]--->channelId:[{}]", entry.getKey(), entry.getValue().id());
                }
            }
        }
    }
}
