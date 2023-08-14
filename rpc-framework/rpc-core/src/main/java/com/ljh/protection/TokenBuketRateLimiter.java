package com.ljh.protection;

import lombok.extern.slf4j.Slf4j;

/**
 * 基于令牌桶算法的限流器
 * @author ljh
 *
 */
@Slf4j
public class TokenBuketRateLimiter implements RateLimiter {

    // 代表令牌的数量，>0 说明有令牌，能放行，放行就减一，==0,无令牌  阻拦
    private int tokens;
    
    // 令牌数
    private final int capacity;
    
    // 按照一定的速率给令牌桶加令牌
    private final int rate;
    
    // 上一次放令牌的时间
    private Long lastTokenTime;
    
    public TokenBuketRateLimiter(int capacity, int rate) {
        this.capacity = capacity;
        this.rate = rate;
        lastTokenTime = System.currentTimeMillis();
        tokens = capacity;
    }
    
    /**
     * 判断请求是否可以放行
     * @return true 放行  false  拦截
     */
    public synchronized boolean allowRequest() {
        // 1、给令牌桶添加令牌
        // 计算从现在到上一次的时间间隔需要添加的令牌数
        Long currentTime = System.currentTimeMillis();
        long timeInterval = currentTime - lastTokenTime;
        // 如果间隔时间超过一秒，放令牌
        if(timeInterval >= 1000/rate){
            int needAddTokens = (int)(timeInterval * rate / 1000);
            System.out.println("needAddTokens = " + needAddTokens);
            // 给令牌桶添加令牌
            tokens = Math.min(capacity, tokens + needAddTokens);
            System.out.println("tokens = " + tokens);
    
            // 标记最后一个放入令牌的时间
            this.lastTokenTime = System.currentTimeMillis();
        }
        
        // 2、自己获取令牌,如果令牌桶中有令牌则放行，否则拦截
        if(tokens > 0){
            tokens --;
            System.out.println("请求被放行---------------");
            return true;
        } else {
            System.out.println("请求被拦截---------------");
            return false;
        }

    }
    
    public static void main(String[] args) {
        TokenBuketRateLimiter rateLimiter = new TokenBuketRateLimiter(10,10);
        for (int i = 0; i < 1000; i++) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            boolean allowRequest = rateLimiter.allowRequest();
            System.out.println("allowRequest = " + allowRequest);
        }
    }
}
