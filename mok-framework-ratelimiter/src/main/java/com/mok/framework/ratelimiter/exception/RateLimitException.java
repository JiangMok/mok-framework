package com.mok.framework.ratelimiter.exception;

/**
 * 限流异常
 * @author mok
 */
public class RateLimitException extends RuntimeException {
    
    /**
     * 错误码
     */
    private final String code;

    /**
     * 限流剩余时间（秒）
     * 作用：告诉客户端需要等待多久才能再次请求
     */
    private final Long retryAfter;

    /**
     * 构造方法，只传消息，默认错误码，无retryAfter
     */
    public RateLimitException(String message) {
        super(message);
        this.code = "RATE_LIMIT_EXCEEDED";
        this.retryAfter = null;
    }

    /**
     * 构造方法，传入自定义错误码和消息
     */
    public RateLimitException(String code, String message) {
        super(message);
        this.code = code;
        this.retryAfter = null;
    }

    /**
     * 构造方法，传入消息和retryAfter，默认错误码
     */
    public RateLimitException(String message, Long retryAfter) {
        super(message);
        this.code = "RATE_LIMIT_EXCEEDED";
        this.retryAfter = retryAfter;
    }

    /**
     * 完整构造方法
     */
    public RateLimitException(String code, String message, Long retryAfter) {
        super(message);
        this.code = code;
        this.retryAfter = retryAfter;
    }
    
    public String getCode() {
        return code;
    }
    
    public Long getRetryAfter() {
        return retryAfter;
    }
}