package com.ljh.config;


import com.ljh.ProtocolConfig;
import com.ljh.discovery.RegistryConfig;
import com.ljh.loadbanlancer.LoadBalancer;
import com.ljh.loadbanlancer.impl.MinimumResponseTimeLoadBalancer;
import com.ljh.untils.id.IdGenerator;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;


/**
 * 全局的配置类，代码配置-->xml配置-->默认项
 *
 * @author ljh
 *
 */
@Data
@Slf4j
public class Configuration {
    
    // 配置信息-->端口号
    private int port = 8097;
    
    // 配置信息-->应用程序的名字
    private String appName = "default";
    
    // 分组信息
   // private String group = "default";
    
    // 配置信息-->注册中心
    private RegistryConfig registryConfig;
    
    // 配置信息-->序列化协议
    private String serializeType = "jdk";
    
    // 配置信息-->压缩使用的协议
    private String compressType = "gzip";
    
    // 配置信息-->id发射器
    public IdGenerator idGenerator = new IdGenerator(1, 2);
    
    // 配置信息-->负载均衡策略
    public LoadBalancer loadBalancer = new MinimumResponseTimeLoadBalancer();

    private ProtocolConfig protocolConfig;
    
    // 为每一个ip配置一个限流器
   // private final Map<SocketAddress, RateLimiter> everyIpRateLimiter = new ConcurrentHashMap<>(16);
    // 为每一个ip配置一个断路器，熔断
  //  private final Map<SocketAddress, CircuitBreaker> everyIpCircuitBreaker = new ConcurrentHashMap<>(16);
    
    // 读xml，dom4j
    public Configuration() {

    }


    
}
