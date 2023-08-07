package com.ljh.channelHandler.handler;

import com.ljh.RpcBootstrap;
import com.ljh.transport.message.RpcResponse;
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
public class MySimpleChannelInboundHandler extends SimpleChannelInboundHandler<RpcResponse> {

    private static final Logger log = LoggerFactory.getLogger(MySimpleChannelInboundHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcResponse rpcResponse) throws Exception {



        //服务提供方，返回的结果
        Object body = rpcResponse.getObject();

        //从全局挂起的清求中寻找匹配待处理的future
        CompletableFuture<Object> future = RpcBootstrap.PENDING_REQUEST.get(1L);
        future.complete(body);
        log.info("已经找到编号为【{}】的completable，处理响应的结果已在", rpcResponse.getRequestId());


    }
}
