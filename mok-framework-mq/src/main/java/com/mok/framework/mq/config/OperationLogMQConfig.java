package com.mok.framework.mq.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * 操作日志的消息队列配置
 */
@Configuration
public class OperationLogMQConfig {

    // 操作日志-队列名称
    public static final String OPERATION_LOG_QUEUE = "operation.log.queue";
    // 操作日志-交换机名称
    public static final String OPERATION_LOG_EXCHANGE = "operation.log.exchange";
    // 操作日志-路由键
    public static final String OPERATION_LOG_ROUTING_KEY = "operation.log.routing";

    /**
     * 创建操作日志队列
     */
    @Bean
    public Queue operationLogQueue() {
        // 参数说明：
        // 1. queue: 队列名称
        // 2. durable: 是否持久化（true表示重启后队列还在）
        // 3. exclusive: 是否排他（true表示仅允许当前连接使用）
        // 4. autoDelete: 是否自动删除（没有消费者时自动删除）
        return new Queue(OPERATION_LOG_QUEUE, true, false, false);
    }

    /**
     * 创建操作日志交换机
     */
    @Bean
    public DirectExchange operationLogExchange() {
        // 参数说明：
        // 1. name: 交换机名称
        // 2. durable: 是否持久化
        // 3. autoDelete: 是否自动删除
        return new DirectExchange(OPERATION_LOG_EXCHANGE, true, false);
    }

    /**
     * 绑定队列到交换机
     */
    @Bean
    public Binding operationLogBinding() {
        return BindingBuilder
                // 绑定队列
                .bind(operationLogQueue())
                // 到交换机
                .to(operationLogExchange())
                // 使用路由键
                .with(OPERATION_LOG_ROUTING_KEY);
    }

}
