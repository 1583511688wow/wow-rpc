package com.ljh;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author ljh
 * @param <T>
 */
public class ReferenceConfig<T> {

    private Class<T> interfaceRef;

    public Class<T> getInterface() {
        return interfaceRef;
    }

    public void setInterface(Class<T> interfaceRef) {
        this.interfaceRef = interfaceRef;
    }

    public T get() {

        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        Class[] calsses = new Class[]{interfaceRef};
        Object result = Proxy.newProxyInstance(contextClassLoader, calsses, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

                System.out.println("hello，调用成功");
                return null;
            }
        });

        return (T)result;
    }
}
