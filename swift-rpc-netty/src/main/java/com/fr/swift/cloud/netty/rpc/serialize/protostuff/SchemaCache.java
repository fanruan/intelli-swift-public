package com.fr.swift.cloud.netty.rpc.serialize.protostuff;


import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import com.fr.swift.cloud.log.SwiftLoggers;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author Heng.J
 * @date 2021/8/4
 * @description
 * @since swift-1.2.0
 */
public class SchemaCache {

    private static class SchemaCacheHolder {
        private static final SchemaCache CACHE = new SchemaCache();
    }

    public static SchemaCache getInstance() {
        return SchemaCacheHolder.CACHE;
    }

    private final Cache<Class<?>, Schema<?>> cache = CacheBuilder.newBuilder()
            .maximumSize(1024).expireAfterWrite(1, TimeUnit.HOURS)
            .build();

    private Schema<?> get(final Class<?> cls, Cache<Class<?>, Schema<?>> cache) {
        try {
            return cache.get(cls, (Callable<RuntimeSchema<?>>) () -> RuntimeSchema.createFrom(cls));
        } catch (ExecutionException e) {
            SwiftLoggers.getLogger().error(e);
            return null;
        }
    }

    public Schema<?> get(final Class<?> cls) {
        return get(cls, cache);
    }

}

