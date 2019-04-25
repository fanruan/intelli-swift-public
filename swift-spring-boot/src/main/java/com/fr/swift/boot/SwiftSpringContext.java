package com.fr.swift.boot;

import org.springframework.context.ApplicationContext;

/**
 * This class created on 2019/4/3
 *
 * @author Lucifer
 * @description
 */
public class SwiftSpringContext {

    private static ApplicationContext applicationContext = null;

    public static void setApplicationContext(ApplicationContext applicationContext) {
        if (SwiftSpringContext.applicationContext == null) {
            SwiftSpringContext.applicationContext = applicationContext;
        }
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public static Object getBean(String name) {
        return getApplicationContext().getBean(name);

    }

    public static <T> T getBean(Class<T> clazz) {
        return getApplicationContext().getBean(clazz);
    }

    public static <T> T getBean(String name, Class<T> clazz) {
        return getApplicationContext().getBean(name, clazz);
    }
}
