package com.ljh.config;

import com.ljh.compress.Compressor;
import com.ljh.compress.CompressorFactory;
import com.ljh.loadbalancer.LoadBalancer;
import com.ljh.serialize.Serializer;
import com.ljh.serialize.SerializerFactory;
import com.ljh.spi.SpiHandler;

import java.util.List;

/**
 * @author ljh
 *
 */
public class SpiResolver {
    
    /**
     * 通过spi的方式加载配置项
     * @param configuration
     */
    public void loadFromSpi(Configuration configuration) {

        List<ObjectWrapper<LoadBalancer>> wrappers = SpiHandler.getList(LoadBalancer.class);
        if (wrappers != null && wrappers.size() > 0){
            configuration.setLoadBalancer(wrappers.get(0).getImpl());
        }


        List<ObjectWrapper<Compressor>> list = SpiHandler.getList(Compressor.class);
        if (list != null){
            list.forEach(CompressorFactory::addCompressor);
        }


        List<ObjectWrapper<Serializer>> list1 = SpiHandler.getList(Serializer.class);
        if (list1 != null){
            list1.forEach(SerializerFactory::addSerializer);
        }



    }


}
