package com.fr.swift.cloud.netty.rpc.handler;

import com.fr.swift.cloud.property.SwiftProperty;
import com.fr.swift.cloud.rpc.compress.CompressMode;
import com.fr.swift.cloud.rpc.handler.RpcMessageHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

/**
 * @author Heng.J
 * @date 2021/7/30
 * @description
 * @since swift-1.2.0
 */
public class JdkHandler implements RpcMessageHandler {

    @Override
    public void handle(CompressMode compressMode, ChannelPipeline pipeline) {
        //H.J TODO: 2021/7/30 ClassResolver 原本客户端是 cacheDisabled 服务端是 weakCachingConcurrentResolver 的原因？
        pipeline.addLast(new ObjectDecoder(SwiftProperty.get().getRpcMaxObjectSize(),
                ClassResolvers.cacheDisabled(this.getClass().getClassLoader())));
        pipeline.addLast(new ObjectEncoder());
    }
}
