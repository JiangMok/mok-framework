package com.mok.framework.ratelimiter.strategy.impl;

import com.mok.framework.common.utils.LogUtils;
import com.mok.framework.ratelimiter.core.RateLimitStrategy;
import com.mok.framework.ratelimiter.model.RateLimitContext;
import com.mok.framework.ratelimiter.model.RateLimitResult;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.UUID;

/**
 * 滑动窗口限流策略
 * 作用：使用Redis有序集合（ZSet）实现滑动窗口，精确控制时间窗口内的请求次数
 * @author mok
 */
// 注册为 spring bean
@Component
public class SlidingWindowStrategy implements RateLimitStrategy {
    private static final Logger log = LogUtils.getLogger(SlidingWindowStrategy.class);
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Redis 滑动窗口限流脚本
     * 逻辑:
     *      1.移除窗口之前的所有元素
     *      2.统计当前窗口内的元素个数
     *      3.如果小于限制,则添加当前请求并返回0
     *      4.如果达到限制,计算最早的请求还有多久过期,返回等待时间
     */
    private static final String SLIDING_WINDOW_SCRIPT =
            "local key = KEYS[1] " +
            "local limit = tonumber(ARGV[1]) " +
            "local window = tonumber(ARGV[2]) " +
            "local current = tonumber(ARGV[3]) " +
            "local requestId = ARGV[4] " +
            "local clearBefore = current - window " +
            " " +
            "redis.call('zremrangebyscore', key, 0, clearBefore) " +    // 移除过期请求
            "local count = redis.call('zcard', key) " +                 // 获取当前窗口内的请求数
            " " +
            "if count < limit then " +
            "    redis.call('zadd', key, current, requestId) " +        // 添加当前请求，score为时间戳
            "    redis.call('expire', key, window) " +                  // 设置过期时间
            "    return 0 " +
            "else " +
            "    local oldest = redis.call('zrange', key, 0, 0, 'withscores') " +   // 获取最早请求及其 score
            "    if #oldest > 0 then " +
            "        local retryAfter = window - (current - tonumber(oldest[2])) " +    // 计算还需等待多久
            "        return retryAfter " +
            "    else " +
            "        return -1 " +  // 异常情况返回-1
            "    end " +
            "end";

    // 构造函数注入
    public SlidingWindowStrategy(@Qualifier("rateLimiterRedisTemplate")RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public RateLimitResult execute(RateLimitContext context) {
        String key = context.getKey();
        long limit = context.getLimit();
        long window = context.getWindow();
        // 这里应该是毫秒级时间戳
        long currentTime = context.getCurrentTime();

        // 生成唯一请求ID，用于ZSet中的成员，避免重复
        String requestId = key + ":" + UUID.randomUUID().toString();

        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(SLIDING_WINDOW_SCRIPT);
        script.setResultType(Long.class);

        // 执行 Lua 脚本
        Long result = redisTemplate.execute(script,
                Arrays.asList(key),
                // 传入唯一标识
                limit, window, currentTime, requestId);

        RateLimitResult rateLimitResult = new RateLimitResult();
        rateLimitResult.setKey(key);
        rateLimitResult.setAllowed(result != null && result == 0);
        rateLimitResult.setLimitCount(limit);

        if (!rateLimitResult.isAllowed() && result != null && result > 0) {
            rateLimitResult.setRetryAfter(result);
        }

        return rateLimitResult;
    }

    @Override
    public String getType() {
        return "sliding_window";
    }
}