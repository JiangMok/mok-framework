package com.mok.framework.mq.service;

import com.mok.framework.common.utils.LogUtils;
import com.mok.framework.model.entity.MqFailedMessage;
import com.mok.framework.model.enums.MessageType;
import com.mok.framework.mq.util.MqFailedMessageBuilder;
import com.mok.framework.mq.util.MessageFieldExtractor;
import org.slf4j.Logger;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.mok.framework.common.constant.mq.SystemCheckMailMQConstant.SYSTEM_CHECK_MAIL_PARKING_QUEUE;

@Component
public class MqFailedMessageSaver {

    private static final Logger log = LogUtils.getLogger(MqFailedMessageSaver.class);

    private final MqFailedMessageService mqFailedMessageService;
    private final RabbitTemplate rabbitTemplate;

    public MqFailedMessageSaver(MqFailedMessageService mqFailedMessageService,
                                RabbitTemplate rabbitTemplate){
        this.mqFailedMessageService=mqFailedMessageService;
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * 处理死信消息：构建记录 -> 提取业务字段 -> 持久化 -> 手动ACK
     * 若保存失败，则转入带 TTL 的停车队列，延迟后重新尝试，避免消息丢失和热循环。
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
        MqFailedMessage record;
        try {
            // 1. 构建基础记录
            record = MqFailedMessageBuilder.buildBaseRecord(message, messageType, deadQueue);

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
        } catch (Exception e) {
            log.error("========== 死信记录保存失败，消息进入延迟停车队列: {}", e.getMessage(), e);
            parkAndAck(message, channel, deliveryTag);
            return;
        }

        try {
            channel.basicAck(deliveryTag, false);
        } catch (IOException exception) {
            // 记录ID是确定性的，Broker 重投后不会重复插入。
            throw new AmqpException("死信记录已保存但ACK失败", exception);
        }
    }

    private void parkAndAck(Message message, Channel channel, long deliveryTag) {
        try {
            CorrelationData correlationData = new CorrelationData(
                    "parking-" + UUID.randomUUID().toString().replace("-", ""));
            rabbitTemplate.send("", SYSTEM_CHECK_MAIL_PARKING_QUEUE, message, correlationData);
            CorrelationData.Confirm confirm = correlationData.getFuture().get(10, TimeUnit.SECONDS);
            if (!confirm.isAck() || correlationData.getReturned() != null) {
                throw new AmqpException("死信消息进入停车队列失败");
            }
            channel.basicAck(deliveryTag, false);
        } catch (Exception parkingException) {
            log.error("========== 消息进入停车队列失败，保留原消息重试", parkingException);
            try {
                channel.basicNack(deliveryTag, false, true);
            } catch (IOException ex) {
                log.error("========== 拒绝消息失败", ex);
            }
        }
    }
}
