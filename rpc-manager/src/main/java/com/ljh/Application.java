package com.ljh;



import com.ljh.untils.zookeeper.ZookeeperNode;
import com.ljh.untils.zookeeper.ZookeeperUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.*;


import java.util.Arrays;

import static com.ljh.Constant.*;

/**
 * 管理注册中心
 */
@Slf4j
public class Application {


    public static void main(String[] args) {


        // 创建一个zookeeper实例
        ZooKeeper zooKeeper =createZookeeper();

        // 定义节点和数据
        String basePath = "/rpc-metadata";
        ZookeeperNode baseNode = new ZookeeperNode(BASE_PATH, null);
        ZookeeperNode providersNode = new ZookeeperNode(BASE_PROVIDERS_PATH, null);
        ZookeeperNode consumersNode = new ZookeeperNode(BASE_CONSUMERS_PATH, null);

        // 创建节点
        Arrays.asList(baseNode, providersNode, consumersNode).stream().forEach(node ->{

            ZookeeperUtils.createNode(zooKeeper, node, null, CreateMode.PERSISTENT);

        });

        // 关闭连接
        ZookeeperUtils.close(zooKeeper);


    }





}
