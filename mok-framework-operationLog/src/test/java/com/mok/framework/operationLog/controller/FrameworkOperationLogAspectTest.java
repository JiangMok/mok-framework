package com.mok.framework.operationLog.controller;

import com.mok.framework.common.BusinessException;
import com.mok.framework.common.R;
import com.mok.framework.operationLog.aspect.FrameworkOperationLogAspect;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import top.jiangmok.operationlog.annotation.OperationLog;
import top.jiangmok.operationlog.config.OperationLogProperties;
import top.jiangmok.operationlog.desensitize.ParamDesensitizer;
import top.jiangmok.operationlog.message.OperationLogMessage;
import top.jiangmok.operationlog.operator.OperatorInfo;
import top.jiangmok.operationlog.operator.OperatorResolver;
import top.jiangmok.operationlog.sender.OperationLogAsyncSender;

import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FrameworkOperationLogAspectTest {

    @AfterEach
    void clearRequestContext() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void errorResponseIsRecordedAsBusinessFailure() throws Exception {
        OperationLogProperties properties = new OperationLogProperties();
        properties.setBusinessExceptions(List.of(BusinessException.class.getName()));
        OperationLogAsyncSender sender = mock(OperationLogAsyncSender.class);
        OperatorResolver operatorResolver = mock(OperatorResolver.class);
        when(operatorResolver.resolve()).thenReturn(
                new OperatorInfo("user-1", "测试用户", "ADMIN", "研发部"));
        ParamDesensitizer desensitizer = mock(ParamDesensitizer.class);
        when(desensitizer.desensitize(any())).thenAnswer(invocation -> invocation.getArgument(0));

        FrameworkOperationLogAspect aspect = new FrameworkOperationLogAspect(
                properties, sender, operatorResolver, desensitizer);
        JoinPoint joinPoint = joinPointFor("businessFailure");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(
                new MockHttpServletRequest("POST", "/test")));

        aspect.doAfterReturning(joinPoint, R.badRequest("参数错误"));

        org.mockito.ArgumentCaptor<OperationLogMessage> captor =
                org.mockito.ArgumentCaptor.forClass(OperationLogMessage.class);
        verify(sender).send(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(2);
        assertThat(captor.getValue().getErrorMsg()).isEqualTo("参数错误");
    }

    private JoinPoint joinPointFor(String methodName) throws Exception {
        Method method = SampleController.class.getDeclaredMethod(methodName);
        MethodSignature signature = mock(MethodSignature.class);
        when(signature.getMethod()).thenReturn(method);
        JoinPoint joinPoint = mock(JoinPoint.class);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(joinPoint.getArgs()).thenReturn(new Object[0]);
        return joinPoint;
    }

    private static class SampleController {
        @OperationLog(title = "测试业务失败")
        public R<String> businessFailure() {
            return R.badRequest("参数错误");
        }
    }
}
