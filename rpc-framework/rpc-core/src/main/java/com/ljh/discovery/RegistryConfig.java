package com.ljh.discovery;

import com.ljh.Constant;

import com.ljh.discovery.impl.ZookeeperRegistry;
import com.ljh.exceptions.DiscoveryException;


/**
 * @author ljhimpl
 * @createTime 2023-06-28
 */
public class RegistryConfig {

    // 定义连接的 url zookeeper://127.0.0.1:2181  redis://192.168.12.125:3306
    private final String connectString;

    public RegistryConfig(String connectString) {
        this.connectString = connectString;
    }

    /**
     * 判断获取配置中心
     * @return 具体的注册中心实例
     */
    public Registry getRegistry() {
        // 1、获取注册中心的类型
        String registryType = getRegistryType(connectString,true).toLowerCase().trim();
        // 2、通过类型获取具体注册中心
        if( registryType.equals("zookeeper") ){
            String host = getRegistryType(connectString, false);
            return new ZookeeperRegistry(host, Constant.TIME_OUT);
        } else if (registryType.equals("nacos")){
            String host = getRegistryType(connectString, false);

            //todo: 记得换Nacos
            return null;
        }
        throw new DiscoveryException("未发现合适的注册中心。");
    }


    private String getRegistryType(String connectString,boolean ifType){
        String[] typeAndHost = connectString.split("://");
        if(typeAndHost.length != 2){
            throw new RuntimeException("给定的注册中心连接url不合法");
        }
        if(ifType){
            return typeAndHost[0];
        } else {
            return typeAndHost[1];
        }
    }

}
