package com.mok.framework.ratelimiter.monitor;

import com.mok.framework.ratelimiter.config.RateLimiterProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 限流监控端点
 * 作用 : 通过Spring Boot Actuator暴露限流模块的运行时信息
 * @author mok
 */
// 注册为 spring bean
@Component
// 声明这是一个Actuator 端点,ID 为ratelimit,访问路径是/actuator/ratelimit
@Endpoint(id = "ratelimit")
public class RateLimitEndpoint {

    // redis 模板
    private final RedisTemplate<String, Object> redisTemplate;
    // 限流配置属性
    private final RateLimiterProperties properties;

    // 构造函数注入
    public RateLimitEndpoint(RedisTemplate<String, Object> redisTemplate,
                             @Qualifier("mok.ratelimiter-com.mok.framework.ratelimiter.config.RateLimiterProperties")
                             RateLimiterProperties properties) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;
    }

    /**
     * 读取操作，当GET请求该端点时执行
     *
     * @return 包含限流信息的 Map
     */
    @ReadOperation
    public Map<String, Object> rateLimitInfo() {
        Map<String, Object> info = new HashMap<>();
        // 填充基本信息
        info.put("enabled", properties.isEnabled());
        info.put("clusterMode", properties.isClusterMode());
        info.put("redisKeyPrefix", properties.getRedisKeyPrefix());
        info.put("duplicateKeyPrefix", properties.getDuplicateKeyPrefix());

        // 获取限流相关 Key 的数量
        if (redisTemplate != null) {
            try {
                // 使用 keys 命令扫描所有以限流前缀开头的 key
                Set<String> rateKeys = redisTemplate.keys(properties.getRedisKeyPrefix() + "*");
                Set<String> duplicateKeys = redisTemplate.keys(properties.getDuplicateKeyPrefix() + "*");

                info.put("rateLimitKeys", rateKeys != null ? rateKeys.size() : 0);
                info.put("duplicateSubmitKeys", duplicateKeys != null ? duplicateKeys.size() : 0);
            } catch (Exception e) {
                info.put("rateLimitKeys", "获取失败");
                info.put("duplicateSubmitKeys", "获取失败");
            }
        } else {
            info.put("rateLimitKeys", "RedisTemplate不可用");
            info.put("duplicateSubmitKeys", "RedisTemplate不可用");
        }

        return info;
    }
}