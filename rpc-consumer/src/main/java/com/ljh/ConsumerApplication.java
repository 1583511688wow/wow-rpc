package com.ljh;

import com.ljh.discovery.RegistryConfig;
import com.ljh.test.HelloRpc;

/**
 * @author ljh
 *
 * 进行消费端订阅
 */
public class ConsumerApplication {

    public static void main(String[] args) {

        //定义了订阅的服务信息，包括接口的信息。
        ReferenceConfig<HelloRpc> reference = new ReferenceConfig<>();
        reference.setInterfaceRef(HelloRpc.class);

        //传入了应用名，注册中心地址，协议的信息以及服务的信息等。
        RpcBootstrap.getInstance()
                .application("first-dubbo-consumer")
                .registry(new RegistryConfig("zookeeper://192.168.123.6:2181"))
                .reference(reference);

        //获取到动态代理的对象并进行调用。
        HelloRpc service = reference.get();
        String message = service.say("55555");
        System.out.println("Receive result ======> " + message);

    }
}
