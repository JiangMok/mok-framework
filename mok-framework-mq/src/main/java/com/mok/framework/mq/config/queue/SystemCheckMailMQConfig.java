package com.mok.framework.mq.config.queue;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

import static com.mok.framework.common.constant.mq.SystemCheckMailMQConstant.*;


/**
 * 系统检查MQ配置类
 */
@Configuration
public class SystemCheckMailMQConfig {

    /**
     * 创建队列
     */
    @Bean
    public Queue systemCheckMailQueue() {
        Map<String,Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange",SYSTEM_CHECK_MAIL_DLX_EXCHANGE);
        args.put("x-dead-letter-routing-key",SYSTEM_CHECK_MAIL_DLX_ROUTING_KEY);
        return QueueBuilder
                .durable(SYSTEM_CHECK_MAIL_QUEUE)   //持久化队列
                .withArguments(args)            //配置参数 - 为操作日志的消息队列配置死信队列相关参数,告诉它把死信发到哪里
                .build();

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
        return new DirectExchange(SYSTEM_CHECK_MAIL_EXCHANGE, true, false);
    }

    /**
     * 通过路由键绑定队列和交换机
     */
    @Bean
    public Binding systemCheckMailBinding() {
        return BindingBuilder
                .bind(systemCheckMailQueue())
                .to(systemCheckMailExchange())
                .with(SYSTEM_CHECK_MAIL_ROUTING_KEY);
    }

    /**
     * @description:  构建系统检查邮件的私死信队列
     * @author: mok
     * @date: 2026/6/29 13:14
     * @return: org.springframework.amqp.core.Queue
    **/
    @Bean
    public Queue systemCheckMailDlxQueue(){
        return new Queue(SYSTEM_CHECK_MAIL_DLX_QUEUE,true,false,false);
    }

    /**
     * @description:  构建系统检查邮件的死信交换机
     * @author: mok
     * @date: 2026/6/29 13:15
     * @return: org.springframework.amqp.core.DirectExchange
    **/
    @Bean
    public DirectExchange systemCheckMailDlxExchange(){
        return new DirectExchange(SYSTEM_CHECK_MAIL_DLX_EXCHANGE,true,false);
    }

    /**
     * @description:  将系统检查邮件的死信队列绑定到交换机
     * @author: mok
     * @date: 2026/6/29 13:17
     * @return: org.springframework.amqp.core.Binding
    **/
    @Bean
    public Binding systemCheckMailDlxBinding(){
        return BindingBuilder
                .bind(systemCheckMailDlxQueue())
                .to(systemCheckMailDlxExchange())
                .with(SYSTEM_CHECK_MAIL_DLX_ROUTING_KEY);
    }

}
