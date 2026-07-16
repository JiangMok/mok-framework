package com.mok.framework.ratelimiter.core;

/**
 * 防重复提交服务接口
 * @author mok
 */
public interface DuplicateSubmitService {
    
    /**
     * 检查是否允许提交
     * @param key 唯一标识
     * @param lockTime 锁定时间（秒）
     * @return true表示允许提交（未重复），false表示不允许（已存在锁）
     */
    boolean isAllowed(String key, int lockTime);

    /**
     * 检查是否允许提交（自定义错误信息）
     * @param key 唯一标识
     * @param lockTime 锁定时间
     * @param message 自定义错误信息
     * @return true/false
     */
    boolean isAllowed(String key, int lockTime, String message);

    /**
     * 释放提交锁
     * @param key 唯一标识
     */
    void release(String key);

    /**
     * 检查并锁定（如果允许）
     * 原子性操作：检查是否存在锁，不存在则创建锁并设置过期时间
     * @param key 唯一标识
     * @param lockTime 锁定时间
     * @return true表示成功获取锁（允许提交），false表示已存在锁
     */
    boolean checkAndLock(String key, int lockTime);
}