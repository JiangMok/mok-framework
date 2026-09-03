package com.mok.framework.mq.consumer;

import com.mok.framework.mail.service.MailService;
import com.mok.framework.model.dto.SystemCheckMailMessage;
import com.mok.framework.mq.service.MqFailedMessageSaver;
import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.AmqpException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class SystemCheckMailConsumerTest {

    @Test
    void propagatesFailureSoContainerRetryCanRun() throws Exception {
        MailService mailService = mock(MailService.class);
        Channel channel = mock(Channel.class);
        SystemCheckMailConsumer consumer = new SystemCheckMailConsumer(
                mock(MqFailedMessageSaver.class), mailService);
        SystemCheckMailMessage message = new SystemCheckMailMessage();
        message.setId("message-id");
        message.setRecipient("receiver@example.com");
        message.setSubject("subject");
        message.setContent("content");

        doThrow(new RuntimeException("smtp down"))
                .when(mailService)
                .sendAndLogMail("receiver@example.com", "subject", "content",
                        "message-id", com.mok.framework.model.enums.MailType.SYSTEM_CHECK, false);

        assertThrows(AmqpException.class,
                () -> consumer.handleSystemCheckMail(message, channel, 1L));
        verify(channel, never()).basicNack(1L, false, false);
        verify(channel, never()).basicAck(1L, false);
    }
}

