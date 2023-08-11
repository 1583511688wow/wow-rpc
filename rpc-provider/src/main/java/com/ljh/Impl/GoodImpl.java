package com.ljh.Impl;

import com.ljh.annotation.WowApi;
import com.ljh.test.GoodRpc;
import com.ljh.test.HelloRpc;

@WowApi
public class GoodImpl implements GoodRpc {
    @Override
    public String say(String msg) {
        return "hello consumer" + msg;
    }
}
