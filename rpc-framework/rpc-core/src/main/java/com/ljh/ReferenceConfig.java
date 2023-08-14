package com.ljh;

import com.ljh.discovery.Registry;
import com.ljh.exceptions.NetworkException;

import com.ljh.netty.NettyBootstrapInitializer;
import com.ljh.proxy.RpcConsumerInvocationHandler;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 定义了订阅的服务信息，包括接口的信息。
 * @author ljh
 * @param <T>
 */
public class ReferenceConfig<T> {

    private static final Logger log = LoggerFactory.getLogger(ReferenceConfig.class);

    private Class<T> interfaceRef;


    private Registry registry;
    private String group;

    public Registry getRegistry() {
        return registry;
    }

    public Class<T> getInterfaceRef() {
        return interfaceRef;
    }

    public void setInterfaceRef(Class<T> interfaceRef) {
        this.interfaceRef = interfaceRef;
    }

    public void setRegistry(Registry registry) {
        this.registry = registry;
    }

    /**
     * 生成代理对象调用方法
     * @return
     */
    public T get() {

        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        Class<T>[] calsses = new Class[]{interfaceRef};
        Object result = Proxy.newProxyInstance(contextClassLoader, calsses,
                new RpcConsumerInvocationHandler(registry, interfaceRef, group));

        return (T)result;
    }

    public void setGroup(String group) {

        this.group = group;
    }

    public String getGroup() {
        return group;
    }
}
