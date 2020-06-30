package com.fr.swift.boot;

import com.fr.swift.boot.trigger.ServicePriorityInitiator;
import com.fr.swift.log.SwiftLoggers;
import com.fr.swift.trigger.TriggerEvent;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * @author lucifer
 * @date 2019/11/23
 * @description
 * @since swift 1.1
 */
@Component
public class ApplicationReadyEventListener implements ApplicationListener<ApplicationReadyEvent> {
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        SwiftLoggers.getLogger().info("Spring boot application is ready!");
        ServicePriorityInitiator.getInstance().triggerByPriority(TriggerEvent.INIT);
    }
}
