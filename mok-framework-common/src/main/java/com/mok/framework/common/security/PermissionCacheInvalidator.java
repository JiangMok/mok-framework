package com.mok.framework.common.security;

import java.util.Collection;

/**
 * 权限缓存失效抽象。
 *
 * <p>接口放在 common 模块，业务模块只声明失效意图，具体缓存实现由认证模块提供，
 * 避免 base 与 auth 形成 Maven 循环依赖。</p>
 */
public interface PermissionCacheInvalidator {

    /**
     * 获取当前用户的权限缓存版本。缓存键必须绑定该版本，避免并发失效后旧值回写。
     */
    String getPermissionCacheVersion(String userId);

    /**
     * 清除指定用户的权限缓存。
     */
    void evictUserPermissions(String userId);

    /**
     * 批量清除用户权限缓存。
     */
    void evictUserPermissions(Collection<String> userIds);

    /**
     * 清除用户有效状态及权限缓存。
     */
    void evictUserSecurityState(String userId);

    /**
     * 清除全部用户权限缓存。
     */
    void evictAllPermissions();
}
