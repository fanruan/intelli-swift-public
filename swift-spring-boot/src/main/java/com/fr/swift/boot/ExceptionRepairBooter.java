package com.fr.swift.boot;

import com.fr.swift.SwiftContext;
import com.fr.swift.basics.base.selector.ProxySelector;
import com.fr.swift.exception.consumer.ExceptionInfoConsumer;
import com.fr.swift.exception.handler.ExceptionHandler;
import com.fr.swift.exception.handler.RegisterExceptionHandler;
import com.fr.swift.exception.queue.MasterExceptionInfoQueue;
import com.fr.swift.exception.queue.SlaveExceptionInfoQueue;
import com.fr.swift.exception.service.ExceptionHandleService;
import com.fr.swift.exception.service.ExceptionHandlerRegistry;
import com.fr.swift.util.concurrent.PoolThreadFactory;
import com.fr.swift.util.concurrent.SwiftExecutors;

import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * @author Marvin
 * @date 8/15/2019
 * @description
 * @since swift 1.1
 */
public class ExceptionRepairBooter {

    public static void boot() {
        bootQueue();

        bootConsumers();

        bootHandlerRegistry();
    }

    private static void bootHandlerRegistry() {
        Map<String, Object> handlers = SwiftContext.get().getBeansByAnnotations(RegisterExceptionHandler.class);
        for (Object value : handlers.values()) {
            ExceptionHandler handler = (ExceptionHandler) value;
            ExceptionHandlerRegistry.getInstance().registerExceptionHandler(handler);
        }
    }

    private static void bootConsumers() {
        int nThreads = Runtime.getRuntime().availableProcessors();
        ExecutorService slaveExecutor = SwiftExecutors.newFixedThreadPool(nThreads, new PoolThreadFactory(ExceptionInfoConsumer.class));
        ExecutorService masterExecutor = SwiftExecutors.newFixedThreadPool(nThreads, new PoolThreadFactory(ExceptionInfoConsumer.class));
        for (int i = 0; i < nThreads; i++) {
            slaveExecutor.execute(new ExceptionInfoConsumer(SlaveExceptionInfoQueue.getInstance(), SwiftContext.get().getBean(ExceptionHandleService.class)));
            masterExecutor.execute(new ExceptionInfoConsumer(MasterExceptionInfoQueue.getInstance(), ProxySelector.getProxy(ExceptionHandleService.class)));
        }
    }

    private static void bootQueue() {
        //初始化任务队列，取出未处理的异常加入队列
        SlaveExceptionInfoQueue.getInstance().initExceptionInfoQueue();
        MasterExceptionInfoQueue.getInstance().initExceptionInfoQueue();
    }
}
