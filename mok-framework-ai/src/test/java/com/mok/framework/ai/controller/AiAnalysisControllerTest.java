package com.mok.framework.ai.controller;

import com.mok.framework.ai.config.AiProperties;
import com.mok.framework.ai.service.AIService;
import com.mok.framework.ai.service.AIStreamHandle;
import com.mok.framework.ai.service.SysAiSystemPromptConfigService;
import com.mok.framework.common.BusinessException;
import com.mok.framework.model.dto.AiAnalysisRequest;
import com.mok.framework.model.entity.MqFailedMessage;
import com.mok.framework.model.enums.AiAnalysisRequestType;
import com.mok.framework.mq.service.MqFailedMessageService;
import org.junit.jupiter.api.Test;
import top.jiangmok.operationlog.service.OperationLogService;

import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class AiAnalysisControllerTest {

    @Test
    void missingBusinessRecordReturnsBusinessErrorInsteadOfNullPointer() {
        AIService aiService = mock(AIService.class);
        MqFailedMessageService failedMessageService = mock(MqFailedMessageService.class);
        AiAnalysisController controller = controller(aiService, failedMessageService, Runnable::run);
        AiAnalysisRequest request = new AiAnalysisRequest(
                "missing", AiAnalysisRequestType.MQ_FAILED_MESSAGE);

        assertThatThrownBy(() -> controller.analysis(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("MQ失败消息不存在");
        verifyNoInteractions(aiService);
    }

    @Test
    void missingDatabasePromptUsesServiceLevelDefaultPrompt() {
        AIService aiService = mock(AIService.class);
        AIStreamHandle handle = mock(AIStreamHandle.class);
        MqFailedMessageService failedMessageService = mock(MqFailedMessageService.class);
        MqFailedMessage failedMessage = new MqFailedMessage();
        failedMessage.setFailReason("failure details");
        when(failedMessageService.getById("message-1")).thenReturn(failedMessage);
        when(aiService.createStream(eq("failure details"), isNull(), any())).thenReturn(handle);
        AiAnalysisController controller = controller(aiService, failedMessageService, Runnable::run);

        controller.analysis(new AiAnalysisRequest(
                "message-1", AiAnalysisRequestType.MQ_FAILED_MESSAGE));

        verify(handle).execute();
        verify(handle).close();
    }

    @Test
    void saturatedExecutorCancelsOnlyThePreparedStream() {
        AIService aiService = mock(AIService.class);
        AIStreamHandle handle = mock(AIStreamHandle.class);
        MqFailedMessageService failedMessageService = mock(MqFailedMessageService.class);
        MqFailedMessage failedMessage = new MqFailedMessage();
        failedMessage.setFailReason("failure details");
        when(failedMessageService.getById("message-1")).thenReturn(failedMessage);
        when(aiService.createStream(eq("failure details"), isNull(), any())).thenReturn(handle);
        Executor rejectedExecutor = command -> {
            throw new RejectedExecutionException("full");
        };
        AiAnalysisController controller = controller(aiService, failedMessageService, rejectedExecutor);

        assertThatThrownBy(() -> controller.analysis(new AiAnalysisRequest(
                "message-1", AiAnalysisRequestType.MQ_FAILED_MESSAGE)))
                .isInstanceOf(BusinessException.class)
                .hasMessage("AI服务繁忙，请稍后重试");
        verify(handle).close();
    }

    private AiAnalysisController controller(AIService aiService,
                                            MqFailedMessageService failedMessageService,
                                            Executor executor) {
        AiProperties properties = new AiProperties();
        properties.setSseTimeoutMillis(30_000L);
        return new AiAnalysisController(
                aiService,
                executor,
                mock(OperationLogService.class),
                failedMessageService,
                mock(SysAiSystemPromptConfigService.class),
                properties);
    }
}
