package com.mok.framework.ratelimiter.annotation;


import com.mok.framework.ratelimiter.enums.RateLimitScope;
import com.mok.framework.ratelimiter.enums.RateLimitType;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * @description :  限流注解类
 * @author : mok
 * @date : 2026/2/25 14:21
 **/
//只能用于方法
@Target(ElementType.METHOD)
//运行时保留
@Retention(RetentionPolicy.RUNTIME)
//包含在 java 文档中
@Documented
public @interface RateLimit {
    
    /**
     * 限流Key前缀，默认为空，会使用类名+方法名
     * 作用：自定义key的前缀，便于区分不同业务
     */
    String key() default "";
    
    /**
     * 限流类型，默认为滑动窗口
     * 作用：选择具体的限流算法
     */
    RateLimitType type() default RateLimitType.SLIDING_WINDOW;
    
    /**
     * 限流作用域，默认为接口级别
     * 作用：决定限流是针对整个接口、用户、IP还是全局
     */
    RateLimitScope scope() default RateLimitScope.API;
    
    /**
     * 时间窗口大小
     * 作用：限流的时间段长度
     */
    long window() default 60;
    
    /**
     * 时间单位
     * 作用：window的时间单位，默认秒
     */
    TimeUnit unit() default TimeUnit.SECONDS;
    
    /**
     * 在时间窗口内允许的最大请求数
     * 作用：限制请求数量
     */
    long limit() default 10;
    
    /**
     * 令牌桶容量（仅限令牌桶算法使用）
     * 作用：令牌桶最多能存放的令牌数
     */
    long capacity() default 20;
    
    /**
     * 令牌生成速率（每秒，仅限令牌桶算法使用）
     * 作用：每秒生成的令牌数量
     */
    double rate() default 5.0;
    
    /**
     * 超过限流后的提示信息
     * 作用：自定义限流提示
     */
    String message() default "";
    
    /**
     * 是否启用，默认启用
     * 作用：可以动态开关某个方法的限流
     */
    boolean enabled() default true;
}