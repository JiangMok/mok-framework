package com.mok.framework.mq.consumer;

import cn.hutool.extra.mail.MailAccount;
import cn.hutool.extra.mail.MailUtil;
import com.alibaba.fastjson2.JSON;
import com.mok.framework.common.utils.LogUtils;
import com.mok.framework.mail.service.MailLogService;
import com.mok.framework.mail.service.MailService;
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
import java.util.Optional;

/**
 * 系统检查邮件发送队列消费者
 */
@Component
public class SystemCheckMailConsumer {

    private static final Logger log = LogUtils.getLogger(SystemCheckMailConsumer.class);

    private final MailLogService mailLogService;
    private final MqFailedMessageSaver mqFailedMessageSaver;
    private final MailService mailService;

    public SystemCheckMailConsumer(MailLogService mailLogService,
                                   MqFailedMessageSaver mqFailedMessageSaver,
                                   MailService mailService){
        this.mailLogService=mailLogService;
        this.mqFailedMessageSaver=mqFailedMessageSaver;
        this.mailService=mailService;
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
        try {
            mailService.sendAndLogMail(
                    msg.getRecipient(),
                    msg.getSubject(),
                    msg.getContent(),
                    msg.getId(),
                    MailType.SYSTEM_CHECK,
                    false
            );
            // 无异常，发送成功
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("邮件发送失败: {}", e.getMessage());
            // 日志已在 sendAndLogMail 中记录，这里只需拒绝消息
            try {
                channel.basicNack(deliveryTag, false, false); // 不重新入队
            } catch (IOException ex) {
                log.error("拒绝消息失败", ex);
            }
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
