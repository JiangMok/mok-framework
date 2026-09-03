package com.mok.framework.auth.service.impl;

import cn.dev33.satoken.temp.SaTempUtil;
import com.mok.framework.auth.config.SaTokenConfigure;
import com.mok.framework.auth.service.RefreshTokenIdentity;
import com.mok.framework.auth.service.RefreshTokenService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.concurrent.TimeUnit;

@Service
public class SaTempRefreshTokenServiceImpl implements RefreshTokenService {

    private static final String CONSUMED_KEY_PREFIX = "security:refresh-token:consumed:";

    private final StringRedisTemplate redisTemplate;

    public SaTempRefreshTokenServiceImpl(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public String create(String userId, long sessionVersion) {
        RefreshTokenIdentity identity = new RefreshTokenIdentity(userId, sessionVersion);
        return SaTempUtil.createToken(identity.toStorageValue(), SaTokenConfigure.REFRESH_TOKEN_EXPIRE);
    }

    @Override
    public RefreshTokenIdentity parse(String refreshToken) {
        return RefreshTokenIdentity.fromStorageValue(SaTempUtil.parseToken(refreshToken));
    }

    @Override
    public RefreshTokenIdentity consume(String refreshToken) {
        if (!markConsumed(refreshToken)) {
            throw new IllegalArgumentException("刷新令牌已被使用");
        }
        RefreshTokenIdentity identity = parse(refreshToken);
        SaTempUtil.deleteToken(refreshToken);
        return identity;
    }

    @Override
    public void revoke(String refreshToken) {
        markConsumed(refreshToken);
        SaTempUtil.deleteToken(refreshToken);
    }

    private boolean markConsumed(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return false;
        }
        Boolean created = redisTemplate.opsForValue().setIfAbsent(
                CONSUMED_KEY_PREFIX + fingerprint(refreshToken),
                "1",
                SaTokenConfigure.REFRESH_TOKEN_EXPIRE,
                TimeUnit.SECONDS);
        return Boolean.TRUE.equals(created);
    }

    private String fingerprint(String refreshToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(
                    digest.digest(refreshToken.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("当前JDK不支持SHA-256", exception);
        }
    }
}
