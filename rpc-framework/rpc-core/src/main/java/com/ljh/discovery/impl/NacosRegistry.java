package com.ljh.discovery.impl;


import com.ljh.ServiceConfig;
import com.ljh.discovery.AbstractRegistry;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooKeeper;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * @author ljh
 *
 */
@Slf4j
public class NacosRegistry extends AbstractRegistry {


    @Override
    public void register(ServiceConfig<?> serviceConfig) {

    }

    @Override
    public List<InetSocketAddress> lookUp(String serviceName) {
        return null;
    }
}
