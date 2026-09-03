package com.mok.framework.mq.service;

import com.mok.framework.common.BusinessException;
import com.mok.framework.model.entity.MqFailedMessage;
import com.mok.framework.mq.mapper.MqFailedMessageMapper;
import com.mok.framework.mq.service.impl.MqFailedMessageServiceImpl;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MqFailedMessageServiceImplTest {

    @Test
    void zeroAffectedRowsIsNotAcknowledgedAsSaved() {
        MqFailedMessageMapper mapper = mock(MqFailedMessageMapper.class);
        MqFailedMessage message = new MqFailedMessage();
        message.setId("message-1");
        when(mapper.selectById("message-1")).thenReturn(null);
        when(mapper.insert(message)).thenReturn(0);

        assertThatThrownBy(() -> new MqFailedMessageServiceImpl(mapper)
                .saveMqFailedMessage(message))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void duplicateDeliveryIsHandledIdempotently() {
        MqFailedMessageMapper mapper = mock(MqFailedMessageMapper.class);
        MqFailedMessage message = new MqFailedMessage();
        message.setId("message-1");
        when(mapper.selectById("message-1")).thenReturn(message);

        assertThatCode(() -> new MqFailedMessageServiceImpl(mapper)
                .saveMqFailedMessage(message)).doesNotThrowAnyException();
        verify(mapper, never()).insert(message);
    }
}

