package com.ljh.serialize.impl;

import com.ljh.serialize.Serializer;

/**
 * json序列化
 * @author ljh
 */
public class JsonSerializer implements Serializer {
    @Override
    public byte[] serialize(Object obj) {
        return new byte[0];
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        return null;
    }
}
