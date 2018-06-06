package com.fr.swift.config.service;

/**
 * @author yee
 * @date 2018/6/6
 */
public interface SwiftPathService {
    /**
     * 设置cube更新路径
     *
     * @param path
     */
    boolean setSwiftPath(String path);

    /**
     * 获取cube更新路径
     *
     * @return
     */
    String getSwiftPath();
}
