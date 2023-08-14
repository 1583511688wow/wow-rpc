package com.ljh;

import com.ljh.core.HeartBeat;
import com.ljh.discovery.RegistryConfig;
import com.ljh.netty.NettyBootstrapInitializer;
import com.ljh.test.HelloRpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author ljh
 *
 * 进行消费端订阅
 */
public class ConsumerApplication {

    private static final Logger log = LoggerFactory.getLogger(ConsumerApplication.class);

    public static void main(String[] args) {

        //定义了订阅的服务信息，包括接口的信息。
        ReferenceConfig<HelloRpc> reference = new ReferenceConfig<>();
        reference.setInterfaceRef(HelloRpc.class);

        //传入了应用名，注册中心地址，协议的信息以及服务的信息等。
        RpcBootstrap.getInstance()
                .application("first-dubbo-consumer")
                .registry(new RegistryConfig("zookeeper://192.168.123.9:2181"))
                .serialize("hessian")
                .compress("gzip")
                .group("primary")
                .reference(reference);

        //获取到动态代理的对象并进行调用。
          HelloRpc service = reference.get();
        for (int i = 0; i < 5; i++){
            String message = service.say("55555-------> 我调用了say接口");
            log.info("say方法" + message);
            System.out.println("say方法" + message);

        }

    }
}
