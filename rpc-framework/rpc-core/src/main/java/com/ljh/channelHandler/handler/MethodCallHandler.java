package com.ljh.channelHandler.handler;

import com.ljh.RpcBootstrap;
import com.ljh.ServiceConfig;
import com.ljh.enumeration.RequestType;
import com.ljh.transport.message.RequestPayload;
import com.ljh.transport.message.RespCode;
import com.ljh.transport.message.RpcRequest;
import com.ljh.transport.message.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 反射方法调用
 * @author ljh
 */
public class MethodCallHandler extends SimpleChannelInboundHandler<RpcRequest> {

    private static final Logger log = LoggerFactory.getLogger(MethodCallHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcRequest rpcRequest) throws Exception {

        //获取负载内容
        RequestPayload payload = rpcRequest.getRequestPayload();

        Object result = null;

        if (! (rpcRequest.getRequestType() == RequestType.HEART_BEAT.getId())){


            //根据负载内容进行调用得到结果
             result = callTargetMethod(payload);

            log.info("请求【{}】已经在服务端完成方法调用", rpcRequest.getRequestId());


        }

        RpcResponse response = new RpcResponse();
        response.setCode((RespCode.SUCCESS.getCode()));
        response.setRequestId(rpcRequest.getRequestId());
        response.setCompressType(rpcRequest.getCompressType());
        response.setSerializeType(rpcRequest.getSerializeType());
        response.setObject(result);


        channelHandlerContext.channel().writeAndFlush(response);

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
