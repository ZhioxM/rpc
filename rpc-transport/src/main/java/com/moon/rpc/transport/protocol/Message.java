package com.moon.rpc.transport.protocol;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author mzx
 * @date 2022/7/13 14:42
 * 将DTO封装成消息对象
 * 算了，先不用了，对泛型编程不是特别熟练
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message<T> implements Serializable {
    private MessageHeader header;

    private T body;
}
