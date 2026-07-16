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

    public static final int SYSTEM_CHECK_MAIL_MAX_RETRY = 3;
}
