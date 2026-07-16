package com.mok.framework.ratelimiter.model;

/**
 * 限流结果
 * 作用：封装限流检查的结果，包含是否允许以及相关信息
 * @author mok
 */
public class RateLimitResult {

    // 是否允许通过
    private boolean allowed;
    // 需要等待的秒数（如果被限流）
    private Long retryAfter;
    // 当前请求数（部分算法可提供）
    private Long currentCount;
    // 限制的总数
    private Long limitCount;
    // 对应的限流 key
    private String key;

    @Override
    public String toString() {
        return "RateLimitResult{" +
                "allowed=" + allowed +
                ", retryAfter=" + retryAfter +
                ", currentCount=" + currentCount +
                ", limitCount=" + limitCount +
                ", key='" + key + '\'' +
                '}';
    }

    // 无参构造，默认allowed为true
    public RateLimitResult() {
        this.allowed = true;
    }

    // 静态工厂方法：创建一个允许的结果
    public static RateLimitResult allowed() {
        return new RateLimitResult();
    }

    // 静态工厂方法：创建一个拒绝的结果，并指定等待时间
    public static RateLimitResult denied(Long retryAfter) {
        RateLimitResult result = new RateLimitResult();
        result.allowed = false;
        result.retryAfter = retryAfter;
        return result;
    }

    // Getters and Setters
    public boolean isAllowed() {
        return allowed;
    }

    public void setAllowed(boolean allowed) {
        this.allowed = allowed;
    }

    public Long getRetryAfter() {
        return retryAfter;
    }

    public void setRetryAfter(Long retryAfter) {
        this.retryAfter = retryAfter;
    }

    public Long getCurrentCount() {
        return currentCount;
    }

    public void setCurrentCount(Long currentCount) {
        this.currentCount = currentCount;
    }

    public Long getLimitCount() {
        return limitCount;
    }

    public void setLimitCount(Long limitCount) {
        this.limitCount = limitCount;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}