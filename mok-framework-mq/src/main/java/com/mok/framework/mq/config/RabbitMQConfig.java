package com.mok.framework.mq.config;

import com.mok.framework.common.utils.LogUtils;
import org.slf4j.Logger;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
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
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);

        // 设置JSON消息转换器
        rabbitTemplate.setMessageConverter(jsonMessageConverter());

        // 设置消息发送确认回调（可选）
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                log.info("========== 消息发送成功");
            } else {
                log.info("========== 消息发送失败:{}", cause);
            }
        });

        return rabbitTemplate;
    }
}