package com.ljh;


import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


/**
 * @author ljh
 * rpc 启动器
 */
@Slf4j
public class RpcBootstrap {



    private static RpcBootstrap rpcBootstrap = new RpcBootstrap();

    public RpcBootstrap() {
    }


    public static RpcBootstrap getInstance() {

        return rpcBootstrap;
    }


    /**
     * 定义程序的名字
     * @param appName
     * @return
     */
    public RpcBootstrap application(String appName) {

        return this;
    }

    /**
     * 配置一个注册中心
     * @param registryConfig
     * @return
     */
    public RpcBootstrap registry(RegistryConfig registryConfig) {

        return this;
    }

    /**
     * 配置当前暴露的服务使用的协议
     * @param protocolConfig
     * @return
     */
    public RpcBootstrap protocol(ProtocolConfig protocolConfig){

        log.info("dsdsd");

        return this;
    }

    /**
     * 单个发布服务
     * @param service
     * @return
     */
    public RpcBootstrap publish(ServiceConfig<?> service) {

        return this;
    }

    /**
     * 批量发布服务
     * @param services
     * @return
     */
    public RpcBootstrap publish(List<ServiceConfig> services) {

        return this;
    }

    /**
     * 启动 netty 服务
     */
    public void start() {

    }

    public RpcBootstrap reference(ReferenceConfig<?> reference) {

        return this;
    }
}
