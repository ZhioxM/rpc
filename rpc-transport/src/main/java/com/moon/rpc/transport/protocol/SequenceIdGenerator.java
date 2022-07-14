package com.moon.rpc.transport.protocol;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author mzx
 * 消息id生成器
 */
public abstract class SequenceIdGenerator {
    private static final AtomicInteger id = new AtomicInteger();

    public static int nextId() {
        return id.incrementAndGet();
    }
}