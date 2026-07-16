package com.mok.framework.ratelimiter.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 限流模块属性配置
 * @author mok
 */
// 注册为spring bean
@Component
// 绑定配置文件中以 mok.ratelimiter 开头的属性
@ConfigurationProperties(prefix = "mok.ratelimiter")
public class RateLimiterProperties {
    
    /**
     * 是否启用限流模块
     */
    private boolean enabled = true;
    
    /**
     * Redis Key前缀
     */
    private String redisKeyPrefix = "rate:limit:";
    
    /**
     * 防重复提交Key前缀
     */
    private String duplicateKeyPrefix = "duplicate:submit:";
    
    /**
     * 默认限流时间窗口（秒）
     */
    private long defaultWindow = 60;
    
    /**
     * 默认限流次数
     */
    private long defaultLimit = 10;
    
    /**
     * 默认防重复提交锁定时间（秒）
     */
    private int defaultDuplicateLockTime = 3;
    
    /**
     * 是否启用集群模式
     */
    private boolean clusterMode = false;
    
    /**
     * 默认限流提示信息
     */
    private String defaultRateLimitMessage = "请求过于频繁，请稍后再试";
    
    /**
     * 默认防重复提交提示信息
     */
    private String defaultDuplicateMessage = "请勿重复提交";
    
    /**
     * 是否启用监控端点
     */
    private boolean enableMonitor = true;
    
    /**
     * 监控端点路径
     */
    private String monitorPath = "/actuator/ratelimit";
    
    // Getters and Setters
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public String getRedisKeyPrefix() {
        return redisKeyPrefix;
    }
    
    public void setRedisKeyPrefix(String redisKeyPrefix) {
        this.redisKeyPrefix = redisKeyPrefix;
    }
    
    public String getDuplicateKeyPrefix() {
        return duplicateKeyPrefix;
    }
    
    public void setDuplicateKeyPrefix(String duplicateKeyPrefix) {
        this.duplicateKeyPrefix = duplicateKeyPrefix;
    }
    
    public long getDefaultWindow() {
        return defaultWindow;
    }
    
    public void setDefaultWindow(long defaultWindow) {
        this.defaultWindow = defaultWindow;
    }
    
    public long getDefaultLimit() {
        return defaultLimit;
    }
    
    public void setDefaultLimit(long defaultLimit) {
        this.defaultLimit = defaultLimit;
    }
    
    public int getDefaultDuplicateLockTime() {
        return defaultDuplicateLockTime;
    }
    
    public void setDefaultDuplicateLockTime(int defaultDuplicateLockTime) {
        this.defaultDuplicateLockTime = defaultDuplicateLockTime;
    }
    
    public boolean isClusterMode() {
        return clusterMode;
    }
    
    public void setClusterMode(boolean clusterMode) {
        this.clusterMode = clusterMode;
    }
    
    public String getDefaultRateLimitMessage() {
        return defaultRateLimitMessage;
    }
    
    public void setDefaultRateLimitMessage(String defaultRateLimitMessage) {
        this.defaultRateLimitMessage = defaultRateLimitMessage;
    }
    
    public String getDefaultDuplicateMessage() {
        return defaultDuplicateMessage;
    }
    
    public void setDefaultDuplicateMessage(String defaultDuplicateMessage) {
        this.defaultDuplicateMessage = defaultDuplicateMessage;
    }
    
    public boolean isEnableMonitor() {
        return enableMonitor;
    }
    
    public void setEnableMonitor(boolean enableMonitor) {
        this.enableMonitor = enableMonitor;
    }
    
    public String getMonitorPath() {
        return monitorPath;
    }
    
    public void setMonitorPath(String monitorPath) {
        this.monitorPath = monitorPath;
    }
}