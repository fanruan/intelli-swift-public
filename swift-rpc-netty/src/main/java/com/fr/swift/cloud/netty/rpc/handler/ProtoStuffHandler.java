package com.fr.swift.cloud.netty.rpc.handler;

import com.fr.swift.cloud.netty.rpc.serialize.protostuff.ProtostuffDecoder;
import com.fr.swift.cloud.netty.rpc.serialize.protostuff.ProtostuffEncoder;
import com.fr.swift.cloud.rpc.compress.CompressMode;
import com.fr.swift.cloud.rpc.handler.RpcMessageHandler;
import com.fr.swift.cloud.rpc.serialize.MessageDecoder;
import com.fr.swift.cloud.rpc.serialize.MessageEncoder;
import io.netty.channel.ChannelPipeline;

/**
 * @author Heng.J
 * @date 2021/8/4
 * @description
 * @since swift-1.2.0
 */
public class ProtoStuffHandler implements RpcMessageHandler {

    @Override
    public void handle(CompressMode compressMode, ChannelPipeline pipeline) {
        pipeline.addLast(new MessageDecoder(new ProtostuffDecoder(), compressMode));
        pipeline.addLast(new MessageEncoder(new ProtostuffEncoder(), compressMode));
    }

    @Deprecated
    public void handle(CompressMode compressMode, ChannelPipeline pipeline, boolean isServer) {
        ProtostuffDecoder protostuffDecoder = new ProtostuffDecoder();
        protostuffDecoder.setServer(isServer);
        pipeline.addLast(new MessageDecoder(protostuffDecoder, compressMode));
        pipeline.addLast(new MessageEncoder(new ProtostuffEncoder(), compressMode));
    }
}
