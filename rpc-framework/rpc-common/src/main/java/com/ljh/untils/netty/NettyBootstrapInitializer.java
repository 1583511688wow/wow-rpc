package com.ljh.untils.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;

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
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(new SimpleChannelInboundHandler() {
                            @Override
                            protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object msg) throws Exception {

                                ByteBuf byteBuf = (ByteBuf) msg;
                                log.info("msg----->", byteBuf.toString(Charset.defaultCharset()));
                                System.out.println(byteBuf.toString(Charset.defaultCharset()));

                            }
                        });
                    }
                });

    }

    public NettyBootstrapInitializer() {
    }
    public static Bootstrap getBootstrap() {
        return bootstrap;
    }


}
