package com.mok.framework.mq.service;

import com.mok.framework.model.enums.MessageType;
import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class MqFailedMessageSaverTest {

    @Test
    void parksDeadLetterWithDelayWhenPersistenceFails() throws Exception {
        MqFailedMessageService service = mock(MqFailedMessageService.class);
        doThrow(new RuntimeException("database down"))
                .when(service).saveMqFailedMessage(any());
        Channel channel = mock(Channel.class);
        MessageProperties properties = new MessageProperties();
        properties.setConsumerQueue("dead.queue");
        Message message = new Message("{}".getBytes(), properties);
        RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
        doAnswer(invocation -> {
            CorrelationData correlationData = invocation.getArgument(3);
            correlationData.getFuture().complete(new CorrelationData.Confirm(true, null));
            return null;
        }).when(rabbitTemplate).send(eq(""), eq("system.check.mail.parking.queue"),
                same(message), any(CorrelationData.class));

        new MqFailedMessageSaver(service, rabbitTemplate).saveAndAck(
                message, channel, 2L, MessageType.SYSTEM_CHECK_MAIL, "dead.queue", null);

        verify(rabbitTemplate).send(eq(""), eq("system.check.mail.parking.queue"),
                same(message), any(CorrelationData.class));
        verify(channel).basicAck(2L, false);
        verify(channel, never()).basicNack(2L, false, true);
    }
}
