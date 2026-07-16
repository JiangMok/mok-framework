package com.mok.framework.ratelimiter.enums;

/**
 * 限流作用域枚举
 * @author mok
 */
public enum RateLimitScope {
    
    /**
     * 接口级别限流 - 同一个接口所有用户共享
     */
    API("api"),
    
    /**
     * 用户级别限流 - 基于用户ID限流
     */
    USER("user"),
    
    /**
     * IP级别限流 - 基于客户端IP限流
     */
    IP("ip"),
    
    /**
     * 全局限流 - 全局限流
     */
    GLOBAL("global");
    
    private final String value;
    
    RateLimitScope(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
}