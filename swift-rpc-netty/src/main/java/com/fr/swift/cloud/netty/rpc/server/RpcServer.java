package com.fr.swift.cloud.netty.rpc.server;

import com.fr.swift.cloud.beans.annotation.SwiftBean;
import com.fr.swift.cloud.log.SwiftLogger;
import com.fr.swift.cloud.log.SwiftLoggers;
import com.fr.swift.cloud.netty.rpc.property.RpcProperty;
import com.fr.swift.cloud.netty.rpc.registry.ServiceRegistry;
import com.fr.swift.cloud.netty.rpc.serialize.SerializeFrame;
import com.fr.swift.cloud.property.SwiftProperty;
import com.fr.swift.cloud.rpc.compress.CompressMode;
import com.fr.swift.cloud.util.Strings;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.NettyRuntime;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.internal.SystemPropertyUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * This class created on 2018/6/6
 *
 * @author Lucifer
 * @description
 * @since Advanced FineBI 5.0
 */
@SwiftBean(name = "rpcServer")
public class RpcServer {
    private static final SwiftLogger LOGGER = SwiftLoggers.getLogger(RpcServer.class);

    private String serviceAddress;

    private ServiceRegistry serviceRegistry;

    private final SwiftProperty swiftProperty;
    private final RpcProperty rpcProperty;

    private static final int DEFAULT_EVENT_LOOP_THREADS;

    static {
        DEFAULT_EVENT_LOOP_THREADS = Math.max(1, SystemPropertyUtil.getInt(
                "io.netty.eventLoopThreads", NettyRuntime.availableProcessors() * 2));
    }

    /**
     * key:服务名
     * value:服务对象
     */
    private Map<String, Object> handlerMap = new HashMap<String, Object>();
    private Map<String, Object> externalMap = new HashMap<String, Object>();

    public RpcServer() {
        swiftProperty = SwiftProperty.get();
        rpcProperty = RpcProperty.get();
        this.serviceAddress = swiftProperty.getServerAddress();
    }

    public void start() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup(DEFAULT_EVENT_LOOP_THREADS, new DefaultThreadFactory("rpc-server-boss"));
        EventLoopGroup workerGroup = new NioEventLoopGroup(DEFAULT_EVENT_LOOP_THREADS, new DefaultThreadFactory("rpc-server-worker"));
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup);
            bootstrap.channel(NioServerSocketChannel.class);
            bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel channel) {
                    ChannelPipeline pipeline = channel.pipeline();

                    CompressMode compressMode = rpcProperty.getCompressMode();
                    compressMode.setMaxObjectSize(swiftProperty.getRpcMaxObjectSize());
                    SerializeFrame.select(rpcProperty.getSerializeProtocol(), compressMode, pipeline); // 处理 压缩 序列化
                    pipeline.addLast(new RpcServerHandler(handlerMap, externalMap)); // 处理 RPC 请求
                }
            });
            bootstrap.option(ChannelOption.SO_BACKLOG, 1024);
            bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
            String[] addressArray = Strings.split(serviceAddress, ":");
            String ip = addressArray[0];
            int port = Integer.parseInt(addressArray[1]);
            ChannelFuture future = bootstrap.bind(ip, port).sync();
            if (serviceRegistry != null) {
                for (String interfaceName : handlerMap.keySet()) {
                    serviceRegistry.register(interfaceName, serviceAddress);
                }
            }
            LOGGER.info("RPC server started on ip:" + ip + ", port :" + port);
            future.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
