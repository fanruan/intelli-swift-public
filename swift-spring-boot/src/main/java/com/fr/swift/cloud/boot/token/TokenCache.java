package com.fr.swift.cloud.boot.token;

/**
 * @author lucifer
 * @date 2020/4/21
 * @description
 * @since swift 1.1
 */
public interface TokenCache {

    String refreshUserToken(String userId);

    String getUserIdByToken(final String token) throws TokenAbsentException,TokenExpireException;
}
