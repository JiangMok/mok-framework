package com.mok.framework.auth.config;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import cn.dev33.satoken.filter.SaServletFilter;
import cn.dev33.satoken.jwt.StpLogicJwtForStateless;
import cn.dev33.satoken.router.SaHttpMethod;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpLogic;
import cn.dev33.satoken.stp.StpUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mok.framework.auth.service.UserAuthService;
import com.mok.framework.common.BusinessException;
import com.mok.framework.common.R;
import com.mok.framework.common.constant.ResponseCode;
import com.mok.framework.common.security.SecuritySessionService;
import com.mok.framework.common.utils.LogUtils;
import com.mok.framework.model.entity.UserEntity;
import org.slf4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * sa-token配置类
 */
@Configuration
public class SaTokenConfigure {

    private static final String BLACKLIST_PREFIX = "blacklist:token:";
    public static final Long REFRESH_TOKEN_EXPIRE = 172800L;//秒

    private final Logger log = LogUtils.getLogger(SaTokenConfigure.class);

    private final StringRedisTemplate redisTemplate;
    private final UserAuthService userAuthService;
    private final SecuritySessionService securitySessionService;

    public SaTokenConfigure(StringRedisTemplate redisTemplate,
                            UserAuthService userAuthService,
                            SecuritySessionService securitySessionService) {
        this.redisTemplate = redisTemplate;
        this.userAuthService = userAuthService;
        this.securitySessionService = securitySessionService;
    }

    /**
     * Sa-Token 整合 jwt (Stateless 无状态模式)
     */
    @Bean
    public StpLogic getStpLogicJwt() {
        log.info("========== Sa-Token 开启无状态模式");
        return new StpLogicJwtForStateless();
    }

    /**
     * 注册 Sa-Token 全局过滤器，并在前置函数中检查 Token 黑名单
     */
    @Bean
    public SaServletFilter getSaServletFilter() {
        ObjectMapper objectMapper = new ObjectMapper();
        log.info("========== Sa-Token 开启全局过滤器");
        return new SaServletFilter()
                // 1. 指定拦截与放行路由
                .addInclude("/**")
                .addExclude(
                        "/auth/login",        // 登录接口
                        "/auth/login-challenge", // 一次性登录凭证与公钥
                        "/auth/refresh",      // 刷新 token
                        "/auth/logout",       // 支持使用 refresh token 退出
                        "/captcha/**",        // 验证码接口

                        // Swagger 相关路径 - 全部放行
                        "/swagger-ui/**",     // Swagger UI
                        "/v3/api-docs/**",    // OpenAPI 文档
                        "/swagger-ui.html",   // Swagger UI HTML
                        "/swagger-resources/**",  // Swagger 资源
                        "/webjars/**",        // WebJars
                        "/swagger/**",        // Swagger
                        "/doc.html",          // Knife4j
                        "/favicon.ico",       // 网站图标

                        // 静态资源
                        "/uploads/**",         // 受控公开头像
                        "/static/**",         // 静态资源
                        "/resources/**",      // 资源文件
                        "/css/**",            // CSS
                        "/js/**",             // JavaScript
                        "/images/**",         // 图片

                        // 错误页面
                        "/error",             // 错误处理
                        "/error/**"           // 错误处理
                ) // 按需排除静态资源等

                // 2. 认证函数：执行主要鉴权逻辑
                .setAuth(r -> {
                    SaRouter.match(SaHttpMethod.OPTIONS).free(ignore -> {}).back();
                    StpUtil.checkLogin();
                    checkUserSecurityState();
                })

                // 3. 前置函数：此处检查黑名单，优先级最高
                .setBeforeAuth(r -> {
                    // 获取请求路径（不包含参数）
                    String requestPath = SaHolder.getRequest().getRequestPath();
                    log.debug("Sa-Token 请求路径: {}", requestPath);
                    String token = StpUtil.getTokenValue();
                    if (token != null && Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + token))) {
                        throw new BusinessException(ResponseCode.TOKEN_INVALID, "Token 已失效，请重新登录");
                    }
                })// 4. 统一异常处理（关键！）
                .setError(e -> {
                    R<?> result;
                    int httpStatus;
                    if (e instanceof NotLoginException) {
                        result = R.tokenInvalid();
                        httpStatus = ResponseCode.UNAUTHORIZED;
                    } else if (e instanceof NotPermissionException || e instanceof NotRoleException) {
                        result = R.forbidden("权限不足，无法访问该资源");
                        httpStatus = ResponseCode.FORBIDDEN;
                    } else if (e instanceof BusinessException businessException) {
                        result = R.error(businessException.getCode(), businessException.getMessage());
                        httpStatus = ResponseCode.TOKEN_INVALID.equals(businessException.getCode())
                                ? ResponseCode.UNAUTHORIZED
                                : ResponseCode.BAD_REQUEST;
                    } else {
                        result = R.error("系统错误");
                        httpStatus = ResponseCode.INTERNAL_SERVER_ERROR;
                    }
                    log.warn("Sa-Token 拒绝请求: code={}, message={}", result.getCode(), result.getMsg());
                    try {
                        String json = objectMapper.writeValueAsString(result);
                        SaHolder.getResponse().setStatus(httpStatus);
                        SaHolder.getResponse().setHeader("Content-Type", "application/json;charset=utf-8");
                        return json;
                    } catch (JsonProcessingException ex) {
                        // 降级处理
                        return "{\"code\":500,\"msg\":\"系统错误\"}";
                    }
                });
    }

    private void checkUserSecurityState() {
        String userId = StpUtil.getLoginIdAsString();
        UserEntity userEntity = userAuthService.selectById(userId);
        if (userEntity == null || !Integer.valueOf(1).equals(userEntity.getStatus())
                || Integer.valueOf(1).equals(userEntity.getIsDeleted())) {
            throw new BusinessException(ResponseCode.TOKEN_INVALID, "用户不存在或已被禁用");
        }

        Object versionClaim;
        try {
            versionClaim = StpUtil.getExtra(SecuritySessionService.SESSION_VERSION_CLAIM);
        } catch (RuntimeException exception) {
            throw new BusinessException(ResponseCode.TOKEN_INVALID, "Token 会话版本无效");
        }
        long tokenVersion = parseSessionVersion(versionClaim);
        if (!securitySessionService.isCurrentVersion(userId, tokenVersion)) {
            throw new BusinessException(ResponseCode.TOKEN_INVALID, "Token 已失效，请重新登录");
        }
    }

    private long parseSessionVersion(Object versionClaim) {
        if (versionClaim == null) {
            throw new BusinessException(ResponseCode.TOKEN_INVALID, "Token 会话版本无效");
        }
        try {
            return Long.parseLong(versionClaim.toString());
        } catch (NumberFormatException exception) {
            throw new BusinessException(ResponseCode.TOKEN_INVALID, "Token 会话版本无效");
        }
    }

}
