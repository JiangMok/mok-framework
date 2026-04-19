package com.mok.framework.model.dto;

import java.util.Objects;

/**
 * @description: 登录响应 DTO(数据传输对象)
 *                  封装登录成功后返回给前端的数据
 *                  适用于 RESTful API 的响应体
 * @author: JN
 * @date: 2026/1/1
 */
public class LoginResponse  {
    //访问令牌字段
    //  作用 : 用于访问受保护资源的令牌
    //  类型 : string 通常是 JWT 格式的字符串
    //  命名 : token,遵循 OAuth 2.0 he JWT 的命名约定
    private String token;

    //刷新令牌字段
    //  作用 : 当访问令牌过期时,用于获取新的访问令牌
    private String refreshToken;

    //令牌过期时间字段
    //  作用 : 表示访问令牌在多长时间后过期(单位是 : 毫秒)
    //  类型 : Long,因为时间值可能比较大
    private Long expiresIn;

    //令牌类型字段
    //  作用 : 表示令牌的类型,通常是 Bearer
    //  初始化 : 默认值为"Bearer",这是最常见的令牌类型
    private String tokenType = "Bearer";

    //用户名字段
    //  作用 : 返回登录用户的用户名
    private String username;

    //用户昵称字段
    private String nickname;

    //用户 ID 字段
    //  作用 : 返回登录用户的唯一标识符
    private String userId;

    private String avatar;

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    // 默认构造函数
    public LoginResponse() {
    }

    // 全参数构造函数（可选）
    public LoginResponse(String token, String refreshToken, Long expiresIn, String tokenType,
                         String username, String nickname, String userId) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.tokenType = tokenType != null ? tokenType : "Bearer";
        this.username = username;
        this.nickname = nickname;
        this.userId = userId;
    }

    // 便捷构造函数，使用默认的 tokenType
    public LoginResponse(String token, String refreshToken, Long expiresIn,
                         String username, String nickname, String userId) {
        this(token, refreshToken, expiresIn, "Bearer", username, nickname, userId);
    }

    // Getter 和 Setter 方法
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType != null ? tokenType : "Bearer";
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    // equals 方法
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LoginResponse that = (LoginResponse) o;
        return Objects.equals(token, that.token) &&
                Objects.equals(refreshToken, that.refreshToken) &&
                Objects.equals(expiresIn, that.expiresIn) &&
                Objects.equals(tokenType, that.tokenType) &&
                Objects.equals(username, that.username) &&
                Objects.equals(nickname, that.nickname) &&
                Objects.equals(userId, that.userId);
    }

    // hashCode 方法
    @Override
    public int hashCode() {
        return Objects.hash(token, refreshToken, expiresIn, tokenType, username, nickname, userId);
    }

    // toString 方法
    @Override
    public String toString() {
        return "LoginResponse{" +
                "token='" + (token != null ? "[PROTECTED]" : null) + '\'' +
                ", refreshToken='" + (refreshToken != null ? "[PROTECTED]" : null) + '\'' +
                ", expiresIn=" + expiresIn +
                ", tokenType='" + tokenType + '\'' +
                ", username='" + username + '\'' +
                ", nickname='" + nickname + '\'' +
                ", userId='" + userId + '\'' +
                '}';
    }
}