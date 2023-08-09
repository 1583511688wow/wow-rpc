package com.ljh.loadbanlancer;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * 负载均衡器的接口
 * @author ljh
 */
public interface LoadBalancer {

    /**
     * 根据服务名获得一个可用的服务
     * @param serviceName
     * @return
     */
    InetSocketAddress selectServiceAddress(String serviceName);
}
