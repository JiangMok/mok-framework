package com.mok.framework.ai.service;

/**
 * 单次 AI 流式请求句柄。
 */
public interface AIStreamHandle extends AutoCloseable {

    /**
     * 同步执行流式请求，通常由受控线程池调用。
     */
    void execute();

    /**
     * 取消当前请求。
     */
    void cancel();

    @Override
    default void close() {
        cancel();
    }
}
