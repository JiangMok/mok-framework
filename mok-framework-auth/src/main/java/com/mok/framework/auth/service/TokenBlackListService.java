package com.mok.framework.auth.service;

/**
 * Token 黑名单服务
 */
public interface TokenBlackListService {

    /**
     * 将token加入到黑名单
     */
    void addToBlacklist(String token,long ttl);

    /**
     * 判断 token 是否已在黑名单中。
     */
    boolean isBlacklisted(String token);
}
