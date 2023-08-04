package com.ljh.exceptions;

/**
 * @author it楠老师
 * @createTime 2023-07-06
 */
public class LoadBalancerException extends RuntimeException {
    
    public LoadBalancerException(String message) {
        super(message);
    }
    
    public LoadBalancerException() {
    }
}
