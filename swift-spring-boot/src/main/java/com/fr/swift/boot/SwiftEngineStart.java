package com.fr.swift.boot;

import com.fineio.FineIO;
import com.fr.swift.SwiftContext;
import com.fr.swift.boot.log.SwiftLog4jLoggers;
import com.fr.swift.boot.register.BootRegister;
import com.fr.swift.log.FineIoLogger;
import com.fr.swift.log.SwiftLoggers;

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
            SwiftLoggers.setLoggerFactory(new SwiftLog4jLoggers());
// TODO: 2020/4/23 这个地方有问题，也耦合了。。要想办法改掉这个东西
            BootRegister.registerEntity();

            BootRegister.registerExecutorTask();
            BootRegister.registerListener();

            SwiftContext.get().init();
            BootRegister.registerProxy();

            FineIO.setLogger(new FineIoLogger());
            SwiftCommandParser.parseCommand(args);
            SwiftLoggers.getLogger().info("Swift engine start successful");
        } catch (Throwable e) {
            SwiftLoggers.getLogger().error(e);
            System.exit(1);
        }
    }
}