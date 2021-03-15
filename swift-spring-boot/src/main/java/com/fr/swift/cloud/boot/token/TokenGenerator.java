package com.fr.swift.cloud.boot.token;

import java.util.UUID;

/**
 * @author lucifer
 * @date 2020/4/21
 * @description
 * @since swift 1.1
 */
public class TokenGenerator {

    /**
     * 先简单实现token值
     *
     * @return
     */
    public static String getToken() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }
}
