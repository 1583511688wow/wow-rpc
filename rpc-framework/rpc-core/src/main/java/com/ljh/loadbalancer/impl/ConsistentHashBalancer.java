package com.ljh.loadbalancer.impl;

import com.ljh.RpcBootstrap;
import com.ljh.loadbalancer.AbstractLoadBalancer;
import com.ljh.loadbalancer.Selector;
import com.ljh.transport.message.RpcRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;


/**
 * 一致性hash的负载均衡策略
 * @author ljh
 * @createTime
 */
public class ConsistentHashBalancer extends AbstractLoadBalancer {

    private static final Logger log = LoggerFactory.getLogger(ConsistentHashBalancer.class);

    @Override
    protected Selector getSelector(List<InetSocketAddress> socketAddressList) {


        return new ConsistentHashSelector(socketAddressList, 128);
    }

    /**
     * 一致性hash的具体算法实现
     */
    private class ConsistentHashSelector implements Selector {

        // hash环用来存储服务器节点
        private SortedMap<Integer,InetSocketAddress> circle= new TreeMap<>();
        // 虚拟节点的个数
        private int virtualNodes;


        public ConsistentHashSelector(List<InetSocketAddress> socketAddressList, int virtualNodes) {
            // 我们应该尝试将节点转化为虚拟节点，进行挂载
            this.virtualNodes = virtualNodes;
            for (InetSocketAddress inetSocketAddress : socketAddressList) {
                // 需要把每一个节点加入到hash环中
                addNodeToCircle(inetSocketAddress);
            }


        }


        @Override
        public InetSocketAddress getNext() {

            RpcRequest rpcRequest = RpcBootstrap.REQUEST_THREAD_LOCAL.get();
            String requestId = String.valueOf(rpcRequest.getRequestId());

//            int hash = hash(requestId);

            int hash = murmurhash(requestId, 0);
            //判断hash值是否能直接落在服务器上
            if (!circle.containsKey(hash)){
                SortedMap<Integer, InetSocketAddress> tailMap = circle.tailMap(hash);
                hash = tailMap.isEmpty() ? circle.firstKey() : tailMap.firstKey();

            }

            return circle.get(hash);
        }



        /**
         * 将每个节点挂载到哈希环上
         * @param inetSocketAddress
         */
        private void addNodeToCircle(InetSocketAddress inetSocketAddress) {

            for (int i = 0; i < virtualNodes; i++) {

               // int hash = hash(inetSocketAddress + "-" + i);
                int hash = murmurhash(inetSocketAddress + "-" + i, 0);
                log.info("hash为[{}]的节点已经挂载到了哈希环上.",hash);
                circle.put(hash, inetSocketAddress);
            }

        }


        /**
         * 将每个节点删除到哈希环上
         * @param inetSocketAddress
         */
        private void removeNodeToCircle(InetSocketAddress inetSocketAddress) {

            for (int i = 0; i < virtualNodes; i++) {

                int hash = hash(inetSocketAddress + "-" + i);
                circle.remove(hash);
            }

        }


        /**
         * MD5算法实现
         * @param s
         * @return
         */
        private int hash(String s) {

            MessageDigest md;
            try {
                md = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
            byte[] digest = md.digest(s.getBytes());
            // md5得到的结果是一个字节数组，但是我们想要int 4个字节

            int res = 0;
            for (int i = 0; i < 4; i++) {
                res = res << 8;
                if( digest[i] < 0 ){
                    res = res | (digest[i] & 255);
                } else {
                    res = res | digest[i];
                }
            }
            return res;


        }

        /**
         * Murmurhash算法实现
         * @param s
         * @param seed
         * @return
         */
        private  int murmurhash(String s, int seed) {

            byte[] data = s.getBytes();
            final int c1 = 0xcc9e2d51;
            final int c2 = 0x1b873593;
            final int r1 = 15;
            final int r2 = 13;
            final int m = 5;
            final int n = 0xe6546b64;

            int hash = seed;
            int len = data.length;
            int pos = 0;

            while (len >= 4) {
                int k = (data[pos] & 0xff) | ((data[pos + 1] & 0xff) << 8) | ((data[pos + 2] & 0xff) << 16) | ((data[pos + 3] & 0xff) << 24);

                k *= c1;
                k = (k << r1) | (k >>> (32 - r1));
                k *= c2;

                hash ^= k;
                hash = (hash << r2) | (hash >>> (32 - r2));
                hash = hash * m + n;

                len -= 4;
                pos += 4;
            }

            if (len > 0) {
                int k = 0;

                for (int i = 0; i < len; i++) {
                    k |= (data[pos + i] & 0xff) << (8 * i);
                }

                k *= c1;
                k = (k << r1) | (k >>> (32 - r1));
                k *= c2;

                hash ^= k;
            }

            hash ^= data.length;
            hash ^= hash >>> 16;
            hash *= 0x85ebca6b;
            hash ^= hash >>> 13;
            hash *= 0xc2b2ae35;
            hash ^= hash >>> 16;
            return hash;
        }






    }




}
