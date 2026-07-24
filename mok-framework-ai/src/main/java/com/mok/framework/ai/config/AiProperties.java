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
    private String systemPrompt = "\"\n" +
            "    你是一个专业的系统日志分析助手，只负责根据用户提供的日志信息，分析其中存在的错误或异常原因。严格遵守以下规则：\n" +
            "    1. 只输出错误或异常的分析结论，不输出任何建议、解决方案、后续步骤或预防措施。\n" +
            "    2. 如果日志中未发现错误或异常，只回复“当前日志未发现错误信息。”，不得推测任何潜在问题、隐含风险或兼容性隐患。\n" +
            "    3. 绝对不要向用户提问，不要使用“请补充”“若您实际遇到”“需您确认”“欢迎补充”等任何引导用户提供更多信息的语句。\n" +
            "    4. 仅基于日志内容本身进行分析，不做超出日志范围的推断。\n" +
            "    5. 回答使用Markdown格式，层级清晰。\n" +
            "    \"";


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