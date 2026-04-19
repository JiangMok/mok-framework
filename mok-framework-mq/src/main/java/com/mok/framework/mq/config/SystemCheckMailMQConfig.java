package com.mok.framework.mq.config;

import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * 系统检查MQ配置类
 */
@Configuration
public class SystemCheckMQConfig {

    // 系统检查-队列名称
    public static final String SYSTEM_CHECK_QUEUE = "system.check.queue";
    // 系统检查-交换机名称
    public static final String SYSTEM_CHECK_EXCHANGE = "system.check.exchange";
    // 系统检查-路由键
    public static final String SYSTEM_CHECK_ROUTING_KEY = "system.check.routing";

    @Bean
    public Queue systemCheckQueue() {
        // 参数说明：
        // 1. queue: 队列名称
        // 2. durable: 是否持久化（true表示重启后队列还在）
        // 3. exclusive: 是否排他（true表示仅允许当前连接使用）
        // 4. autoDelete: 是否自动删除（没有消费者时自动删除）
        return new Queue(SYSTEM_CHECK_QUEUE, true, true, false);
    }

    public DirectExchange systemCheckExchange

}
