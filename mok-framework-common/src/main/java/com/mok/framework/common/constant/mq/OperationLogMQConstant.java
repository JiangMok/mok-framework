package com.mok.framework.common.constant.mq;

/**
 *
 * @author: mok
 * @date: 2026/7/15
 */
public class OperationLogMQConstant {
    // 操作日志-队列名称
    public static final String OPERATION_LOG_QUEUE = "operation.log.queue";
    // 操作日志-交换机名称
    public static final String OPERATION_LOG_EXCHANGE = "operation.log.exchange";
    // 操作日志-路由键
    public static final String OPERATION_LOG_ROUTING_KEY = "operation.log.routing";

    // 操作日志-死信队列
    public static final String OPERATION_LOG_DLX_QUEUE = "operation.log.dlx.queue";
    // 操作日志-死信交换机
    public static final String OPERATION_LOG_DLX_EXCHANGE = "operation.log.dlx.exchange";
    // 操作日志-死信路由键
    public static final String OPERATION_LOG_DLX_ROUTING_KEY = "operation.log.dlx.routing";

    public static final int OPERATION_LOG_MAX_RETRY = 3;
}
