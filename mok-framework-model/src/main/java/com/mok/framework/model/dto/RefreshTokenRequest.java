package com.mok.framework.model.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 刷新访问令牌请求。
 */
public class RefreshTokenRequest {

    @NotBlank(message = "刷新令牌不能为空")
    private String refreshToken;

    public RefreshTokenRequest() {
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
