package com.fr.swift.netty.rpc.pool;

import com.fr.swift.log.SwiftLoggers;
import com.fr.swift.netty.rpc.client.AbstractRpcClientHandler;
import com.fr.swift.netty.rpc.property.RpcProperty;
import org.apache.commons.pool2.KeyedObjectPool;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;

/**
 * This class created on 2018/8/1
 *
 * @author Lucifer
 * @description
 * @since Advanced FineBI 5.0
 */
public abstract class AbstractRpcPool implements KeyedObjectPool<String, AbstractRpcClientHandler> {

    protected GenericKeyedObjectPool keyedObjectPool;

    public AbstractRpcPool(AbstractRpcKeyPoolFactory rpcKeyPoolFactory) {
        GenericKeyedObjectPoolConfig config = new GenericKeyedObjectPoolConfig();
        config.setTimeBetweenEvictionRunsMillis(RpcProperty.get().getTimeBetweenEvictionRunsMillis());
        config.setMinEvictableIdleTimeMillis(RpcProperty.get().getMinEvictableIdleTimeMillis());
        config.setMaxTotalPerKey(RpcProperty.get().getMaxTotalPerKey());
        config.setMaxIdlePerKey(RpcProperty.get().getMaxIdlePerKey());
        config.setMinIdlePerKey(RpcProperty.get().getMinIdlePerKey());
        config.setMaxTotal(RpcProperty.get().getMaxTotal());
        keyedObjectPool = new GenericKeyedObjectPool(rpcKeyPoolFactory, config);
    }

    @Override
    public AbstractRpcClientHandler borrowObject(String key) throws Exception {
        SwiftLoggers.getLogger().info("borrow rpc key[{}]", key);
        AbstractRpcClientHandler handler = (AbstractRpcClientHandler) keyedObjectPool.borrowObject(key);
        SwiftLoggers.getLogger().info("current key pool size: max[{}],active[{}],idle[{}]"
                , keyedObjectPool.getMaxTotalPerKey(), keyedObjectPool.getNumActive(), keyedObjectPool.getNumIdle());
        return handler;
    }

    @Override
    public void returnObject(String key, AbstractRpcClientHandler handler) {
        SwiftLoggers.getLogger().info("return rpc key[{}]", key);
        keyedObjectPool.returnObject(key, handler);
        SwiftLoggers.getLogger().info("current key pool size: max[{}],active[{}],idle[{}]"
                , keyedObjectPool.getMaxTotalPerKey(), keyedObjectPool.getNumActive(), keyedObjectPool.getNumIdle());
    }

    @Override
    public void invalidateObject(String key, AbstractRpcClientHandler handler) throws Exception {
        keyedObjectPool.invalidateObject(key, handler);
    }

    @Override
    public void addObject(String key) throws Exception {
        keyedObjectPool.addObject(key);
    }

    @Override
    public int getNumIdle(String key) {
        return keyedObjectPool.getNumIdle(key);
    }

    @Override
    public int getNumActive(String key) {
        return keyedObjectPool.getNumActive(key);
    }

    @Override
    public int getNumIdle() {
        return keyedObjectPool.getNumIdle();
    }

    @Override
    public int getNumActive() {
        return keyedObjectPool.getNumActive();
    }

    @Override
    public void clear() {
        keyedObjectPool.clear();
    }

    @Override
    public void clear(String key) {
        keyedObjectPool.clear(key);
    }

    @Override
    public void close() {
        keyedObjectPool.close();
    }
}
