package com.mok.framework.auth.config;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.filter.SaServletFilter;
import cn.dev33.satoken.jwt.StpLogicJwtForStateless;
import cn.dev33.satoken.stp.StpLogic;
import cn.dev33.satoken.stp.StpUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mok.framework.common.BusinessException;
import com.mok.framework.common.R;
import com.mok.framework.common.utils.LogUtils;
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
    public static final Long REFRESH_TOKEN_EXPIRE = 60L;//秒

    private final Logger log = LogUtils.getLogger(SaTokenConfigure.class);

    private final StringRedisTemplate redisTemplate;

    public SaTokenConfigure(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
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
                        "/api/debug/**",          // 调试接口
                        "/auth/login",        // 登录接口
                        "/auth/logout",       // 退出登录
                        "/auth/refresh",      // 刷新token
                        "/captcha/**",        // 验证码接口
                        "/captcha/generate",        // 验证码接口

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
                        "/uploads/**",         // 静态资源
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
                    // 可以留空，鉴权工作可继续由 @SaCheckLogin 注解完成
                    // 或在此处统一执行 StpUtil.checkLogin() 进行路由级鉴权
                })

                // 3. 前置函数：此处检查黑名单，优先级最高
                .setBeforeAuth(r -> {
                    // 2. 获取请求路径（包含ContextPath，不包含参数）
                    String requestPath = SaHolder.getRequest().getRequestPath();
                    log.info("===================================== 请求路径 : {}", requestPath);
                    String token = StpUtil.getTokenValue();
                    if (token != null && redisTemplate.hasKey(BLACKLIST_PREFIX + token)) {
                        throw new BusinessException("Token 已失效，请重新登录");
                    }
                })// 4. 统一异常处理（关键！）
                .setError(e -> {
                    R<?> result;
                    if (e instanceof BusinessException) {
                        result = R.error(e.getMessage());
                    } else {
                        result = R.error("系统错误");
                    }
                    log.info("===================================== 系统拦截 : {}", result);
                    try {
                        String json = objectMapper.writeValueAsString(result);
                        SaHolder.getResponse().setHeader("Content-Type", "application/json;charset=utf-8");
                        return json;
                    } catch (JsonProcessingException ex) {
                        // 降级处理
                        return "{\"code\":500,\"msg\":\"系统错误\"}";
                    }
                });
    }

}