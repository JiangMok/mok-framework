package com.mok.framework.mq.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * 系统检查MQ配置类
 */
@Configuration
public class SystemCheckMailMQConfig {

    // 系统检查-队列名称
    public static final String SYSTEM_CHECK_MAIN_QUEUE = "system.check.mail.queue";
    // 系统检查-交换机名称
    public static final String SYSTEM_CHECK_MAIN_EXCHANGE = "system.check.mail.exchange";
    // 系统检查-路由键
    public static final String SYSTEM_CHECK_MAIN_ROUTING_KEY = "system.check.mail.routing";

    /**
     * 创建队列
     */
    @Bean
    public Queue systemCheckMailQueue() {
        // 参数说明：
        // 1. queue: 队列名称
        // 2. durable: 是否持久化（true表示重启后队列还在）
        // 3. exclusive: 是否排他（true表示仅允许当前连接使用）
        // 4. autoDelete: 是否自动删除（没有消费者时自动删除）
        return new Queue(SYSTEM_CHECK_MAIN_QUEUE, true, true, false);
    }

    /**
     * 创建交换机
     */
    @Bean
    public DirectExchange systemCheckMailExchange() {
        // 参数说明：
        // 1. name: 交换机名称
        // 2. durable: 是否持久化
        // 3. autoDelete: 是否自动删除
        return new DirectExchange(SYSTEM_CHECK_MAIN_EXCHANGE, true, false);
    }

    /**
     * 通过路由键绑定队列和交换机
     */
    @Bean
    public Binding systemCheckMailBinding() {
        return BindingBuilder
                .bind(systemCheckMailQueue())
                .to(systemCheckMailExchange())
                .with(SYSTEM_CHECK_MAIN_ROUTING_KEY);
    }

}
