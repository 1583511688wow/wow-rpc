package com.ljh.Impl;

import com.ljh.test.HelloRpc;

public class HelloImpl implements HelloRpc {
    @Override
    public String say(String msg) {
        return "hello consumer" + msg;
    }
}
