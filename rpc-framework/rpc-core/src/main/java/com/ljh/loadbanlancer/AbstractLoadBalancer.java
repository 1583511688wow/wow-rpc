package com.ljh.loadbanlancer;

import com.ljh.RpcBootstrap;
import com.ljh.discovery.Registry;
import com.ljh.discovery.impl.ZookeeperRegistry;
import com.ljh.loadbanlancer.impl.RoundRobinLoadBalancer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractLoadBalancer implements LoadBalancer{
    private Registry registry;

    private static final Logger log = LoggerFactory.getLogger(AbstractLoadBalancer.class);


    private Map<String, Selector> cache = new ConcurrentHashMap<>(8);

    public AbstractLoadBalancer(){

        this.registry = RpcBootstrap.getInstance().getRegistry();

    }
    @Override
    public InetSocketAddress selectServiceAddress(String serviceName) {

        Selector selector = cache.get(serviceName);
        if (selector == null){

            List<InetSocketAddress> socketAddressList = registry.lookUp(serviceName);

            selector = getSelector(socketAddressList);
            cache.put(serviceName, selector);

        }

        return selector.getNext();




    }

    /**
     * 由子类进行扩展
     * @param socketAddressList
     * @return
     */
    protected abstract Selector getSelector(List<InetSocketAddress> socketAddressList);




}
