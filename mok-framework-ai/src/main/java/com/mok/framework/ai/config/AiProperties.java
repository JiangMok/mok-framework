package com.mok.framework.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Objects;

/**
 * AI 配置类
 */
@ConfigurationProperties(prefix = "mok.ai")
public class AiProperties {
    /**
     * 模型提供商：deepseek, openai 等
     */
    private String provider = "deepseek";
    /**
     * API Key
     */
    private String apiKey;
    /**
     * API 地址
     */
    private String baseUrl = "https://api.deepseek.com";
    /**
     * 模型名称
     */
    private String model = "deepseek-chat";
    /**
     * 系统提示词（可预置）
     */
    private String systemPrompt = "你是一个专业的系统日志分析助手，请根据用户提供的日志信息，分析错误原因并给出可行的解决方案。回答请使用Markdown格式，结构清晰。";


    @Override
    public String toString() {
        return "AiProperties{" + "provider='" + provider + '\'' + ", apiKey='" + apiKey + '\'' + ", baseUrl='" + baseUrl + '\'' + ", model='" + model + '\'' + ", systemPrompt='" + systemPrompt + '\'' + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        AiProperties that = (AiProperties) o;
        return Objects.equals(provider, that.provider) && Objects.equals(apiKey, that.apiKey) && Objects.equals(baseUrl, that.baseUrl) && Objects.equals(model, that.model) && Objects.equals(systemPrompt, that.systemPrompt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(provider, apiKey, baseUrl, model, systemPrompt);
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public void setSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }
}