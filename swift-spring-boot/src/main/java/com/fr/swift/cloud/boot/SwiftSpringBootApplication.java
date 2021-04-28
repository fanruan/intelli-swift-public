package com.fr.swift.cloud.boot;

import com.fr.swift.cloud.boot.dispatcher.SwiftControllerDispatcher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.DispatcherServlet;

/**
 * This class created on 2018/10/30
 *
 * @author Lucifer
 * @description
 * @since Advanced FineBI 5.0
 */
@SpringBootApplication
public class SwiftSpringBootApplication {
    public static void main(String[] args) {
        SwiftEngineStart.start(args);
        ApplicationContext run = SpringApplication.run(SwiftSpringBootApplication.class, args);
        SwiftSpringContext.setApplicationContext(run);
    }

    @Bean
    @Qualifier(DispatcherServletAutoConfiguration.DEFAULT_DISPATCHER_SERVLET_BEAN_NAME)
    public DispatcherServlet dispatcherServlet() {
        return new SwiftControllerDispatcher();
    }

    @Bean
    public ApplicationShutdown customShutdown() {
        return new ApplicationShutdown();
    }

    @Bean
    public ConfigurableServletWebServerFactory webServerFactory(final ApplicationShutdown customShutdown) {
        TomcatServletWebServerFactory tomcatServletWebServerFactory = new TomcatServletWebServerFactory();
        tomcatServletWebServerFactory.addConnectorCustomizers(customShutdown);
        return tomcatServletWebServerFactory;
    }
}
