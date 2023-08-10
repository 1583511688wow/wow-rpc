package com.ljh.discovery;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Op {
    public static void main(String[] args) throws ExecutionException, InterruptedException {

        CompletableFuture<Integer> future = new CompletableFuture<>();


        String s = "ddsdsds";
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
            System.out.println(md);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        byte[] digest = md.digest(s.getBytes());
        System.out.println(Arrays.toString(digest));
    }
}
