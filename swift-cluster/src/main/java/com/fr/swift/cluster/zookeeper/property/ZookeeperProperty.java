package com.fr.swift.cluster.zookeeper.property;

import com.fr.swift.config.ConfigInputUtil;
import com.fr.swift.util.Crasher;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * This class created on 2020/4/30
 *
 * @author Kuifang.Liu
 */
public class ZookeeperProperty {
    private Properties properties;
    private String zookeeperAddress;
    private int zookeeperSessionTimeout;
    private int zookeeperConnectionTimeout;

    private static final ZookeeperProperty INSTANCE = new ZookeeperProperty();

    private ZookeeperProperty() {
        initProperty();
    }

    public static ZookeeperProperty get() {
        return INSTANCE;
    }

    public void initProperty() {
        properties = new Properties();
        InputStream zookeeperIn = ConfigInputUtil.getConfigInputStream("zookeeper.properties");
        try (InputStream in = zookeeperIn) {
            properties.load(in);
            initZookeeperAddress();
            initZookeeperSessionTimeout();
            initZookeeperConnectionTimeout();
        } catch (IOException e) {
            Crasher.crash(e);
        }
    }

    private void initZookeeperAddress() {
        this.zookeeperAddress = properties.getProperty("swift.zookeeper.address");
    }

    private void initZookeeperSessionTimeout() {
        this.zookeeperSessionTimeout = Integer.parseInt((String) properties.getOrDefault("swift.zookeeper.sessionTimeout", "20000"));
    }

    private void initZookeeperConnectionTimeout() {
        this.zookeeperConnectionTimeout = Integer.parseInt((String) properties.getOrDefault("swift.zookeeper.connectionTimeout", "30000"));
    }

    public String getZookeeperAddress() {
        return zookeeperAddress;
    }

    public int getZookeeperSessionTimeout() {
        return zookeeperSessionTimeout;
    }

    public int getZookeeperConnectionTimeout() {
        return zookeeperConnectionTimeout;
    }
}
