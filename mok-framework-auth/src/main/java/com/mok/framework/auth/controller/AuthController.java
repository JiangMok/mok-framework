package com.mok.framework.auth.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.temp.SaTempUtil;
import com.mok.framework.auth.config.SaTokenConfigure;
import com.mok.framework.auth.service.PermissionAuthService;
import com.mok.framework.auth.service.TokenBlackListService;
import com.mok.framework.auth.service.UserAuthService;
import com.mok.framework.captcha.service.CaptchaService;
import com.mok.framework.common.R;
import com.mok.framework.common.annotation.OperationLog;
import com.mok.framework.common.enums.BusinessType;
import com.mok.framework.common.utils.LogUtils;
import com.mok.framework.model.dto.LoginRequest;
import com.mok.framework.model.dto.LoginResponse;
import com.mok.framework.model.entity.UserEntity;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;


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

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final PermissionAuthService permissionAuthService;

    public AuthController(UserAuthService userService,
                          CaptchaService captchaService,
                          TokenBlackListService tokenBlackListService,
                          PermissionAuthService permissionAuthService) {
        this.userService = userService;
        this.captchaService = captchaService;
        this.tokenBlackListService = tokenBlackListService;
        this.permissionAuthService = permissionAuthService;
    }

    /**
     * 登录
     *
     * @param loginRequest
     * @return
     */
    @OperationLog(title = "用户登录", businessType = BusinessType.LOGIN)
    @PostMapping("/login")
    public R<LoginResponse> loadUser(@RequestBody LoginRequest loginRequest) {
        //验证验证码
        if (!captchaService.validateCaptcha(loginRequest.getCaptchaKey(), loginRequest.getCaptcha())) {
            return R.error(1002, "验证码错误或已过期");
        }

        UserEntity userEntity = userService.getByUserName(loginRequest.getUsername());

        if (userEntity == null) {
            return R.userNotFound();
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

        return R.ok(loginResponse);
    }

    @RequestMapping("isLogin")
    public String isLogin() {
        return "当前会话是否登录：" + StpUtil.isLogin();
    }

    /**
     * 刷新token
     *
     * @param refreshToken
     * @return
     */
    @OperationLog(title = "刷新token", businessType = BusinessType.UPDATE)
    @PostMapping("/refresh")
    public R<LoginResponse> refreshToken(@RequestParam String refreshToken) {
        // 解析 refreshToken 获取用户id
        Object userId = SaTempUtil.parseToken(refreshToken);
        if (userId == null) {
            return R.error(401, "刷新令牌无效或已过期");
        }

        // 为用户重新登录，生成新的 accessToken
        StpUtil.login(userId);
        UserEntity userEntity = userService.selectById(userId.toString());
        LoginResponse response = new LoginResponse();
        response.setToken(StpUtil.getTokenValue());
        String newRefreshToken = SaTempUtil.createToken(userEntity.getId(), SaTokenConfigure.REFRESH_TOKEN_EXPIRE);
        response.setRefreshToken(newRefreshToken);
        response.setExpiresIn(StpUtil.getTokenTimeout());
        response.setUsername(userEntity.getUsername());
        response.setNickname(userEntity.getNickname());
        response.setUserId(userEntity.getId());
        return R.ok(response);
    }

    /**
     * 退出登录
     *
     * @return
     */
    @OperationLog(title = "退出登录", businessType = BusinessType.LOGOUT)
    @GetMapping("/logout")
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

}