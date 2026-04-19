package com.mok.framework.mq.consumer;

import com.mok.framework.elasticsearch.service.OperationLogService;
import com.mok.framework.model.dto.OperationLogMessage;
import com.mok.framework.model.entity.OperationLogEntity;
import com.mok.framework.mq.config.OperationLogMQConfig;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 操作日志消费者
 * <p>
 * 操作日志生产者在 com.mok.framework.common.aspect.OperationLogAspect 里
 */
@Component
public class OperationLogConsumer {

    private static final Logger log = LoggerFactory.getLogger(OperationLogConsumer.class);

    private final OperationLogService operationLogService;

    public OperationLogConsumer(OperationLogService operationLogService) {
        this.operationLogService = operationLogService;
    }

    @RabbitListener(queues = OperationLogMQConfig.OPERATION_LOG_QUEUE)
    public void handleOperationLog(OperationLogMessage message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        log.info("========== 接收到操作日志消息 : {}", message.getTitle());
        try {
            if (operationLogService.findById(message.getId()) != null) {
                // 确认已处理的消息
                // 参数1: deliveryTag >>> 消息的投递标签/递送编号
                // 参数2: boolean >>> 是否批量确认
                //          false : (常用)仅确认当前这一条信息,（对应 deliveryTag 的这一条）
                //          true : (!!!谨慎使用)批量确认,确认这条消息及以前的所有未确认的消息
                channel.basicAck(deliveryTag, false);
                log.info("========== 操作日志已存在，跳过处理: {}", message.getId());
                return;
            }
            // 转换为操作日志实体
            OperationLogEntity operationLogEntity = convertToEntity(message);
            log.debug("========== 操作日志实体信息 : {}", operationLogEntity);
            operationLogService.saveOperationLog(operationLogEntity);
            // 手动确认消息
            channel.basicAck(deliveryTag, false);
            log.info("========== 操作日志保存成功 : {}", message.getTitle());

        } catch (Exception e) {
            log.error("========== 处理操作日志消息失败: {}", e.getMessage(), e);
            try {
                // 手动拒绝消息
                // 参数1: deliveryTag >>> 哪条消息
                // 参数2: boolean >>> 拒绝一条还是一批   false:只拒绝当前这一条   true:拒绝当前及以前所有未确认的消息
                // 参数3: boolean >>> 要不要塞回队列   false:消息不重新入队(被丢弃或者进入死信队列)   true:重新放回队列头部等待再次消费
                channel.basicNack(deliveryTag, false, false);
            } catch (Exception ex) {
                log.error("========== 处理操作日志消息失败>>>拒绝消息并不重新入队: {}", ex.getMessage(), ex);
            }
        }


    }

    /**
     * 将消息DTO转换为数据库实体
     * 这是一个简单的转换方法
     */
    private OperationLogEntity convertToEntity(OperationLogMessage message) {
        OperationLogEntity entity = new OperationLogEntity();

        // 设置基本字段
        entity.setId(message.getId());
        entity.setTitle(message.getTitle());
        entity.setBusinessType(message.getBusinessType());
        entity.setMethod(message.getMethod());
        entity.setRequestMethod(message.getRequestMethod());
        entity.setOperUrl(message.getOperUrl());
        entity.setOperIp(message.getOperIp());
        entity.setOperatorName(message.getOperatorName());
        entity.setOperatorType(message.getOperatorType());
        entity.setOperParam(message.getOperParam());
        entity.setJsonResult(message.getJsonResult());
        entity.setStatus(message.getStatus());
        entity.setErrorMsg(message.getErrorMsg());
        entity.setCreateTime(LocalDateTime.now());
        // 设置时间字段
        if (message.getOperTime() != null) {
            entity.setOperTime(message.getOperTime());
        } else {
            entity.setOperTime(LocalDateTime.now());
        }

        return entity;
    }
}
