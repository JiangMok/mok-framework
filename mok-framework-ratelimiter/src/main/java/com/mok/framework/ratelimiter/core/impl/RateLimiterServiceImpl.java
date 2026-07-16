package com.mok.framework.ratelimiter.core.impl;

import com.mok.framework.common.utils.LogUtils;
import com.mok.framework.ratelimiter.config.RateLimiterProperties;
import com.mok.framework.ratelimiter.core.DuplicateSubmitService;
import com.mok.framework.ratelimiter.core.RateLimiterService;
import com.mok.framework.ratelimiter.exception.DuplicateSubmitException;
import com.mok.framework.ratelimiter.exception.RateLimitException;
import com.mok.framework.ratelimiter.model.RateLimitContext;
import com.mok.framework.ratelimiter.model.RateLimitResult;
import com.mok.framework.ratelimiter.strategy.RateLimitStrategyFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 限流服务实现
 * 作用：实现RateLimiterService和DuplicateSubmitService接口，提供基于Redis的限流和防重复提交功能
 * @author mok
 */
@Service
public class RateLimiterServiceImpl implements RateLimiterService, DuplicateSubmitService {
    private static final Logger log = LogUtils.getLogger(RateLimiterServiceImpl.class);
    // redis 模板
    private final RedisTemplate<String, Object> redisTemplate;
    // 限流策略工厂
    private final RateLimitStrategyFactory strategyFactory;
    // 限流配置属性
    private final RateLimiterProperties properties;

    // 构造函数注入
    public RateLimiterServiceImpl(
            // 指定使用哪个 bean (指定使用哪个RedisTemplate)
            @Qualifier("rateLimiterRedisTemplate")RedisTemplate<String, Object> redisTemplate,
            RateLimitStrategyFactory strategyFactory,
            @Qualifier("mok.ratelimiter-com.mok.framework.ratelimiter.config.RateLimiterProperties")
                                  RateLimiterProperties properties) {
        this.redisTemplate = redisTemplate;
        this.strategyFactory = strategyFactory;
        this.properties = properties;
    }
    // ==================== RateLimiterService 接口实现 ====================
    @Override
    public boolean isAllowed(RateLimitContext context) {
        // 调用check方法，只关心是否允许
        return check(context).isAllowed();
    }
    
    @Override
    public RateLimitResult check(RateLimitContext context) {
        // 从策略工厂获取对应的限流策略，然后执行
        return strategyFactory.getStrategy(context.getType()).execute(context);
    }
    
    @Override
    public void clear(String key) {
        // 直接删除 Redis中的 key
        redisTemplate.delete(key);
    }
    
    @Override
    public long getRemaining(String key) {
        // 这里需要根据具体的策略实现
        // 简化实现：返回key的剩余生存时间作为参考
        Long ttl = redisTemplate.getExpire(key);
        return ttl != null ? ttl : 0;
    }
    
    @Override
    public long getResetTime(String key) {
        // 获取key的剩余生存时间，计算重置时间戳（当前时间+剩余秒数）
        Long ttl = redisTemplate.getExpire(key);
        if (ttl != null && ttl > 0) {
            return System.currentTimeMillis() / 1000 + ttl;
        }
        return 0;
    }
    // ==================== DuplicateSubmitService 接口实现 ====================
    @Override
    public boolean isAllowed(String key, int lockTime) {
        // 调用重载方法，使用默认提示信息
        return isAllowed(key, lockTime, properties.getDefaultDuplicateMessage());
    }
    
    @Override
    public boolean isAllowed(String key, int lockTime, String message) {
        // 直接调用 checkAndLock
        return checkAndLock(key, lockTime);
    }
    
    @Override
    public void release(String key) {
        // 删除防重复提交的key
        redisTemplate.delete(properties.getDuplicateKeyPrefix() + key);
    }
    
    @Override
    public boolean checkAndLock(String key, int lockTime) {
        // 拼接完整key
        String fullKey = properties.getDuplicateKeyPrefix() + key;

        // Lua 脚本保证原子性：setnx + expire
        String lockScript =
                // 如果 key 不存在 -> 设置成功
            "if redis.call('setnx', KEYS[1], ARGV[1]) == 1 then " +
                // 设置过期时间,并返回1（expire成功返回1）
            "    return redis.call('expire', KEYS[1], ARGV[2]) " +
            "else " +
                // // key已存在，返回0
            "    return 0 " +
            "end";
        
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(lockScript);
        script.setResultType(Long.class);

        // 执行 lua 脚本,参数: key 列表,value(这里设为"locked"),过期时间
        Long result = redisTemplate.execute(script,
                List.of(fullKey),
            "locked", lockTime);
        // 返回结果为1表示成功获取锁(允许提交),否则不允许
        return result != null && result == 1;
    }

    /**
     * 手动限流检查（编程式使用）
     * 作用：供业务代码直接调用进行限流检查
     */
    public void checkRateLimit(String key, long limit, long window) {
        // 构建限流上下文
        RateLimitContext context = RateLimitContext.builder()
            .key(key)
            .limit(limit)
            .window(window)
            .build();
        
        RateLimitResult result = check(context);
        if (!result.isAllowed()) {
            //如果被限流,抛出异常
            throw new RateLimitException(properties.getDefaultRateLimitMessage(), result.getRetryAfter());
        }
    }
    
    /**
     * 手动防重复提交检查（编程式使用）
     */
    public void checkDuplicateSubmit(String key, int lockTime) {
        if (!checkAndLock(key, lockTime)) {
            // 如果重复提交，抛出DuplicateSubmitException
            throw new DuplicateSubmitException(properties.getDefaultDuplicateMessage());
        }
    }
}