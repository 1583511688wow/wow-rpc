package com.ljh.discovery.impl;

import com.ljh.Constant;
import com.ljh.ServiceConfig;
import com.ljh.discovery.AbstractRegistry;
import com.ljh.exceptions.DiscoveryException;
import com.ljh.untils.network.NetUtils;
import com.ljh.untils.zookeeper.ZookeeperNode;
import com.ljh.untils.zookeeper.ZookeeperUtils;

import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooKeeper;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * @author it楠老师
 * @createTime 2023-06-30
 */
@Slf4j
public class ZookeeperRegistry extends AbstractRegistry {

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
        //todo: 后续处理端口问题
        String node = parentNode + "/" + NetUtils.getIp() + ":" + 8088;

        if (!ZookeeperUtils.exists(zooKeeper, node, null)){
            ZookeeperNode zookeeperNode = new ZookeeperNode(node, null);
            ZookeeperUtils.createNode(zooKeeper, zookeeperNode, null, CreateMode.EPHEMERAL);

        }


    }
    




}
