package com.mok.framework.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/** 登录加密配置。私钥文件使用外部 PKCS#8 PEM，禁止写入仓库。 */
@Component
@ConfigurationProperties(prefix = "mok.security.login-crypto")
public class LoginCryptoProperties {

    private String keyId = "login-v1";
    private String privateKeyPath;
    private long challengeTtlSeconds = 120;
    // 轮换期间保留的旧 keyId -> 私钥路径，只用于解密尚未过期的登录凭证。
    private Map<String, String> previousPrivateKeyPaths = new HashMap<>();

    public String getKeyId() {
        return keyId;
    }

    public void setKeyId(String keyId) {
        this.keyId = keyId;
    }

    public String getPrivateKeyPath() {
        return privateKeyPath;
    }

    public void setPrivateKeyPath(String privateKeyPath) {
        this.privateKeyPath = privateKeyPath;
    }

    public long getChallengeTtlSeconds() {
        return challengeTtlSeconds;
    }

    public void setChallengeTtlSeconds(long challengeTtlSeconds) {
        this.challengeTtlSeconds = challengeTtlSeconds;
    }

    public Map<String, String> getPreviousPrivateKeyPaths() {
        return previousPrivateKeyPaths;
    }

    public void setPreviousPrivateKeyPaths(Map<String, String> previousPrivateKeyPaths) {
        this.previousPrivateKeyPaths = previousPrivateKeyPaths;
    }
}
