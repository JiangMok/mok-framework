package com.mok.framework.mq.util;

import com.mok.framework.model.entity.MqFailedMessage;
import com.mok.framework.model.enums.MessageType;
import org.springframework.amqp.core.Message;
import java.time.LocalDateTime;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static com.mok.framework.common.constant.mq.SystemCheckMailMQConstant.SYSTEM_CHECK_MAIL_MAX_RETRY;

/**
 * MQ 死信失败记录构建工具
 * 统一从 Spring AMQP Message 中提取通用字段，返回一个已预填充的 MqFailedMessage 对象
 */
public class MqFailedMessageBuilder {

    /**
     * 构建基础失败记录（业务无关的公共字段）
     *
     * @param message     原始 Spring AMQP Message
     * @param messageType 消息类型枚举
     * @return 已填充基础信息的 MqFailedMessage 对象，业务特有字段（如 messageId、originalTimestamp）需调用方自行补充
     */
    public static MqFailedMessage buildBaseRecord(Message message, MessageType messageType) {
        MqFailedMessage record = new MqFailedMessage();
        
        // 1. 消息类型
        record.setMessageType(messageType.getCode());
        
        // 2. 消息体（字符串）
        String body = new String(message.getBody(), java.nio.charset.StandardCharsets.UTF_8);
        record.setMessageBody(body);
        String idSource = messageType.getCode() + '|' + body + '|'
                + message.getMessageProperties().getReceivedExchange() + '|'
                + message.getMessageProperties().getReceivedRoutingKey();
        record.setId(UUID.nameUUIDFromBytes(idSource.getBytes(StandardCharsets.UTF_8))
                .toString().replace("-", ""));
        
        // 3. 队列与交换机信息
        Map<String, Object> headers = message.getMessageProperties().getHeaders();
        Map<?, ?> originalDeath = getOldestDeath(headers.get("x-death"));
        Object originalQueue = originalDeath != null ? originalDeath.get("queue") : null;
        record.setOriginalQueue(originalQueue != null
                ? originalQueue.toString()
                : message.getMessageProperties().getConsumerQueue());
        // 死信队列名通常从消费者注解可知，此处留空由调用方设置或保持 null
        record.setDlxExchange(message.getMessageProperties().getReceivedExchange());
        record.setDlxRoutingKey(message.getMessageProperties().getReceivedRoutingKey());
        
        // 4. 死信原因与 x-death 头
        String reason = Optional.ofNullable(headers.get("x-death")).orElse("未知原因").toString();
        record.setFailReason(reason);
        if (headers.get("x-death") != null) {
            record.setXDeathHeader(headers.get("x-death").toString());
        }
        
        // 5. 重试与状态默认值
        Object configuredAttempts = headers.get("x-consumer-max-attempts");
        int maxRetry = configuredAttempts instanceof Number number
                ? Math.max(0, number.intValue() - 1)
                : SYSTEM_CHECK_MAIL_MAX_RETRY;
        record.setRetryCount(maxRetry);
        record.setMaxRetry(maxRetry);
        record.setStatus("PENDING");
        
        // 6. 时间
        record.setFailedTime(LocalDateTime.now());
        
        return record;
    }

    public static MqFailedMessage buildBaseRecord(Message message, MessageType messageType, String deadQueue) {
        MqFailedMessage record = buildBaseRecord(message, messageType);
        record.setDeadQueue(deadQueue);
        return record;
    }

    private static Map<?, ?> getOldestDeath(Object xDeathHeader) {
        if (xDeathHeader instanceof java.util.List<?> deaths) {
            // RabbitMQ 按最近一次死亡排在最前；停车队列过期后，最末项才是原业务队列。
            for (int index = deaths.size() - 1; index >= 0; index--) {
                if (deaths.get(index) instanceof Map<?, ?> death) {
                    return death;
                }
            }
        }
        return null;
    }

}
