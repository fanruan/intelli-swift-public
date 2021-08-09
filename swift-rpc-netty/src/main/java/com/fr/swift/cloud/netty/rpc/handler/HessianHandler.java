package com.fr.swift.cloud.netty.rpc.handler;

import com.fr.swift.cloud.netty.rpc.serialize.hessian.HessianDecoder;
import com.fr.swift.cloud.netty.rpc.serialize.hessian.HessianEncoder;
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
public class HessianHandler implements RpcMessageHandler {

    @Override
    public void handle(CompressMode compressMode, ChannelPipeline pipeline) {
        pipeline.addLast(new MessageDecoder(new HessianDecoder(), compressMode));
        pipeline.addLast(new MessageEncoder(new HessianEncoder(), compressMode));
    }
}
