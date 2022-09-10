package com.moon.rpc.client.generic;

/**
 * @author xuxueli 2018-12-04
 */
public interface RpcGenericService {

    /**
     * generic invoke
     *
     * @param iface          iface name
     * @param version        iface version
     * @param method         method name
     * @param parameterTypes parameter types, limit base type like "int、java.lang.Integer、java.util.List、java.util.Map ..."
     * @param args
     * @return
     */
    public <T> T invoke(String iface,  String method, Class<?>[] parameterTypes, Object[] args);

}