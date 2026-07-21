package com.mok.framework.ai.controller;

import com.mok.framework.ai.service.AIService;
import com.mok.framework.ai.service.SysAiSystemPromptConfigService;
import com.mok.framework.model.dto.AiAnalysisRequest;
import com.mok.framework.model.enums.AiAnalysisRequestType;
import com.mok.framework.mq.service.MqFailedMessageService;
import com.mok.framework.operationLog.service.OperationLogService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.Executor;

@RestController // 标识为 REST 控制器
@RequestMapping("/ai") // 设置基础请求路径 /ai
public class AiAnalysisController {

    private final AIService aiService; // 注入 AI 服务，用于实际的流式分析
    private final Executor executor;   // Spring 管理的线程池，替代 CachedThreadPool
    private final OperationLogService operationLogService;
    private final MqFailedMessageService mqFailedMessageService;
    private final SysAiSystemPromptConfigService sysAiSystemPromptConfigService;

    public AiAnalysisController(AIService aiService,
                                @Qualifier("aiAnalysisExecutor") Executor executor,
                                OperationLogService operationLogService,
                                MqFailedMessageService mqFailedMessageService,
                                SysAiSystemPromptConfigService sysAiSystemPromptConfigService) { // 构造器注入
        this.aiService = aiService;
        this.executor = executor;
        this.operationLogService = operationLogService;
        this.mqFailedMessageService = mqFailedMessageService;
        this.sysAiSystemPromptConfigService = sysAiSystemPromptConfigService;
    }

    /**
     * @description: AI 分析
     * @author: mok
     * @date: 2026/7/21 10:32
     * @param: [aiAnalysisRequest]
     * @return: org.springframework.web.servlet.mvc.method.annotation.SseEmitter
    **/
    @PostMapping(value = "/analysis", produces = MediaType.TEXT_EVENT_STREAM_VALUE) // 接收 POST 请求，响应为 SSE 事件流
    public SseEmitter analysis(@RequestBody AiAnalysisRequest aiAnalysisRequest) { // 请求体为 JSON map
        // 获取查询数据的ID
        String id = aiAnalysisRequest.getId();
        // 获取需要分析的业务类型
        AiAnalysisRequestType aiAnalysisRequestType = aiAnalysisRequest.getAiAnalysisRequestType();
        // 数据库查询 content
        String content = switch (aiAnalysisRequestType) {
            case OPERATION_LOG -> operationLogService.findById(id).getErrorMsg();
            case MQ_FAILED_MESSAGE -> mqFailedMessageService.getById(id).getFailReason();
        };
        if (content == null || content.isBlank()) { // 校验内容非空
            throw new IllegalArgumentException("分析内容不能为空"); // 抛出非法参数异常
        }

        // 0 表示不超时，可根据实际要求设置
        SseEmitter emitter = new SseEmitter(0L); // 创建 SSE 发射器，永不超时

        // 获取系统提示词
        String systemPrompt =
                sysAiSystemPromptConfigService
                        .getByAiAnalysisRequestType(aiAnalysisRequestType)
                        .getSystemPrompt();
        // 异步执行，提交任务

        executor.execute(() -> {
            try {
                aiService.streamAnalysis(content, systemPrompt, chunk -> { // 调用 AI 服务的流式分析方法，chunk 为每个词块
                    try {
                        emitter.send(SseEmitter.event().data(chunk)); // 将词块以 SSE 事件格式发送给客户端
                    } catch (IOException e) { // 发送失败，可能是客户端已断开
                        // 客户端断开连接，中断 AI 请求
                        aiService.close(); // 关闭 AI 底层连接，释放资源
                        throw new RuntimeException("SSE send error", e); // 包装为运行时异常
                    }
                });
                // 正常结束
                emitter.send(SseEmitter.event().data("[DONE]")); // 发送完成标识
                emitter.complete(); // 正常完成 SSE 流
            } catch (Exception e) { // 捕获所有异常，包括 AI 服务异常、发送异常等
                emitter.completeWithError(e); // 异常结束，将异常传递给客户端
            }
        });

        // 清理：当连接完成、超时或出错时，中断底层 AI 请求
        // 注册完成回调
        // 确保 AI 连接关闭
        emitter.onCompletion(aiService::close);
        emitter.onTimeout(() -> { // 注册超时回调
            aiService.close(); // 超时时关闭 AI 连接
            emitter.complete(); // 并完成 SSE 流
        });
        emitter.onError(throwable -> { // 注册错误回调
            aiService.close(); // 出错时关闭 AI 连接
        });
        return emitter; // 返回 SSE 发射器，由 Spring 异步处理
    }
}