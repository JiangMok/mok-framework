package com.mok.framework.ai.service.impl;

import com.alibaba.fastjson2.JSON; // fastjson2 核心解析类
import com.alibaba.fastjson2.JSONArray; // JSON 数组
import com.alibaba.fastjson2.JSONObject; // JSON 对象
import com.mok.framework.ai.config.AiProperties; // AI 相关配置属性
import com.mok.framework.ai.service.AIService; // AI 服务接口
import com.mok.framework.common.utils.LogUtils;
import com.mok.framework.model.enums.AiAnalysisRequestType;
import okhttp3.*; // OkHttp 网络请求相关类
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger; // 日志接口

import java.io.BufferedReader; // 用于读取流
import java.io.IOException; // IO 异常
import java.io.InputStreamReader; // 字节流转字符流
import java.util.function.Consumer; // 消费者函数接口，用于接收流式内容

public class DeepSeekAIService implements AIService { // 实现 AI 服务接口，对接 DeepSeek API

    private static final Logger log = LogUtils.getLogger(DeepSeekAIService.class); // 日志记录器
    private final AiProperties properties; // AI 配置，如 API 地址、密钥、模型等
    private final OkHttpClient client; // 复用 OkHttp 客户端，性能更好
    private Call currentCall; // 保存当前正在进行的网络请求，用于手动取消

    public DeepSeekAIService(AiProperties properties) { // 构造函数注入配置
        this.properties = properties;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS) // 连接超时 30 秒
                .readTimeout(0, java.util.concurrent.TimeUnit.MILLISECONDS) // 读取超时设为 0 表示永不超时，适合流式响应
                .build();
    }

    @Override
    public void streamAnalysis(String prompt, String systemPrompt, Consumer<String> consumer) { // 流式分析入口
        // 构建 system 消息，角色为 system，内容使用预置的系统提示词
        JSONObject body = getJsonObject(prompt,systemPrompt);

        // 构建 HTTP 请求
        Request request = new Request.Builder()
                .url(properties.getBaseUrl() + "/v1/chat/completions") // 拼接完整请求地址
                .header("Authorization", "Bearer " + properties.getApiKey()) // 添加认证头
                .post(RequestBody.create(body.toJSONString(), MediaType.parse("application/json"))) // POST 提交 JSON 数据
                .build();

        currentCall = client.newCall(request); // 创建异步或同步调用，并保存引用以便外部取消
        try (Response response = currentCall.execute()) { // 执行同步请求，获取响应
            if (!response.isSuccessful()) { // 判断 HTTP 状态码是否成功
                String errBody = response.body() != null ? response.body().string() : "unknown"; // 尝试获取错误详情
                throw new RuntimeException("AI 请求失败: " + response.code() + " " + errBody); // 抛出异常
            }
            ResponseBody responseBody = response.body(); // 获取响应体
            if (responseBody == null) return; // 空响应直接返回

            BufferedReader reader = new BufferedReader(new InputStreamReader(responseBody.byteStream())); // 将字节流包装为字符缓冲流
            String line;
            while ((line = reader.readLine()) != null) { // 逐行读取 SSE 数据流
                if (line.startsWith("data: ")) { // SSE 数据行以 "data: " 开头
                    String data = line.substring(6).trim(); // 截取真正的 JSON 数据部分
                    if ("[DONE]".equals(data)) break; // 收到结束标记，退出循环
                    try {
                        JSONObject json = JSON.parseObject(data); // 解析 JSON
                        JSONArray choices = json.getJSONArray("choices"); // 提取 choices 数组
                        if (choices != null && !choices.isEmpty()) { // 确保有选择项
                            JSONObject delta = choices.getJSONObject(0).getJSONObject("delta"); // 增量内容
                            String content = delta.getString("content"); // 获取文本片段
                            if (content != null) {
                                consumer.accept(content); // 将片段推送给消费者，通常是 SSE 发送
                            }
                        }
                    } catch (Exception e) { // 解析或提取失败时记录警告，继续处理下一行，避免中断流
                        log.warn("解析SSE消息失败: {}", line, e);
                    }
                }
            }
        } catch (IOException e) { // 网络层或读取异常
            if (!client.dispatcher().executorService().isShutdown()) { // 区分是否因应用关闭导致的异常
                log.error("AI 流式请求异常", e); // 记录真实错误
                throw new RuntimeException("AI 服务连接失败: " + e.getMessage(), e); // 抛出运行时异常
            }
        } finally {
            currentCall = null; // 清理引用，防止后续误操作
        }
    }

    private @NonNull JSONObject getJsonObject(String prompt,String systemPrompt) {
        JSONObject systemMessage = new JSONObject();
        systemMessage.put("role", "system");
        systemMessage.put("content", systemPrompt.isEmpty() ? properties.getSystemPrompt():systemPrompt);

        // 构建 user 消息，内容为用户输入的 prompt
        JSONObject userMessage = new JSONObject();
        userMessage.put("role", "user");
        userMessage.put("content", prompt);

        // 组装 messages 数组，包含 system 和 user 两条消息
        JSONArray messages = new JSONArray();
        messages.add(systemMessage);
        messages.add(userMessage);

        // 构建请求体 JSON
        JSONObject body = new JSONObject();
        body.put("model", properties.getModel()); // 指定使用的模型名称
        body.put("messages", messages); // 对话消息
        body.put("stream", true); // 开启流式响应
        return body;
    }

    @Override
    public void close() { // 中断当前正在执行的 AI 请求
        if (currentCall != null) { // 存在活跃调用时
            currentCall.cancel(); // 取消网络请求，释放连接
        }
    }
}