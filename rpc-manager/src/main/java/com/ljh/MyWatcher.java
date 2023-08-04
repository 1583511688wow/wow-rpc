package com.ljh;

import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import java.util.logging.Logger;

@Slf4j
public class MyWatcher implements Watcher {

    @Override
    public void process(WatchedEvent watchedEvent) {

        //判断事件类型，连接类型的事件
        if (watchedEvent.getType() == Event.EventType.None){
            if (watchedEvent.getState() == Event.KeeperState.SyncConnected){
                if (log.isDebugEnabled()) {
                    log.debug("dsd");
                }


            }else if (watchedEvent.getState() == Event.KeeperState.AuthFailed){
                System.out.println("连接失败");

            }  else if (watchedEvent.getState() == Event.KeeperState.Disconnected){
                System.out.println("断开连接");

            }else if (watchedEvent.getType() == Event.EventType.NodeCreated){
                System.out.println("创建一个节点");

            }
        }
    }
}
