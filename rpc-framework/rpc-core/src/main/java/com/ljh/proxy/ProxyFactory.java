package com.ljh.proxy;

import com.ljh.ReferenceConfig;
import com.ljh.RpcBootstrap;
import com.ljh.discovery.RegistryConfig;
import com.ljh.test.HelloRpc;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ljh
 */
public class ProxyFactory {



    private static Map<Class<?>,Object> cache = new ConcurrentHashMap<>(32);

    public static <T> T getProxy(Class<T> clazz) {

        Object bean = cache.get(clazz);
        if(bean != null){
            return (T)bean;
        }

        ReferenceConfig<T> reference = new ReferenceConfig<>();
        reference.setInterfaceRef(clazz);


        RpcBootstrap.getInstance()
                .application("first-rpc-consumer")
                .registry(new RegistryConfig("zookeeper://192.168.123.9:2181"))
                .serialize("hessian")
                .compress("gzip")
                .group("primary")
                .reference(reference);
        T t = reference.get();
        cache.put(clazz,t);
        return t;
    }

}
