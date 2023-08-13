package com.ljh.exceptions;

/**
 * @author ljh
 *
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
