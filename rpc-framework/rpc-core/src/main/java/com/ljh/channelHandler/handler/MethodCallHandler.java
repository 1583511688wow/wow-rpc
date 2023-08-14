package com.ljh.channelHandler.handler;

import com.ljh.RpcBootstrap;
import com.ljh.ServiceConfig;
import com.ljh.core.ShutDownHolder;
import com.ljh.enumeration.RequestType;
import com.ljh.protection.RateLimiter;
import com.ljh.protection.TokenBuketRateLimiter;
import com.ljh.transport.message.RequestPayload;
import com.ljh.transport.message.RespCode;
import com.ljh.transport.message.RpcRequest;
import com.ljh.transport.message.RpcResponse;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.SocketAddress;
import java.util.Map;

/**
 * 反射方法调用
 * @author ljh
 */
public class MethodCallHandler extends SimpleChannelInboundHandler<RpcRequest> {

    private static final Logger log = LoggerFactory.getLogger(MethodCallHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcRequest rpcRequest) throws Exception {

        // 1、先封装部分响应
        RpcResponse rpcResponse = new RpcResponse();
        rpcResponse.setRequestId(rpcRequest.getRequestId());
        rpcResponse.setCompressType(rpcRequest.getCompressType());
        rpcResponse.setSerializeType(rpcRequest.getSerializeType());


        // 2、 获得通道
        Channel channel = channelHandlerContext.channel();


        // 3、查看关闭的挡板是否打开，如果挡板已经打开，返回一个错误的响应
        if( ShutDownHolder.BAFFLE.get() ){
            rpcResponse.setCode(RespCode.BECOLSING.getCode());
            channel.writeAndFlush(rpcResponse);
            return;
        }

        // 4、计数器加一
        ShutDownHolder.REQUEST_COUNTER.increment();

        //5.完成限流相关的操作
        SocketAddress socketAddress = channel.remoteAddress();
        Map<SocketAddress, RateLimiter> everyIpRateLimiter = RpcBootstrap.getInstance().getConfiguration().getEveryIpRateLimiter();
        RateLimiter rateLimiter = everyIpRateLimiter.get(socketAddress);
        if (rateLimiter == null){
            rateLimiter   = new TokenBuketRateLimiter(500, 300);
            everyIpRateLimiter.put(socketAddress, rateLimiter);
        }

        boolean allowRequest = rateLimiter.allowRequest();

        // 6、处理请求的逻辑
        // 限流
        if (!allowRequest) {
            // 需要封装响应并且返回了
            rpcResponse.setCode(RespCode.RATE_LIMIT.getCode());

            // 处理心跳
        } else if (rpcRequest.getRequestType() == RequestType.HEART_BEAT.getId()) {
            // 需要封装响应并且返回
            rpcResponse.setCode(RespCode.SUCCESS_HEART_BEAT.getCode());

            // 正常调用
        } else {
            /** ---------------具体的调用过程--------------**/
            // （1）获取负载内容
            RequestPayload requestPayload = rpcRequest.getRequestPayload();

            // （2）根据负载内容进行方法调用
            try {
                Object result = callTargetMethod(requestPayload);
                if (log.isDebugEnabled()) {
                    log.debug("请求【{}】已经在服务端完成方法调用。", rpcRequest.getRequestId());
                }
                // （3）封装响应   我们是否需要考虑另外一个问题，响应码，响应类型
                rpcResponse.setCode(RespCode.SUCCESS.getCode());
                rpcResponse.setObject(result);
            } catch (Exception e){
                log.error("编号为【{}】的请求在调用过程中发生异常。",rpcRequest.getRequestId(),e);
                rpcResponse.setCode(RespCode.FAIL.getCode());
            }
        }

        // 7、写出响应
        channel.writeAndFlush(rpcResponse);

        // 8、计数器减一
        ShutDownHolder.REQUEST_COUNTER.decrement();

    }

    private Object callTargetMethod(RequestPayload payload) {

        //方法执行结果
        Object invoke;

        String methodName = payload.getMethodName();
        String interfaceName = payload.getInterfaceName();
        Class<?>[] parametersType = payload.getParametersType();
        Object[] parametersValue = payload.getParametersValue();

        //根据接口名 得到本地服务列表匹配
        ServiceConfig<?> serviceConfig = RpcBootstrap.serviceList.get(interfaceName);

        //得到具体的实现
        Object ref = serviceConfig.getRef();
        Class<?> aClass = ref.getClass();


            try {
                Method method = null;
                method = aClass.getMethod(methodName, parametersType);
                invoke = method.invoke(ref, parametersValue);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
               log.error("调用服务【{}】的方法【{}】时发生了异常",interfaceName, methodName, e);
                throw new RuntimeException(e);
            }

        return invoke;
    }
}
