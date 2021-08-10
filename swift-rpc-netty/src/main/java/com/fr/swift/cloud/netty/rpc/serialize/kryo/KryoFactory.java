package com.fr.swift.cloud.netty.rpc.serialize.kryo;

import com.fr.third.esotericsoftware.kryo.Kryo;
import com.fr.third.org.objenesis.strategy.StdInstantiatorStrategy;

/**
 * @author Heng.J
 * @date 2021/7/28
 * @description 对应上层的对象池生命周期, 这里没有单独使用对象池维护
 * @since swift-1.2.0
 */
public class KryoFactory {

    private static final ThreadLocal<Kryo> KRYO_THREAD_LOCAL = ThreadLocal.withInitial(KryoFactory::createKryo);

    public static Kryo getKryo() {
        return KRYO_THREAD_LOCAL.get();
    }

    public static Kryo createKryo() {
        Kryo kryo = new Kryo();
        kryo.setReferences(false);
        kryo.setRegistrationRequired(false);

        // 首先使用默认无参构造策略DefaultInstantiatorStrategy，若创建对象失败则采用StdInstantiatorStrategy
        kryo.setInstantiatorStrategy(new Kryo.DefaultInstantiatorStrategy(new StdInstantiatorStrategy()));
//        kryo.setWarnUnregisteredClasses(true);
        return kryo;
    }
}
