package com.mok.framework.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** 解密后的登录凭据，仅在服务端认证过程中使用。 */
public class LoginCredentials {

    @NotBlank(message = "用户名不能为空")
    @Size(max = 64, message = "用户名过长")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(max = 128, message = "密码过长")
    private String password;

    @NotBlank(message = "登录凭证不能为空")
    @Pattern(regexp = "[a-f0-9]{64}", message = "登录凭证格式错误")
    private String challengeId;

    @NotBlank(message = "验证码 key 不能为空")
    @Size(max = 64, message = "验证码 key 过长")
    private String captchaKey;

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

    public String getChallengeId() {
        return challengeId;
    }

    public void setChallengeId(String challengeId) {
        this.challengeId = challengeId;
    }

    public String getCaptchaKey() {
        return captchaKey;
    }

    public void setCaptchaKey(String captchaKey) {
        this.captchaKey = captchaKey;
    }

    @Override
    public String toString() {
        return "LoginCredentials{[PROTECTED]}";
    }
}
