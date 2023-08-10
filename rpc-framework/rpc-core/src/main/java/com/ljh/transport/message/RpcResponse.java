package com.ljh.transport.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 服务提供方回复的响应
 *
 * @author ljh
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RpcResponse implements Serializable {

    /**
     * 请求id
     */
    private long requestId;

    /**
     * 响应码
     */
    private byte code;

    /**
     * 压缩类型
     */
    private byte compressType;

    /**
     *
     * 序列化方式
     */
    private byte serializeType;

    /**
     * 具响应内容
     */
    private Object object;

    /**
     * 时间戳
     */
    private long timeStamp;



}
