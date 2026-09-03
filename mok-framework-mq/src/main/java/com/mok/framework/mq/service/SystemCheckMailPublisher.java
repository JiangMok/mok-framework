package com.mok.framework.mq.service;

import cn.hutool.core.util.IdUtil;
import com.mok.framework.common.utils.LogUtils;
import com.mok.framework.mail.service.MailRecipientService;
import com.mok.framework.mail.util.MailMessageIdGenerator;
import com.mok.framework.model.dto.SystemCheckMailMessage;
import com.mok.framework.model.entity.MailRecipient;
import com.mok.framework.model.enums.MailType;
import org.slf4j.Logger;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.mok.framework.common.constant.mq.SystemCheckMailMQConstant.SYSTEM_CHECK_MAIL_EXCHANGE;
import static com.mok.framework.common.constant.mq.SystemCheckMailMQConstant.SYSTEM_CHECK_MAIL_ROUTING_KEY;

/**
 * 系统健康邮件消息发布器。
 * 将每个启用的订阅人拆分为独立消息，失败时由 RabbitMQ 重试和死信链路接管。
 */
@Component
public class SystemCheckMailPublisher {

    private static final Logger log = LogUtils.getLogger(SystemCheckMailPublisher.class);
    private static final long CONFIRM_TIMEOUT_SECONDS = 10;

    private final RabbitTemplate rabbitTemplate;
    private final MailRecipientService mailRecipientService;
    private final int consumerMaxAttempts;

    public SystemCheckMailPublisher(RabbitTemplate rabbitTemplate,
                                    MailRecipientService mailRecipientService,
                                    @Value("${spring.rabbitmq.listener.simple.retry.max-attempts:3}")
                                    int consumerMaxAttempts) {
        this.rabbitTemplate = rabbitTemplate;
        this.mailRecipientService = mailRecipientService;
        this.consumerMaxAttempts = Math.max(consumerMaxAttempts, 1);
    }

    public int publish(String subject, String content, boolean isHtml) {
        return publish(IdUtil.simpleUUID(), subject, content, isHtml);
    }

    /**
     * 使用稳定事件 ID 发布。同一事件整批重试时，每个收件人的消息 ID 保持不变。
     */
    public int publish(String eventId, String subject, String content, boolean isHtml) {
        List<MailRecipient> recipients =
                mailRecipientService.listByMailType(MailType.SYSTEM_CHECK.getCode());
        if (recipients == null || recipients.isEmpty()) {
            log.warn("没有启用且订阅 SYSTEM_CHECK 的收件人，跳过消息发布");
            return 0;
        }

        List<CorrelationData> confirmations = new ArrayList<>(recipients.size());
        for (MailRecipient recipient : recipients) {
            String messageId = MailMessageIdGenerator.generate(eventId, recipient.getEmail());
            SystemCheckMailMessage message = new SystemCheckMailMessage();
            message.setId(messageId);
            message.setRecipient(recipient.getEmail());
            message.setSubject(subject);
            message.setContent(content);
            message.setHtml(isHtml);

            CorrelationData correlationData = new CorrelationData(messageId);
            confirmations.add(correlationData);
            rabbitTemplate.convertAndSend(
                    SYSTEM_CHECK_MAIL_EXCHANGE,
                    SYSTEM_CHECK_MAIL_ROUTING_KEY,
                    message,
                    amqpMessage -> {
                        amqpMessage.getMessageProperties().setHeader(
                                "x-consumer-max-attempts", consumerMaxAttempts);
                        return amqpMessage;
                    },
                    correlationData);
        }

        for (CorrelationData correlationData : confirmations) {
            awaitConfirm(correlationData);
        }

        log.info("系统健康邮件消息已发布，共 {} 个收件人", recipients.size());
        return recipients.size();
    }

    private void awaitConfirm(CorrelationData correlationData) {
        try {
            CorrelationData.Confirm confirm = correlationData.getFuture()
                    .get(CONFIRM_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!confirm.isAck()) {
                throw new AmqpException("RabbitMQ拒绝消息: " + confirm.getReason());
            }
            if (correlationData.getReturned() != null) {
                throw new AmqpException("RabbitMQ消息无法路由: "
                        + correlationData.getReturned().getReplyText());
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new AmqpException("等待RabbitMQ确认时线程被中断", exception);
        } catch (ExecutionException | TimeoutException exception) {
            throw new AmqpException("等待RabbitMQ消息确认失败", exception);
        }
    }
}
