package com.mok.framework.ai.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.mok.framework.ai.config.AiProperties;
import com.mok.framework.ai.service.AIService;
import com.mok.framework.ai.service.AIStreamHandle;
import com.mok.framework.ai.service.SysAiSystemPromptConfigService;
import com.mok.framework.common.BusinessException;
import com.mok.framework.model.dto.AiAnalysisRequest;
import com.mok.framework.model.enums.AiAnalysisRequestType;
import com.mok.framework.mq.service.MqFailedMessageService;
import jakarta.validation.Valid;
import top.jiangmok.operationlog.service.OperationLogService;
import top.jiangmok.ratelimiter.annotation.PreventDuplicate;
import top.jiangmok.ratelimiter.annotation.RateLimit;
import top.jiangmok.ratelimiter.enums.RateLimitScope;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

@RestController // 标识为 REST 控制器
@RequestMapping("/ai") // 设置基础请求路径 /ai
@SaCheckRole("ROLE_ADMIN")
public class AiAnalysisController {

    private final AIService aiService; // 注入 AI 服务，用于实际的流式分析
    private final Executor executor;   // Spring 管理的线程池，替代 CachedThreadPool
    private final OperationLogService operationLogService;
    private final MqFailedMessageService mqFailedMessageService;
    private final SysAiSystemPromptConfigService sysAiSystemPromptConfigService;
    private final AiProperties aiProperties;

    public AiAnalysisController(AIService aiService,
                                @Qualifier("aiAnalysisExecutor") Executor executor,
                                OperationLogService operationLogService,
                                MqFailedMessageService mqFailedMessageService,
                                SysAiSystemPromptConfigService sysAiSystemPromptConfigService,
                                AiProperties aiProperties) { // 构造器注入
        this.aiService = aiService;
        this.executor = executor;
        this.operationLogService = operationLogService;
        this.mqFailedMessageService = mqFailedMessageService;
        this.sysAiSystemPromptConfigService = sysAiSystemPromptConfigService;
        this.aiProperties = aiProperties;
    }

    /**
     * @description: AI 分析
     * @author: mok
     * @date: 2026/7/21 10:32
     * @param: [aiAnalysisRequest]
     * @return: org.springframework.web.servlet.mvc.method.annotation.SseEmitter
    **/
    @RateLimit(scope = RateLimitScope.USER, limit = 5, message = "AI调用过于频繁，请稍后重试")
    @PreventDuplicate(lockTime = 5, message = "请勿重复提交AI请求")
    @PostMapping(value = "/analysis", produces = MediaType.TEXT_EVENT_STREAM_VALUE) // 接收 POST 请求，响应为 SSE 事件流
    public SseEmitter analysis(@Valid @RequestBody AiAnalysisRequest aiAnalysisRequest) { // 请求体为 JSON map
        if (aiAnalysisRequest == null) {
            throw new BusinessException("AI分析请求不能为空");
        }
        // 获取查询数据的ID
        String id = aiAnalysisRequest.getId();
        // 获取需要分析的业务类型
        AiAnalysisRequestType aiAnalysisRequestType = aiAnalysisRequest.getAiAnalysisRequestType();
        if (!StringUtils.hasText(id)) {
            throw new BusinessException("待分析记录ID不能为空");
        }
        if (aiAnalysisRequestType == null) {
            throw new BusinessException("AI分析类型不能为空");
        }
        // 数据库查询 content
        String content = switch (aiAnalysisRequestType) {
            case OPERATION_LOG -> {
                var operationLog = operationLogService.findById(id);
                if (operationLog == null) {
                    throw new BusinessException("操作日志不存在");
                }
                yield operationLog.getErrorMsg();
            }
            case MQ_FAILED_MESSAGE -> {
                var failedMessage = mqFailedMessageService.getById(id);
                if (failedMessage == null) {
                    throw new BusinessException("MQ失败消息不存在");
                }
                yield failedMessage.getFailReason();
            }
        };
        if (content == null || content.isBlank()) { // 校验内容非空
            throw new BusinessException("分析内容不能为空");
        }

        long sseTimeoutMillis = aiProperties.getSseTimeoutMillis();
        if (sseTimeoutMillis <= 0) {
            throw new BusinessException("AI SSE超时配置必须大于0");
        }
        SseEmitter emitter = new SseEmitter(sseTimeoutMillis);

        // 获取系统提示词
        var promptConfig = sysAiSystemPromptConfigService
                .getByAiAnalysisRequestType(aiAnalysisRequestType);
        String systemPrompt = promptConfig == null ? null : promptConfig.getSystemPrompt();

        AIStreamHandle streamHandle = aiService.createStream(content, systemPrompt, chunk -> {
            try {
                emitter.send(SseEmitter.event().data(chunk));
            } catch (IOException e) {
                throw new IllegalStateException("SSE发送失败", e);
            }
        });

        // 所有回调只操作本次请求的句柄，不会影响其他并发请求。
        emitter.onCompletion(streamHandle::cancel);
        emitter.onTimeout(() -> {
            streamHandle.cancel();
            emitter.complete();
        });
        emitter.onError(throwable -> streamHandle.cancel());

        try {
            executor.execute(() -> {
                try {
                    streamHandle.execute();
                    // 正常结束
                    emitter.send(SseEmitter.event().data("[DONE]"));
                    emitter.complete();
                } catch (Exception e) {
                    emitter.completeWithError(e);
                } finally {
                    streamHandle.close();
                }
            });
        } catch (RejectedExecutionException e) {
            streamHandle.close();
            throw new BusinessException("AI服务繁忙，请稍后重试", e);
        }
        return emitter; // 返回 SSE 发射器，由 Spring 异步处理
    }
}
