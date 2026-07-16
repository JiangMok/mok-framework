package com.mok.framework.mq.consumer;

import com.alibaba.fastjson2.JSON;
import com.mok.framework.common.utils.LogUtils;
import com.mok.framework.model.enums.MessageType;
import com.mok.framework.mq.service.MqFailedMessageSaver;
import com.mok.framework.operationLog.service.OperationLogService;
import com.mok.framework.model.dto.OperationLogMessage;
import com.mok.framework.model.entity.OperationLogEntity;
import com.mok.framework.mq.config.queue.OperationLogMQConfig;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

import static com.mok.framework.common.constant.mq.OperationLogMQConstant.*;

/**
 * 操作日志消费者
 * <p>
 * 操作日志生产者在 com.mok.framework.common.aspect.OperationLogAspect 里
 */
@Component
public class OperationLogConsumer {

    private static final Logger log = LogUtils.getLogger(OperationLogConsumer.class);

    private final OperationLogService operationLogService;
    private final MqFailedMessageSaver mqFailedMessageSaver;

    public OperationLogConsumer(OperationLogService operationLogService,
                                MqFailedMessageSaver mqFailedMessageSaver) {
        this.operationLogService = operationLogService;
        this.mqFailedMessageSaver = mqFailedMessageSaver;
    }

    @RabbitListener(queues = OPERATION_LOG_QUEUE)
    public void handleOperationLog(OperationLogMessage message, Channel channel,
                                   @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        log.info("接收到操作日志消息: {}", message.getTitle());

        OperationLogEntity logEntity = convertToEntity(message);
        // 确保实体设置消息ID
        logEntity.setId(message.getId());

        try {
            // 1. 幂等性检查：查询已有日志
            OperationLogEntity existLog = operationLogService.findById(message.getId());

            // 2. 已成功处理（status=0），直接确认
            if (existLog != null && Integer.valueOf(0).equals(existLog.getStatus())) {
                channel.basicAck(deliveryTag, false);
                log.info("操作日志已成功处理，跳过: {}", message.getId());
                return;
            }

            // 3. 失败重试次数判断
            int currentRetry = (existLog != null && existLog.getRetryCount() != null) ? existLog.getRetryCount() : 0;
            if (existLog != null
                    && Integer.valueOf(1).equals(existLog.getStatus())
                    && currentRetry >= OPERATION_LOG_MAX_RETRY) {
                log.warn("消息 {} 重试次数已达上限 {}，丢弃",
                        message.getId(),
                        OPERATION_LOG_MAX_RETRY);
                channel.basicAck(deliveryTag, false);
                return;
            }

            // 4. 业务处理：保存操作日志（成功）
            logEntity.setStatus(0);
            logEntity.setRetryCount(currentRetry);
            saveOrUpdateLog(logEntity);
            channel.basicAck(deliveryTag, false);
            log.info("操作日志保存成功: {}", message.getTitle());

        } catch (Exception e) {
            log.error("处理操作日志失败: {}", e.getMessage(), e);
            // 失败处理：status=1，重试次数+1
            int retryCount = (logEntity.getRetryCount() == null ? 0 : logEntity.getRetryCount()) + 1;
            logEntity.setStatus(1);
            logEntity.setRetryCount(retryCount);
            saveOrUpdateLog(logEntity);

            try {
                // 重新入队，触发重试
                channel.basicNack(deliveryTag, false, true);
            } catch (IOException ex) {
                log.error("拒绝消息失败", ex);
            }
        }
    }

    /**
     * 保存或更新操作日志（根据 messageId 判断）
     */
    private void saveOrUpdateLog(OperationLogEntity entity) {
        OperationLogEntity existLog = operationLogService.findById(entity.getId());
        if (existLog == null) {
            operationLogService.saveOperationLog(entity);
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


    @RabbitListener(queues = OPERATION_LOG_DLX_QUEUE)
    public void handleDlxOperationLog(Message message,
                                      Channel channel,
                                      @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        mqFailedMessageSaver.saveAndAck(message, channel, deliveryTag,
                MessageType.OPERATION_LOG,
                OPERATION_LOG_DLX_QUEUE,
                (body, record) -> {
                    OperationLogMessage opLog = JSON.parseObject(body, OperationLogMessage.class);
                    record.setMessageId(opLog.getId());
                    if (opLog.getOperTime() != null) {
                        record.setOriginalTimestamp(opLog.getOperTime());
                    }
                });
    }
}
