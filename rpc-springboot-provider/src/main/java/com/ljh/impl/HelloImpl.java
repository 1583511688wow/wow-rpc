package com.ljh.impl;

import com.ljh.annotation.WowApi;
import com.ljh.test.HelloRpc;


@WowApi(group = "primary")
public class HelloImpl implements HelloRpc {
    @Override
    public String say(String msg) {
        return "hello consumer" + msg;
    }
}
