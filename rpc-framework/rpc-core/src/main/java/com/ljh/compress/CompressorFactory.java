package com.ljh.compress;

import com.ljh.compress.impl.GzipCompressor;
import com.ljh.config.ObjectWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 压缩工厂
 * @author ljh
 */
public class CompressorFactory {

    private static final Logger log = LoggerFactory.getLogger(CompressorFactory.class);

    private final static Map<String, ObjectWrapper<Compressor>> COMPRESSOR_CACHE = new ConcurrentHashMap<>(8);
    private final static Map<Byte, ObjectWrapper<Compressor>> COMPRESSOR_CACHE_CODE = new ConcurrentHashMap<>(8);


    static {


        ObjectWrapper<Compressor> gzip = new ObjectWrapper<>((byte) 1, "gzip", new GzipCompressor());
        COMPRESSOR_CACHE.put("gzip", gzip);
        COMPRESSOR_CACHE_CODE.put((byte) 1, gzip);



    }

    /**
     * 使用工厂方法获取一个SerializerWrapper
     * @param compressorType
     * @return
     */
    public static ObjectWrapper<Compressor> getCompressor(String compressorType) {

        ObjectWrapper<Compressor> objectWrapper = COMPRESSOR_CACHE.get(compressorType);
        if (objectWrapper == null){

            log.error("未找到您配置的【{}】压缩算法，默认选用gzip算法。",compressorType);
            return COMPRESSOR_CACHE.get("gzip");
        }
        return objectWrapper;


    }



    public static ObjectWrapper<Compressor> getCompressor(byte serializeCode) {

        ObjectWrapper<Compressor> objectWrapper = COMPRESSOR_CACHE_CODE.get(serializeCode);
        if (objectWrapper == null){

            log.error("未找到您配置的编号为【{}】的压缩算法，默认选用gzip算法。",serializeCode);
            return COMPRESSOR_CACHE.get("gzip");
        }




        return objectWrapper;


    }


    /**
     * 添加压缩策略
     * @param objectWrapper
     */
    public static void addCompressor(ObjectWrapper<Compressor> objectWrapper ){

        COMPRESSOR_CACHE.put(objectWrapper.getName(), objectWrapper);
        COMPRESSOR_CACHE_CODE.put(objectWrapper.getCode(), objectWrapper);
    }



}
