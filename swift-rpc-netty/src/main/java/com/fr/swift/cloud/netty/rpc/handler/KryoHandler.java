package com.fr.swift.cloud.netty.rpc.handler;

import com.fr.swift.cloud.netty.rpc.serialize.kryo.KryoDecoder;
import com.fr.swift.cloud.netty.rpc.serialize.kryo.KryoEncoder;
import com.fr.swift.cloud.rpc.compress.CompressMode;
import com.fr.swift.cloud.rpc.handler.RpcMessageHandler;
import com.fr.swift.cloud.rpc.serialize.MessageDecoder;
import com.fr.swift.cloud.rpc.serialize.MessageEncoder;
import io.netty.channel.ChannelPipeline;

/**
 * @author Heng.J
 * @date 2021/7/30
 * @description
 * @since swift-1.2.0
 */
public class KryoHandler implements RpcMessageHandler {

    @Override
    public void handle(CompressMode compressMode, ChannelPipeline pipeline) {
        pipeline.addLast(new MessageDecoder(new KryoDecoder(), compressMode));
        pipeline.addLast(new MessageEncoder(new KryoEncoder(), compressMode));
    }
}

