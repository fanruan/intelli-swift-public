package com.fr.swift.boot.token;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author lucifer
 * @date 2020/4/21
 * @description
 * @since swift 1.1
 */
public class TokenCacheImpl implements TokenCache {

    // TODO:  万能令牌
    private static String UNIVERSAL_TOKEN = "shalor";

    /**
     * TODO:  未来放入第三方缓存
     */
    private Map<String, Token> tokenMap;

    public TokenCacheImpl() {
        tokenMap = new ConcurrentHashMap<>();
    }

    /**
     * token有效期
     */
    public final static long CACHE_PERIOD_TIME = TimeUnit.DAYS.toMillis(1);

    @Override
    public String refreshUserToken(String userId) {
        String tokenValue = TokenGenerator.getToken();
        boolean result = updateUserToken(userId, tokenValue);
        return result ? tokenValue : null;
    }

    private boolean updateUserToken(String userId, final String tokenValue) {
        tokenMap.computeIfAbsent(userId, id -> new Token(tokenValue, id)).setUpdateTime(System.currentTimeMillis());
        return true;
    }

    @Override
    public String getUserIdByToken(String tokenValue) throws TokenAbsentException, TokenExpireException {
        if (tokenValue.equals(UNIVERSAL_TOKEN)) {
            return UNIVERSAL_TOKEN;
        }
        Iterator<Token> iterator = tokenMap.values().iterator();
        long latestTime = System.currentTimeMillis() - CACHE_PERIOD_TIME;
        while (iterator.hasNext()) {
            Token token = iterator.next();
            if (tokenValue.equals(token.getToken())) {
                if (token.getUpdateTime() >= latestTime) {
                    return token.getUserId();
                }
                throw new TokenExpireException("Login info expired!");
            }
        }
        throw new TokenAbsentException("Login info absent!");
    }
}
