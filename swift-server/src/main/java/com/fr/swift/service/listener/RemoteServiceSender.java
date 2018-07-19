package com.fr.swift.service.listener;

import com.fr.swift.event.base.SwiftRpcEvent;
import com.fr.swift.log.SwiftLogger;
import com.fr.swift.log.SwiftLoggers;
import com.fr.swift.annotation.RpcMethod;
import com.fr.swift.annotation.RpcService;
import com.fr.swift.annotation.RpcServiceType;
import com.fr.swift.service.SwiftService;
import com.fr.swift.service.SwiftServiceEvent;

import java.io.Serializable;

/**
 * Created by pony on 2017/11/9.
 * 待实现，向远程的serverService注册本地启动的服务，触发事件
 */
@RpcService(value = SwiftServiceListenerHandler.class, type = RpcServiceType.SERVER_SERVICE)
public class RemoteServiceSender implements SwiftServiceListenerHandler {

    private static final SwiftLogger LOGGER = SwiftLoggers.getLogger(RemoteServiceSender.class);

    private static final RemoteServiceSender INSTANCE = new RemoteServiceSender();

    private RemoteServiceSender() {
    }

    public static RemoteServiceSender getInstance() {
        return INSTANCE;
    }

    @Override
    public void addListener(SwiftServiceListener listener) {

    }

    @Override
    public void trigger(SwiftServiceEvent event) {

    }

    @Override
    @RpcMethod(methodName = "rpcTrigger")
    public Serializable trigger(SwiftRpcEvent event) {
        return RemoteServiceReceiver.getInstance().trigger(event);
    }

    @Override
    public void registerService(SwiftService service) {
        LOGGER.info("RemoteServiceSender registerService");
        RemoteServiceReceiver.getInstance().registerService(service);
    }

    @Override
    public void unRegisterService(SwiftService service) {
        LOGGER.info("RemoteServiceSender unRegisterService");
        RemoteServiceReceiver.getInstance().unRegisterService(service);
    }
}
