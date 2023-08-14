package com.ljh.transport.message;

import java.io.Serializable;

/**
 * 响应枚举类
 * @author ljh
 */
public enum RespCode  {


    SUCCESS((byte) 20,"成功"),
    SUCCESS_HEART_BEAT((byte) 21,"心跳检测成功返回"),
    RATE_LIMIT((byte)31,"服务被限流" ),
    RESOURCE_NOT_FOUND((byte)44,"请求的资源不存在" ),
    FAIL((byte)50,"调用方法发生异常"),
    BECOLSING((byte)51,"调用方法发生异常");


    private byte code;
    private String desc;

    RespCode(byte code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public byte getCode() {
        return code;
    }

    public void setCode(byte code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    @Override
    public String toString() {
        return "RespCode{" +
                "code=" + code +
                ", desc='" + desc + '\'' +
                '}';
    }
}
