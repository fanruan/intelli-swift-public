package com.fr.swift.cloud.netty.rpc.pool;

import com.fr.swift.cloud.log.SwiftLoggers;
import com.fr.swift.cloud.netty.rpc.client.AbstractRpcClientHandler;
import com.fr.swift.cloud.netty.rpc.property.RpcProperty;
import com.fr.swift.cloud.netty.rpc.serialize.SerializeFrame;
import com.fr.swift.cloud.property.SwiftProperty;
import com.fr.swift.cloud.rpc.compress.CompressMode;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.apache.commons.pool2.BaseKeyedPooledObjectFactory;
import org.apache.commons.pool2.PooledObject;

/**
 * This class created on 2018/8/1
 *
 * @author Lucifer
 * @description
 * @since Advanced FineBI 5.0
 */
public abstract class AbstractRpcKeyPoolFactory<T extends AbstractRpcClientHandler> extends BaseKeyedPooledObjectFactory<String, T> {

    private SwiftProperty swiftProperty = SwiftProperty.get();
    private final RpcProperty rpcProperty = RpcProperty.get();

    protected ChannelFuture bindBootstrap(final AbstractRpcClientHandler clientHandler, final String threadName) throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup(1, new DefaultThreadFactory(threadName));

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel channel) {
                ChannelPipeline pipeline = channel.pipeline();

                CompressMode compressMode = rpcProperty.getCompressMode();
                compressMode.setMaxObjectSize(swiftProperty.getRpcMaxObjectSize());
                SerializeFrame.select(rpcProperty.getSerializeProtocol(), compressMode, pipeline); // 处理 压缩 序列化
                pipeline.addLast(clientHandler);
            }
        });
        bootstrap.option(ChannelOption.TCP_NODELAY, true);
        clientHandler.setGroup(group);
        return bootstrap.connect(clientHandler.getHost(), clientHandler.getPort()).sync();
    }

    @Override
    public void destroyObject(String key, PooledObject<T> pooledObject) throws Exception {
        super.destroyObject(key, pooledObject);
        pooledObject.getObject().shutdown();
        SwiftLoggers.getLogger().debug("Destroy idle object end! [key:" + key + "]");
    }
}
