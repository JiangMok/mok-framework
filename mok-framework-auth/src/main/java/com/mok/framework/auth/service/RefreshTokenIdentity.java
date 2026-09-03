package com.mok.framework.auth.service;

/**
 * 刷新令牌绑定的用户身份与安全会话版本。
 */
public record RefreshTokenIdentity(String userId, long sessionVersion) {

    private static final String SEPARATOR = ":";

    public RefreshTokenIdentity {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("刷新令牌用户ID不能为空");
        }
        if (sessionVersion < 0) {
            throw new IllegalArgumentException("刷新令牌会话版本不正确");
        }
    }

    public String toStorageValue() {
        return sessionVersion + SEPARATOR + userId;
    }

    public static RefreshTokenIdentity fromStorageValue(Object value) {
        if (!(value instanceof String text)) {
            throw new IllegalArgumentException("刷新令牌载荷格式不正确");
        }
        int separatorIndex = text.indexOf(SEPARATOR);
        if (separatorIndex <= 0 || separatorIndex == text.length() - 1) {
            throw new IllegalArgumentException("刷新令牌载荷格式不正确");
        }
        try {
            long version = Long.parseLong(text.substring(0, separatorIndex));
            return new RefreshTokenIdentity(text.substring(separatorIndex + 1), version);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("刷新令牌会话版本不正确", exception);
        }
    }
}
