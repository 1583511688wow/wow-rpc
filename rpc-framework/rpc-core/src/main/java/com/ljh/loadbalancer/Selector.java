package com.ljh.loadbalancer;

import java.net.InetSocketAddress;

/**
 * 负载均衡算法接口
 * @author ljh
 */
public interface Selector {

    /**
     * 用该算法根据服务列表获取一个节点
     * @param
     * @return
     */
    InetSocketAddress getNext();
}
