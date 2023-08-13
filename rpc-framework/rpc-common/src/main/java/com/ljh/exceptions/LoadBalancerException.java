package com.ljh.exceptions;

/**
 * @author ljh
 *
 */
public class LoadBalancerException extends RuntimeException {
    
    public LoadBalancerException(String message) {
        super(message);
    }
    
    public LoadBalancerException() {
    }
}
