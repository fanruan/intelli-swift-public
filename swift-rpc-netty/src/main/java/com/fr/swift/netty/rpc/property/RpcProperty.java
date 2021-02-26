package com.fr.swift.netty.rpc.property;

import com.fr.swift.config.ConfigInputUtil;
import com.fr.swift.util.Crasher;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * @author Heng.J
 * @date 2021/1/26
 * @description
 * @since swift-1.2.0
 */
public class RpcProperty {

    private Properties properties;

    private static final long IDLE_OBJ_EXPIRE_TIME = TimeUnit.MINUTES.toMillis(30);

    /**
     * 数量控制参数
     */
    private int minIdlePerKey;

    private int maxIdlePerKey;

    private int maxTotalPerKey;

    private int maxTotal;

    private long minEvictableIdleTimeMillis;  // 连接最小空闲时间,超时空闲被移除

    /**
     * 检测参数
     */
    private long timeBetweenEvictionRunsMillis; // 空闲链接检测线程检测的周期

    private RpcProperty() {
        initProperties();
    }

    private static final RpcProperty INSTANCE = new RpcProperty();

    public static RpcProperty get() {
        return INSTANCE;
    }

    private void initProperties() {
        properties = new Properties();
        try (InputStream inputStream = ConfigInputUtil.getConfigInputStream("rpc.properties")) {
            properties.load(inputStream);
            minIdlePerKey = Integer.parseInt((String) properties.getOrDefault("minIdlePerKey", "0"));
            maxIdlePerKey = Integer.parseInt((String) properties.getOrDefault("maxIdlePerKey", "8"));
            maxTotalPerKey = Integer.parseInt((String) properties.getOrDefault("maxTotalPerKey", "8"));
            maxTotal = Integer.parseInt((String) properties.getOrDefault("maxTotal", "-1"));
            minEvictableIdleTimeMillis = Long.parseLong((String) properties.getOrDefault("minEvictableIdleTimeMillis", IDLE_OBJ_EXPIRE_TIME));
            timeBetweenEvictionRunsMillis = Long.parseLong((String) properties.getOrDefault("timeBetweenEvictionRunsMillis", IDLE_OBJ_EXPIRE_TIME));
        } catch (IOException e) {
            Crasher.crash(e);
        }
    }

    public int getMinIdlePerKey() {
        return minIdlePerKey;
    }

    public int getMaxIdlePerKey() {
        return maxIdlePerKey;
    }

    public int getMaxTotalPerKey() {
        return maxTotalPerKey;
    }

    public int getMaxTotal() {
        return maxTotal;
    }

    public long getMinEvictableIdleTimeMillis() {
        return minEvictableIdleTimeMillis;
    }

    public long getTimeBetweenEvictionRunsMillis() {
        return timeBetweenEvictionRunsMillis;
    }
}
