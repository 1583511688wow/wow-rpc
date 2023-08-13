package com.ljh.compress.impl;

import com.ljh.compress.Compressor;

import com.ljh.exceptions.CompressException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * 使用gzip算法进行压缩的具体实现
 * @author ljh
 *
 */

public class GzipCompressor implements Compressor {


    private static final Logger log = LoggerFactory.getLogger(GzipCompressor.class);

    @Override
    public byte[] compress(byte[] bytes) {
    
        try (
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            GZIPOutputStream gzipOutputStream = new GZIPOutputStream(baos);
        ) {
            gzipOutputStream.write(bytes);
            gzipOutputStream.finish();
            byte[] result = baos.toByteArray();
            if(log.isDebugEnabled()){
                log.debug("对字节数组进行了压缩长度由【{}】压缩至【{}】.",bytes.length,result.length);
            }
            return result;
        } catch (IOException e){
            log.error("对字节数组进行压缩时发生异常",e);
            throw new CompressException(e);
        }

    }
    
    @Override
    public byte[] decompress(byte[] bytes) {
        try (
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            GZIPInputStream gzipInputStream = new GZIPInputStream(bais);
            ByteArrayOutputStream baos = new ByteArrayOutputStream()
        ) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = gzipInputStream.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            byte[] toByteArray = baos.toByteArray();
            if(log.isDebugEnabled()){
                log.debug("对字节数组进行了解压缩长度由【{}】变为【{}】.",bytes.length,toByteArray.length);
            }
            return  toByteArray;
        } catch (IOException e){
            log.error("对字节数组进行压缩时发生异常",e);
            throw new CompressException(e);
        }
    }
}
