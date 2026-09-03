package com.mok.framework.mail.service.impl;

import cn.hutool.extra.mail.MailAccount;
import com.mok.framework.mail.service.MailDeliveryClaim;
import com.mok.framework.mail.service.MailLogService;
import com.mok.framework.mail.service.MailTransport;
import com.mok.framework.model.enums.MailType;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MailDeliveryExecutorTest {

    @Test
    void shouldSkipSmtpWhenMessageWasAlreadyDelivered() {
        MailTransport transport = mock(MailTransport.class);
        MailLogService logService = mock(MailLogService.class);
        when(logService.claimDelivery(any(), anyLong()))
                .thenReturn(MailDeliveryClaim.ALREADY_SUCCESS);
        MailDeliveryExecutor executor = new MailDeliveryExecutor(transport, logService, 300);
        @SuppressWarnings("unchecked")
        Supplier<MailAccount> accountSupplier = mock(Supplier.class);

        executor.sendAndLog(accountSupplier, "user@example.com", "subject", "content",
                "message-success", MailType.NOTIFICATION, true);

        verify(accountSupplier, never()).get();
        verify(transport, never()).send(any(), any(), any(), any(),
                org.mockito.ArgumentMatchers.anyBoolean());
    }

    @Test
    void shouldNotReadAccountWhenAnotherInstanceOwnsTheDelivery() {
        MailTransport transport = mock(MailTransport.class);
        MailLogService logService = mock(MailLogService.class);
        when(logService.claimDelivery(any(), anyLong()))
                .thenReturn(MailDeliveryClaim.IN_PROGRESS);
        MailDeliveryExecutor executor = new MailDeliveryExecutor(transport, logService, 300);
        @SuppressWarnings("unchecked")
        Supplier<MailAccount> accountSupplier = mock(Supplier.class);

        assertThatThrownBy(() -> executor.sendAndLog(
                accountSupplier, "user@example.com", "subject", "content",
                "message-progress", MailType.NOTIFICATION, true))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("正在由其他实例投递");

        verify(accountSupplier, never()).get();
        verify(transport, never()).send(any(), any(), any(), any(),
                org.mockito.ArgumentMatchers.anyBoolean());
    }

    @Test
    void shouldNotReportDeliveryFailureWhenSmtpSucceededButLogPersistenceFailed() {
        MailTransport transport = mock(MailTransport.class);
        MailLogService logService = mock(MailLogService.class);
        when(logService.claimDelivery(any(), anyLong()))
                .thenReturn(MailDeliveryClaim.CLAIMED);
        doThrow(new IllegalStateException("log database unavailable"))
                .when(logService).completeDelivery(any(), any(), any(), any());
        MailDeliveryExecutor executor = new MailDeliveryExecutor(transport, logService, 300);

        assertThatCode(() -> executor.sendAndLog(
                MailAccount::new, "user@example.com", "subject", "content",
                "message-1", MailType.NOTIFICATION, true))
                .doesNotThrowAnyException();

        verify(transport).send(any(MailAccount.class),
                org.mockito.ArgumentMatchers.eq("user@example.com"),
                org.mockito.ArgumentMatchers.eq("subject"),
                org.mockito.ArgumentMatchers.eq("content"),
                org.mockito.ArgumentMatchers.eq(true));
    }

    @Test
    void shouldPreserveSmtpFailureWhenFailureLogAlsoCannotBeSaved() {
        MailTransport transport = mock(MailTransport.class);
        MailLogService logService = mock(MailLogService.class);
        when(logService.claimDelivery(any(), anyLong()))
                .thenReturn(MailDeliveryClaim.CLAIMED);
        doThrow(new IllegalArgumentException("smtp rejected"))
                .when(transport).send(any(), any(), any(), any(), org.mockito.ArgumentMatchers.anyBoolean());
        doThrow(new IllegalStateException("log database unavailable"))
                .when(logService).completeDelivery(any(), any(), any(), any());
        MailDeliveryExecutor executor = new MailDeliveryExecutor(transport, logService, 300);

        assertThatThrownBy(() -> executor.sendAndLog(
                MailAccount::new, "user@example.com", "subject", "content",
                "message-2", MailType.NOTIFICATION, false))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("邮件发送失败")
                .hasCauseInstanceOf(IllegalArgumentException.class)
                .satisfies(exception -> assertThat(exception.getSuppressed()).hasSize(1));

        verify(logService, times(3)).completeDelivery(
                eq("message-2"), any(LocalDateTime.class), eq("FAILED"), eq("smtp rejected"));
    }

    @Test
    void shouldRetryTransientCompletionFailureBeforeReturning() {
        MailTransport transport = mock(MailTransport.class);
        MailLogService logService = mock(MailLogService.class);
        when(logService.claimDelivery(any(), anyLong()))
                .thenReturn(MailDeliveryClaim.CLAIMED);
        when(logService.completeDelivery(any(), any(), any(), any()))
                .thenThrow(new IllegalStateException("temporary-1"))
                .thenThrow(new IllegalStateException("temporary-2"))
                .thenReturn(true);
        MailDeliveryExecutor executor = new MailDeliveryExecutor(transport, logService, 300);

        assertThatCode(() -> executor.sendAndLog(
                MailAccount::new, "user@example.com", "subject", "content",
                "message-retry", MailType.NOTIFICATION, true))
                .doesNotThrowAnyException();

        verify(logService, times(3)).completeDelivery(
                eq("message-retry"), any(LocalDateTime.class), eq("SUCCESS"), isNull());
    }

    @Test
    void sameJvmRetrySkipsSmtpAfterSuccessResultCouldNotBeSaved() {
        MailTransport transport = mock(MailTransport.class);
        MailLogService logService = mock(MailLogService.class);
        when(logService.claimDelivery(any(), anyLong()))
                .thenReturn(MailDeliveryClaim.CLAIMED);
        when(logService.completeDelivery(any(), any(), any(), any()))
                .thenThrow(new IllegalStateException("database unavailable"));
        AtomicLong now = new AtomicLong(1_000_000L);
        MailDeliveryExecutor executor = new MailDeliveryExecutor(
                transport, logService, 300, now::get);
        @SuppressWarnings("unchecked")
        Supplier<MailAccount> accountSupplier = mock(Supplier.class);
        when(accountSupplier.get()).thenReturn(new MailAccount());

        executor.sendAndLog(accountSupplier, "user@example.com", "subject", "content",
                "message-local-success", MailType.NOTIFICATION, true);
        now.addAndGet(TimeUnit.SECONDS.toMillis(301));
        executor.sendAndLog(accountSupplier, "user@example.com", "subject", "content",
                "message-local-success", MailType.NOTIFICATION, true);

        verify(accountSupplier).get();
        verify(transport).send(any(), any(), any(), any(), eq(true));
        verify(logService).claimDelivery(any(), anyLong());
    }

    @Test
    void sameJvmRetryCanSendAgainAfterFailureResultCouldNotBeSaved() {
        MailTransport transport = mock(MailTransport.class);
        MailLogService logService = mock(MailLogService.class);
        when(logService.claimDelivery(any(), anyLong()))
                .thenReturn(MailDeliveryClaim.CLAIMED);
        doThrow(new IllegalStateException("smtp unavailable"))
                .when(transport).send(any(), any(), any(), any(), eq(false));
        when(logService.completeDelivery(any(), any(), any(), any()))
                .thenThrow(new IllegalStateException("database unavailable"));
        MailDeliveryExecutor executor = new MailDeliveryExecutor(transport, logService, 300);

        assertThatThrownBy(() -> executor.sendAndLog(
                MailAccount::new, "user@example.com", "subject", "content",
                "message-local-failure", MailType.NOTIFICATION, false))
                .hasMessage("邮件发送失败");
        assertThatThrownBy(() -> executor.sendAndLog(
                MailAccount::new, "user@example.com", "subject", "content",
                "message-local-failure", MailType.NOTIFICATION, false))
                .hasMessage("邮件发送失败");

        verify(transport, times(2)).send(any(), any(), any(), any(), eq(false));
        verify(logService).claimDelivery(any(), anyLong());
    }
}
