package com.fr.swift.cloud.netty.rpc.pool;

import com.fr.swift.cloud.log.SwiftLogger;
import com.fr.swift.cloud.log.SwiftLoggers;
import com.fr.swift.cloud.netty.rpc.client.sync.SyncRpcClientHandler;
import io.netty.channel.ChannelFuture;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

/**
 * This class created on 2018/8/1
 *
 * @author Lucifer
 * @description
 * @since Advanced FineBI 5.0
 */
public class SyncRpcKeyPoolFactory extends AbstractRpcKeyPoolFactory<SyncRpcClientHandler> {

    private static final SwiftLogger LOGGER = SwiftLoggers.getLogger();

    @Override
    public SyncRpcClientHandler create(String address) throws Exception {
        final SyncRpcClientHandler clientHandler = new SyncRpcClientHandler(address);
        ChannelFuture future = bindBootstrap(clientHandler, "rpc-client-sync");
        return clientHandler;
    }

    @Override
    public PooledObject<SyncRpcClientHandler> wrap(SyncRpcClientHandler handler) {
        return new DefaultPooledObject<SyncRpcClientHandler>(handler);
    }

}
