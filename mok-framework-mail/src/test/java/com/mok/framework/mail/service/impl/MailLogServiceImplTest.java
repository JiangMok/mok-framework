package com.mok.framework.mail.service.impl;

import com.mok.framework.mail.mapper.MailLogMapper;
import com.mok.framework.mail.service.MailDeliveryClaim;
import com.mok.framework.model.entity.MailLog;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MailLogServiceImplTest {

    @Test
    void shouldCreateSendingReservationForTheFirstDelivery() {
        MailLogMapper mapper = mock(MailLogMapper.class);
        when(mapper.insert(any(MailLog.class))).thenReturn(1);
        MailLogServiceImpl service = new MailLogServiceImpl(mapper);
        MailLog mailLog = mailLog("message-1");

        assertThat(service.claimDelivery(mailLog, 300))
                .isEqualTo(MailDeliveryClaim.CLAIMED);
        assertThat(mailLog.getSendStatus()).isEqualTo("SENDING");
        assertThat(mailLog.getRetryCount()).isZero();
    }

    @Test
    void shouldSkipAnAlreadySuccessfulMessage() {
        MailLogMapper mapper = mock(MailLogMapper.class);
        when(mapper.insert(any(MailLog.class)))
                .thenThrow(new DuplicateKeyException("duplicate message id"));
        MailLog existing = mailLog("message-1");
        existing.setSendStatus("SUCCESS");
        when(mapper.selectOne(any())).thenReturn(existing);
        MailLogServiceImpl service = new MailLogServiceImpl(mapper);

        assertThat(service.claimDelivery(mailLog("message-1"), 300))
                .isEqualTo(MailDeliveryClaim.ALREADY_SUCCESS);
    }

    @Test
    void shouldUseConditionalUpdateToReclaimFailedDelivery() {
        MailLogMapper mapper = mock(MailLogMapper.class);
        when(mapper.insert(any(MailLog.class)))
                .thenThrow(new DuplicateKeyException("duplicate message id"));
        MailLog existing = mailLog("message-1");
        existing.setSendStatus("FAILED");
        when(mapper.selectOne(any())).thenReturn(existing);
        when(mapper.claimForDelivery(eq("message-1"), any(), anyLong())).thenReturn(1);
        MailLogServiceImpl service = new MailLogServiceImpl(mapper);
        MailLog retry = mailLog("message-1");

        assertThat(service.claimDelivery(retry, 300))
                .isEqualTo(MailDeliveryClaim.CLAIMED);
        verify(mapper).claimForDelivery("message-1", retry.getSendTime(), 300);
    }

    @Test
    void shouldNotStealAReservationThatIsStillActive() {
        MailLogMapper mapper = mock(MailLogMapper.class);
        when(mapper.insert(any(MailLog.class)))
                .thenThrow(new DuplicateKeyException("duplicate message id"));
        MailLog existing = mailLog("message-1");
        existing.setSendStatus("SENDING");
        when(mapper.selectOne(any())).thenReturn(existing);
        when(mapper.claimForDelivery(eq("message-1"), any(), anyLong())).thenReturn(0);
        MailLogServiceImpl service = new MailLogServiceImpl(mapper);

        assertThat(service.claimDelivery(mailLog("message-1"), 300))
                .isEqualTo(MailDeliveryClaim.IN_PROGRESS);
    }

    @Test
    void shouldCompleteOnlyTheMatchingReservation() {
        MailLogMapper mapper = mock(MailLogMapper.class);
        LocalDateTime claimTime = LocalDateTime.now().withNano(0);
        when(mapper.completeDelivery("message-1", claimTime, "SUCCESS", null)).thenReturn(1);
        MailLogServiceImpl service = new MailLogServiceImpl(mapper);

        assertThat(service.completeDelivery("message-1", claimTime, "SUCCESS", null)).isTrue();
    }

    private MailLog mailLog(String messageId) {
        MailLog mailLog = new MailLog();
        mailLog.setId("log-" + messageId);
        mailLog.setMessageId(messageId);
        mailLog.setSendTime(LocalDateTime.now().withNano(0));
        mailLog.setUpdateTime(mailLog.getSendTime());
        return mailLog;
    }
}
