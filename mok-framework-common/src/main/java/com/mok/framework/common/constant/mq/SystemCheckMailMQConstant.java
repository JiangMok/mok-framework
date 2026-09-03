package com.mok.framework.common.constant.mq;

/**
 *
 * @author: mok
 * @date: 2026/7/15
 */
public class SystemCheckMailMQConstant {
    // 系统检查-队列名称
    public static final String SYSTEM_CHECK_MAIL_QUEUE = "system.check.mail.queue";
    // 系统检查-交换机名称
    public static final String SYSTEM_CHECK_MAIL_EXCHANGE = "system.check.mail.exchange";
    // 系统检查-路由键
    public static final String SYSTEM_CHECK_MAIL_ROUTING_KEY = "system.check.mail.routing";

    // 系统检查-死信队列
    public static final String SYSTEM_CHECK_MAIL_DLX_QUEUE = "system.check.mail.dlx.queue";
    // 系统检查-死信交换机
    public static final String SYSTEM_CHECK_MAIL_DLX_EXCHANGE = "system.check.mail.dlx.exchange";
    // 系统检查-死信路由键
    public static final String SYSTEM_CHECK_MAIL_DLX_ROUTING_KEY = "system.check.mail.dlx.routing";

    // 死信持久化失败时进入延迟停车队列，避免数据库故障期间立即热循环
    public static final String SYSTEM_CHECK_MAIL_PARKING_QUEUE = "system.check.mail.parking.queue";
    public static final int SYSTEM_CHECK_MAIL_PARKING_TTL = 30000;

    // Spring AMQP 默认 3 次尝试包含首次消费，因此对应 2 次重试。
    public static final int SYSTEM_CHECK_MAIL_MAX_RETRY = 2;
}
