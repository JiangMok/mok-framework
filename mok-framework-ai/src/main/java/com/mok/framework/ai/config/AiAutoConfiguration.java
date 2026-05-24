package com.mok.framework.ai.config;

import com.mok.framework.ai.service.AIService;
import com.mok.framework.ai.service.impl.DeepSeekAIService;
import com.mok.framework.ai.service.impl.OpenAIAIService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AiProperties.class)
public class AiAutoConfiguration {

    @Bean
    @ConditionalOnProperty(name = "mok.ai.provider", havingValue = "deepseek", matchIfMissing = true)
    public AIService deepSeekAIService(AiProperties properties) {
        return new DeepSeekAIService(properties);
    }

    @Bean
    @ConditionalOnProperty(name = "mok.ai.provider", havingValue = "openai")
    public AIService openAIAIService(AiProperties properties) {
        return new OpenAIAIService(properties);
    }

    // 可继续添加其他模型...
}