package com.ljh.loadbanlancer;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * 负载均衡算法接口
 * @author ljh
 */
public interface Selector {

    /**
     * 用该算法根据服务列表获取一个节点
     * @param serviceList
     * @return
     */
    InetSocketAddress getNext();
}
