package com.ljh;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/provider")
public class Provider {

    @GetMapping("/1")
    public String hello(){

        return "hello";

    }}
