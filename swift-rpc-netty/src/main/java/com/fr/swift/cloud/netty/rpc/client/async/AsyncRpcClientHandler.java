package com.fr.swift.cloud.netty.rpc.client.async;

import com.fr.swift.cloud.basic.Request;
import com.fr.swift.cloud.basic.Response;
import com.fr.swift.cloud.basics.RpcFuture;
import com.fr.swift.cloud.log.SwiftLogger;
import com.fr.swift.cloud.log.SwiftLoggers;
import com.fr.swift.cloud.netty.rpc.client.AbstractRpcClientHandler;
import com.fr.swift.cloud.netty.rpc.pool.AsyncRpcPool;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

/**
 * This class created on 2018/6/11
 *
 * @author Lucifer
 * @description
 * @since Advanced FineBI 5.0
 */
@ChannelHandler.Sharable
public class AsyncRpcClientHandler extends AbstractRpcClientHandler<RpcFuture> {
    public static final String POOL_KEY = "AsyncRpcClientHandler";
    private static final SwiftLogger LOGGER = SwiftLoggers.getLogger(AsyncRpcClientHandler.class);
    private Map<String, RpcFuture> pendingRPC = new ConcurrentHashMap<>();

    public AsyncRpcClientHandler(String address) {
        super(address);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Response response) {
        String requestId = response.getRequestId();
        LOGGER.info("Receive response : " + requestId);
        RpcFuture rpcFuture = pendingRPC.get(requestId);
        if (rpcFuture != null) {
            pendingRPC.remove(requestId);
            rpcFuture.done(response);
        }
        AsyncRpcPool.getInstance().returnObject(address, this);
    }

    public void close() {
        channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }

    public RpcFuture send(final Request request) throws Exception {
        RpcFuture rpcFuture = new SwiftFuture(request);
        pendingRPC.put(request.getRequestId(), rpcFuture);
        final CountDownLatch latch = new CountDownLatch(1);
        channel.writeAndFlush(request).sync().addListener((ChannelFutureListener) future -> {
            LOGGER.info("Send request : " + request.getRequestId());
            latch.countDown();
        });
        return rpcFuture;
    }
}
