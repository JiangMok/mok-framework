package com.mok.framework.ratelimiter.enums;

/**
 * 限流类型枚举
 * @author mok
 */
public enum RateLimitType {
    
    /**
     * 滑动窗口算法 - 精确控制单位时间内的请求次数
     */
    SLIDING_WINDOW("sliding_window"),
    
    /**
     * 令牌桶算法 - 平滑限流，允许突发流量
     */
    TOKEN_BUCKET("token_bucket"),
    
    /**
     * 固定窗口算法 - 简单限流，可能有边界问题
     */
    FIXED_WINDOW("fixed_window");
    
//    /**
//     * 漏桶算法 - 严格控制流出速率
//     */
//    LEAKY_BUCKET("leaky_bucket");
    
    private final String value;
    
    RateLimitType(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
}