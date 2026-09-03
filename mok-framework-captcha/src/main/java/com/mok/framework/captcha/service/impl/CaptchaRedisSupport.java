package com.mok.framework.captcha.service.impl;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.List;

/**
 * 验证码 Redis 原子操作。
 */
final class CaptchaRedisSupport {

    private static final RedisScript<String> GET_AND_DELETE_SCRIPT = RedisScript.of(
            "local value = redis.call('GET', KEYS[1]); " +
                    "if value then redis.call('DEL', KEYS[1]); end; " +
                    "return value",
            String.class);

    private CaptchaRedisSupport() {
    }

    static String getAndDelete(RedisTemplate<String, String> redisTemplate, String key) {
        return redisTemplate.execute(GET_AND_DELETE_SCRIPT, List.of(key));
    }
}
