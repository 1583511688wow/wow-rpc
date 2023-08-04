package com.ljh.exceptions;

/**
 * @author it楠老师
 * @createTime 2023-06-29
 */
public class DiscoveryException extends RuntimeException{
    
    public DiscoveryException() {
    }
    
    public DiscoveryException(String message) {
        super(message);
    }
    
    public DiscoveryException(Throwable cause) {
        super(cause);
    }
}
