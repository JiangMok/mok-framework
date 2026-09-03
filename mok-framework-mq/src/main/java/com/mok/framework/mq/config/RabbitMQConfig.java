package com.mok.framework.mq.config;

import com.mok.framework.common.utils.LogUtils;
import org.slf4j.Logger;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.autoconfigure.amqp.RabbitTemplateConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * rabbitMQ主配置类
 */
@Configuration
public class RabbitMQConfig {

    private final static Logger log = LogUtils.getLogger(RabbitMQConfig.class);

    /**
     * JSON消息转换器
     * 让RabbitMQ支持发送和接收JSON格式的消息
     */
    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 配置RabbitTemplate
     * RabbitTemplate是发送消息的主要工具类
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         RabbitTemplateConfigurer configurer) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate();
        // 应用 spring.rabbitmq.template 下的 mandatory、retry 等统一配置
        configurer.configure(rabbitTemplate, connectionFactory);

        // 设置JSON消息转换器
        rabbitTemplate.setMessageConverter(jsonMessageConverter());

        // 设置消息发送确认回调（可选）
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                log.debug("========== 消息发送成功: correlationId={}",
                        correlationData != null ? correlationData.getId() : null);
            } else {
                log.error("========== 消息发送失败: correlationId={}, cause={}",
                        correlationData != null ? correlationData.getId() : null, cause);
            }
        });

        rabbitTemplate.setReturnsCallback(returned ->
                log.error("========== 消息路由失败: exchange={}, routingKey={}, replyText={}",
                        returned.getExchange(), returned.getRoutingKey(), returned.getReplyText()));

        return rabbitTemplate;
    }
}
