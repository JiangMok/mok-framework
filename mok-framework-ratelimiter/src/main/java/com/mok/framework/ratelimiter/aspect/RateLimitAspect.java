package com.mok.framework.ratelimiter.aspect;

import com.mok.framework.common.BusinessException;
import com.mok.framework.ratelimiter.annotation.RateLimit;
import com.mok.framework.ratelimiter.config.RateLimiterProperties;
import com.mok.framework.ratelimiter.expression.SpelExpressionEvaluator;
import com.mok.framework.ratelimiter.model.RateLimitContext;
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
 * @description:  限流切面
 * @author: mok
 * @date: 2026/2/25 14:24
**/
//这是一个切面类
@Aspect
//将这个类注册成 Spring Bean
@Component
//作用：通过AOP拦截带有@RateLimit注解的方法，执行限流逻辑
public class RateLimitAspect {
    
    private static final Logger logger = LoggerFactory.getLogger(RateLimitAspect.class);
    //限流服务实现
    private final RateLimiterServiceImpl rateLimiterService;
    //限流配置属性
    private final RateLimiterProperties properties;
    //限流 key 构建器
    private final RateLimitKeyBuilder keyBuilder;
    //SpEL 表达式解析器
    private final SpelExpressionEvaluator spelEvaluator;

    //构造函数注入
    public RateLimitAspect(RateLimiterServiceImpl rateLimiterService,
                           @Qualifier("mok.ratelimiter-com.mok.framework.ratelimiter.config.RateLimiterProperties")
                           RateLimiterProperties properties,
                           RateLimitKeyBuilder keyBuilder,
                           SpelExpressionEvaluator spelEvaluator) {
        this.rateLimiterService = rateLimiterService;
        this.properties = properties;
        this.keyBuilder = keyBuilder;
        this.spelEvaluator = spelEvaluator;
    }

    /**
     * 限流切点
     */
    // @Before -> 前置通知,在方法执行前执行
    // @@annotation(rateLimitAnnotation) -> 匹配所有带有 @RateLimit 注解的方法,并将注解对象注入到rateLimitAnnotation
    @Before("@annotation(rateLimitAnnotation)")
    public void doRateLimit(JoinPoint joinPoint, RateLimit rateLimitAnnotation) {
        // 判断是否启用限流模块以及当前注解是否启用,如果未启用则直接返回
        if (!properties.isEnabled() || !rateLimitAnnotation.enabled()) {
            return;
        }
        
        try {
            // 构建限流 Key,调用keyBuilder.buildRateLimitKey方法,传入切入点和注解
            String key = keyBuilder.buildRateLimitKey(joinPoint, rateLimitAnnotation);
            
            // 构建限流上下文对象,使用建造者模式设置各个属性
            RateLimitContext context = RateLimitContext.builder()
                // 限流 key
                .key(key)
                // 限流类型
                .type(rateLimitAnnotation.type())
                // 限流作用域
                .scope(rateLimitAnnotation.scope())
                // 窗口时间(秒)
                .window(getWindowInSeconds(rateLimitAnnotation))
                // 限制次数
                .limit(getLimit(rateLimitAnnotation))
                // 令牌桶容量
                .capacity(rateLimitAnnotation.capacity())
                // 令牌生成速率
                .rate(rateLimitAnnotation.rate())
                // 提示信息
                .message(getMessage(rateLimitAnnotation))
                .build();
            // 执行限流检查,待用 rateLimiterService.check 方法,传入限流上下文
            var result = rateLimiterService.check(context);
            // 如果检查结果不允许通过
            if (!result.isAllowed()) {
                //构造提示信息,如果result中有retryAfter则加上等待时间
                String message = result.getRetryAfter() != null ?
                    String.format("%s，请等待 %d 秒后重试", context.getMessage(), result.getRetryAfter()) :
                    context.getMessage();
                
                //throw new RateLimitException(message, result.getRetryAfter());
                //抛出业务异常
                throw new BusinessException(message);
            }
            
            logger.debug("限流通过: key={}, scope={}", key, rateLimitAnnotation.scope());
            
        //} catch (RateLimitException e) {
            //throw e;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            // 捕获其他异常,仅记录日志,不中断业务(防止限流组件异常影响主流程)
            logger.error("限流切面错误", e);
            // 限流组件异常不影响业务
        }
    }
    
    /**
     * 获取以秒为单位的时间窗口
     * 参数：rateLimit注解对象
     * 作用：将注解中的window和unit转换为秒
     */
    private long getWindowInSeconds(RateLimit rateLimit) {
        return rateLimit.unit().toSeconds(rateLimit.window());
    }

    /**
     * 获取限流次数
     * 参数：rateLimit注解对象
     * 作用：如果注解中limit>0则使用注解值，否则使用配置中的默认值
     */
    private long getLimit(RateLimit rateLimit) {
        return rateLimit.limit() > 0 ? rateLimit.limit() : properties.getDefaultLimit();
    }

    /**
     * 获取提示信息
     * 参数：rateLimit注解对象
     * 作用：如果注解中message不为空则使用，否则使用配置中的默认信息
     */
    private String getMessage(RateLimit rateLimit) {
        String message = rateLimit.message();
        return (message == null || message.isEmpty()) ? 
            properties.getDefaultRateLimitMessage() : message;
    }
}