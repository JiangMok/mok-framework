package com.mok.framework.mq.util;

import com.mok.framework.model.entity.MqFailedMessage;
import com.mok.framework.model.enums.MessageType;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MqFailedMessageBuilderTest {

    @Test
    void readsOriginalQueueAndConfiguredConsumerAttempts() {
        MessageProperties properties = new MessageProperties();
        properties.setConsumerQueue("dead.queue");
        properties.setHeader("x-death", List.of(Map.of(
                "queue", "source.queue",
                "count", 2L)));
        properties.setHeader("x-consumer-max-attempts", 3);

        MqFailedMessage record = MqFailedMessageBuilder.buildBaseRecord(
                new Message("{}".getBytes(), properties),
                MessageType.SYSTEM_CHECK_MAIL,
                "dead.queue");

        assertEquals("source.queue", record.getOriginalQueue());
        assertEquals(2, record.getRetryCount());
        assertEquals(2, record.getMaxRetry());

        MqFailedMessage duplicate = MqFailedMessageBuilder.buildBaseRecord(
                new Message("{}".getBytes(), properties),
                MessageType.SYSTEM_CHECK_MAIL,
                "dead.queue");
        assertEquals(record.getId(), duplicate.getId());
    }

    @Test
    void keepsOriginalQueueAfterParkingQueueExpires() {
        MessageProperties properties = new MessageProperties();
        properties.setConsumerQueue("dead.queue");
        properties.setHeader("x-death", List.of(
                Map.of("queue", "system.check.mail.parking.queue", "reason", "expired"),
                Map.of("queue", "system.check.mail.queue", "reason", "rejected")));

        MqFailedMessage record = MqFailedMessageBuilder.buildBaseRecord(
                new Message("{}".getBytes(), properties),
                MessageType.SYSTEM_CHECK_MAIL,
                "dead.queue");

        assertEquals("system.check.mail.queue", record.getOriginalQueue());
    }
}
