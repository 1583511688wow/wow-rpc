package com.ljh.enumeration;

/**
 *
 *请求类型枚举类
 * @author ljh
 */

public enum RequestType {
    REQUEST((byte) 1, "普通请求"),
    HEART_BEAT((byte) 2, "心跳检测请求");


    RequestType(byte id, String type) {
        this.id = id;
        this.type = type;
    }

    private byte id;
    private String type;

    public byte getId() {
        return id;
    }

    public void setId(byte id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "RequestType{" +
                "id=" + id +
                ", type='" + type + '\'' +
                '}';
    }
}
