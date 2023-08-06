package com.ljh.channelHandler.handler;

import com.ljh.RpcBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;

/**
 *
 * @author ljh
 */
public class MySimpleChannelInboundHandler extends SimpleChannelInboundHandler {

    private static final Logger log = LoggerFactory.getLogger(MySimpleChannelInboundHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object msg) throws Exception {



        //服务提供方，返回的结果
        ByteBuf byteBuf = (ByteBuf) msg;
        String result = byteBuf.toString(Charset.defaultCharset());


        log.info("在netty中收到" + result);

        CompletableFuture<Object> future = RpcBootstrap.PENDING_REQUEST.get(1L);
        future.complete(result);


    }
}
