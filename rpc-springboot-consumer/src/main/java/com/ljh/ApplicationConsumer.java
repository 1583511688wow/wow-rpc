package com.ljh;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author ljh
 *
 */
@SpringBootApplication
@RestController
public class ApplicationConsumer {
    
    public static void main(String[] args) {
        SpringApplication.run(ApplicationConsumer.class,args);
    }

    
}
