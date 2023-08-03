package com.ljh;

import com.ljh.Impl.HelloImpl;
import com.ljh.test.HelloRpc;

/**
 * @author ljh
 *
 * 服务端发布服务
 */
public class Application {

    public static void main(String[] args) {

        //  定义了发布的服务信息，包括接口的信息以及对应的实现类对象
        ServiceConfig<HelloRpc> service = new ServiceConfig<>();
        service.setInterface(HelloRpc.class);
        service.setRef(new HelloImpl());

        // 启动 rpc启动器 传入了应用名，注册中心地址，协议的信息以及服务的信息等。
        RpcBootstrap.getInstance()
                .application("first-dubbo-provider")
                .registry(new RegistryConfig("zookeeper://127.0.0.1:2181"))
                .protocol(new ProtocolConfig("dubbo"))
                .publish(service)
                .start();
    }
}