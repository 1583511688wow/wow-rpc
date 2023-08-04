package com.ljh;

/**
 * @author ljh
 */
public class Constant {
    
    // zookeeper的默认连接地址
    public static final String DEFAULT_ZK_CONNECT = "192.168.123.6:2181";
    
    // zookeeper默认的超时时间
    public static final int TIME_OUT = 10000;
    
    // 服务提供方和调用方在注册中心的基础路径
    public static final String BASE_PATH = "/rpc-metadata";
    public static final String BASE_PROVIDERS_PATH = "/rpc-metadata/providers";
    public static final String BASE_CONSUMERS_PATH = "/rpc-metadata/consumers";
    
}
