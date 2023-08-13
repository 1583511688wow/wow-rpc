package com.ljh.loadbalancer.impl;

import com.ljh.exceptions.LoadBalancerException;
import com.ljh.loadbalancer.AbstractLoadBalancer;
import com.ljh.loadbalancer.Selector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 轮询负载均衡器
 * @author ljh
 */
public class RoundRobinLoadBalancer extends AbstractLoadBalancer {


    @Override
    protected Selector getSelector(List<InetSocketAddress> socketAddressList) {


        return new RoundRobinSelector(socketAddressList);
    }

    private static class RoundRobinSelector implements Selector{

        private static final Logger log = LoggerFactory.getLogger(RoundRobinSelector.class);


        private List<InetSocketAddress> serviceList;
        private AtomicInteger index;

        public RoundRobinSelector(List<InetSocketAddress> serviceList) {
            this.serviceList = serviceList;
            this.index = new AtomicInteger(0);
        }

        @Override
        public InetSocketAddress getNext() {

            if (serviceList == null || serviceList.size() == 0){
                log.error("进行负载均衡选取节点为空");
                throw new LoadBalancerException();

            }
            int index = this.index.get();

            InetSocketAddress inetSocketAddress = serviceList.get(index);

            if (index == serviceList.size() -1){

                this.index.set(0);
            }

            this.index.incrementAndGet();

            return inetSocketAddress;

        }
    }

}
