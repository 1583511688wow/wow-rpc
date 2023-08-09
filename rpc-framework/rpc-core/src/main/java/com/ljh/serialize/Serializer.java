package com.ljh.serialize;

/**
 * 序列化器
 * @author ljh
 */
public interface Serializer {

    /**
     * 抽象的用来做序列化的方法
     * @param obj
     * @return
     */
    byte[] serialize(Object obj);


    /**
     * 反序列化方法
     * @param bytes
     * @param clazz
     * @param <T>
     * @return
     */
    <T> T deserialize(byte[] bytes, Class<T> clazz);


}
