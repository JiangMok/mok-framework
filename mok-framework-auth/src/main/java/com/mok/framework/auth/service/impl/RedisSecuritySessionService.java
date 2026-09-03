package com.mok.framework.auth.service.impl;

import com.mok.framework.common.security.SecuritySessionService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisSecuritySessionService implements SecuritySessionService {

    private static final String SESSION_VERSION_KEY = "security:user:session-version:%s";

    private final StringRedisTemplate redisTemplate;

    public RedisSecuritySessionService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public long getCurrentVersion(String userId) {
        String value = redisTemplate.opsForValue().get(buildKey(userId));
        if (value == null) {
            return 0L;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException exception) {
            throw new IllegalStateException("用户安全会话版本数据损坏", exception);
        }
    }

    @Override
    public boolean isCurrentVersion(String userId, long tokenVersion) {
        return tokenVersion == getCurrentVersion(userId);
    }

    @Override
    public void invalidateUserSessions(String userId) {
        String key = buildKey(userId);
        Long version = redisTemplate.opsForValue().increment(key);
        if (version == null) {
            throw new IllegalStateException("用户安全会话失效失败");
        }
        // 会话版本不能过期回退，否则后续签发的有效令牌会被提前判定为失效。
        // 每个用户仅占用一个整数键，保留该键可确保版本始终单调递增。
    }

    private String buildKey(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        return String.format(SESSION_VERSION_KEY, userId);
    }
}
