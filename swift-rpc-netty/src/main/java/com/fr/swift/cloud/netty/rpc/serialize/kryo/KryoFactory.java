package com.fr.swift.cloud.netty.rpc.serialize.kryo;

import com.esotericsoftware.kryo.Kryo;
import org.objenesis.strategy.StdInstantiatorStrategy;

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
        kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
//        kryo.setWarnUnregisteredClasses(true);
        return kryo;
    }
}
