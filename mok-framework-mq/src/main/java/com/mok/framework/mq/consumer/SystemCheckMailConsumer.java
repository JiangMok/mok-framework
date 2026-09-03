package com.mok.framework.mq.consumer;

import com.alibaba.fastjson2.JSON;
import com.mok.framework.common.utils.LogUtils;
import com.mok.framework.mail.service.MailService;
import com.mok.framework.model.dto.SystemCheckMailMessage;
import com.mok.framework.model.enums.MailType;
import com.mok.framework.model.enums.MessageType;
import com.mok.framework.mq.service.MqFailedMessageSaver;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import static com.mok.framework.common.constant.mq.SystemCheckMailMQConstant.SYSTEM_CHECK_MAIL_QUEUE;

/**
 * 系统检查邮件发送队列消费者
 */
@Component
public class SystemCheckMailConsumer {

    private static final Logger log = LogUtils.getLogger(SystemCheckMailConsumer.class);

    private final MqFailedMessageSaver mqFailedMessageSaver;
    private final MailService mailService;

    public SystemCheckMailConsumer(MqFailedMessageSaver mqFailedMessageSaver,
                                   MailService mailService){
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
    @RabbitListener(queues = SYSTEM_CHECK_MAIL_QUEUE)
    public void handleSystemCheckMail(SystemCheckMailMessage msg, Channel channel,
                                      @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        log.info("======== 接收到系统健康检查邮件的消息: {}", msg.getSubject());
        try {
            mailService.sendAndLogMail(
                    msg.getRecipient(),
                    msg.getSubject(),
                    msg.getContent(),
                    msg.getId(),
                    MailType.SYSTEM_CHECK,
                    msg.isHtml()
            );
            // 无异常，发送成功
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("邮件发送失败: {}", e.getMessage());
            // 交给 Spring AMQP Retry 处理；重试耗尽后再按容器策略进入死信队列
            throw new AmqpException("系统健康邮件发送失败", e);
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
