package com.mok.framework.common.aspect;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.mok.framework.common.annotation.OperationLog;
import com.mok.framework.common.config.operationlog.OperationLogConfig;
import com.mok.framework.common.constant.mq.OperationLogMQConstant;
import com.mok.framework.common.mapper.OperationLogUserMapper;
import com.mok.framework.common.utils.JsonDesensitizationUtil;
import com.mok.framework.common.utils.LogUtils;
import com.mok.framework.model.dto.OperationLogMessage;
import com.mok.framework.model.entity.UserEntity;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 操作日志切面
 */
@Aspect
@Component
public class OperationLogAspect {

    private static final Logger log = LogUtils.getLogger(OperationLogAspect.class);

    private final OperationLogConfig operationLogConfig;
    private final RabbitTemplate rabbitTemplate;
    private final JsonDesensitizationUtil jsonDesensitizationUtil;
    private final OperationLogUserMapper operationLogUserMapper;

    public OperationLogAspect(RabbitTemplate rabbitTemplate,
                              JsonDesensitizationUtil jsonDesensitizationUtil,
                              OperationLogUserMapper operationLogUserMapper,
                              OperationLogConfig operationLogConfig) {
        this.rabbitTemplate = rabbitTemplate;
        this.jsonDesensitizationUtil = jsonDesensitizationUtil;
        this.operationLogUserMapper = operationLogUserMapper;
        this.operationLogConfig = operationLogConfig;
    }

    /**
     * 定义切点
     */
    @Pointcut("@annotation(com.mok.framework.common.annotation.OperationLog)")
    public void operationLogPointCut() {
        // 定义切点,不需要实现代码
    }

    /**
     * 让一段代码在目标方法执行之前自动运行
     */
    @Before("@annotation(com.mok.framework.common.annotation.OperationLog)")
    public void doBefore(JoinPoint joinPoint) {
        log.info("===================================================================");
    }

    @AfterReturning(pointcut = "operationLogPointCut()", returning = "jsonResult")
    public void doAfterReturning(JoinPoint joinPoint, Object jsonResult) {
        handleLog(joinPoint, null, jsonResult);
    }

    @AfterThrowing(pointcut = "operationLogPointCut()", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, Exception e) {
        handleLog(joinPoint, e, null);
    }

    /**
     * 收集日志信息并通过消息队列发送
     */
    protected void handleLog(final JoinPoint joinPoint, final Exception e, Object jsonResult) {
        try {
            // 1. 获取当前请求
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return;  // 如果不是Web请求，直接返回
            }
            HttpServletRequest request = attributes.getRequest();

            // 2. 获取方法信息
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();

            // 3. 获取方法上的@OperationLog注解
            OperationLog annotation = method.getAnnotation(OperationLog.class);

            // 4. 检查是否要记录GET请求
            if (!operationLogConfig.getRecordGet() && "GET".equals(request.getMethod())) {
                return;
            }

            // 5. 构建操作日志消息
            OperationLogMessage message = buildOperationLogMessage(joinPoint, request, annotation, e, jsonResult);
            // 6. 异步发送到消息队列（关键改动）
            // 直接使用RabbitTemplate发送
            // 发送到消息队列
            rabbitTemplate.convertAndSend(OperationLogMQConstant.OPERATION_LOG_EXCHANGE, OperationLogMQConstant.OPERATION_LOG_ROUTING_KEY, message);
            log.info("操作日志已发送到消息队列: {}", message.getTitle());

        } catch (Exception ex) {
            // 捕获异常，不影响主业务流程
            log.error("构建操作日志消息失败", ex);
        }
    }

    /**
     * 构建操作日志消息
     */
    private OperationLogMessage buildOperationLogMessage(JoinPoint joinPoint, HttpServletRequest request, OperationLog annotation, Exception e, Object jsonResult) {

        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();

        // 1. 创建消息对象
        OperationLogMessage message = new OperationLogMessage();

        // 2. 设置基本信息
        message.setId(IdUtil.simpleUUID());
        message.setTitle(annotation.title());
        message.setBusinessType(annotation.businessType().getValue());
        message.setMethod(method.getDeclaringClass().getName() + "." + method.getName());
        message.setRequestMethod(request.getMethod());
        message.setOperUrl(request.getRequestURI());
        message.setOperIp(getIpAddress(request));
        message.setOperTime(LocalDateTime.now());

        // 3. 设置操作人信息
        setOperatorInfo(message);

        // 4. 处理请求参数（如果需要保存）
        if (annotation.saveRequestParam()) {
            String operParam = buildOperParam(joinPoint);
            message.setOperParam(operParam);
        }

        // 5. 处理响应数据（如果需要保存）
        if (annotation.saveResponseData() && jsonResult != null) {
            message.setJsonResult(jsonResult.toString());
        }

        // 6. 设置状态和错误信息
        if (e != null) {
            message.setStatus(1);  // 失败
            message.setErrorMsg(StrUtil.sub(e.getMessage(), 0, 2000));  // 限制错误信息长度
        } else {
            message.setStatus(0);  // 成功
        }

        return message;
    }

    /**
     * 构建请求参数字符串
     */
    private String buildOperParam(JoinPoint joinPoint) {
        try {
            StringBuilder result = new StringBuilder();
            Object[] args = joinPoint.getArgs();

            for (int i = 0; i < args.length; i++) {
                if (args[i] != null) {
                    // 将参数转为JSON字符串
                    String jsonStr = JSONUtil.toJsonStr(args[i]);

                    // 对敏感信息进行脱敏处理
                    String desensitizedStr = jsonDesensitizationUtil.desensitizeJson(jsonStr);
                    result.append(desensitizedStr);

                    // 添加分隔符（除了最后一个参数）
                    if (i < args.length - 1) {
                        result.append(",");
                    }
                }
            }
            return result.toString();
        } catch (Exception ex) {
            log.warn("构建请求参数失败", ex);
            return "";
        }
    }

    /**
     * 设置操作人信息
     */
    private void setOperatorInfo(OperationLogMessage message) {
        try {
            // 从Spring Security获取当前登录用户
            if (StpUtil.isLogin()) {
                String userId = StpUtil.getLoginId().toString();
                UserEntity userEntity = operationLogUserMapper.selectById(userId);
                message.setOperatorName(userEntity.getUsername());
                message.setOperatorType(1);  // 用户类型：系统用户
            }
        } catch (Exception e) {
            log.warn("获取操作人失败", e);
        }
    }

    /**
     * 获取客户端IP地址
     */
    private String getIpAddress(HttpServletRequest request) {
        // 从各种请求头中获取IP地址
        String ip = request.getHeader("X-Forwarded-For");

        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // 处理多个IP的情况（如：192.168.1.1,192.168.1.2）
        if (ip != null && ip.length() > 15 && ip.indexOf(",") > 0) {
            ip = ip.substring(0, ip.indexOf(","));
        }

        return ip;
    }

}
