package com.mok.framework.ai.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.mok.framework.ai.config.AiProperties;
import com.mok.framework.ai.service.AIService;
import com.mok.framework.ai.service.AIStreamHandle;
import com.mok.framework.common.utils.LogUtils;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.slf4j.Logger;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * OpenAI-compatible 流式接口实现，默认用于 DeepSeek。
 */
public class DeepSeekAIService implements AIService {

    private static final Logger log = LogUtils.getLogger(DeepSeekAIService.class);
    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");

    private final AiProperties properties;
    private final Call.Factory callFactory;

    public DeepSeekAIService(AiProperties properties) {
        this(properties, buildClient(properties));
    }

    DeepSeekAIService(AiProperties properties, Call.Factory callFactory) {
        this.properties = Objects.requireNonNull(properties, "AI配置不能为空");
        this.callFactory = Objects.requireNonNull(callFactory, "AI请求工厂不能为空");
    }

    @Override
    public AIStreamHandle createStream(String prompt,
                                       String systemPrompt,
                                       Consumer<String> consumer) {
        if (!StringUtils.hasText(prompt)) {
            throw new IllegalArgumentException("AI分析内容不能为空");
        }
        Objects.requireNonNull(consumer, "AI流式回调不能为空");

        String effectiveSystemPrompt = StringUtils.hasText(systemPrompt)
                ? systemPrompt
                : properties.getSystemPrompt();
        if (!StringUtils.hasText(effectiveSystemPrompt)) {
            throw new IllegalStateException("AI系统提示词未配置");
        }
        if (!StringUtils.hasText(properties.getApiKey())) {
            throw new IllegalStateException("AI API Key未配置");
        }
        if (!StringUtils.hasText(properties.getBaseUrl())) {
            throw new IllegalStateException("AI API地址未配置");
        }
        if (!StringUtils.hasText(properties.getModel())) {
            throw new IllegalStateException("AI模型未配置");
        }

        JSONObject body = buildRequestBody(prompt, effectiveSystemPrompt);
        String baseUrl = trimTrailingSlash(properties.getBaseUrl().trim());
        Request request = new Request.Builder()
                .url(baseUrl + "/v1/chat/completions")
                .header("Authorization", "Bearer " + properties.getApiKey())
                .post(RequestBody.create(body.toJSONString(), JSON_MEDIA_TYPE))
                .build();
        Call call = callFactory.newCall(request);

        return new AIStreamHandle() {
            private final AtomicBoolean executed = new AtomicBoolean(false);

            @Override
            public void execute() {
                if (!executed.compareAndSet(false, true)) {
                    throw new IllegalStateException("AI流式请求不能重复执行");
                }
                executeCall(call, consumer);
            }

            @Override
            public void cancel() {
                call.cancel();
            }
        };
    }

    private void executeCall(Call call, Consumer<String> consumer) {
        try (Response response = call.execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() == null ? "unknown" : response.body().string();
                throw new IllegalStateException("AI请求失败: " + response.code() + " " + errorBody);
            }

            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                throw new IllegalStateException("AI服务返回空响应");
            }

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(responseBody.byteStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.startsWith("data:")) {
                        continue;
                    }
                    String data = line.substring(5).trim();
                    if ("[DONE]".equals(data)) {
                        break;
                    }
                    String content = extractContent(data, line);
                    if (content != null) {
                        // 回调异常必须向上传播，以便客户端断开时立即取消上游请求。
                        consumer.accept(content);
                    }
                }
            }
        } catch (IOException e) {
            if (call.isCanceled()) {
                return;
            }
            log.error("AI流式请求异常", e);
            throw new IllegalStateException("AI服务连接失败: " + e.getMessage(), e);
        }
    }

    private String extractContent(String data, String originalLine) {
        try {
            JSONObject json = JSON.parseObject(data);
            JSONArray choices = json.getJSONArray("choices");
            if (choices == null || choices.isEmpty()) {
                return null;
            }
            JSONObject choice = choices.getJSONObject(0);
            JSONObject delta = choice == null ? null : choice.getJSONObject("delta");
            return delta == null ? null : delta.getString("content");
        } catch (RuntimeException e) {
            log.warn("解析SSE消息失败: {}", originalLine, e);
            return null;
        }
    }

    private JSONObject buildRequestBody(String prompt, String systemPrompt) {
        JSONObject systemMessage = new JSONObject();
        systemMessage.put("role", "system");
        systemMessage.put("content", systemPrompt);

        JSONObject userMessage = new JSONObject();
        userMessage.put("role", "user");
        userMessage.put("content", prompt);

        JSONArray messages = new JSONArray();
        messages.add(systemMessage);
        messages.add(userMessage);

        JSONObject body = new JSONObject();
        body.put("model", properties.getModel());
        body.put("messages", messages);
        body.put("stream", true);
        return body;
    }

    private static OkHttpClient buildClient(AiProperties properties) {
        Objects.requireNonNull(properties, "AI配置不能为空");
        long connectTimeout = requirePositive(properties.getConnectTimeoutSeconds(), "AI连接超时");
        long readTimeout = requirePositive(properties.getReadTimeoutSeconds(), "AI读取超时");
        return new OkHttpClient.Builder()
                .connectTimeout(connectTimeout, TimeUnit.SECONDS)
                .readTimeout(readTimeout, TimeUnit.SECONDS)
                .writeTimeout(connectTimeout, TimeUnit.SECONDS)
                .build();
    }

    private static long requirePositive(long value, String name) {
        if (value <= 0) {
            throw new IllegalArgumentException(name + "必须大于0");
        }
        return value;
    }

    private static String trimTrailingSlash(String value) {
        int end = value.length();
        while (end > 0 && value.charAt(end - 1) == '/') {
            end--;
        }
        return value.substring(0, end);
    }
}
