package com.mok.framework.auth.service;

/**
 * 刷新令牌生命周期服务。
 */
public interface RefreshTokenService {

    String create(String userId, long sessionVersion);

    RefreshTokenIdentity parse(String refreshToken);

    /**
     * 原子消费刷新令牌。同一令牌只能成功消费一次。
     */
    RefreshTokenIdentity consume(String refreshToken);

    void revoke(String refreshToken);
}
