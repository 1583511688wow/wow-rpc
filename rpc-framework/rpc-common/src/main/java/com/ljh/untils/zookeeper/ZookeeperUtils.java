package com.ljh.untils.zookeeper;


import com.ljh.Constant;
import com.ljh.exceptions.ZookeeperException;

import org.apache.zookeeper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @author ljh
 *
 */

public class ZookeeperUtils {

    private static final Logger log = LoggerFactory.getLogger(ZookeeperUtils.class);

    /**
     * 使用默认配置创建zookeeper实例
     * @return zookeeper实例
     */
    public static ZooKeeper createZookeeper(){
        // 定义连接参数
        String connectString = Constant.DEFAULT_ZK_CONNECT;
        // 定义超时时间
        int timeout = Constant.TIME_OUT;
        return createZookeeper(connectString,timeout);
    }
    
    public static ZooKeeper createZookeeper(String connectString,int timeout){
        CountDownLatch countDownLatch = new CountDownLatch(1);
        try {
            // 创建zookeeper实例，建立连接
            final ZooKeeper zooKeeper = new ZooKeeper(connectString, timeout, event -> {
                // 只有连接成功才放行
                if (event.getState() == Watcher.Event.KeeperState.SyncConnected) {
                    log.debug("客户端已经连接成功。");
                    System.out.println("客户端已经连接成功。");
                    countDownLatch.countDown();
                }
            });
        
            countDownLatch.await();
            return zooKeeper;
        } catch (IOException  | InterruptedException e) {
            log.error("创建zookeeper实例时发生异常：",e);
            throw new ZookeeperException();
        }
    }

    /**
     * 创建一个节点的工具方法
     * @param zooKeeper
     * @param node
     * @param watcher
     * @param createMode
     * @return
     */
    public static Boolean createNode(ZooKeeper zooKeeper, ZookeeperNode node, Watcher watcher, CreateMode createMode){
        try {
            if (zooKeeper.exists(node.getNodePath(), watcher) == null) {
                String result = zooKeeper.create(node.getNodePath(), node.getData(),
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, createMode);
                log.info("节点【{}】，成功创建。",result);
                System.out.println("节点:  " + result + "成功创建。" );
                return true;
            } else {
                if(log.isDebugEnabled()){
                    log.info("节点【{}】已经存在，无需创建。",node.getNodePath());
                    System.out.println("节点已存在创建。" );
                }
                return false;
            }
        } catch (KeeperException| InterruptedException e) {
            log.error("创建基础目录时发生异常：",e);
            throw new ZookeeperException();
        }
    }
    
    /**
     * 判断节点是否存在
     * @param zk
     * @param node
     * @param watcher
     * @return ture 存在 | false 不存在
     */
    public static boolean exists(ZooKeeper zk,String node,Watcher watcher){
        try {
            return zk.exists(node,watcher) != null;
        } catch (KeeperException | InterruptedException e) {
            log.error("判断节点[{}]是否存在是发生异常",node,e);
            throw new ZookeeperException(e);
        }
    }
    
    /**
     * 关闭zookeeper的方法
     * @param zooKeeper zooKeeper实例
     */
    public static void close(ZooKeeper zooKeeper){
        try {
            zooKeeper.close();
        } catch (InterruptedException e) {
            log.error("关闭zookeeper时发生问题：",e);
            throw new ZookeeperException();
        }
    }
    
    /**
     * 查询一个节点的子元素
     * @param zooKeeper
     * @param serviceNode
     * @return 子元素列表
     */
    public static List<String> getChildren(ZooKeeper zooKeeper, String serviceNode,Watcher watcher) {
        try {
            return zooKeeper.getChildren(serviceNode, watcher);
        } catch (KeeperException | InterruptedException e) {
            log.error("获取节点【{}】的子元素时发生异常.",serviceNode,e);
            throw new ZookeeperException(e);
        }
    }
}
