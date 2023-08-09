package com.ljh.compress;

/**
 * 压缩
 * @author ljh
 */
public interface Compressor {

    /**
     * 对字节数组进行压缩
     * @param bytes
     * @return
     */
    byte[] compress(byte[] bytes);


    /**
     * 对字节数组进行解压缩
     * @param bytes
     * @return
     */
    byte[] decompress(byte[] bytes);

}
