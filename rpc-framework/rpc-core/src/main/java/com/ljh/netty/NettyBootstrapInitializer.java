package com.ljh.netty;

import com.ljh.channelHandler.ConsumerChannelInitializer;
import com.ljh.channelHandler.handler.MySimpleChannelInboundHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 提供bootstrap单例
 *
 * @author ljh
 *
 */
public class NettyBootstrapInitializer {

    private static final Bootstrap bootstrap = new Bootstrap();
    private static final Logger log = LoggerFactory.getLogger(NettyBootstrapInitializer.class);


    static {
        // 定义线程池，EventLoopGroup
        NioEventLoopGroup group = new NioEventLoopGroup();
        bootstrap.group(group)
                // 选择初始化一个什么样的channel
                .channel(NioSocketChannel.class)
                .handler(new ConsumerChannelInitializer());

    }

    public NettyBootstrapInitializer() {
    }
    public static Bootstrap getBootstrap() {
        return bootstrap;
    }


}
