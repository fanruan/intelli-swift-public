package com.fr.swift.cloud.netty.rpc.serialize;

import com.fr.swift.cloud.netty.rpc.handler.HessianHandler;
import com.fr.swift.cloud.netty.rpc.handler.JdkHandler;
import com.fr.swift.cloud.netty.rpc.handler.KryoHandler;
import com.fr.swift.cloud.rpc.compress.CompressMode;
import com.fr.swift.cloud.rpc.handler.RpcMessageHandler;
import com.fr.swift.cloud.rpc.serialize.SerializeProtocol;
import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.MutableClassToInstanceMap;
import io.netty.channel.ChannelPipeline;


/**
 * @author Heng.J
 * @date 2021/7/30
 * @description
 * @since swift-1.2.0
 */
public class SerializeFrame {

    private static final ClassToInstanceMap<RpcMessageHandler> HANDLER = MutableClassToInstanceMap.create();

    static {
        HANDLER.putInstance(JdkHandler.class, new JdkHandler());
        HANDLER.putInstance(KryoHandler.class, new KryoHandler());
        HANDLER.putInstance(HessianHandler.class, new HessianHandler());
    }

    public static void select(SerializeProtocol protocol, CompressMode compressMode, ChannelPipeline pipeline) {
        switch (protocol) {
            //others :
            case KRYO_SERIALIZE: {
                HANDLER.getInstance(KryoHandler.class).handle(compressMode, pipeline);
                break;
            }
            case HESSIAN_SERIALIZE: {
                HANDLER.getInstance(HessianHandler.class).handle(compressMode, pipeline);
                break;
            }
            case JDK_SERIALIZE:
            default: {
                HANDLER.getInstance(JdkHandler.class).handle(compressMode, pipeline);
                break;
            }
        }
    }
}

