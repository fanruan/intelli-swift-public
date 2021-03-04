package com.fr.swift.cloud.netty.rpc.registry.impl;

import com.fr.swift.cloud.beans.annotation.SwiftBean;
import com.fr.swift.cloud.netty.rpc.registry.ServiceRegistry;

/**
 * This class created on 2018/6/7
 *
 * @author Lucifer
 * @description
 * @since Advanced FineBI 5.0
 */
@SwiftBean(name = "serviceRegistry")
public class SimpleServiceRegistry implements ServiceRegistry {

    @Override
    public void register(String serviceName, String serviceAddress) {

    }
}
