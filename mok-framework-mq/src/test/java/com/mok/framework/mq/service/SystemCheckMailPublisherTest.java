package com.mok.framework.mq.service;

import com.mok.framework.mail.service.MailRecipientService;
import com.mok.framework.model.dto.SystemCheckMailMessage;
import com.mok.framework.model.entity.MailRecipient;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SystemCheckMailPublisherTest {

    @Test
    void onlyReportsSuccessAfterBrokerAck() {
        RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
        MailRecipientService recipientService = recipientService();
        completeConfirm(rabbitTemplate, true);
        SystemCheckMailPublisher publisher =
                new SystemCheckMailPublisher(rabbitTemplate, recipientService, 3);

        assertThat(publisher.publish("subject", "content", true)).isEqualTo(1);
    }

    @Test
    void brokerNackIsReportedAsPublishFailure() {
        RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
        completeConfirm(rabbitTemplate, false);
        SystemCheckMailPublisher publisher =
                new SystemCheckMailPublisher(rabbitTemplate, recipientService(), 3);

        assertThatThrownBy(() -> publisher.publish("subject", "content", true))
                .isInstanceOf(AmqpException.class);
    }

    @Test
    void sameAlertAndRecipientAlwaysUseTheSameMessageId() {
        RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
        List<String> messageIds = new CopyOnWriteArrayList<>();
        doAnswer(invocation -> {
            SystemCheckMailMessage message = invocation.getArgument(2);
            CorrelationData correlationData = invocation.getArgument(4);
            messageIds.add(message.getId());
            correlationData.getFuture().complete(new CorrelationData.Confirm(true, null));
            return null;
        }).when(rabbitTemplate).convertAndSend(
                eq("system.check.mail.exchange"),
                eq("system.check.mail.routing"),
                any(),
                any(MessagePostProcessor.class),
                any(CorrelationData.class));
        SystemCheckMailPublisher publisher =
                new SystemCheckMailPublisher(rabbitTemplate, recipientService(), 3);

        publisher.publish("alert-1", "subject", "content", true);
        publisher.publish("alert-1", "subject", "content", true);

        assertThat(messageIds).hasSize(2);
        assertThat(messageIds.get(0)).isEqualTo(messageIds.get(1));
    }

    private MailRecipientService recipientService() {
        MailRecipientService service = mock(MailRecipientService.class);
        MailRecipient recipient = new MailRecipient();
        recipient.setEmail("receiver@example.com");
        when(service.listByMailType("SYSTEM_CHECK")).thenReturn(List.of(recipient));
        return service;
    }

    private void completeConfirm(RabbitTemplate rabbitTemplate, boolean ack) {
        doAnswer(invocation -> {
            CorrelationData correlationData = invocation.getArgument(4);
            correlationData.getFuture().complete(new CorrelationData.Confirm(ack, ack ? null : "nack"));
            return null;
        }).when(rabbitTemplate).convertAndSend(
                eq("system.check.mail.exchange"),
                eq("system.check.mail.routing"),
                any(),
                any(MessagePostProcessor.class),
                any(CorrelationData.class));
    }
}
