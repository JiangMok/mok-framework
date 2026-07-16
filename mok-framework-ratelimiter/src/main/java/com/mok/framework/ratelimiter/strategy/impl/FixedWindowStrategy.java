package com.mok.framework.ratelimiter.strategy.impl;

import com.mok.framework.common.utils.LogUtils;
import com.mok.framework.ratelimiter.core.RateLimitStrategy;
import com.mok.framework.ratelimiter.enums.RateLimitType;
import com.mok.framework.ratelimiter.model.RateLimitContext;
import com.mok.framework.ratelimiter.model.RateLimitResult;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * 固定窗口限流策略
 * 作用：实现固定窗口算法，统计一个固定时间窗口内的请求次数
 * @author mok
 */
// 注册为 spring bean
@Component
public class FixedWindowStrategy implements RateLimitStrategy {
    private static final Logger log = LogUtils.getLogger(FixedWindowStrategy.class);

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Redis 固定窗口限流脚本
     * 逻辑:
     *      1.获取当前计数,如果已存在且达到限制,返回剩余过期时间
     *      2.如果未达到限制,计数+1
     *      3.如果 key 不存在,创建并设置过期时间
     */
    private static final String FIXED_WINDOW_SCRIPT =
            "local key = KEYS[1] " +
            "local limit = tonumber(ARGV[1]) " +
            "local window = tonumber(ARGV[2]) " +
            "local current = tonumber(ARGV[3]) " +
            " " +
            "local count = redis.call('get', key) " +
            " " +
            "if count then " +
            "    count = tonumber(count) " +
            "    if count >= limit then " +
            "        local ttl = redis.call('ttl', key) " +
            "        return ttl " +  // 返回剩余过期时间
            "    else " +
            "        redis.call('incr', key) " +
            "        return 0 " +  // 0表示通过
            "    end " +
            "else " +
            "    redis.call('setex', key, window, 1) " +
            "    return 0 " +  // 0表示通过
            "end";

    //构造函数注入
    public FixedWindowStrategy(@Qualifier("rateLimiterRedisTemplate")RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public RateLimitResult execute(RateLimitContext context) {
        // 限流 key
        String key = context.getKey();
        // 限制次数
        long limit = context.getLimit();
        // 窗口大小(秒)
        long window = context.getWindow();
        // 当前时间戳(当前未使用,保留参数位置)
        long currentTime = context.getCurrentTime();

        // 创建Redis 脚本对象
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(FIXED_WINDOW_SCRIPT);
        script.setResultType(Long.class);

        // 执行脚本
        Long result = redisTemplate.execute(script,
            Arrays.asList(key),
            limit, window, currentTime);

        // 构建结果对象
        RateLimitResult rateLimitResult = new RateLimitResult();
        // 如果 result = 0 ,表示允许
        rateLimitResult.setAllowed(result != null && result == 0);
        // 如果不允许且result>0，则设置retryAfter
        if (!rateLimitResult.isAllowed() && result != null && result > 0) {
            rateLimitResult.setRetryAfter(result);
        }

        return rateLimitResult;
    }

    @Override
    public String getType() {
        // 返回策略类型，与枚举中的值对应
        return RateLimitType.FIXED_WINDOW.getValue();
    }
}