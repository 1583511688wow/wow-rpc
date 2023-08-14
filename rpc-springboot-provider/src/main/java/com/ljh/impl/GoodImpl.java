package com.ljh.impl;

import com.ljh.annotation.WowApi;
import com.ljh.test.GoodRpc;

@WowApi(group = "primary")
public class GoodImpl implements GoodRpc {
    @Override
    public String say(String msg) {
        return "hello consumer" + msg;
    }
}
