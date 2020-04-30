package com.fr.swift.cluster.zookeeper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * This class created on 2020/4/13
 *
 * @author Kuifang.Liu
 */
@Service
public class ZkProperty {
    private String zookeeperAddress = "192.168.5.66:2181";

    private int sessionTimeout = 30000;
    private int connectionTimeout = 20000;

//    @Autowired
//    public void setZookeeperAddress(@Value("${swift.zookeeper_address}") String zookeeperAddress) {
//        this.zookeeperAddress = zookeeperAddress;
//    }

    public String getZookeeperAddress() {
        return zookeeperAddress;
    }

    public int getSessionTimeout() {
        return sessionTimeout;
    }

    @Autowired(required = false)
    public void setSessionTimeout(@Value("${swift.zookeeper_session_timeout}") int sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    @Autowired(required = false)
    public void setConnectionTimeout(@Value("${swift.zookeeper_connection_timeout}") int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }
}
