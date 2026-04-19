package com.mok.framework.model.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.Objects;

/**
 * @description: 登录请求 DTO(数据传输对象)
 * @author: JN
 * @date: 2026/1/1
 */
public class LoginRequest  {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotBlank(message = "验证码不能为空")
    private String captcha;

    @NotBlank(message = "验证码 key 不能为空")
    private String captchaKey;

    // 默认构造函数
    public LoginRequest() {
    }

    // 全参数构造函数（可选）
    public LoginRequest(String username, String password, String captcha, String captchaKey) {
        this.username = username;
        this.password = password;
        this.captcha = captcha;
        this.captchaKey = captchaKey;
    }

    // Getter 和 Setter 方法
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCaptcha() {
        return captcha;
    }

    public void setCaptcha(String captcha) {
        this.captcha = captcha;
    }

    public String getCaptchaKey() {
        return captchaKey;
    }

    public void setCaptchaKey(String captchaKey) {
        this.captchaKey = captchaKey;
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
        LoginRequest that = (LoginRequest) o;
        return Objects.equals(username, that.username) &&
                Objects.equals(password, that.password) &&
                Objects.equals(captcha, that.captcha) &&
                Objects.equals(captchaKey, that.captchaKey);
    }

    // hashCode 方法
    @Override
    public int hashCode() {
        return Objects.hash(username, password, captcha, captchaKey);
    }

    // toString 方法
    @Override
    public String toString() {
        return "LoginRequest{" +
                "username='" + username + '\'' +
                ", password='" + "[PROTECTED]" + '\'' + // 出于安全考虑，不直接显示密码
                ", captcha='" + captcha + '\'' +
                ", captchaKey='" + captchaKey + '\'' +
                '}';
    }
}