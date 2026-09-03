package com.mok.framework.model.dto;

/**
 * 退出登录请求。
 */
public class LogoutRequest {

    private String refreshToken;

    public LogoutRequest() {
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
