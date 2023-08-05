package com.ljh.discovery;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Op {
    public static void main(String[] args) throws ExecutionException, InterruptedException {

        CompletableFuture<Integer> future = new CompletableFuture<>();


        new Thread(() ->{
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            int i = 9;
            future.complete(i);


        }).start();

        Integer integer = future.get();
        System.out.println(integer);


    }
}
