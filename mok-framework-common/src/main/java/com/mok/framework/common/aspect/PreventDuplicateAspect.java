package com.mok.framework.common.aspect;

import com.mok.framework.common.DuplicateSubmitException;
import com.mok.framework.common.annotation.PreventDuplicate;
import com.mok.framework.common.utils.LogUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
public class PreventDuplicateAspect {

    private final static Logger log = LogUtils.getLogger(PreventDuplicateAspect.class);

    private final RedisTemplate<String, Object> redisTemplate;

    public PreventDuplicateAspect(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 定义切点
     */
    @Pointcut("@annotation(com.mok.framework.common.annotation.PreventDuplicate)")
    public void preventDuplicatePointCut() {
    }

    @Around("@annotation(com.mok.framework.common.annotation.PreventDuplicate)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result;
        Exception exception;
        // 获取方法信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        PreventDuplicate annotation = method.getAnnotation(PreventDuplicate.class);
        // 动态解析注解中的 key（支持 SpEL）
        String keyExpression = annotation.key();
        String resolvedKey = "";
        try {
            if (keyExpression.isEmpty()) {
                resolvedKey = method.getDeclaringClass().getName() + "." + method.getName()
                        + ":" + Arrays.toString(joinPoint.getArgs());
            } else {
                resolvedKey = resolveKey(keyExpression, method, joinPoint.getArgs());
            }
            // 如果没有指定 key，生成默认防重 key（避免所有请求被同一把锁卡住）

        } catch (Exception e) {
            log.error("========== 防重复提交注解在解析SpEL表达式时出现异常,异常信息:{}", e.getMessage());
            throw new DuplicateSubmitException("防重复提交注解在解析SpEL表达式时出现异常");
        }
        String lockKey = "user:" + resolvedKey + ":type:" + annotation.type();
        String lockValue = UUID.randomUUID().toString();
        int timeout = annotation.lockTime();
        String message = annotation.message();
        try {
            log.info("========== lockKey:{}", lockKey);
            // 尝试获取锁
            Boolean lockAcquired = redisTemplate.opsForValue().setIfAbsent(lockKey, lockValue, timeout, TimeUnit.SECONDS);
            // 判断锁是否查询在
            if (lockAcquired == null || !lockAcquired) {
                log.info("========== ❌ 禁止通过");
                throw new DuplicateSubmitException(message);
            } else {
                log.info("========== ✅ 可通过");
            }
            result = joinPoint.proceed();
            return result;
        } finally {
            // 严谨方案：用 Lua 脚本保证“判断+删除”的原子性
            String script =
                    "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                            "    return redis.call('del', KEYS[1]) " +
                            "else " +
                            "    return 0 " +
                            "end";
            redisTemplate.execute(
                    new DefaultRedisScript<>(script, Long.class),
                    Collections.singletonList(lockKey),
                    lockValue
            );
        }
    }

    /**
     * 解析 SpEL 表达式，将 #参数名 替换为实际参数值
     */
    private String resolveKey(String expression, Method method, Object[] args) {
        if (expression == null || !expression.contains("#")) {
            return expression; // 没有 SpEL，直接返回原字符串
        }

        // 获取方法参数名
        DefaultParameterNameDiscoverer discoverer = new DefaultParameterNameDiscoverer();
        String[] parameterNames = discoverer.getParameterNames(method);

        // 构建 SpEL 上下文
        EvaluationContext context = new StandardEvaluationContext();
        if (parameterNames != null) {
            for (int i = 0; i < parameterNames.length; i++) {
                context.setVariable(parameterNames[i], args[i]);
            }
        }

        // 解析表达式
        ExpressionParser parser = new SpelExpressionParser();
        Object value = parser.parseExpression(expression).getValue(context);
        return value != null ? value.toString() : "";
    }

}
