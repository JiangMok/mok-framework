package com.mok.framework.mq.service;

import com.mok.framework.common.utils.LogUtils;
import com.mok.framework.model.entity.MqFailedMessage;
import com.mok.framework.model.enums.MessageType;
import com.mok.framework.mq.util.MqFailedMessageBuilder;
import com.mok.framework.mq.util.MessageFieldExtractor;
import org.slf4j.Logger;
import org.springframework.amqp.core.Message;
import com.rabbitmq.client.Channel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class MqFailedMessageSaver {

    private static final Logger log = LogUtils.getLogger(MqFailedMessageSaver.class);

    private final MqFailedMessageService mqFailedMessageService;

    public MqFailedMessageSaver(MqFailedMessageService mqFailedMessageService){
        this.mqFailedMessageService=mqFailedMessageService;
    }

    /**
     * 处理死信消息：构建记录 -> 提取业务字段 -> 持久化 -> 手动ACK
     * 若保存失败，则 NACK 并丢弃消息（避免死循环）
     *
     * @param message      原始消息
     * @param channel      通道
     * @param deliveryTag  投递标签
     * @param messageType  消息类型枚举
     * @param deadQueue    当前死信队列名称
     * @param extractor    业务字段提取器，可为 null（不提取额外字段）
     */
    public void saveAndAck(Message message, Channel channel, long deliveryTag,
                           MessageType messageType, String deadQueue,
                           MessageFieldExtractor extractor) {
        try {
            // 1. 构建基础记录
            MqFailedMessage record = MqFailedMessageBuilder.buildBaseRecord(message, messageType, deadQueue);

            // 2. 提取业务特有字段
            if (extractor != null) {
                try {
                    extractor.extract(message.getBody(), record);
                } catch (Exception e) {
                    log.warn("========== 死信消息业务字段提取失败: {}", e.getMessage());
                }
            }

            // 3. 持久化
            mqFailedMessageService.saveMqFailedMessage(record);

            // 4. 手动确认
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("========== 死信记录保存失败，消息将被丢弃: {}", e.getMessage(), e);
            try {
                channel.basicNack(deliveryTag, false, false);
            } catch (IOException ex) {
                log.error("========== 拒绝消息失败", ex);
            }
        }
    }
}