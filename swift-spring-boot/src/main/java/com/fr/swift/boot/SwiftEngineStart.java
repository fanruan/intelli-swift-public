package com.fr.swift.boot;

import com.fineio.FineIO;
import com.fr.swift.SwiftContext;
import com.fr.swift.boot.register.BootRegister;
import com.fr.swift.bytebuddy.DynamicClassLoader;
import com.fr.swift.cluster.listener.NodeStartedListener;
import com.fr.swift.config.PublicConfig;
import com.fr.swift.cube.queue.ProviderTaskManager;
import com.fr.swift.event.ClusterEvent;
import com.fr.swift.event.ClusterEventType;
import com.fr.swift.event.ClusterListenerHandler;
import com.fr.swift.event.ClusterType;
import com.fr.swift.log.FineIoLogger;
import com.fr.swift.log.SwiftLog4jLoggers;
import com.fr.swift.log.SwiftLoggers;
import com.fr.swift.property.SwiftProperty;
import com.fr.swift.service.local.LocalManager;
import com.fr.swift.service.local.ServerManager;

/**
 * This class created on 2018/6/12
 *
 * @author Lucifer
 * @description
 * @since Advanced FineBI 5.0
 */
public class SwiftEngineStart {

    public static void start(String[] args) {
        try {
            ClassLoader loader = new DynamicClassLoader(SwiftEngineStart.class.getClassLoader());
            BootRegister.registerEntity(loader);
            BootRegister.registerExecutorTask();

            SwiftLoggers.setLoggerFactory(new SwiftLog4jLoggers());
            SwiftContext.get().init();
            ClusterListenerHandler.addInitialListener(new SwiftClusterListener());
            ClusterListenerHandler.addInitialListener(NodeStartedListener.INSTANCE);
            FineIO.setLogger(new FineIoLogger());
            ProviderTaskManager.start();
            SwiftCommandParser.parseCommand(args);

            BootRegister.registerProxy();
            BootRegister.registerListener();

            PublicConfig.load();

            SwiftContext.get().getBean(LocalManager.class).startUp();
            if (SwiftProperty.getProperty().isCluster()) {
                ClusterListenerHandler.handlerEvent(new ClusterEvent(ClusterEventType.JOIN_CLUSTER, ClusterType.CONFIGURE));
            }
            SwiftContext.get().getBean(ServerManager.class).startUp();
            SwiftLoggers.getLogger().info("Swift engine start successful");
        } catch (Throwable e) {
            SwiftLoggers.getLogger().error(e);
            System.exit(1);
        }
    }
}