package com.mok.framework.mq.consumer;

import cn.hutool.extra.mail.MailUtil;
import com.alibaba.fastjson2.JSON;
import com.mok.framework.common.utils.LogUtils;
import com.mok.framework.mail.service.MailLogService;
import com.mok.framework.mail.util.MailLogBuilder;
import com.mok.framework.model.dto.SystemCheckMailMessage;
import com.mok.framework.model.entity.MailLog;
import com.mok.framework.model.enums.MailType;
import com.mok.framework.model.enums.MessageType;
import com.mok.framework.mq.config.queue.SystemCheckMailMQConfig;
import com.mok.framework.mq.service.MqFailedMessageSaver;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * 系统检查邮件发送队列消费者
 */
@Component
public class SystemCheckMailConsumer {

    private static final Logger log = LogUtils.getLogger(SystemCheckMailConsumer.class);

    private final MailLogService mailLogService;
    private final MqFailedMessageSaver mqFailedMessageSaver;

    public SystemCheckMailConsumer(MailLogService mailLogService,
                                   MqFailedMessageSaver mqFailedMessageSaver){
        this.mailLogService=mailLogService;
        this.mqFailedMessageSaver=mqFailedMessageSaver;
    }

    /**
     * 监听系统健康检查的邮件发送队列
     *
     * @param msg     SystemCheckMailMessage
     * @param channel
     * @param deliveryTag
     */
    @RabbitListener(queues = SystemCheckMailMQConfig.SYSTEM_CHECK_MAIL_QUEUE)
    public void handleSystemCheckMail(SystemCheckMailMessage msg, Channel channel,
                                      @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {

        MailLog logEntity = MailLogBuilder.build(
                msg.getId(),
                msg.getRecipient(),
                msg.getSubject(),
                msg.getContent(),
                MailType.SYSTEM_CHECK
        );

        try {
            // 1. 幂等性检查：查询已有日志
            MailLog existLog = mailLogService.getMailLogByMessageId(msg.getId());

            // 2. 已成功，直接确认
            if (existLog != null && "SUCCESS".equals(existLog.getSendStatus())) {
                channel.basicAck(deliveryTag, false);
                return;
            }

            // 3. 失败重试次数判断
            int currentRetryCount = existLog != null ? (existLog.getRetryCount() == null ? 0 : existLog.getRetryCount()) : 0;
            if (existLog != null
                    && "FAILED".equals(existLog.getSendStatus())
                    && currentRetryCount >= SystemCheckMailMQConfig.SYSTEM_CHECK_MAIL_MAX_RETRY) {
                log.warn("消息 {} 重试次数已达上限 {}，不再重试",
                        msg.getId(),
                        SystemCheckMailMQConfig.SYSTEM_CHECK_MAIL_MAX_RETRY);
                channel.basicAck(deliveryTag, false); // 确认丢弃
                return;
            }

            // 4. 发送邮件
            MailUtil.send(msg.getRecipient(), msg.getSubject(), msg.getContent(), false);

            // 5. 记录成功（更新已有记录或新增）
            saveMailLog(logEntity, "SUCCESS", null, currentRetryCount);
            channel.basicAck(deliveryTag, false);

        } catch (Exception e) {
            log.error("系统检查邮件发送失败: {}", e.getMessage());
            // 失败重试次数（当前次数+1）
            int retryCount = (logEntity.getRetryCount() == null ? 0 : logEntity.getRetryCount()) + 1;
            saveMailLog(logEntity, "FAILED", e.getMessage(), retryCount);

            // 重新入队
            try {
                channel.basicNack(deliveryTag, false, true);
            } catch (IOException ex) {
                log.error("拒绝消息失败", ex);
            }
        }
    }

    /**
     * 保存或更新邮件日志
     */
    private void saveMailLog(MailLog logEntity, String status, String failReason, int retryCount) {
        MailLog existLog = mailLogService.getMailLogByMessageId(logEntity.getMessageId());
        if (existLog != null) {
            // 更新
            existLog.setSendStatus(status);
            existLog.setFailReason(failReason);
            existLog.setRetryCount(retryCount);
            existLog.setUpdateTime(LocalDateTime.now());
            mailLogService.updateById(existLog);
        } else {
            // 新增
            logEntity.setSendStatus(status);
            logEntity.setFailReason(failReason);
            logEntity.setRetryCount(retryCount);
            mailLogService.saveMailLog(logEntity);
        }
    }

    @RabbitListener(queues = "system.check.mail.dlx.queue")
    public void handleDlxSystemCheckMail(Message message, Channel channel,
                                         @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        mqFailedMessageSaver.saveAndAck(message, channel, deliveryTag,
                MessageType.SYSTEM_CHECK_MAIL,
                "system.check.mail.dlx.queue",
                (body, record) -> {
                    SystemCheckMailMessage mailMsg = JSON.parseObject(body, SystemCheckMailMessage.class);
                    record.setMessageId(mailMsg.getId());
                    // 如有时间字段可设 originalTimestamp
                });
    }
}
