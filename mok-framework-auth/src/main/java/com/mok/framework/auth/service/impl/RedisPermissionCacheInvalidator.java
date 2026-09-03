package com.mok.framework.auth.service.impl;

import com.mok.framework.common.constant.PermissionCacheConstant;
import com.mok.framework.common.security.PermissionCacheInvalidator;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

import static com.mok.framework.common.constant.PermissionCacheConstant.GLOBAL_PERMISSION_VERSION_KEY;
import static com.mok.framework.common.constant.PermissionCacheConstant.USER_PERMISSION_VERSION_KEY;

@Service
public class RedisPermissionCacheInvalidator implements PermissionCacheInvalidator {

    private final StringRedisTemplate redisTemplate;

    public RedisPermissionCacheInvalidator(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public String getPermissionCacheVersion(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        long globalVersion = readVersion(GLOBAL_PERMISSION_VERSION_KEY);
        long userVersion = readVersion(String.format(USER_PERMISSION_VERSION_KEY, userId));
        return globalVersion + "-" + userVersion;
    }

    @Override
    public void evictUserPermissions(String userId) {
        if (userId == null || userId.isBlank()) {
            return;
        }
        incrementVersion(String.format(USER_PERMISSION_VERSION_KEY, userId));
    }

    @Override
    public void evictUserPermissions(Collection<String> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }
        List<String> distinctUserIds = userIds.stream()
                .filter(userId -> userId != null && !userId.isBlank())
                .distinct()
                .toList();
        for (String userId : distinctUserIds) {
            incrementVersion(String.format(USER_PERMISSION_VERSION_KEY, userId));
        }
    }

    @Override
    public void evictUserSecurityState(String userId) {
        if (userId == null || userId.isBlank()) {
            return;
        }
        incrementVersion(String.format(USER_PERMISSION_VERSION_KEY, userId));
        redisTemplate.delete(String.format(PermissionCacheConstant.USER_EXISTS_KEY, userId));
    }

    @Override
    public void evictAllPermissions() {
        incrementVersion(GLOBAL_PERMISSION_VERSION_KEY);
    }

    private long readVersion(String key) {
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return 0L;
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException exception) {
            throw new IllegalStateException("权限缓存版本数据损坏: " + key, exception);
        }
    }

    private void incrementVersion(String key) {
        Long version = redisTemplate.opsForValue().increment(key);
        if (version == null) {
            throw new IllegalStateException("权限缓存版本更新失败: " + key);
        }
    }
}
