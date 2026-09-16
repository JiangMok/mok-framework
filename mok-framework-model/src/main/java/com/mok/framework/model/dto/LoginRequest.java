package com.mok.framework.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.Objects;

/**
 * @description: 登录请求 DTO(数据传输对象)
 * @author: JN
 * @date: 2026/1/1
 */
public class LoginRequest  {

    @NotBlank(message = "密钥标识不能为空")
    @Pattern(regexp = "[A-Za-z0-9_-]{1,64}", message = "密钥标识格式错误")
    private String keyId;

    @NotBlank(message = "加密登录凭据不能为空")
    @Size(min = 512, max = 512, message = "加密登录凭据长度错误")
    private String encryptedCredentials;

    @NotBlank(message = "验证码不能为空")
    @Size(max = 16, message = "验证码长度错误")
    private String captcha;

    @NotBlank(message = "验证码 key 不能为空")
    @Size(max = 64, message = "验证码 key 长度错误")
    private String captchaKey;

    // 默认构造函数
    public LoginRequest() {
    }

    // 全参数构造函数（可选）
    public LoginRequest(String keyId, String encryptedCredentials, String captcha, String captchaKey) {
        this.keyId = keyId;
        this.encryptedCredentials = encryptedCredentials;
        this.captcha = captcha;
        this.captchaKey = captchaKey;
    }

    // Getter 和 Setter 方法
    public String getKeyId() {
        return keyId;
    }

    public void setKeyId(String keyId) {
        this.keyId = keyId;
    }

    public String getEncryptedCredentials() {
        return encryptedCredentials;
    }

    public void setEncryptedCredentials(String encryptedCredentials) {
        this.encryptedCredentials = encryptedCredentials;
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
        return Objects.equals(keyId, that.keyId) &&
                Objects.equals(encryptedCredentials, that.encryptedCredentials) &&
                Objects.equals(captcha, that.captcha) &&
                Objects.equals(captchaKey, that.captchaKey);
    }

    // hashCode 方法
    @Override
    public int hashCode() {
        return Objects.hash(keyId, encryptedCredentials, captcha, captchaKey);
    }

    // toString 方法
    @Override
    public String toString() {
        return "LoginRequest{" +
                "keyId='" + keyId + '\'' +
                ", encryptedCredentials='[PROTECTED]'" +
                '}';
    }
}
