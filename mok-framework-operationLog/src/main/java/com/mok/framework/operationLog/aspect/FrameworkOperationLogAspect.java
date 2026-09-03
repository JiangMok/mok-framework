package com.mok.framework.operationLog.aspect;

import com.mok.framework.common.BusinessException;
import com.mok.framework.common.R;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import top.jiangmok.operationlog.aspect.OperationLogAspect;
import top.jiangmok.operationlog.config.OperationLogProperties;
import top.jiangmok.operationlog.desensitize.ParamDesensitizer;
import top.jiangmok.operationlog.operator.OperatorResolver;
import top.jiangmok.operationlog.sender.OperationLogAsyncSender;

/**
 * 脚手架统一响应适配。
 * Controller 正常返回 R.error(...) 时，也应记录为业务失败或系统失败。
 */
@Aspect
@Component
@ConditionalOnProperty(
        prefix = "mok.operation-log",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
public class FrameworkOperationLogAspect extends OperationLogAspect {

    public FrameworkOperationLogAspect(OperationLogProperties properties,
                                       OperationLogAsyncSender sender,
                                       OperatorResolver operatorResolver,
                                       ParamDesensitizer paramDesensitizer) {
        super(properties, sender, operatorResolver, paramDesensitizer);
    }

    @Override
    @AfterReturning(
            pointcut = "@annotation(top.jiangmok.operationlog.annotation.OperationLog)",
            returning = "jsonResult")
    public void doAfterReturning(JoinPoint joinPoint, Object jsonResult) {
        if (jsonResult instanceof R<?> result && result.isError()) {
            Integer code = result.getCode();
            Exception exception = code != null && code >= 500 && code < 1000
                    ? new IllegalStateException(result.getMsg())
                    : new BusinessException(code, result.getMsg());
            handleLog(joinPoint, exception, jsonResult);
            return;
        }
        handleLog(joinPoint, null, jsonResult);
    }

    @Override
    @AfterThrowing(
            pointcut = "@annotation(top.jiangmok.operationlog.annotation.OperationLog)",
            throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, Exception e) {
        handleLog(joinPoint, e, null);
    }
}
