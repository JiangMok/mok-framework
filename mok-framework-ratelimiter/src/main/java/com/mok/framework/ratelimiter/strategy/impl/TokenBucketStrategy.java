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
 * 令牌桶限流策略
 * 作用：实现令牌桶算法，平滑限流，允许突发流量
 * @author mok
 */
// 注册为 spring bean
@Component
public class TokenBucketStrategy implements RateLimitStrategy {
    private static final Logger log = LogUtils.getLogger(TokenBucketStrategy.class);
    private final RedisTemplate<String, Object> redisTemplate;
    
    /**
     * Redis 令牌桶限流脚本
     * 逻辑:
     *      1.获取桶中当前令牌数和上次填充时间
     *      2.计算时间差,补充令牌(不超过容量)
     *      3.如果当前令牌足够消耗,则扣除令牌并返回0
     *      4.如果令牌不足,则返回等待的时间
     */
    private static final String TOKEN_BUCKET_SCRIPT = 
            "local key = KEYS[1] " +
            "local capacity = tonumber(ARGV[1]) " +
            "local rate = tonumber(ARGV[2]) " +
            "local tokens = tonumber(ARGV[3]) " +
            "local now = tonumber(ARGV[4]) " +
            " " +
            "local bucket = redis.call('hmget', key, 'tokens', 'lastRefillTime') " +
            "local currentTokens = capacity " +
            "local lastRefillTime = now " +
            " " +
            "if bucket[1] and bucket[2] then " +
            "    currentTokens = tonumber(bucket[1]) " +
            "    lastRefillTime = tonumber(bucket[2]) " +
            "    " +
            "    local timePassed = now - lastRefillTime " +
            "    local refillTokens = math.floor(timePassed * rate) " +         // 计算需要补充的令牌
            "    currentTokens = math.min(capacity, currentTokens + refillTokens) " +   //补充但不能超过容量
            "    " +
            "    if timePassed > 0 then " +
            "        lastRefillTime = now " +
            "    end " +
            "end " +
            " " +
            "if currentTokens >= tokens then " +
            "    currentTokens = currentTokens - tokens " +         // 消耗令牌
            "    redis.call('hmset', key, 'tokens', currentTokens, 'lastRefillTime', lastRefillTime) " +
            "    redis.call('expire', key, math.ceil(capacity / rate) * 2) " +  // 设置过期时间,避免永久占用
            "    return 0 " +  // 0表示通过
            "else " +
            "    local needTokens = tokens - currentTokens " +
            "    local waitTime = math.ceil(needTokens / rate) " +  // 计算需要等待多少秒才能有足够令牌
            "    return waitTime " +  // 返回需要等待的秒数
            "end";

    // 构造函数注入
    public TokenBucketStrategy(@Qualifier("rateLimiterRedisTemplate")RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    @Override
    public RateLimitResult execute(RateLimitContext context) {
        String key = context.getKey();
        // 桶容量
        long capacity = context.getCapacity();
        // 令牌生成速率(每秒)
        double rate = context.getRate();
        // 当前时间戳(秒)
        long currentTime = context.getCurrentTime();
        
        // 默认每次请求消耗1个令牌
        long tokens = 1;
        
        // 执行 Lua脚本
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(TOKEN_BUCKET_SCRIPT);
        script.setResultType(Long.class);
        
        Long result = redisTemplate.execute(script, 
            Arrays.asList(key), 
            capacity, rate, tokens, currentTime);
        
        RateLimitResult rateLimitResult = new RateLimitResult();
        rateLimitResult.setAllowed(result != null && result == 0);
        
        if (!rateLimitResult.isAllowed() && result != null && result > 0) {
            rateLimitResult.setRetryAfter(result);
        }
        
        return rateLimitResult;
    }
    
    @Override
    public String getType() {
        return RateLimitType.TOKEN_BUCKET.getValue();
    }
}