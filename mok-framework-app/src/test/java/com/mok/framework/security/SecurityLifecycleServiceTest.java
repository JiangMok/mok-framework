package com.mok.framework.security;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.temp.SaTempUtil;
import com.mok.framework.auth.config.SaTokenConfigure;
import com.mok.framework.auth.config.LoginCryptoProperties;
import com.mok.framework.auth.service.impl.RsaLoginCryptoServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mok.framework.model.dto.LoginChallengeResponse;
import com.mok.framework.model.dto.LoginCredentials;
import com.mok.framework.model.dto.LoginRequest;
import com.mok.framework.model.dto.LoginResponse;
import com.mok.framework.auth.controller.AuthController;
import com.mok.framework.auth.mapper.PermissionAuthMapper;
import com.mok.framework.auth.mapper.UserAuthMapper;
import com.mok.framework.auth.service.PermissionAuthService;
import com.mok.framework.auth.service.LoginCryptoService;
import com.mok.framework.auth.service.RefreshTokenIdentity;
import com.mok.framework.auth.service.RefreshTokenService;
import com.mok.framework.auth.service.TokenBlackListService;
import com.mok.framework.auth.service.UserAuthService;
import com.mok.framework.auth.service.impl.RedisSecuritySessionService;
import com.mok.framework.auth.service.impl.PermissionAuthServiceImpl;
import com.mok.framework.auth.service.impl.SaTempRefreshTokenServiceImpl;
import com.mok.framework.base.mapper.PermissionMapper;
import com.mok.framework.base.mapper.RoleMapper;
import com.mok.framework.base.mapper.RolePermissionMapper;
import com.mok.framework.base.mapper.UserMapper;
import com.mok.framework.base.mapper.UserRoleMapper;
import com.mok.framework.base.controller.UserController;
import com.mok.framework.base.service.DepartmentService;
import com.mok.framework.base.service.PermissionService;
import com.mok.framework.base.service.RoleService;
import com.mok.framework.base.service.UserService;
import com.mok.framework.base.service.impl.RoleServiceImpl;
import com.mok.framework.base.service.impl.PermissionServiceImpl;
import com.mok.framework.base.service.impl.UserServiceImpl;
import com.mok.framework.captcha.service.CaptchaService;
import com.mok.framework.common.BusinessException;
import com.mok.framework.common.R;
import com.mok.framework.common.constant.ResponseCode;
import com.mok.framework.common.security.PermissionCacheInvalidator;
import com.mok.framework.common.security.SecuritySessionService;
import com.mok.framework.model.dto.PasswordChangeRequest;
import com.mok.framework.model.dto.PasswordResetRequest;
import com.mok.framework.model.dto.LogoutRequest;
import com.mok.framework.model.dto.PermissionDTO;
import com.mok.framework.model.dto.RefreshTokenRequest;
import com.mok.framework.model.entity.PermissionEntity;
import com.mok.framework.model.dto.UserDTO;
import com.mok.framework.model.dto.UserUpdateDto;
import com.mok.framework.model.entity.RoleEntity;
import com.mok.framework.model.entity.UserEntity;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.mock.web.MockHttpServletResponse;
import org.mockito.MockedStatic;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyFactory;
import java.security.spec.X509EncodedKeySpec;
import java.security.spec.MGF1ParameterSpec;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

@SuppressWarnings({"unchecked", "rawtypes"})
class SecurityLifecycleServiceTest {

    private static KeyPair loginKeyPair;
    private static jakarta.validation.ValidatorFactory loginValidatorFactory;

    @BeforeAll
    static void createEphemeralLoginKey() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(3072);
        loginKeyPair = generator.generateKeyPair();
        loginValidatorFactory = Validation.buildDefaultValidatorFactory();
    }

    @AfterAll
    static void closeLoginValidatorFactory() {
        if (loginValidatorFactory != null) {
            loginValidatorFactory.close();
        }
    }

    @Test
    void encryptedLoginRoundTripConsumesChallengeOnce(@TempDir Path directory) throws Exception {
        LoginCryptoFixture fixture = loginCryptoFixture(directory);
        LoginChallengeResponse challenge = fixture.service().createChallenge();
        LoginRequest request = encryptedLogin(challenge, "管理员", "Secret123", "captcha-key");

        LoginCredentials credentials = fixture.service().decryptAndConsume(request);

        assertEquals("管理员", credentials.getUsername());
        assertEquals("Secret123", credentials.getPassword());
        assertEquals(120, challenge.getExpiresIn());
        assertFalse(credentials.toString().contains("Secret123"));
        assertFalse(request.toString().contains(request.getEncryptedCredentials()));
        assertThrows(BusinessException.class, () -> fixture.service().decryptAndConsume(request));
        verify(fixture.redis().opsForValue()).setIfAbsent(
                "security:login:challenge:" + challenge.getChallengeId(), "login-v1",
                120L, TimeUnit.SECONDS);
    }

    @Test
    void encryptedLoginRejectsTamperingAndExpiredChallenge(@TempDir Path directory) throws Exception {
        LoginCryptoFixture fixture = loginCryptoFixture(directory);
        LoginChallengeResponse challenge = fixture.service().createChallenge();
        LoginRequest request = encryptedLogin(challenge, "admin", "Secret123", "captcha-key");
        String validCiphertext = request.getEncryptedCredentials();
        byte[] bytes = Base64.getDecoder().decode(validCiphertext);
        bytes[0] ^= 1;
        request.setEncryptedCredentials(Base64.getEncoder().encodeToString(bytes));
        BusinessException tampered = assertThrows(BusinessException.class,
                () -> fixture.service().decryptAndConsume(request));
        assertEquals("登录凭据无效或已过期，请重新登录", tampered.getMessage());
        assertEquals(null, tampered.getCause());

        request.setEncryptedCredentials(validCiphertext);
        fixture.challenges().clear(); // 模拟Redis TTL已经到期，不连接真实Redis。
        assertThrows(BusinessException.class, () -> fixture.service().decryptAndConsume(request));
    }

    @Test
    void encryptedLoginBindsCaptchaAndKeyAndValidatesPlaintext(@TempDir Path directory) throws Exception {
        LoginCryptoFixture fixture = loginCryptoFixture(directory);
        LoginChallengeResponse challenge = fixture.service().createChallenge();
        LoginRequest request = encryptedLogin(challenge, "admin", "Secret123", "captcha-key");
        request.setCaptchaKey("another-captcha");
        assertThrows(BusinessException.class, () -> fixture.service().decryptAndConsume(request));
        request.setCaptchaKey("captcha-key");
        request.setKeyId("unknown-key");
        assertThrows(BusinessException.class, () -> fixture.service().decryptAndConsume(request));

        LoginRequest blankPassword = encryptedLogin(challenge, "admin", " ", "captcha-key");
        assertThrows(BusinessException.class, () -> fixture.service().decryptAndConsume(blankPassword));
        LoginRequest oldPlaintext = new ObjectMapper().readerFor(LoginRequest.class)
                .without(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .readValue("{\"username\":\"admin\",\"password\":\"Secret123\","
                        + "\"captcha\":\"1234\",\"captchaKey\":\"captcha-key\"}");
        assertThrows(BusinessException.class, () -> fixture.service().decryptAndConsume(oldPlaintext));
    }

    @Test
    void onlyOneConcurrentLoginCanConsumeTheChallenge(@TempDir Path directory) throws Exception {
        LoginCryptoFixture fixture = loginCryptoFixture(directory);
        LoginRequest request = encryptedLogin(
                fixture.service().createChallenge(), "admin", "Secret123", "captcha-key");
        var executor = Executors.newFixedThreadPool(2);
        try {
            java.util.concurrent.Callable<Boolean> attempt = () -> {
                try {
                    fixture.service().decryptAndConsume(request);
                    return true;
                } catch (BusinessException expected) {
                    return false;
                }
            };
            var results = executor.invokeAll(List.of(attempt, attempt));
            assertEquals(1, (results.get(0).get() ? 1 : 0) + (results.get(1).get() ? 1 : 0));
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void missingPrivateKeyFailsWithoutGeneratingAReplacement() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            assertThrows(IllegalStateException.class, () -> new RsaLoginCryptoServiceImpl(
                    new LoginCryptoProperties(), mock(StringRedisTemplate.class),
                    new ObjectMapper(), factory.getValidator()));
        }
    }

    @Test
    void challengeResponseDisablesCachingAndInvalidCaptchaSkipsDecryption() {
        LoginCryptoService cryptoService = mock(LoginCryptoService.class);
        CaptchaService captchaService = mock(CaptchaService.class);
        UserAuthService users = mock(UserAuthService.class);
        AuthController controller = new AuthController(users, captchaService,
                mock(TokenBlackListService.class), mock(PermissionAuthService.class),
                mock(RoleService.class), mock(RefreshTokenService.class),
                mock(SecuritySessionService.class), cryptoService);
        MockHttpServletResponse response = new MockHttpServletResponse();
        controller.loginChallenge(response);
        assertEquals("no-store", response.getHeader("Cache-Control"));

        R<LoginResponse> result = controller.loadUser(
                new LoginRequest("login-v1", "invalid", "wrong", "captcha-key"));
        assertEquals(1002, result.getCode());
        verify(cryptoService, never()).decryptAndConsume(any());
        verify(users, never()).getByUserName(anyString());
    }

    private LoginCryptoFixture loginCryptoFixture(Path directory) throws Exception {
        Path privateKey = directory.resolve("ephemeral-login-key.pem");
        Files.writeString(privateKey, "-----BEGIN PRIVATE KEY-----\n"
                + Base64.getEncoder().encodeToString(loginKeyPair.getPrivate().getEncoded())
                + "\n-----END PRIVATE KEY-----\n", StandardCharsets.US_ASCII);
        LoginCryptoProperties properties = new LoginCryptoProperties();
        properties.setPrivateKeyPath(privateKey.toAbsolutePath().toString());
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        ValueOperations<String, String> operations = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(operations);
        Map<String, String> challenges = new ConcurrentHashMap<>();
        when(operations.setIfAbsent(anyString(), anyString(), anyLong(), eq(TimeUnit.SECONDS)))
                .thenAnswer(invocation -> challenges.putIfAbsent(
                        invocation.getArgument(0), invocation.getArgument(1)) == null);
        when(redis.execute(any(RedisScript.class), any(List.class), any(Object[].class)))
                .thenAnswer(invocation -> {
                    List<String> keys = invocation.getArgument(1);
                    Object[] arguments = (Object[]) invocation.getRawArguments()[2];
                    return challenges.remove(keys.get(0), arguments[0]) ? 1L : 0L;
                });
        Validator validator = loginValidatorFactory.getValidator();
        return new LoginCryptoFixture(new RsaLoginCryptoServiceImpl(
                properties, redis, new ObjectMapper(), validator), redis, challenges);
    }

    private LoginRequest encryptedLogin(LoginChallengeResponse challenge, String username,
                                        String password, String captchaKey) throws Exception {
        var publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(
                Base64.getDecoder().decode(challenge.getPublicKey())));
        byte[] plaintext = new ObjectMapper().writeValueAsBytes(Map.of(
                "username", username, "password", password,
                "challengeId", challenge.getChallengeId(), "captchaKey", captchaKey));
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPPadding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey, new OAEPParameterSpec(
                "SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT));
        return new LoginRequest(challenge.getKeyId(),
                Base64.getEncoder().encodeToString(cipher.doFinal(plaintext)), "1234", captchaKey);
    }

    private record LoginCryptoFixture(RsaLoginCryptoServiceImpl service,
                                       StringRedisTemplate redis,
                                       Map<String, String> challenges) {
    }

    @Test
    void logoutRevokesTheCurrentRefreshTokenAndBlacklistsAccessToken() {
        UserAuthService userAuthService = mock(UserAuthService.class);
        CaptchaService captchaService = mock(CaptchaService.class);
        TokenBlackListService blacklistService = mock(TokenBlackListService.class);
        PermissionAuthService permissionAuthService = mock(PermissionAuthService.class);
        RoleService roleService = mock(RoleService.class);
        RefreshTokenService refreshTokenService = mock(RefreshTokenService.class);
        SecuritySessionService sessionService = mock(SecuritySessionService.class);
        AuthController controller = new AuthController(userAuthService, captchaService,
                blacklistService, permissionAuthService, roleService,
                refreshTokenService, sessionService, mock(LoginCryptoService.class));
        LogoutRequest logoutRequest = new LogoutRequest();
        logoutRequest.setRefreshToken("refresh-1");
        HttpServletRequest servletRequest = mock(HttpServletRequest.class);
        when(servletRequest.getHeader("Authorization")).thenReturn("Bearer access-1");
        when(refreshTokenService.consume("refresh-1"))
                .thenReturn(new RefreshTokenIdentity("user-1", 0L));
        when(sessionService.isCurrentVersion("user-1", 0L)).thenReturn(true);
        when(userAuthService.selectById("user-1"))
                .thenReturn(new UserEntity().setId("user-1").setStatus(1).setIsDeleted(0));

        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(() -> StpUtil.getLoginIdByToken("access-1")).thenReturn("user-1");
            stpUtil.when(() -> StpUtil.getTokenTimeout("access-1")).thenReturn(120L);
            stpUtil.when(() -> StpUtil.getExtra(
                    "access-1", SecuritySessionService.SESSION_VERSION_CLAIM)).thenReturn(0L);

            R<String> result = controller.logOut(logoutRequest, servletRequest);

            assertEquals(200, result.getCode());
            verify(blacklistService).addToBlacklist("access-1", 120L);
            verify(refreshTokenService).consume("refresh-1");
            verify(sessionService).invalidateUserSessions("user-1");
        }
    }

    @Test
    void logoutUsesRefreshTokenWhenAccessTokenHasExpired() {
        UserAuthService userAuthService = mock(UserAuthService.class);
        RefreshTokenService refreshTokenService = mock(RefreshTokenService.class);
        SecuritySessionService sessionService = mock(SecuritySessionService.class);
        TokenBlackListService blacklistService = mock(TokenBlackListService.class);
        AuthController controller = new AuthController(userAuthService, mock(CaptchaService.class),
                blacklistService, mock(PermissionAuthService.class), mock(RoleService.class),
                refreshTokenService, sessionService, mock(LoginCryptoService.class));
        LogoutRequest logoutRequest = new LogoutRequest();
        logoutRequest.setRefreshToken("refresh-1");
        HttpServletRequest servletRequest = mock(HttpServletRequest.class);
        when(servletRequest.getHeader("Authorization")).thenReturn("Bearer expired-access");
        when(refreshTokenService.consume("refresh-1"))
                .thenReturn(new RefreshTokenIdentity("user-1", 3L));
        when(sessionService.isCurrentVersion("user-1", 3L)).thenReturn(true);
        when(userAuthService.selectById("user-1"))
                .thenReturn(new UserEntity().setId("user-1").setStatus(1).setIsDeleted(0));

        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(() -> StpUtil.getLoginIdByToken("expired-access"))
                    .thenThrow(new IllegalArgumentException("token expired"));

            R<String> result = controller.logOut(logoutRequest, servletRequest);

            assertEquals(200, result.getCode());
            verify(refreshTokenService).consume("refresh-1");
            verify(sessionService).invalidateUserSessions("user-1");
            verify(blacklistService, never()).addToBlacklist(anyString(), anyLong());
        }
    }

    @Test
    void logoutUsesRefreshTokenWhenAccessTokenIsMissing() {
        UserAuthService userAuthService = mock(UserAuthService.class);
        RefreshTokenService refreshTokenService = mock(RefreshTokenService.class);
        SecuritySessionService sessionService = mock(SecuritySessionService.class);
        TokenBlackListService blacklistService = mock(TokenBlackListService.class);
        AuthController controller = new AuthController(userAuthService, mock(CaptchaService.class),
                blacklistService, mock(PermissionAuthService.class), mock(RoleService.class),
                refreshTokenService, sessionService, mock(LoginCryptoService.class));
        LogoutRequest logoutRequest = new LogoutRequest();
        logoutRequest.setRefreshToken("refresh-1");
        HttpServletRequest servletRequest = mock(HttpServletRequest.class);
        when(refreshTokenService.consume("refresh-1"))
                .thenReturn(new RefreshTokenIdentity("user-1", 3L));
        when(sessionService.isCurrentVersion("user-1", 3L)).thenReturn(true);
        when(userAuthService.selectById("user-1"))
                .thenReturn(new UserEntity().setId("user-1").setStatus(1).setIsDeleted(0));

        R<String> result = controller.logOut(logoutRequest, servletRequest);

        assertEquals(200, result.getCode());
        verify(refreshTokenService).consume("refresh-1");
        verify(sessionService).invalidateUserSessions("user-1");
        verify(blacklistService, never()).addToBlacklist(anyString(), anyLong());
    }

    @Test
    void logoutUsesAccessTokenWhenRefreshTokenIsMissing() {
        UserAuthService userAuthService = mock(UserAuthService.class);
        SecuritySessionService sessionService = mock(SecuritySessionService.class);
        TokenBlackListService blacklistService = mock(TokenBlackListService.class);
        AuthController controller = new AuthController(userAuthService, mock(CaptchaService.class),
                blacklistService, mock(PermissionAuthService.class), mock(RoleService.class),
                mock(RefreshTokenService.class), sessionService, mock(LoginCryptoService.class));
        HttpServletRequest servletRequest = mock(HttpServletRequest.class);
        when(servletRequest.getHeader("Authorization")).thenReturn("Bearer access-1");
        when(sessionService.isCurrentVersion("user-1", 0L)).thenReturn(true);
        when(userAuthService.selectById("user-1"))
                .thenReturn(new UserEntity().setId("user-1").setStatus(1).setIsDeleted(0));

        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(() -> StpUtil.getLoginIdByToken("access-1")).thenReturn("user-1");
            stpUtil.when(() -> StpUtil.getTokenTimeout("access-1")).thenReturn(120L);
            stpUtil.when(() -> StpUtil.getExtra(
                    "access-1", SecuritySessionService.SESSION_VERSION_CLAIM)).thenReturn(0L);

            R<String> result = controller.logOut(null, servletRequest);

            assertEquals(200, result.getCode());
            verify(blacklistService).addToBlacklist("access-1", 120L);
            verify(sessionService).invalidateUserSessions("user-1");
        }
    }

    @Test
    void logoutRejectsCredentialsBelongingToDifferentUsers() {
        UserAuthService userAuthService = mock(UserAuthService.class);
        RefreshTokenService refreshTokenService = mock(RefreshTokenService.class);
        SecuritySessionService sessionService = mock(SecuritySessionService.class);
        TokenBlackListService blacklistService = mock(TokenBlackListService.class);
        AuthController controller = new AuthController(userAuthService, mock(CaptchaService.class),
                blacklistService, mock(PermissionAuthService.class), mock(RoleService.class),
                refreshTokenService, sessionService, mock(LoginCryptoService.class));
        LogoutRequest logoutRequest = new LogoutRequest();
        logoutRequest.setRefreshToken("refresh-2");
        HttpServletRequest servletRequest = mock(HttpServletRequest.class);
        when(servletRequest.getHeader("Authorization")).thenReturn("Bearer access-1");
        when(refreshTokenService.consume("refresh-2"))
                .thenReturn(new RefreshTokenIdentity("user-2", 0L));
        when(sessionService.isCurrentVersion("user-1", 0L)).thenReturn(true);
        when(sessionService.isCurrentVersion("user-2", 0L)).thenReturn(true);

        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(() -> StpUtil.getLoginIdByToken("access-1")).thenReturn("user-1");
            stpUtil.when(() -> StpUtil.getTokenTimeout("access-1")).thenReturn(120L);
            stpUtil.when(() -> StpUtil.getExtra(
                    "access-1", SecuritySessionService.SESSION_VERSION_CLAIM)).thenReturn(0L);

            R<String> result = controller.logOut(logoutRequest, servletRequest);

            assertEquals(ResponseCode.UNAUTHORIZED, result.getCode());
            verify(sessionService, never()).invalidateUserSessions(anyString());
            verify(blacklistService, never()).addToBlacklist(anyString(), anyLong());
        }
    }

    @Test
    void logoutRejectsRequestWithoutAnyValidCredential() {
        SecuritySessionService sessionService = mock(SecuritySessionService.class);
        AuthController controller = new AuthController(mock(UserAuthService.class),
                mock(CaptchaService.class), mock(TokenBlackListService.class),
                mock(PermissionAuthService.class), mock(RoleService.class),
                mock(RefreshTokenService.class), sessionService, mock(LoginCryptoService.class));
        HttpServletRequest servletRequest = mock(HttpServletRequest.class);

        R<String> result = controller.logOut(null, servletRequest);

        assertEquals(ResponseCode.UNAUTHORIZED, result.getCode());
        verify(sessionService, never()).invalidateUserSessions(anyString());
    }

    @Test
    void refreshRejectsARevokedSessionVersionBeforeIssuingNewTokens() {
        UserAuthService userAuthService = mock(UserAuthService.class);
        RefreshTokenService refreshTokenService = mock(RefreshTokenService.class);
        SecuritySessionService sessionService = mock(SecuritySessionService.class);
        AuthController controller = new AuthController(userAuthService, mock(CaptchaService.class),
                mock(TokenBlackListService.class), mock(PermissionAuthService.class),
                mock(RoleService.class), refreshTokenService, sessionService, mock(LoginCryptoService.class));
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("refresh-old");
        when(refreshTokenService.consume("refresh-old"))
                .thenReturn(new RefreshTokenIdentity("user-1", 2L));
        when(sessionService.isCurrentVersion("user-1", 2L)).thenReturn(false);

        R<?> result = controller.refreshToken(request);

        assertEquals(ResponseCode.UNAUTHORIZED, result.getCode());
        verify(userAuthService, never()).selectById(any());
        verify(refreshTokenService, never()).create(any(), anyLong());
    }

    @Test
    void ordinaryRequestRejectsDisabledUsersBeforeAuthorization() {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        UserAuthService userAuthService = mock(UserAuthService.class);
        SecuritySessionService sessionService = mock(SecuritySessionService.class);
        SaTokenConfigure configure = new SaTokenConfigure(
                redisTemplate, userAuthService, sessionService);
        when(userAuthService.selectById("user-1"))
                .thenReturn(new UserEntity().setId("user-1").setStatus(0));

        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(StpUtil::getLoginIdAsString).thenReturn("user-1");
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> ReflectionTestUtils.invokeMethod(configure, "checkUserSecurityState"));
            assertEquals(ResponseCode.TOKEN_INVALID, exception.getCode());
        }
    }

    @Test
    void refreshTokenPayloadRequiresAndPreservesSessionVersion() {
        RefreshTokenIdentity identity = new RefreshTokenIdentity("user:with-colon", 7L);

        assertEquals(identity, RefreshTokenIdentity.fromStorageValue(identity.toStorageValue()));
        assertThrows(IllegalArgumentException.class,
                () -> RefreshTokenIdentity.fromStorageValue("legacy-user-id"));
    }

    @Test
    void passwordDtosEnforceOneUnifiedPolicy() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        UserDTO user = new UserDTO();
        user.setUsername("tester");
        user.setNickname("测试用户");
        user.setPassword("123456");

        assertTrue(validator.validate(user).stream()
                .anyMatch(violation -> "password".equals(violation.getPropertyPath().toString())));

        user.setPassword("StrongPwd8");
        assertFalse(validator.validate(user).stream()
                .anyMatch(violation -> "password".equals(violation.getPropertyPath().toString())));

        PasswordChangeRequest request = new PasswordChangeRequest();
        request.setOldPassword("OldPwd123");
        request.setNewPassword("weakpwd8");
        request.setConfirmPassword("weakpwd8");
        assertTrue(validator.validate(request).stream()
                .anyMatch(violation -> "newPassword".equals(violation.getPropertyPath().toString())));

        PasswordResetRequest resetRequest = new PasswordResetRequest();
        resetRequest.setNewPassword("123456");
        assertTrue(validator.validate(resetRequest).stream()
                .anyMatch(violation -> "newPassword".equals(violation.getPropertyPath().toString())));
    }

    @Test
    void clearingRolePermissionsAlsoEvictsAffectedUsers() {
        UserRoleMapper userRoleMapper = mock(UserRoleMapper.class);
        RolePermissionMapper rolePermissionMapper = mock(RolePermissionMapper.class);
        PermissionMapper permissionMapper = mock(PermissionMapper.class);
        RoleMapper roleMapper = mock(RoleMapper.class);
        UserMapper userMapper = mock(UserMapper.class);
        PermissionCacheInvalidator cacheInvalidator = mock(PermissionCacheInvalidator.class);
        RoleServiceImpl service = new RoleServiceImpl(
                userRoleMapper, rolePermissionMapper, permissionMapper, userMapper, cacheInvalidator);
        ReflectionTestUtils.setField(service, "baseMapper", roleMapper);

        when(roleMapper.selectById("role-1")).thenReturn(new RoleEntity().setId("role-1"));
        when(userRoleMapper.selectUserIdsByRoleId("role-1")).thenReturn(List.of("user-1"));

        service.assignRolePermissions("role-1", List.of());

        verify(rolePermissionMapper).delete(any());
        verify(cacheInvalidator).evictUserPermissions(List.of("user-1"));
    }

    @Test
    void cannotClearAdministratorRolePermissions() {
        RoleMapper roleMapper = mock(RoleMapper.class);
        RoleServiceImpl service = new RoleServiceImpl(
                mock(UserRoleMapper.class), mock(RolePermissionMapper.class),
                mock(PermissionMapper.class), mock(UserMapper.class),
                mock(PermissionCacheInvalidator.class));
        ReflectionTestUtils.setField(service, "baseMapper", roleMapper);
        when(roleMapper.selectById("admin-role")).thenReturn(
                new RoleEntity().setId("admin-role").setRoleCode("ROLE_ADMIN"));

        assertThrows(BusinessException.class,
                () -> service.assignRolePermissions("admin-role", List.of()));
    }

    @Test
    void assigningUserRolesAlwaysEvictsThatUsersPermissionCache() {
        UserRoleMapper userRoleMapper = mock(UserRoleMapper.class);
        RolePermissionMapper rolePermissionMapper = mock(RolePermissionMapper.class);
        PermissionMapper permissionMapper = mock(PermissionMapper.class);
        UserMapper userMapper = mock(UserMapper.class);
        PermissionCacheInvalidator cacheInvalidator = mock(PermissionCacheInvalidator.class);
        RoleServiceImpl service = new RoleServiceImpl(
                userRoleMapper, rolePermissionMapper, permissionMapper, userMapper, cacheInvalidator);

        service.assignUserRoles("user-1", List.of());

        verify(userRoleMapper).delete(any());
        verify(cacheInvalidator).evictUserPermissions("user-1");
    }

    @Test
    void updatingPermissionEvictsAuthorizationCaches() {
        PermissionMapper permissionMapper = mock(PermissionMapper.class);
        RoleMapper roleMapper = mock(RoleMapper.class);
        RolePermissionMapper rolePermissionMapper = mock(RolePermissionMapper.class);
        PermissionCacheInvalidator cacheInvalidator = mock(PermissionCacheInvalidator.class);
        PermissionServiceImpl service = new PermissionServiceImpl(
                permissionMapper, roleMapper, rolePermissionMapper, cacheInvalidator);
        ReflectionTestUtils.setField(service, "baseMapper", permissionMapper);
        PermissionEntity permission = new PermissionEntity().setId("permission-1");
        when(permissionMapper.selectById("permission-1")).thenReturn(permission);
        when(permissionMapper.selectCount(any())).thenReturn(0L);
        when(permissionMapper.updateById(permission)).thenReturn(1);
        PermissionDTO dto = new PermissionDTO();
        dto.setId("permission-1");
        dto.setPermissionName("更新后的权限");
        dto.setPermissionCode("system:test:updated");

        assertTrue(service.updatePermission(dto));

        verify(cacheInvalidator).evictAllPermissions();
    }

    @Test
    void missingTargetUserDoesNotCrashAdminEditCheck() {
        PermissionService permissionService = mock(PermissionService.class);
        RoleService roleService = mock(RoleService.class);
        DepartmentService departmentService = mock(DepartmentService.class);
        UserMapper userMapper = mock(UserMapper.class);
        PermissionCacheInvalidator cacheInvalidator = mock(PermissionCacheInvalidator.class);
        SecuritySessionService sessionService = mock(SecuritySessionService.class);
        UserServiceImpl service = new UserServiceImpl(permissionService, roleService,
                departmentService, userMapper, cacheInvalidator, sessionService);
        UserEntity currentUser = new UserEntity().setId("admin-2").setUsername("admin-2");
        when(userMapper.selectById("admin-2")).thenReturn(currentUser);
        when(userMapper.selectById("missing-user")).thenReturn(null);
        when(roleService.getRolesByUserId("admin-2"))
                .thenReturn(List.of(RoleEntity.builder().roleCode("ROLE_ADMIN").build()));

        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(StpUtil::getLoginId).thenReturn("admin-2");
            assertFalse(service.canEditUser("missing-user"));
        }
    }

    @Test
    void passwordUpdateInvalidatesAllExistingSessions() {
        PermissionService permissionService = mock(PermissionService.class);
        RoleService roleService = mock(RoleService.class);
        DepartmentService departmentService = mock(DepartmentService.class);
        UserMapper userMapper = mock(UserMapper.class);
        PermissionCacheInvalidator cacheInvalidator = mock(PermissionCacheInvalidator.class);
        SecuritySessionService sessionService = mock(SecuritySessionService.class);
        UserServiceImpl service = new UserServiceImpl(permissionService, roleService,
                departmentService, userMapper, cacheInvalidator, sessionService);
        ReflectionTestUtils.setField(service, "baseMapper", userMapper);
        UserEntity passwordUpdate = new UserEntity().setId("user-1").setPassword("encoded");
        when(userMapper.updateUserPwdById(passwordUpdate)).thenReturn(1);

        assertEquals(1, service.updateUserPwdById(passwordUpdate));
        verify(sessionService).invalidateUserSessions("user-1");
    }

    @Test
    void userStatusChangeInvalidatesPermissionStateAndAllSessions() {
        PermissionService permissionService = mock(PermissionService.class);
        RoleService roleService = mock(RoleService.class);
        DepartmentService departmentService = mock(DepartmentService.class);
        UserMapper userMapper = mock(UserMapper.class);
        PermissionCacheInvalidator cacheInvalidator = mock(PermissionCacheInvalidator.class);
        SecuritySessionService sessionService = mock(SecuritySessionService.class);
        UserServiceImpl service = new UserServiceImpl(permissionService, roleService,
                departmentService, userMapper, cacheInvalidator, sessionService);
        ReflectionTestUtils.setField(service, "baseMapper", userMapper);
        UserEntity disabledUser = new UserEntity().setId("user-1").setStatus(0);
        when(userMapper.updateById(disabledUser)).thenReturn(1);

        assertTrue(service.updateUserStatus(disabledUser));
        verify(cacheInvalidator).evictUserSecurityState("user-1");
        verify(sessionService).invalidateUserSessions("user-1");
    }

    @Test
    void userAndRoleCompositeWritesDeclareOneTransactionBoundary() throws Exception {
        for (String methodName : List.of(
                "createUserWithRoles", "updateUserWithRoles", "deleteUserWithRoles")) {
            Method method = findUserMutation(methodName);
            Transactional transactional = method.getAnnotation(Transactional.class);
            assertNotNull(transactional, methodName + " 必须声明事务");
            assertTrue(List.of(transactional.rollbackFor()).contains(Exception.class));
        }
    }

    @Test
    void sessionVersionStartsAtZeroAndIncrementsOnInvalidation() {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("security:user:session-version:user-1")).thenReturn(null);
        when(valueOperations.increment("security:user:session-version:user-1")).thenReturn(1L);
        RedisSecuritySessionService service = new RedisSecuritySessionService(redisTemplate);

        assertEquals(0L, service.getCurrentVersion("user-1"));
        service.invalidateUserSessions("user-1");

        verify(valueOperations).increment("security:user:session-version:user-1");
    }

    @Test
    void refreshTokenCanOnlyBeConsumedOnce() {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(anyString(), eq("1"),
                eq(SaTokenConfigure.REFRESH_TOKEN_EXPIRE), eq(TimeUnit.SECONDS)))
                .thenReturn(true)
                .thenReturn(false);
        SaTempRefreshTokenServiceImpl service = new SaTempRefreshTokenServiceImpl(redisTemplate);

        try (MockedStatic<SaTempUtil> saTempUtil = mockStatic(SaTempUtil.class)) {
            saTempUtil.when(() -> SaTempUtil.parseToken("refresh-1")).thenReturn("0:user-1");

            assertEquals(new RefreshTokenIdentity("user-1", 0), service.consume("refresh-1"));
            assertThrows(IllegalArgumentException.class, () -> service.consume("refresh-1"));
            saTempUtil.verify(() -> SaTempUtil.deleteToken("refresh-1"));
        }
    }

    @Test
    void permissionQueryNeverWritesOldDataIntoANewerCacheVersion() {
        PermissionAuthMapper permissionMapper = mock(PermissionAuthMapper.class);
        UserAuthMapper userMapper = mock(UserAuthMapper.class);
        @SuppressWarnings("unchecked")
        RedisTemplate<String, Object> redisTemplate = mock(RedisTemplate.class);
        @SuppressWarnings("unchecked")
        ValueOperations<String, Object> valueOperations = mock(ValueOperations.class);
        PermissionCacheInvalidator cacheInvalidator = mock(PermissionCacheInvalidator.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("security:user:exists:user-1")).thenReturn("true");
        when(cacheInvalidator.getPermissionCacheVersion("user-1"))
                .thenReturn("0-0", "0-1", "0-1", "0-1");
        when(permissionMapper.selectPermissionCodeByUserId("user-1"))
                .thenReturn(List.of("old:permission"), List.of("new:permission"));
        PermissionAuthServiceImpl service = new PermissionAuthServiceImpl(
                permissionMapper, redisTemplate, userMapper, cacheInvalidator);

        assertEquals(List.of("new:permission"), service.listPermissionCodeByUserId("user-1"));

        verify(valueOperations, never()).set(
                eq("security:user:permissions:user-1:0-0"),
                any(), anyLong(), any(TimeUnit.class));
        verify(valueOperations).set(
                eq("security:user:permissions:user-1:0-1"),
                eq(List.of("new:permission")), anyLong(), eq(TimeUnit.MINUTES));
    }

    @Test
    void cannotChangeProtectedAdminIdentityOrRolesThroughGenericUpdate() {
        UserService userService = mock(UserService.class);
        RoleService roleService = mock(RoleService.class);
        UserController controller = new UserController(
                userService, roleService, mock(DepartmentService.class), mock(PasswordEncoder.class));
        UserEntity admin = new UserEntity()
                .setId("admin-id")
                .setUsername("admin")
                .setStatus(1);
        when(userService.canEditUser("admin-id")).thenReturn(true);
        when(userService.getById("admin-id")).thenReturn(admin);
        when(roleService.getRolesByUserId("admin-id")).thenReturn(List.of(
                RoleEntity.builder().id("admin-role").roleCode("ROLE_ADMIN").build()));
        UserUpdateDto request = new UserUpdateDto();
        request.setId("admin-id");
        request.setUsername("renamed-admin");
        request.setNickname("管理员");
        request.setStatus(1);
        request.setRoleIds(List.of("admin-role"));

        R<String> response = controller.update(request);

        assertEquals(ResponseCode.FORBIDDEN, response.getCode());
        verify(userService, never()).updateUserWithRoles(any(), any());
    }

    private Method findUserMutation(String methodName) throws NoSuchMethodException {
        return switch (methodName) {
            case "deleteUserWithRoles" -> UserServiceImpl.class.getMethod(methodName, UserEntity.class);
            default -> UserServiceImpl.class.getMethod(
                    methodName, UserEntity.class, List.class);
        };
    }
}
