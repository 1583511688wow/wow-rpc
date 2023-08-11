package com.ljh.discovery.impl;

import com.ljh.Constant;
import com.ljh.RpcBootstrap;
import com.ljh.ServiceConfig;
import com.ljh.discovery.AbstractRegistry;
import com.ljh.exceptions.DiscoveryException;
import com.ljh.untils.network.NetUtils;
import com.ljh.untils.zookeeper.ZookeeperNode;
import com.ljh.untils.zookeeper.ZookeeperUtils;

import com.ljh.watch.UpAndDownWatcher;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author jjh
 * @createTime
 */

public class ZookeeperRegistry extends AbstractRegistry {

    private static final Logger log = LoggerFactory.getLogger(ZookeeperRegistry.class);
    // 维护一个zk实例
    private ZooKeeper zooKeeper;

    public ZookeeperRegistry() {
        this.zooKeeper = ZookeeperUtils.createZookeeper();
    }

    public ZookeeperRegistry(String connectString,int timeout) {
        this.zooKeeper = ZookeeperUtils.createZookeeper(connectString,timeout);
    }
    
    @Override
    public void register(ServiceConfig<?> service) {

        //发布服务的节点名称
        String parentNode = Constant.BASE_PROVIDERS_PATH + "/" + service.getInterface().getName();


        if (!ZookeeperUtils.exists(zooKeeper, parentNode, null)){
            ZookeeperNode zookeeperNode = new ZookeeperNode(parentNode, null);
            ZookeeperUtils.createNode(zooKeeper, zookeeperNode, null, CreateMode.PERSISTENT);

        }

        //创建本机的临时节点
        String node = parentNode + "/" + NetUtils.getIp() + ":" + RpcBootstrap.getInstance().getConfiguration().getPort();

        if (!ZookeeperUtils.exists(zooKeeper, node, null)){
            ZookeeperNode zookeeperNode = new ZookeeperNode(node, null);
            ZookeeperUtils.createNode(zooKeeper, zookeeperNode, null, CreateMode.EPHEMERAL);

        }


    }


    /**
     * 发现服务，从注册中心，寻找一个可用的服务，传入服务的名字
     * @param serviceName 服务的名称
     * @return  IP + 端口
     */
    @Override
    public List<InetSocketAddress> lookUp(String serviceName) {

        //找到服务对应的节点
        String serviceNode = Constant.BASE_PROVIDERS_PATH + "/" + serviceName;

        //从zk中获取他的子节点
        List<String> children = ZookeeperUtils.getChildren(zooKeeper, serviceNode, new UpAndDownWatcher());
        List<InetSocketAddress> collect = children.stream().map(ipString -> {

            String[] ipAndPort = ipString.split(":");
            String ip = ipAndPort[0];
            int port = Integer.parseInt(ipAndPort[1]);
            return new InetSocketAddress(ip, port);
        }).collect(Collectors.toList());

        if(collect.size() == 0){
            throw new DiscoveryException("未发现任何可用的服务主机.");
        }

        //todo: 用负载均衡 缓存 + watcher 寻去一个可用的服务
        return collect;
    }


}
