package com.mok.framework.ai.service.impl;

import com.mok.framework.ai.config.AiProperties;
import com.mok.framework.ai.service.AIService;
import com.mok.framework.common.utils.LogUtils;
import com.mok.framework.model.enums.AiAnalysisRequestType;
import org.slf4j.Logger;

import java.util.function.Consumer;

public class OpenAIAIService implements AIService {

    private final static Logger log = LogUtils.getLogger(OpenAIAIService.class);

    private final AiProperties properties;

    public OpenAIAIService(AiProperties properties) {
        this.properties = properties;
    }

    @Override
    public void streamAnalysis(String prompt, String systemPrompt, Consumer<String> consumer) {
        log.info("========== 使用openAI");
        throw new UnsupportedOperationException("========== OpenAI provider is not yet implemented");
    }

    @Override
    public void close() {

    }
    // 实现与 DeepSeek 几乎一致，仅 baseUrl 改为 https://api.openai.com，模型名改为 gpt-3.5-turbo 等
    // 可根据需要添加个性化处理，此处省略重复代码。
}