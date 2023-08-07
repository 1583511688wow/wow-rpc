package com.ljh.channelHandler;

import com.ljh.channelHandler.handler.MySimpleChannelInboundHandler;
import com.ljh.channelHandler.handler.RpcRequestEncoder;
import com.ljh.channelHandler.handler.RpcResponseDecoder;
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
                .addLast(new RpcRequestEncoder())
                //入栈解码器
                .addLast(new RpcResponseDecoder())
                //处理结果
                .addLast(new MySimpleChannelInboundHandler());

    }
}
