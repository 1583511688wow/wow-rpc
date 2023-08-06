package com.ljh.channelHandler;

import com.ljh.channelHandler.handler.MySimpleChannelInboundHandler;
import com.ljh.channelHandler.handler.RpcMessageEncoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 *
 * @author ljh
 */
public class ConsumerChannelInitializer extends ChannelInitializer<SocketChannel> {


    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        socketChannel.pipeline()
                //日志处理器
                .addLast(new LoggingHandler(LogLevel.DEBUG))
                //消息编码器
                .addLast(new RpcMessageEncoder())
                .addLast(new MySimpleChannelInboundHandler());

    }
}
