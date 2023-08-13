package com.ljh.serialize;


import com.ljh.compress.CompressorFactory;
import com.ljh.config.ObjectWrapper;
import com.ljh.serialize.impl.HessianSerializer;
import com.ljh.serialize.impl.JdkSerializer;
import com.ljh.serialize.impl.JsonSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 序列化工厂
 * @author ljh
 */
public class SerializerFactory {

    private static final Logger log = LoggerFactory.getLogger(SerializerFactory.class);



    private final static ConcurrentHashMap<String, ObjectWrapper<Serializer>> SERIALIZER_CACHE = new ConcurrentHashMap<>(8);
    private final static ConcurrentHashMap<Byte, ObjectWrapper<Serializer>> SERIALIZER_CACHE_CODE = new ConcurrentHashMap<>(8);


    static {


        ObjectWrapper<Serializer> jdk = new ObjectWrapper<Serializer>((byte) 1, "jdk", new JdkSerializer());
        ObjectWrapper<Serializer> json = new ObjectWrapper<Serializer>((byte) 2, "json", new JsonSerializer());
        ObjectWrapper<Serializer> hessian = new ObjectWrapper<Serializer>((byte) 3, "hessian", new HessianSerializer());
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
    public static ObjectWrapper<Serializer> getSerializer(String serializeType) {
        ObjectWrapper<Serializer> serializerWrapper = SERIALIZER_CACHE.get(serializeType);
        if (serializerWrapper == null){


            return SERIALIZER_CACHE.get("jdk");
        }

        return SERIALIZER_CACHE.get(serializeType);


    }



    public static ObjectWrapper<Serializer> getSerializer(byte serializeCode) {
        ObjectWrapper<Serializer> serializerWrapper = SERIALIZER_CACHE_CODE.get(serializeCode);
        if(serializerWrapper == null){
            log.error("未找到您配置的【{}】序列化工具，默认选用jdk的序列化方式。",serializeCode);
            return SERIALIZER_CACHE.get("jdk");
        }
        return SERIALIZER_CACHE_CODE.get(serializeCode);




    }



    /**
     * 新增一个新的序列化器
     * @param serializerObjectWrapper 序列化器的包装
     */
    public static void addSerializer(ObjectWrapper<Serializer> serializerObjectWrapper){
        SERIALIZER_CACHE.put(serializerObjectWrapper.getName(),serializerObjectWrapper);
        SERIALIZER_CACHE_CODE.put(serializerObjectWrapper.getCode(),serializerObjectWrapper);
    }


}
