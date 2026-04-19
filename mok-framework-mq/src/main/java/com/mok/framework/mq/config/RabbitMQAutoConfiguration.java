package com.mok.framework.mq.config;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.context.annotation.Configuration;

/**
 * 启用RabbitMQ注解支持
 * 这个配置类告诉Spring Boot启用RabbitMQ的相关功能
 */
@Configuration
@EnableRabbit  // 这个注解启用@RabbitListener等注解
public class RabbitMQAutoConfiguration {
    // 这个类不需要写任何代码，注解已经完成了所有配置
}