package com.ljh.config;

import com.ljh.ProtocolConfig;
import com.ljh.compress.Compressor;
import com.ljh.compress.impl.GzipCompressor;
import com.ljh.discovery.RegistryConfig;
import com.ljh.loadbalancer.LoadBalancer;
import com.ljh.loadbalancer.impl.MinimumResponseTimeLoadBalancer;
import com.ljh.protection.CircuitBreaker;
import com.ljh.protection.RateLimiter;
import com.ljh.serialize.Serializer;
import com.ljh.serialize.impl.JdkSerializer;
import com.ljh.untils.id.IdGenerator;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 全局的配置类，代码配置-->xml配置-->默认项
 *
 *
 * @createTime 2023-07-11
 */
@Data
@Slf4j
public class Configuration {

    // 配置信息-->端口号
    private int port = 8011;

    // 配置信息-->应用程序的名字
    private String appName = "default";

    // 分组信息
    private String group = "default";

    // 配置信息-->注册中心
    private RegistryConfig registryConfig = new RegistryConfig("zookeeper://127.0.0.1:2181");

    // 配置信息-->序列化协议
    private String serializeType = "jdk";

    // 配置信息-->压缩使用的协议
    private String compressType = "gzip";


    // 配置信息-->id发射器
    public IdGenerator idGenerator = new IdGenerator(1, 2);

    // 配置信息-->负载均衡策略
    private LoadBalancer loadBalancer = new MinimumResponseTimeLoadBalancer();


    private Serializer serializer = new JdkSerializer();


    // 为每一个ip配置一个限流器
    private final Map<SocketAddress, RateLimiter> everyIpRateLimiter = new ConcurrentHashMap<>(16);
    // 为每一个ip配置一个断路器，熔断
    private final Map<SocketAddress, CircuitBreaker> everyIpCircuitBreaker = new ConcurrentHashMap<>(16);



    //
    public Configuration() {

        //成员变量的默认配置

        //spi机制发现
        SpiResolver spiResolver = new SpiResolver();
        spiResolver.loadFromSpi(this);

        //xml配置
        XmlResolver xmlResolver = new XmlResolver();
        xmlResolver.loadFromXml(this);

    }



}
