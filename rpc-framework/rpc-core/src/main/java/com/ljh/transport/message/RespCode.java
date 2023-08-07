package com.ljh.transport.message;

import java.io.Serializable;

/**
 * 响应枚举类
 * @author ljh
 */
public enum RespCode  {
    SUCCESS((byte) 1, "成功"),
    FAIT((byte) 1, "失败");


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
