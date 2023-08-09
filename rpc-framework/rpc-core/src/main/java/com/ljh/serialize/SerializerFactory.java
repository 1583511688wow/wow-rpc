package com.ljh.serialize;

import com.ljh.serialize.Impl.HessianSerializer;
import com.ljh.serialize.Impl.JdkSerializer;
import com.ljh.serialize.Impl.JsonSerializer;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 序列化工厂
 * @author ljh
 */
public class SerializerFactory {


    private final static ConcurrentHashMap<String, SerializerWrapper> SERIALIZER_CACHE = new ConcurrentHashMap<>(8);
    private final static ConcurrentHashMap<Byte, SerializerWrapper> SERIALIZER_CACHE_CODE = new ConcurrentHashMap<>(8);


    static {


        SerializerWrapper jdk = new SerializerWrapper((byte) 1, "jdk", new JdkSerializer());
        SerializerWrapper json = new SerializerWrapper((byte) 2, "json", new JdkSerializer());
        SerializerWrapper hessian = new SerializerWrapper((byte) 3, "hessian", new HessianSerializer());
        SERIALIZER_CACHE.put("jdk", jdk);
        SERIALIZER_CACHE.put("json", json);
        SERIALIZER_CACHE.put("hessian", hessian);

        SERIALIZER_CACHE_CODE.put((byte) 1, jdk);
        SERIALIZER_CACHE_CODE.put((byte) 2, json);
        SERIALIZER_CACHE_CODE.put((byte) 3, hessian);



    }

    /**
     * 使用工厂方法获取一个SerializerWrapper
     * @param serializeType
     * @return
     */
    public static SerializerWrapper getSerializer(String serializeType) {
        SerializerWrapper serializerWrapper = SERIALIZER_CACHE.get(serializeType);
        if (serializerWrapper == null){


            return SERIALIZER_CACHE.get("jdk");
        }

        return SERIALIZER_CACHE.get(serializeType);


    }



    public static SerializerWrapper getSerializer(byte serializeCode) {


        return SERIALIZER_CACHE_CODE.get(serializeCode);


    }
}
