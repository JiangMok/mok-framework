package com.mok.framework.common.constant;

/**
 * 权限缓存常量类
 */
public interface PermissionCacheConstant {
    //用户存在的 key
    String USER_EXISTS_KEY = "security:user:exists:%s";
    //用户权限缓存 key
    String USER_PERMISSION_KEY = "security:user:permissions:%s:%s";
    // 全局与单用户权限版本
    String GLOBAL_PERMISSION_VERSION_KEY = "security:permissions:version";
    String USER_PERMISSION_VERSION_KEY = "security:user:permission-version:%s";
    // 空值标记
    String NULL_VALUE = "NULL";
    //缓存过期时间 : 30分钟
    long CACHE_EXPIRE = 30;
    // 空值缓存时间（较短）
    long NULL_CACHE_EXPIRE = 5;
}
