package com.ljh.transport.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 服务调用方发起的请求
 *
 * @author ljh
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RpcRequest implements Serializable {

    /**
     * 请求id
     */
    private long requestId;

    /**
     * 请求类型
     */
    private byte requestType;

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
     * 具体了内容
     */
    private RequestPayload requestPayload;


    /**
     * 时间戳
     */
    private long timeStamp;


}
