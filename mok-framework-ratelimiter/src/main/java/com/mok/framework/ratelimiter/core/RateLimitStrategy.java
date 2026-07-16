package com.mok.framework.ratelimiter.core;


import com.mok.framework.ratelimiter.model.RateLimitContext;
import com.mok.framework.ratelimiter.model.RateLimitResult;

/**
 * 限流策略接口
 * @author mok
 */
public interface RateLimitStrategy {

    /**
     * 执行限流检查
     * @param context 限流上下文，包含key、窗口、限制数等参数
     * @return 限流结果
     */
    RateLimitResult execute(RateLimitContext context);

    /**
     * 获取策略类型
     * @return 策略类型字符串，用于在工厂中匹配
     */
    String getType();
}