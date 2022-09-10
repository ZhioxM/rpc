package com.moon.rpc.transport.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;

/**
 * @author mzx
 */
@Getter
@Setter
public class RpcRequest extends Message {
    /**
     * 接口的名字(全限定名)
     */
    private String interfaceName;

    /**
     * 接口的版本
     */
    private String version;

    /**
     * 调用接口中的方法名
     */
    private String methodName;

    /**
     * 方法参数类型数组
     */
    private Class<?>[] parameterTypes;

    /**
     * 方法参数值数组
     */
    private Object[] parameterValue;

    /**
     * 超时时间
     */
    private long timeout;

    /**
     * 请求创建的时间，用于判断超时时间是否超时
     */
    private long createTime;


    public RpcRequest(int sequenceId, String interfaceName, String version, String methodName, Class<?>[] parameterTypes, Object[] parameterValue, long timeout) {
        this.sequenceId = sequenceId;
        this.interfaceName = interfaceName;
        this.version = version;
        this.methodName = methodName;
        this.parameterTypes = parameterTypes;
        this.parameterValue = parameterValue;
        this.timeout = timeout;
    }

    @Override
    public String toString() {
        return "RpcRequest{" +
                "interfaceName='" + interfaceName + '\'' +
                ", version='" + version + '\'' +
                ", methodName='" + methodName + '\'' +
                ", parameterTypes=" + Arrays.toString(parameterTypes) +
                ", parameterValue=" + Arrays.toString(parameterValue) +
                ", timeout=" + timeout +
                ", sequenceId=" + sequenceId +
                ", messageType=" + messageType +
                '}';
    }

    public boolean isTimeOut() {
        return System.currentTimeMillis() - createTime > timeout;
    }
}
