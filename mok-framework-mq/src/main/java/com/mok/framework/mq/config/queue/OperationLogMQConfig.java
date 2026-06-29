package com.mok.framework.mq.config.queue;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;


/**
 * 操作日志的消息队列配置
 */
@Configuration
public class OperationLogMQConfig {

//    // 操作日志-队列名称
//    public static final String OPERATION_LOG_QUEUE = "operation.log.queue";
//    // 操作日志-交换机名称
//    public static final String OPERATION_LOG_EXCHANGE = "operation.log.exchange";
//    // 操作日志-路由键
//    public static final String OPERATION_LOG_ROUTING_KEY = "operation.log.routing";
//
//    // 操作日志-死信队列
//    public static final String OPERATION_LOG_DLX_QUEUE = "operation.log.dlx.queue";
//    // 操作日志-死信交换机
//    public static final String OPERATION_LOG_DLX_EXCHANGE = "operation.log.dlx.exchange";
//    // 操作日志-死信路由键
//    public static final String OPERATION_LOG_DLX_ROUTING_KEY = "operation.log.dlx.routing";

    public static final int OPERATION_LOG_MAX_RETRY = 3;

    // 操作日志-队列名称
    public static final String OPERATION_LOG_QUEUE = "operation.log.dev.queue";
    // 操作日志-交换机名称
    public static final String OPERATION_LOG_EXCHANGE = "operation.log.dev.exchange";
    // 操作日志-路由键
    public static final String OPERATION_LOG_ROUTING_KEY = "operation.log.dev.routing";

    // 操作日志-死信队列
    public static final String OPERATION_LOG_DLX_QUEUE = "operation.log.dlx.dev.queue";
    // 操作日志-死信交换机
    public static final String OPERATION_LOG_DLX_EXCHANGE = "operation.log.dlx.dev.exchange";
    // 操作日志-死信路由键
    public static final String OPERATION_LOG_DLX_ROUTING_KEY = "operation.log.dlx.dev.routing";

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
        Map<String,Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange",OPERATION_LOG_DLX_EXCHANGE);
        args.put("x-dead-letter-routing-key",OPERATION_LOG_DLX_ROUTING_KEY);
        return QueueBuilder
                .durable(OPERATION_LOG_QUEUE)   //持久化队列
                .withArguments(args)            //配置参数 - 为操作日志的消息队列配置死信队列相关参数,告诉它把死信发到哪里
                .build();
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

    /**
     * @description:  构建操作日志死信队列
     * @author: mok
     * @date: 2026/6/29 10:23
    **/
    @Bean
    public Queue operationLogDlxQueue(){
        return new Queue(OPERATION_LOG_DLX_QUEUE,true,false,false);
    }

    /**
     * @description:  构建操作日志死信交换机
     * @author: mok
     * @date: 2026/6/29 10:25
    **/
    @Bean
    public DirectExchange operationLogDlxExchange(){
        return new DirectExchange(OPERATION_LOG_DLX_EXCHANGE,true,false);
    }

    /**
     * @description:  绑定操作日志死信队列到死信交换机
     * @author: mok
     * @date: 2026/6/29 10:27
    **/
    @Bean
    public Binding operationLogDlxBinding(){
        return BindingBuilder
                .bind(operationLogDlxQueue())
                .to(operationLogDlxExchange())
                .with(OPERATION_LOG_DLX_ROUTING_KEY);
    }

}
