package com.ljh.compress;

import com.ljh.compress.Impl.GzipCompressor;
import com.ljh.serialize.Impl.HessianSerializer;
import com.ljh.serialize.Impl.JdkSerializer;
import com.ljh.serialize.SerializerWrapper;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 压缩工厂
 * @author ljh
 */
public class CompressorFactory {


    private final static ConcurrentHashMap<String, CompressWrapper> COMPRESSOR_CACHE = new ConcurrentHashMap<>(8);
    private final static ConcurrentHashMap<Byte, CompressWrapper> COMPRESSOR_CACHE_CODE = new ConcurrentHashMap<>(8);


    static {


        CompressWrapper gzip = new CompressWrapper((byte) 3, "gzip", new GzipCompressor());
        COMPRESSOR_CACHE.put("gzip", gzip);
        COMPRESSOR_CACHE_CODE.put((byte) 3, gzip);



    }

    /**
     * 使用工厂方法获取一个SerializerWrapper
     * @param compressorType
     * @return
     */
    public static CompressWrapper getCompressor(String compressorType) {

        CompressWrapper compressWrapper = COMPRESSOR_CACHE.get(compressorType);
        if (compressWrapper == null){


            return COMPRESSOR_CACHE.get("gzip");
        }
        return compressWrapper;


    }



    public static CompressWrapper getCompressor(byte serializeCode) {

        CompressWrapper compressWrapper = COMPRESSOR_CACHE.get(serializeCode);
        if (compressWrapper == null){


            return COMPRESSOR_CACHE.get("gzip");
        }




        return compressWrapper;


    }
}
