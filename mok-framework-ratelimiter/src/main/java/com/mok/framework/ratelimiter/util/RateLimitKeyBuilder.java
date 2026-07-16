package com.mok.framework.ratelimiter.util;

import cn.dev33.satoken.stp.StpUtil;
import com.mok.framework.ratelimiter.annotation.PreventDuplicate;
import com.mok.framework.ratelimiter.annotation.RateLimit;
import com.mok.framework.ratelimiter.config.RateLimiterProperties;
import com.mok.framework.ratelimiter.expression.SpelExpressionEvaluator;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;


/**
 * 限流 Key构建器
 * 作用：根据注解、连接点、请求上下文等信息构建唯一的限流key
 * @author mok
 */
// 注册为 spring bean
@Component
public class RateLimitKeyBuilder {

    /**
     * 添加日志
     */
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RateLimitKeyBuilder.class);
    // 配置属性
    private final RateLimiterProperties properties;
    // SpEL 解析器
    private final SpelExpressionEvaluator spelEvaluator;

    // 构造函数注入
    public RateLimitKeyBuilder(@Qualifier("mok.ratelimiter-com.mok.framework.ratelimiter.config.RateLimiterProperties")
                               RateLimiterProperties properties,
                               SpelExpressionEvaluator spelEvaluator) {
        this.properties = properties;
        this.spelEvaluator = spelEvaluator;
    }
    /**
     * 构建限流 Key
     * @param joinPoint 连接点，可获取方法信息
     * @param rateLimit 限流注解
     * @return 完整的限流 key字符串
     */
    public String buildRateLimitKey(JoinPoint joinPoint, RateLimit rateLimit) {
        // 添加全局前缀
        StringBuilder keyBuilder = new StringBuilder(properties.getRedisKeyPrefix());

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        // 使用简单类名
        String className = method.getDeclaringClass().getSimpleName();
        String methodName = method.getName();
        // 获取请求的 URI
        String uri = getRequestUri();

        log.info("构建限流Key - 类名: {}, 方法名: {}, URI: {}", className, methodName, uri);

        // 根据作用域添加不同的前缀
        switch (rateLimit.scope()) {
            case API:
                keyBuilder.append("api:");
                break;
            case USER:
                // 获取当前用户 ID
                String userId = StpUtil.getLoginId().toString();
                keyBuilder.append("user:").append(userId).append(":");
                log.info("用户级别限流 - 用户ID: {}", userId);
                break;
            case IP:
                // 获取当前用户 IP
                String ip = getClientIp();
                keyBuilder.append("ip:").append(ip).append(":");
                log.info("IP级别限流 - 客户端IP: {}", ip);
                break;
            case GLOBAL:
                keyBuilder.append("global:");
                break;
        }

        // 添加类名和方法名
        keyBuilder.append(className).append(".").append(methodName);
        if (uri != null && !uri.equals("unknown")) {
            //添加 URI
            keyBuilder.append(":").append(uri);
        }

        // 添加自定义Key（支持SpEL表达式）
        String customKey = rateLimit.key();
        if (customKey != null && !customKey.isEmpty()) {
            try {
                String evaluatedKey = spelEvaluator.evaluate(customKey, joinPoint);
                if (evaluatedKey != null && !evaluatedKey.isEmpty()) {
                    keyBuilder.append(":").append(evaluatedKey);
                }
            } catch (Exception e) {
                log.warn("解析SpEL表达式失败: {}", customKey, e);
            }
        }

        String finalKey = keyBuilder.toString();
        log.info("生成的限流Key: {}", finalKey);

        return finalKey;
    }

    /**
     * 构建防重复提交 Key
     * @param joinPoint 连接点
     * @param preventDuplicate 防重复提交注解
     * @return 完整的防重复提交 key
     */
    public String buildDuplicateKey(JoinPoint joinPoint, PreventDuplicate preventDuplicate) {
        // 添加全局前缀
        StringBuilder keyBuilder = new StringBuilder(properties.getDuplicateKeyPrefix());

        // 添加用户信息
//        keyBuilder.append("user:").append(StpUtil.getLoginId().toString()).append(":");
        keyBuilder.append("user:").append("test123").append(":");

        // 添加操作类型
        keyBuilder.append("type:").append(preventDuplicate.type().getCode()).append(":");

        // 添加方法信息(全限定类名+方法名)
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        keyBuilder.append("method:").append(method.getDeclaringClass().getName())
                  .append(".").append(method.getName()).append(":");

        // 处理自定义Key（支持SpEL表达式）
        String customKey = preventDuplicate.key();
        if (customKey != null && !customKey.isEmpty()) {
            String evaluatedKey = spelEvaluator.evaluate(customKey, joinPoint);
            keyBuilder.append("custom:").append(evaluatedKey).append(":");
        }

        return keyBuilder.toString();
    }

    /**
     * 获取客户端 IP
     * 考虑代理转发的情况，从多个Header中获取真实IP
     */
    private String getClientIp() {
        try {
            HttpServletRequest request = getHttpServletRequest();
            if (request == null) {
                log.warn("无法获取HttpServletRequest，返回默认IP");
                // 默认 IP
                return "127.0.0.1";
            }

            // 尝试从多个Header 获取IP
            String[] headers = {
                    "X-Forwarded-For",
                    "Proxy-Client-IP",
                    "WL-Proxy-Client-IP",
                    "HTTP_CLIENT_IP",
                    "HTTP_X_FORWARDED_FOR",
                    "X-Real-IP"
            };

            String ip = null;
            for (String header : headers) {
                ip = request.getHeader(header);
                if (isValidIp(ip)) {
                    break;
                }
            }

            // 如果从Header获取不到，使用RemoteAddr
            if (!isValidIp(ip)) {
                ip = request.getRemoteAddr();
            }

            // 处理多个IP的情况（例如X-Forwarded-For可能包含多个逗号分隔的IP）
            if (ip != null && ip.contains(",")) {
                ip = ip.split(",")[0].trim();
            }

            // 特殊IP 处理
            if ("0:0:0:0:0:0:0:1".equals(ip) || "127.0.0.1".equals(ip)) {
                ip = "127.0.0.1";
            }

            log.debug("获取到的客户端IP: {}", ip);
            return ip;
        } catch (Exception e) {
            log.error("获取客户端IP失败", e);
            // 默认 IP
            return "127.0.0.1";
        }
    }


    /**
     * 验证IP 是否有效
     * @param ip IP 字符串
     * @return true 表示有效
     */
    private boolean isValidIp(String ip) {
        return ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip);
    }
    /**
     * 获取请求 URI
     */
    private String getRequestUri() {
        try {
            HttpServletRequest request = getHttpServletRequest();
            return request != null ? request.getRequestURI() : "unknown";
        } catch (Exception e) {
            log.error("获取请求URI失败", e);
            return "unknown";
        }
    }

    /**
     * 获取HttpServletRequest
     * 通过RequestContextHolder获取当前请求的RequestAttributes
     */
    private HttpServletRequest getHttpServletRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes)
                    RequestContextHolder.getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            log.error("获取HttpServletRequest失败", e);
            return null;
        }
    }

    /**
     * 计算参数哈希
     * 将参数对象拼接后计算MD5，用于区分不同参数的请求
     */
    private String hashParams(Object[] args) {
        if (args == null || args.length == 0) {
            return "empty";
        }

        try {
            StringBuilder paramsStr = new StringBuilder();
            for (Object arg : args) {
                if (arg != null) {
                    paramsStr.append(arg.toString());
                }
            }

            return DigestUtils.md5DigestAsHex(
                paramsStr.toString().getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            return "error";
        }
    }
}