package com.ljh.test;

/**
 * @author 李俊豪
 * 公共的测试接口,server和 client都需要依赖
 */
public interface GoodRpc {

    /**
     * 简单的返回一个消息
     * @param msg
     * @return
     */
    String say(String msg);
}
