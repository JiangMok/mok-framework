package com.mok.framework.mq.consumer;

import cn.hutool.extra.mail.MailUtil;
import com.mok.framework.model.dto.SystemCheckMailMessage;
import com.mok.framework.mq.config.SystemCheckMailMQConfig;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

/**
 * 系统检查邮件发送队列消费者
 */
@Component
public class SystemCheckMailConsumer {

    private static final Logger log = LoggerFactory.getLogger(SystemCheckMailConsumer.class);

    /**
     * 监听系统健康检查的邮件发送队列
     *
     * @param message     SystemCheckMailMessage
     * @param channel
     * @param deliveryTag
     */
    @RabbitListener(queues = SystemCheckMailMQConfig.SYSTEM_CHECK_MAIN_QUEUE)
    public void handleSystemCheckMail(
            SystemCheckMailMessage message,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        try {
            log.info("========== 接收到系统检查邮件发送消息 : {}", message.getSubject());
            // 发送邮件
            MailUtil.send(message.getRecipient(), message.getSubject(), message.getContent(), false);
            // 确认已处理的消息
            // 参数1: deliveryTag >>> 消息的投递标签/递送编号
            // 参数2: boolean >>> 是否批量确认
            //          false : (常用)仅确认当前这一条信息,（对应 deliveryTag 的这一条）
            //          true : (!!!谨慎使用)批量确认,确认这条消息及以前的所有未确认的消息s
            channel.basicAck(deliveryTag, false);
            log.info("========== 系统检查邮件发送成功 : {}", message.getSubject());
        } catch (Exception e) {
            log.info("========== 系统检查邮件发送失败  : {}", message.getSubject());
            try {
                // 手动拒绝消息
                // 参数1: deliveryTag >>> 哪条消息
                // 参数2: boolean >>> 拒绝一条还是一批   false:只拒绝当前这一条   true:拒绝当前及以前所有未确认的消息
                // 参数3: boolean >>> 要不要塞回队列   false:消息不重新入队(被丢弃或者进入死信队列)   true:重新放回队列头部等待再次消费
                channel.basicNack(deliveryTag, false, false);
            } catch (Exception ex) {
                log.error("========== 系统检查邮件发送失败>>>拒绝消息并不重新入队: {}", ex.getMessage(), ex);
            }
        }

    }
}
