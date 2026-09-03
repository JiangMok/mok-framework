package com.mok.framework.ai.service;

import java.util.function.Consumer;

/**
 * 大模型流式分析服务。
 * 每次请求返回独立的句柄，避免并发请求共享同一个取消状态。
 */
public interface AIService {

    /**
     * 创建一次流式分析调用。
     *
     * @param prompt       用户提示词
     * @param systemPrompt 系统提示词，为空时使用配置默认值
     * @param consumer     每段文本回调
     * @return 当前请求独享的流式调用句柄
     */
    AIStreamHandle createStream(String prompt,
                                String systemPrompt,
                                Consumer<String> consumer);
}
