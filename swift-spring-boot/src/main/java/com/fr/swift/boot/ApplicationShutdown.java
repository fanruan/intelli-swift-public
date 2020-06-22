package com.fr.swift.boot;

import com.fr.swift.boot.trigger.ServicePriorityInitiator;
import com.fr.swift.trigger.TriggerEvent;
import org.apache.catalina.connector.Connector;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Author: lucifer
 * @Description:
 * @Date: Created in 2020/6/17
 */
public class ApplicationShutdown implements TomcatConnectorCustomizer, ApplicationListener<ContextClosedEvent> {
    private static final int TIMEOUT = 30;

    private volatile Connector connector;

    @Override
    public void customize(Connector connector) {
        this.connector = connector;
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        this.connector.pause();
        Executor executor = this.connector.getProtocolHandler().getExecutor();
        if (executor instanceof ThreadPoolExecutor) {
            ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) executor;
            threadPoolExecutor.shutdown();
            try {
                if (!threadPoolExecutor.awaitTermination(TIMEOUT, TimeUnit.SECONDS)) {
                    threadPoolExecutor.shutdownNow();
                }
                ServicePriorityInitiator.getInstance().triggerByPriority(TriggerEvent.DESTROY);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
