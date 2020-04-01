package com.fr.swift.boot;

import com.fineio.FineIO;
import com.fr.swift.SwiftContext;
import com.fr.swift.boot.log.SwiftLog4jLoggers;
import com.fr.swift.boot.register.BootRegister;
import com.fr.swift.log.FineIoLogger;
import com.fr.swift.log.SwiftLoggers;
import com.fr.swift.service.ServiceContext;

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
            BootRegister.registerEntity();
            BootRegister.registerExecutorTask();

            SwiftLoggers.setLoggerFactory(new SwiftLog4jLoggers());
            SwiftContext.get().init();

            FineIO.setLogger(new FineIoLogger());
            SwiftCommandParser.parseCommand(args);
            BootRegister.registerProxy();

            BootRegister.registerListener();

            SwiftContext.get().getBean(ServiceContext.class).start();

            SwiftLoggers.getLogger().info("Swift engine start successful");
        } catch (Throwable e) {
            SwiftLoggers.getLogger().error(e);
            System.exit(1);
        }
    }
}