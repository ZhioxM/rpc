package com.moon.rpc.transport.dto;

import com.moon.rpc.transport.constant.MessageType;
import jdk.nashorn.internal.ir.RuntimeNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * @author mzx
 */
@Getter
@Setter
public class RpcRequest extends Message{
    /**
     * 接口的名字(全限定名)
     */
    private String apiName;

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
     * 超时时间，这个时间提供给服务端使用
     */
    private long timeout;


    /**
     * 超时时间的单位
     */
    private TimeUnit timeUnit;

    public RpcRequest(int sequenceId, String apiName, String methodName, Class<?>[] parameterTypes, Object[] parameterValue, long timeout, TimeUnit timeUnit) {
        this.sequenceId = sequenceId;
        this.messageType = MessageType.REQUEST.getType();
        this.apiName = apiName;
        this.methodName = methodName;
        this.parameterTypes = parameterTypes;
        this.parameterValue = parameterValue;
        this.timeout = timeout;
        this.timeUnit = timeUnit;
    }

    @Override
    public String toString() {
        return "RpcRequest{" +
                "apiName='" + apiName + '\'' +
                ", methodName='" + methodName + '\'' +
                ", parameterTypes=" + Arrays.toString(parameterTypes) +
                ", parameterValue=" + Arrays.toString(parameterValue) +
                ", timeout=" + timeout +
                ", timeUnit=" + timeUnit +
                ", sequenceId=" + sequenceId +
                ", messageType=" + messageType +
                '}';
    }
}
