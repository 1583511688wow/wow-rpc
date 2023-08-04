package com.ljh;

import com.ljh.discovery.Registry;
import com.ljh.discovery.RegistryConfig;
import com.ljh.untils.network.NetUtils;
import com.ljh.untils.zookeeper.ZookeeperNode;
import com.ljh.untils.zookeeper.ZookeeperUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooKeeper;

import java.util.List;

/**
 * @author ljh
 * rpc 启动器
 */
@Slf4j
public class RpcBootstrap {


    // 定义相关的一些基础配置
    private String applicationName = "default";
    private ProtocolConfig protocolConfig;
    private int port = 8088;

    private Registry registry ;


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

        this.applicationName = appName;
        return this;
    }

    /**
     * 配置一个注册中心
     * @param registryConfig
     * @return
     */
    public RpcBootstrap registry(RegistryConfig registryConfig) {


        Registry registry = registryConfig.getRegistry();
        this.registry = registry;
        return this;
    }

    /**
     * 配置当前暴露的服务使用的协议
     * @param protocolConfig
     * @return
     */
    public RpcBootstrap protocol(ProtocolConfig protocolConfig){

        this.protocolConfig = protocolConfig;

        if (log.isDebugEnabled()){

        log.info("当前工程使用了jdk");

        }

        return this;
    }

    /**
     * 单个发布服务
     * @param service
     * @return
     */
    public RpcBootstrap publish(ServiceConfig<?> service) {


        registry.register(service);

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

        try {
            Thread.sleep(100000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public RpcBootstrap reference(ReferenceConfig<?> reference) {

        return this;
    }
}
