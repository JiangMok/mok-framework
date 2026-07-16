package com.mok.framework.ratelimiter.model;


import com.mok.framework.ratelimiter.enums.RateLimitScope;
import com.mok.framework.ratelimiter.enums.RateLimitType;

/**
 * 限流上下文
 * 作用：封装一次限流检查所需的所有参数
 * @author mok
 */
public class RateLimitContext {

    // 限流key，最终存储在Redis中的键
    private String key;
    // 限流算法类型
    private RateLimitType type;
    // 限流作用域
    private RateLimitScope scope;
    // 时间窗口（秒）
    private long window;
    // 限制次数
    private long limit;
    // 令牌桶容量
    private long capacity;
    // 令牌生成速率
    private double rate;
    // 提示信息
    private String message;
    // 当前时间戳（秒），用于算法计算
    private long currentTime;
    // 客户端 IP
    private String clientIp;
    // 用户 ID
    private String userId;
    // 请求方法
    private String method;
    // 请求 URI
    private String uri;

    @Override
    public String toString() {
        return "RateLimitContext{" +
                "key='" + key + '\'' +
                ", type=" + type +
                ", scope=" + scope +
                ", window=" + window +
                ", limit=" + limit +
                ", capacity=" + capacity +
                ", rate=" + rate +
                ", message='" + message + '\'' +
                ", currentTime=" + currentTime +
                ", clientIp='" + clientIp + '\'' +
                ", userId='" + userId + '\'' +
                ", method='" + method + '\'' +
                ", uri='" + uri + '\'' +
                '}';
    }

    // 无参构造方法，初始化当前时间
    public RateLimitContext() {
        // 转换为秒
        this.currentTime = System.currentTimeMillis() / 1000;
    }

    // Builder模式，方便创建对象
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final RateLimitContext context;

        private Builder() {
            this.context = new RateLimitContext();
        }

        public Builder key(String key) {
            context.key = key;
            return this;
        }

        public Builder type(RateLimitType type) {
            context.type = type;
            return this;
        }

        public Builder scope(RateLimitScope scope) {
            context.scope = scope;
            return this;
        }

        public Builder window(long window) {
            context.window = window;
            return this;
        }

        public Builder limit(long limit) {
            context.limit = limit;
            return this;
        }

        public Builder capacity(long capacity) {
            context.capacity = capacity;
            return this;
        }

        public Builder rate(double rate) {
            context.rate = rate;
            return this;
        }

        public Builder message(String message) {
            context.message = message;
            return this;
        }

        public Builder clientIp(String clientIp) {
            context.clientIp = clientIp;
            return this;
        }

        public Builder userId(String userId) {
            context.userId = userId;
            return this;
        }

        public Builder method(String method) {
            context.method = method;
            return this;
        }

        public Builder uri(String uri) {
            context.uri = uri;
            return this;
        }

        public RateLimitContext build() {
            return context;
        }
    }

    // Getters and Setters
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public RateLimitType getType() {
        return type;
    }

    public void setType(RateLimitType type) {
        this.type = type;
    }

    public RateLimitScope getScope() {
        return scope;
    }

    public void setScope(RateLimitScope scope) {
        this.scope = scope;
    }

    public long getWindow() {
        return window;
    }

    public void setWindow(long window) {
        this.window = window;
    }

    public long getLimit() {
        return limit;
    }

    public void setLimit(long limit) {
        this.limit = limit;
    }

    public long getCapacity() {
        return capacity;
    }

    public void setCapacity(long capacity) {
        this.capacity = capacity;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(long currentTime) {
        this.currentTime = currentTime;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }
}