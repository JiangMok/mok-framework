package com.mok.framework.ai.service.impl;

import com.mok.framework.ai.config.AiProperties;

/**
 * OpenAI provider 使用与 DeepSeek 相同的 OpenAI-compatible 流式协议。
 * base-url、model 与 api-key 均由 mok.ai 配置提供。
 */
public class OpenAIAIService extends DeepSeekAIService {

    public OpenAIAIService(AiProperties properties) {
        super(properties);
    }
}
