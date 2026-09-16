package com.mok.framework.model.dto;

/** 一次性登录凭证和 SPKI 格式的 Base64 公钥。 */
public class LoginChallengeResponse {

    private final String keyId;
    private final String publicKey;
    private final String challengeId;
    private final long expiresIn;

    public LoginChallengeResponse(String keyId, String publicKey, String challengeId, long expiresIn) {
        this.keyId = keyId;
        this.publicKey = publicKey;
        this.challengeId = challengeId;
        this.expiresIn = expiresIn;
    }

    public String getKeyId() {
        return keyId;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public String getChallengeId() {
        return challengeId;
    }

    public long getExpiresIn() {
        return expiresIn;
    }
}
