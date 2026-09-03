package com.mok.framework.auth.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import com.mok.framework.auth.service.PermissionAuthService;
import com.mok.framework.auth.service.RefreshTokenIdentity;
import com.mok.framework.auth.service.RefreshTokenService;
import com.mok.framework.auth.service.TokenBlackListService;
import com.mok.framework.auth.service.UserAuthService;
import com.mok.framework.base.service.RoleService;
import com.mok.framework.captcha.service.CaptchaService;
import com.mok.framework.common.R;
import com.mok.framework.common.constant.ResponseCode;
import com.mok.framework.common.security.SecuritySessionService;
import top.jiangmok.operationlog.annotation.OperationLog;
import top.jiangmok.operationlog.enums.BusinessType;
import com.mok.framework.common.utils.LogUtils;
import com.mok.framework.model.dto.LoginRequest;
import com.mok.framework.model.dto.LoginResponse;
import com.mok.framework.model.dto.LogoutRequest;
import com.mok.framework.model.dto.RefreshTokenRequest;
import com.mok.framework.model.entity.RoleEntity;
import com.mok.framework.model.entity.UserEntity;
import top.jiangmok.ratelimiter.annotation.PreventDuplicate;
import top.jiangmok.ratelimiter.annotation.RateLimit;
import top.jiangmok.ratelimiter.enums.RateLimitScope;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;


/**
 * 用户认证控制器
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger log = LogUtils.getLogger(AuthController.class);

    private final UserAuthService userService;
    private final CaptchaService captchaService;
    private final TokenBlackListService tokenBlackListService;
    private final RoleService roleService;
    private final RefreshTokenService refreshTokenService;
    private final SecuritySessionService securitySessionService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final PermissionAuthService permissionAuthService;

    public AuthController(UserAuthService userService,
                          CaptchaService captchaService,
                          TokenBlackListService tokenBlackListService,
                          PermissionAuthService permissionAuthService,
                          RoleService roleService,
                          RefreshTokenService refreshTokenService,
                          SecuritySessionService securitySessionService) {
        this.userService = userService;
        this.captchaService = captchaService;
        this.tokenBlackListService = tokenBlackListService;
        this.permissionAuthService = permissionAuthService;
        this.roleService = roleService;
        this.refreshTokenService = refreshTokenService;
        this.securitySessionService = securitySessionService;
    }

    /**
     * 登录
     *
     * @param loginRequest
     * @return
     */
    @OperationLog(title = "用户登录", businessType = BusinessType.LOGIN)
    @RateLimit(scope = RateLimitScope.IP, limit = 5, message = "登录请求过于频繁，请稍后重试")
    @PreventDuplicate(
            key = "#loginRequest.username",
            lockTime = 5,
            message = "请勿重复提交登录请求")
    @PostMapping("/login")
    @SaIgnore
    public R<LoginResponse> loadUser(@Valid @RequestBody LoginRequest loginRequest) {
        //验证验证码
        if (!captchaService.validateCaptcha(loginRequest.getCaptchaKey(), loginRequest.getCaptcha())) {
            return R.error(1002, "验证码错误或已过期");
        }

        UserEntity userEntity = userService.getByUserName(loginRequest.getUsername());

        if (userEntity == null) {
            return R.passwordError();
        }

        //判断密码
        if (!passwordEncoder.matches(loginRequest.getPassword(), userEntity.getPassword())) {
            return R.passwordError();
        }

        long sessionVersion = securitySessionService.getCurrentVersion(userEntity.getId());
        loginWithVersion(userEntity.getId(), sessionVersion);
        // 缓存用户权限
        permissionAuthService.listPermissionCodeByUserId(userEntity.getId());
        //生成访问令牌
        //  使用 JwtTokenProvider 生成 JWT 令牌
        String token = StpUtil.getTokenValue();

        //刷新访问令牌
        //  刷新令牌用于访问令牌过期后获取新的访问令牌
        String refreshToken = refreshTokenService.create(userEntity.getId(), sessionVersion);

        //构建登录响应
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setToken(token);
        loginResponse.setAvatar(userEntity.getAvatar());
        loginResponse.setRefreshToken(refreshToken);
        // Sa-Token 返回剩余有效期，单位为秒
        loginResponse.setExpiresIn(StpUtil.getTokenTimeout());
        loginResponse.setUsername(userEntity.getUsername());
        loginResponse.setNickname(userEntity.getNickname());
        loginResponse.setUserId(userEntity.getId());
        loginResponse.setRoles(getRoleCodes(userEntity.getId()));

        return R.ok(loginResponse);
    }

    /**
     * 刷新token
     *
     * @param request
     * @return
     */
    @OperationLog(title = "刷新token", businessType = BusinessType.UPDATE)
    @RateLimit(scope = RateLimitScope.IP, limit = 10, message = "刷新过于频繁，请稍后重试")
    @PreventDuplicate(
            key = "#request.fingerprint",
            lockTime = 3,
            message = "请勿重复刷新Token")
    @PostMapping("/refresh")
    @SaIgnore
    public R<LoginResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        // 解析 refreshToken 获取用户与会话版本
        RefreshTokenIdentity refreshIdentity;
        try {
            refreshIdentity = refreshTokenService.consume(request.getRefreshToken());
        } catch (RuntimeException exception) {
            log.warn("刷新令牌解析失败: {}", exception.getMessage());
            return R.error(ResponseCode.UNAUTHORIZED, "刷新令牌无效或已过期");
        }
        if (!securitySessionService.isCurrentVersion(
                refreshIdentity.userId(), refreshIdentity.sessionVersion())) {
            return R.error(ResponseCode.UNAUTHORIZED, "刷新令牌无效或已过期");
        }

        UserEntity userEntity = userService.selectById(refreshIdentity.userId());
        if (userEntity == null || !Integer.valueOf(1).equals(userEntity.getStatus())
                || Integer.valueOf(1).equals(userEntity.getIsDeleted())) {
            return R.error(ResponseCode.USER_DISABLED, "用户不存在或已被禁用");
        }

        // 为有效用户重新登录，生成新的 accessToken
        loginWithVersion(userEntity.getId(), refreshIdentity.sessionVersion());
        LoginResponse response = new LoginResponse();
        response.setToken(StpUtil.getTokenValue());
        String newRefreshToken = refreshTokenService.create(
                userEntity.getId(), refreshIdentity.sessionVersion());
        response.setRefreshToken(newRefreshToken);
        response.setExpiresIn(StpUtil.getTokenTimeout());
        response.setUsername(userEntity.getUsername());
        response.setNickname(userEntity.getNickname());
        response.setUserId(userEntity.getId());
        response.setAvatar(userEntity.getAvatar());
        response.setRoles(getRoleCodes(userEntity.getId()));
        return R.ok(response);
    }

    /**
     * 退出登录
     *
     * @return
     */
    @OperationLog(title = "退出登录", businessType = BusinessType.LOGOUT)
    @RateLimit(scope = RateLimitScope.IP, limit = 20, message = "退出请求过于频繁，请稍后重试")
    @PostMapping("/logout")
    @SaIgnore
    public R<String> logOut(@RequestBody(required = false) LogoutRequest logoutRequest,
                            HttpServletRequest request) {
        AccessTokenIdentity accessIdentity = resolveAccessToken(request);
        RefreshTokenIdentity refreshIdentity = consumeRefreshToken(logoutRequest);

        if (accessIdentity == null && refreshIdentity == null) {
            return R.error(ResponseCode.UNAUTHORIZED, "未提供有效的访问令牌或刷新令牌");
        }

        if (accessIdentity != null && refreshIdentity != null
                && !accessIdentity.userId().equals(refreshIdentity.userId())) {
            log.warn("退出时访问令牌与刷新令牌所属用户不一致");
            return R.error(ResponseCode.UNAUTHORIZED, "访问令牌与刷新令牌不属于同一用户");
        }

        String currentUserId = refreshIdentity != null
                ? refreshIdentity.userId()
                : accessIdentity.userId();
        if (!isActiveUser(currentUserId)) {
            return R.error(ResponseCode.UNAUTHORIZED, "用户不存在或已被禁用");
        }

        if (accessIdentity != null) {
            tokenBlackListService.addToBlacklist(
                    accessIdentity.token(), accessIdentity.ttl());
        }

        // 当前架构没有设备级 sessionId，退出时递增用户会话版本，确保并发刷新以及
        // 其它遗留 refresh token 都无法再签发新的 access token。
        securitySessionService.invalidateUserSessions(currentUserId);
        return R.ok("退出成功");
    }

    private List<String> getRoleCodes(String userId) {
        List<String> roles = roleService.getRolesByUserId(userId).stream()
                .map(RoleEntity::getRoleCode)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        return roles.contains("ROLE_GUEST") ? List.of("ROLE_GUEST") : roles;
    }

    private void loginWithVersion(String userId, long sessionVersion) {
        SaLoginParameter parameter = SaLoginParameter.create()
                .setExtra(SecuritySessionService.SESSION_VERSION_CLAIM, sessionVersion);
        StpUtil.login(userId, parameter);
    }

    private AccessTokenIdentity resolveAccessToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            return null;
        }
        String accessToken = bearerToken.substring(7).trim();
        if (accessToken.isEmpty() || tokenBlackListService.isBlacklisted(accessToken)) {
            return null;
        }

        try {
            Object loginId = StpUtil.getLoginIdByToken(accessToken);
            long ttl = StpUtil.getTokenTimeout(accessToken);
            Long sessionVersion = parseSessionVersion(StpUtil.getExtra(
                    accessToken, SecuritySessionService.SESSION_VERSION_CLAIM));
            if (loginId == null || sessionVersion == null
                    || (ttl <= 0 && ttl != -1)
                    || !securitySessionService.isCurrentVersion(
                            loginId.toString(), sessionVersion)) {
                return null;
            }
            return new AccessTokenIdentity(loginId.toString(), accessToken, ttl);
        } catch (RuntimeException exception) {
            log.debug("退出时访问令牌已失效: {}", exception.getMessage());
            return null;
        }
    }

    private RefreshTokenIdentity consumeRefreshToken(LogoutRequest logoutRequest) {
        if (logoutRequest == null || logoutRequest.getRefreshToken() == null
                || logoutRequest.getRefreshToken().isBlank()) {
            return null;
        }
        try {
            RefreshTokenIdentity identity = refreshTokenService.consume(
                    logoutRequest.getRefreshToken());
            return securitySessionService.isCurrentVersion(
                    identity.userId(), identity.sessionVersion()) ? identity : null;
        } catch (RuntimeException exception) {
            log.debug("退出时刷新令牌已失效: {}", exception.getMessage());
            return null;
        }
    }

    private boolean isActiveUser(String userId) {
        UserEntity userEntity = userService.selectById(userId);
        return userEntity != null
                && Integer.valueOf(1).equals(userEntity.getStatus())
                && !Integer.valueOf(1).equals(userEntity.getIsDeleted());
    }

    private Long parseSessionVersion(Object versionClaim) {
        if (versionClaim == null) {
            return null;
        }
        try {
            return Long.parseLong(versionClaim.toString());
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private record AccessTokenIdentity(String userId, String token, long ttl) {
    }

}
