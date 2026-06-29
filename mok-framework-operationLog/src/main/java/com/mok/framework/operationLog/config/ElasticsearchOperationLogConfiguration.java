package com.mok.framework.operationLog.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@ConditionalOnProperty(name = "operationlogImpl.save-location.type", havingValue = "es")
@EnableElasticsearchRepositories(basePackages = "com.mok.framework.operationLog.repository")
public class ElasticsearchOperationLogConfiguration {
    // 如果需要自定义 RestClient 或 ElasticsearchClient 可以在这里定义
    // 但通常 Spring Boot 自动配置被排除后，必须自己创建 ElasticsearchClient Bean。
    // 如果你不需要连接 ES，这个配置类根本不会被加载。
}