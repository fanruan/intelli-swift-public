package com.fr.swift.boot.token;

/**
 * @author lucifer
 * @date 2020/4/21
 * @description
 * @since swift 1.1
 */
public class Token {
    private String token;
    private String userId;
    private long updateTime;

    public Token(String token, String userId) {
        this.token = token;
        this.userId = userId;
        this.updateTime = 0;
    }

    public String getToken() {
        return token;
    }

    public String getUserId() {
        return userId;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }
}
