package com.ljh.transport.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 请求调用接口的描述
 * @author ljh
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestPayload implements Serializable {

    /**
     * 接口的名字
     */
    private String interfaceName;

    /**
     * 方法的名字
     */
    private String methodName;

    /**
     * 参数
     *
     */
    private Class<?>[] parametersType;

    private Object[] parametersValue;

    /**
     * 返回值
     */
    private Class<?> returnType;
}
