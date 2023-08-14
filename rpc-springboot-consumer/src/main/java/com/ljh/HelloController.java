package com.ljh;


import com.ljh.annotation.RpcService;
import com.ljh.test.GoodRpc;
import com.ljh.test.HelloRpc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author ljh
 *
 */
@RestController
public class HelloController {
    
    // 需要注入一个代理对象
    @RpcService
    private HelloRpc helloRpc;


    @RpcService
    private GoodRpc goodRpc;
    
    @GetMapping("hello")
    public String hello(){
        return helloRpc.say("成功rpc目标方法");
    }
    
}
