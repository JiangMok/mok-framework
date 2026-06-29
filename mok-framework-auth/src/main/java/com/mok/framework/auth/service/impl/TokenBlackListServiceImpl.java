package com.mok.framework.auth.service.impl;

import com.mok.framework.auth.service.TokenBlackListService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class TokenBlackListServiceImpl implements TokenBlackListService {

    private static final String BLACKLIST_PREFIX = "blacklist:token:";

    private final StringRedisTemplate redisTemplate;

    public TokenBlackListServiceImpl(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 将Token加入黑名单,TTL 自动设为 Token 剩余有效期
     */
    public void addToBlacklist(String token,long ttl) {
        String key = BLACKLIST_PREFIX + token;

        if (ttl > 0) {
            // Token 还有效，设置相同的过期时间
            redisTemplate.opsForValue().set(key, token, ttl, TimeUnit.SECONDS);
        } else if (ttl == -1) {
            // Token 永不过期（一般不会），黑名单也永久有效
            redisTemplate.opsForValue().set(key, token);
        } else {
            // ttl == -2 或其他负数，表示 Token 已失效，无需加入黑名单（因为已经无法使用）
            // 或者你也可以选择设置一个默认较短过期时间，例如 1 小时
            redisTemplate.opsForValue().set(key, token, 3600, TimeUnit.SECONDS);
        }
    }
}
