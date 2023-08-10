package com.ljh.discovery.impl;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Op {

    private static final Logger log = LoggerFactory.getLogger(Op.class);
    public static void main(String[] args) {

        new Thread(() ->{

            try {
                Thread.sleep(1000);
                System.out.println("1");
                Thread.sleep(3000);
                System.out.println("2");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        },"dsfsd").start();

        System.out.println("3");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("4");


    }
}
