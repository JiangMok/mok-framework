package com.mok.framework.auth.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.temp.SaTempUtil;
import com.mok.framework.auth.config.SaTokenConfigure;
import com.mok.framework.auth.service.PermissionAuthService;
import com.mok.framework.auth.service.TokenBlackListService;
import com.mok.framework.auth.service.UserAuthService;
import com.mok.framework.base.service.RoleService;
import com.mok.framework.captcha.service.CaptchaService;
import com.mok.framework.common.R;
import com.mok.framework.common.constant.ResponseCode;
import top.jiangmok.operationlog.annotation.OperationLog;
import top.jiangmok.operationlog.enums.BusinessType;
import com.mok.framework.common.utils.LogUtils;
import com.mok.framework.model.dto.LoginRequest;
import com.mok.framework.model.dto.LoginResponse;
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

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final PermissionAuthService permissionAuthService;

    public AuthController(UserAuthService userService,
                          CaptchaService captchaService,
                          TokenBlackListService tokenBlackListService,
                          PermissionAuthService permissionAuthService,
                          RoleService roleService) {
        this.userService = userService;
        this.captchaService = captchaService;
        this.tokenBlackListService = tokenBlackListService;
        this.permissionAuthService = permissionAuthService;
        this.roleService = roleService;
    }

    /**
     * 登录
     *
     * @param loginRequest
     * @return
     */
    @OperationLog(title = "用户登录", businessType = BusinessType.LOGIN)
    @RateLimit(scope = RateLimitScope.IP, limit = 5, message = "登录请求过于频繁，请稍后重试")
    @PreventDuplicate(lockTime = 5, message = "请勿重复提交登录请求")
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

        StpUtil.login(userEntity.getId());
        // 缓存用户权限
        permissionAuthService.listPermissionCodeByUserId(userEntity.getId());
        //生成访问令牌
        //  使用 JwtTokenProvider 生成 JWT 令牌
        String token = StpUtil.getTokenValue();

        //刷新访问令牌
        //  刷新令牌用于访问令牌过期后获取新的访问令牌
        String refreshToken = SaTempUtil.createToken(userEntity.getId(), SaTokenConfigure.REFRESH_TOKEN_EXPIRE);

        //构建登录响应
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setToken(token);
        loginResponse.setAvatar(userEntity.getAvatar());
        loginResponse.setRefreshToken(refreshToken);
        //设定过期时间为两个小时(毫秒) >>> 7200000L
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
    @PreventDuplicate(lockTime = 3, message = "请勿重复刷新Token")
    @PostMapping("/refresh")
    @SaIgnore
    public R<LoginResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        // 解析 refreshToken 获取用户id
        Object userId;
        try {
            userId = SaTempUtil.parseToken(request.getRefreshToken());
        } catch (RuntimeException exception) {
            log.warn("刷新令牌解析失败: {}", exception.getMessage());
            return R.error(ResponseCode.UNAUTHORIZED, "刷新令牌无效或已过期");
        }
        if (userId == null) {
            return R.error(ResponseCode.UNAUTHORIZED, "刷新令牌无效或已过期");
        }

        UserEntity userEntity = userService.selectById(userId.toString());
        if (userEntity == null || !Integer.valueOf(1).equals(userEntity.getStatus())
                || Integer.valueOf(1).equals(userEntity.getIsDeleted())) {
            return R.error(ResponseCode.USER_DISABLED, "用户不存在或已被禁用");
        }

        // 为有效用户重新登录，生成新的 accessToken
        StpUtil.login(userId);
        LoginResponse response = new LoginResponse();
        response.setToken(StpUtil.getTokenValue());
        String newRefreshToken = SaTempUtil.createToken(userEntity.getId(), SaTokenConfigure.REFRESH_TOKEN_EXPIRE);
        response.setRefreshToken(newRefreshToken);
        response.setExpiresIn(StpUtil.getTokenTimeout());
        response.setUsername(userEntity.getUsername());
        response.setNickname(userEntity.getNickname());
        response.setUserId(userEntity.getId());
        response.setAvatar(userEntity.getAvatar());
        response.setRoles(getRoleCodes(userEntity.getId()));
        if (!newRefreshToken.equals(request.getRefreshToken())) {
            SaTempUtil.deleteToken(request.getRefreshToken());
        }
        return R.ok(response);
    }

    /**
     * 退出登录
     *
     * @return
     */
    @OperationLog(title = "退出登录", businessType = BusinessType.LOGOUT)
    @PostMapping("/logout")
    @SaCheckLogin
    public R<String> logOut(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            return R.error("token 格式不正确");
        }
        long ttl = StpUtil.getTokenTimeout();
        tokenBlackListService.addToBlacklist(bearerToken.substring(7), ttl);
        StpUtil.logout(); // 最后清除本地会话
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

}
