package com.mok.framework.ai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * AI 模块线程池配置
 * 替代 Executors.newCachedThreadPool()，由 Spring 统一管理生命周期
 *
 * @author mok
 * @date 2026/07/01
 */
@Configuration
public class AiThreadPoolConfig {

    @Bean("aiAnalysisExecutor")
    public Executor aiAnalysisExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(50);
        executor.setKeepAliveSeconds(120);
        executor.setThreadNamePrefix("ai-analysis-");
        // 队列满时快速失败，避免耗时 AI 请求占用 Web 请求线程
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        // 应用关闭时等待任务完成
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }
}
