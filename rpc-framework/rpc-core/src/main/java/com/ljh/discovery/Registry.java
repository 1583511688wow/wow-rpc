package com.ljh.discovery;

import com.ljh.ServiceConfig;

import java.net.InetSocketAddress;
import java.util.List;


/**
 * 注册中心
 *
 * @author ljh
 *
 */
public interface Registry {
    
    /**
     * 注册服务
     * @param serviceConfig 服务的配置内容
     */
    void register(ServiceConfig<?> serviceConfig);



    /**
     * 从注册中心拉取服务列表
     * @param serviceName 服务的名称
     * @return 服务的地址
     */
    InetSocketAddress lookUp(String serviceName);


}
