package com.fr.swift.service.listener;

import com.fr.swift.exception.SwiftServiceException;
import com.fr.swift.log.SwiftLogger;
import com.fr.swift.log.SwiftLoggers;
import com.fr.swift.service.SwiftService;
import com.fr.swift.service.SwiftServiceEvent;

/**
 * Created by pony on 2017/11/10.
 * 待实现，接收远程serverService的注册，触发事件
 */
public class RemoteServiceReceiver implements SwiftServiceListenerHandler {

    private static final SwiftLogger LOGGER = SwiftLoggers.getLogger(RemoteServiceReceiver.class);

    private static final RemoteServiceReceiver INSTANCE = new RemoteServiceReceiver();

    private RemoteServiceReceiver() {
    }

    public static RemoteServiceReceiver getInstance() {
        return INSTANCE;
    }

    @Override
    public void addListener(SwiftServiceListener listener) {

    }

    @Override
    public void trigger(SwiftServiceEvent event) {
    }

    @Override
    public void registerService(SwiftService service) {
        try {
            LOGGER.info("RemoteServiceReceiver registerService");
            SwiftServiceListenerManager.getInstance().registerService(service);
        } catch (SwiftServiceException e) {
            LOGGER.error(e);
        }
    }

    @Override
    public void unRegisterService(SwiftService service) {
        try {
            LOGGER.info("RemoteServiceReceiver unRegisterService");
            SwiftServiceListenerManager.getInstance().unRegisterService(service);
        } catch (SwiftServiceException e) {
            LOGGER.error(e);
        }
    }
}
