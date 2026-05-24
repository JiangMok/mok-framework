package com.mok.framework.ai.service;

import java.io.Closeable;
import java.util.function.Consumer;

/**
 * 大模型流式分析服务，返回的 Closeable 用于中断请求
 */
public interface AIService extends Closeable {
    /**
     * 执行流式分析
     *
     * @param prompt   用户提示词
     * @param consumer 每段文本回调
     */
    void streamAnalysis(String prompt, Consumer<String> consumer);

    /**
     * 关闭连接（中断当前的流）
     */
    @Override
    void close();
}