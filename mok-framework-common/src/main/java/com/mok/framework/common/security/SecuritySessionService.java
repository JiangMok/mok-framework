package com.mok.framework.common.security;

/**
 * 用户安全会话版本服务。
 *
 * <p>访问令牌和刷新令牌都绑定用户当前会话版本。禁用、删除或修改密码时递增版本，
 * 即可让该用户此前签发的所有令牌立即失效。</p>
 */
public interface SecuritySessionService {

    String SESSION_VERSION_CLAIM = "sessionVersion";

    /**
     * 获取用户当前会话版本。未初始化的用户版本为 0。
     */
    long getCurrentVersion(String userId);

    /**
     * 判断令牌携带的版本是否仍为当前版本。
     */
    boolean isCurrentVersion(String userId, long tokenVersion);

    /**
     * 使指定用户此前签发的所有访问令牌和刷新令牌失效。
     */
    void invalidateUserSessions(String userId);
}
