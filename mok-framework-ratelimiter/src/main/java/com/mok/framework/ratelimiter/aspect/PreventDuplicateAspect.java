package com.mok.framework.ratelimiter.aspect;

import com.mok.framework.common.BusinessException;
import com.mok.framework.common.utils.LogUtils;
import com.mok.framework.ratelimiter.annotation.PreventDuplicate;
import com.mok.framework.ratelimiter.config.RateLimiterProperties;
import com.mok.framework.ratelimiter.core.impl.RateLimiterServiceImpl;
import com.mok.framework.ratelimiter.util.RateLimitKeyBuilder;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * 防重复提交切面
 * 拦截带 @PreventDuplicate 注解的方法，基于 Redis 原子锁防止短时间内相同请求重复提交
 *
 * @author mok
 * @date 2026/7/16
 */
@Aspect
@Component
public class PreventDuplicateAspect {

    private static final Logger log = LogUtils.getLogger(PreventDuplicateAspect.class);

    private final RateLimiterServiceImpl rateLimiterService;
    private final RateLimiterProperties properties;
    private final RateLimitKeyBuilder keyBuilder;

    public PreventDuplicateAspect(RateLimiterServiceImpl rateLimiterService,
                                   @Qualifier("mok.ratelimiter-com.mok.framework.ratelimiter.config.RateLimiterProperties")
                                   RateLimiterProperties properties,
                                   RateLimitKeyBuilder keyBuilder) {
        this.rateLimiterService = rateLimiterService;
        this.properties = properties;
        this.keyBuilder = keyBuilder;
    }

    @Before("@annotation(preventDuplicate)")
    public void doPreventDuplicate(JoinPoint joinPoint, PreventDuplicate preventDuplicate) {
        if (!properties.isEnabled()) {
            return;
        }

        try {
            String key = keyBuilder.buildDuplicateKey(joinPoint, preventDuplicate);
            int lockTime = preventDuplicate.lockTime();

            boolean allowed = rateLimiterService.checkAndLock(key, lockTime);
            log.info("========== allowed :{}",allowed);
            if (!allowed) {
                String message = preventDuplicate.message();
                if (message == null || message.isEmpty()) {
                    message = properties.getDefaultDuplicateMessage();
                }
                throw new BusinessException(message);
            }

            log.debug("防重复提交通过: key={}", key);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("防重复提交切面错误", e);
        }
    }
}
