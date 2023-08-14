package com.ljh.test;

import com.ljh.annotation.TryTimes;

/**
 * @author 李俊豪
 * 公共的测试接口,server和 client都需要依赖
 */
public interface HelloRpc {

    /**
     * 简单的返回一个消息
     * @param msg
     * @return
     */
    @TryTimes(tryTimes = 3, intervalTime = 3000)
    String say(String msg);
}
