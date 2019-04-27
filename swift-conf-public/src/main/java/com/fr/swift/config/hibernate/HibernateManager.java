package com.fr.swift.config.hibernate;

import com.fr.swift.config.SwiftConfigRegistryImpl;
import com.fr.swift.util.Assert;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

import java.io.File;

/**
 * @author yee
 * @date 2018/6/29
 */
public enum HibernateManager {
    //
    INSTANCE;
    private static volatile SessionFactory sessionFactory;

    private Configuration getConfiguration() {
        Configuration configuration = new Configuration();
        File confFile = new File("hibernate.cfg.xml");
        if (confFile.exists()) {
            configuration.configure(confFile);
        } else {
            configuration.configure("hibernate.cfg.xml");
        }

//        SwiftConfigConstants.registerEntity(AbstractExecutorTask.TYPE);
        for (Class<?> entity : SwiftConfigRegistryImpl.INSTANCE.getEntities()) {
            Assert.notNull(entity);
            configuration.addAnnotatedClass(entity);
        }
        return configuration;
    }

    synchronized
    public SessionFactory getFactory() {
        if (null == sessionFactory) {
            sessionFactory = initSessionFactory();
        }
        return sessionFactory;
    }

    private SessionFactory initSessionFactory() {
        Configuration configuration = getConfiguration();
        ServiceRegistry registry = new StandardServiceRegistryBuilder().applySettings(
                configuration.getProperties()).build();
        return configuration.buildSessionFactory(registry);
    }
}
