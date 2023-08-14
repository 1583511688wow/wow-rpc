package com.ljh;

import com.ljh.discovery.RegistryConfig;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * @author ljh
 *
 */
@Component
@Slf4j
public class RpcStarter implements CommandLineRunner {
    @Override
    public void run(String... args) throws Exception {
        Thread.sleep(5000);
        log.info("rpc 开始启动...");

        RpcBootstrap.getInstance()
                .application("first-dubbo-provider")
                .registry(new RegistryConfig("zookeeper://192.168.123.9:2181"))
                .serialize("hessian")
                .scan("com.ljh")
                .start();
    }
}
