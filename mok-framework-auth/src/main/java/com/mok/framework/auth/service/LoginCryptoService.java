package com.mok.framework.auth.service;

import com.mok.framework.model.dto.LoginChallengeResponse;
import com.mok.framework.model.dto.LoginCredentials;
import com.mok.framework.model.dto.LoginRequest;

/** 登录专用的加密凭据处理。 */
public interface LoginCryptoService {

    LoginChallengeResponse createChallenge();

    /** 解密并校验凭据，原子消费 challenge，成功后才允许进入账号密码认证。 */
    LoginCredentials decryptAndConsume(LoginRequest request);
}
