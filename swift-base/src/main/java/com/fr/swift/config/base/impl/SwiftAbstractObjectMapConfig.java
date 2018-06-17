package com.fr.swift.config.base.impl;

import com.fr.config.DefaultConfiguration;
import com.fr.config.holder.factory.Holders;
import com.fr.config.holder.impl.ObjectMapConf;
import com.fr.swift.config.base.FRConfTransactionWorker;
import com.fr.swift.config.base.SwiftMapConfig;
import com.fr.transaction.Configurations;

import java.util.HashMap;
import java.util.Map;

/**
 * @author yee
 * @date 2018/6/15
 */
public abstract class SwiftAbstractObjectMapConfig<T> extends DefaultConfiguration implements SwiftMapConfig<T> {
    private ObjectMapConf<Map<String, T>> configHolder;

    public SwiftAbstractObjectMapConfig(Class<T> clazz) {
        configHolder = Holders.objMap(new HashMap<String, T>(), String.class, clazz);
    }

    @Override
    public abstract String getNameSpace();

    @Override
    public boolean addOrUpdate(final String key, final T value) {
        return Configurations.update(new FRConfTransactionWorker(new Class[]{this.getClass()}) {
            @Override
            public void run() {
                configHolder.put(key, value);
            }
        });
    }

    @Override
    public T get(String key) {
        return (T) configHolder.get(key);
    }

    @Override
    public boolean remove(final String key) {
        return Configurations.update(new FRConfTransactionWorker(new Class[]{this.getClass()}) {
            @Override
            public void run() {
                configHolder.remove(key);
            }
        });
    }
}
