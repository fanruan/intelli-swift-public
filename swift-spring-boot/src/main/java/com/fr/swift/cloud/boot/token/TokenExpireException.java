package com.fr.swift.cloud.boot.token;

/**
 * @author lucifer
 * @date 2020/4/21
 * @description
 * @since swift 1.1
 */
public class TokenExpireException extends Exception {

    public TokenExpireException(String message) {
        super(message);
    }
}
