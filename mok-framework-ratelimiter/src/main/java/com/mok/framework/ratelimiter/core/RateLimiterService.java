package com.mok.framework.ratelimiter.core;


import com.mok.framework.ratelimiter.model.RateLimitContext;
import com.mok.framework.ratelimiter.model.RateLimitResult;

/**
 * 限流服务接口
 * @author mok
 */
public interface RateLimiterService {

    /**
     * 检查是否允许请求
     * @param context 限流上下文
     * @return true表示允许，false表示拒绝
     */
    boolean isAllowed(RateLimitContext context);

    /**
     * 检查是否允许请求并返回详细信息
     * @param context 限流上下文
     * @return RateLimitResult 包含是否允许、剩余时间等信息
     */
    RateLimitResult check(RateLimitContext context);

    /**
     * 清除限流记录
     * @param key 限流 key
     */
    void clear(String key);

    /**
     * 获取剩余次数
     * @param key 限流 key
     * @return 剩余可请求次数（简化实现可能返回TTL）
     */
    long getRemaining(String key);

    /**
     * 获取重置时间
     * @param key 限流 key
     * @return 重置时间戳（秒）
     */
    long getResetTime(String key);
}