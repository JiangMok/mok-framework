package com.mok.framework.mail.service.impl;

import cn.hutool.extra.mail.MailAccount;
import com.mok.framework.common.utils.LogUtils;
import com.mok.framework.mail.service.MailDeliveryClaim;
import com.mok.framework.mail.service.MailLogService;
import com.mok.framework.mail.service.MailTransport;
import com.mok.framework.mail.util.MailLogBuilder;
import com.mok.framework.model.entity.MailLog;
import com.mok.framework.model.enums.MailType;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

/**
 * 单封邮件投递执行器。
 * SMTP 投递结果与邮件日志持久化结果分别处理，避免邮件已发送却因日志失败被重复投递。
 * JVM 内通过租约结果缓存覆盖暂时的日志故障；跨进程在 SMTP 已接收、SUCCESS 尚未落库时崩溃，
 * 仍只能保证至少一次投递，无法在无 SMTP 幂等协议的前提下严格实现 exactly-once。
 */
@Service
public class MailDeliveryExecutor {

    private static final Logger log = LogUtils.getLogger(MailDeliveryExecutor.class);
    private static final long MIN_SENDING_LEASE_SECONDS = 300;
    private static final long SUCCESS_CACHE_TTL_SECONDS = TimeUnit.HOURS.toSeconds(24);
    private static final int COMPLETION_MAX_ATTEMPTS = 3;
    private static final long COMPLETION_RETRY_INTERVAL_MILLIS = 100;

    private final MailTransport mailTransport;
    private final MailLogService mailLogService;
    private final long sendingLeaseSeconds;
    private final LongSupplier currentTimeMillisSupplier;
    /** 仅兜底数据库结果暂时无法落库的同 JVM 重投；正常完成后立即移除。 */
    private final ConcurrentMap<String, LocalDeliveryState> localDeliveryStates =
            new ConcurrentHashMap<>();

    @Autowired
    public MailDeliveryExecutor(MailTransport mailTransport,
                                MailLogService mailLogService,
                                @Value("${mok.mail.delivery.sending-lease-seconds:300}")
                                long sendingLeaseSeconds) {
        this(mailTransport, mailLogService, sendingLeaseSeconds, System::currentTimeMillis);
    }

    MailDeliveryExecutor(MailTransport mailTransport,
                         MailLogService mailLogService,
                         long sendingLeaseSeconds,
                         LongSupplier currentTimeMillisSupplier) {
        this.mailTransport = mailTransport;
        this.mailLogService = mailLogService;
        this.sendingLeaseSeconds = Math.max(sendingLeaseSeconds, MIN_SENDING_LEASE_SECONDS);
        this.currentTimeMillisSupplier = Objects.requireNonNull(
                currentTimeMillisSupplier, "当前时间提供器不能为空");
    }

    public void sendAndLog(Supplier<MailAccount> accountSupplier,
                           String recipient, String subject, String content,
                           String messageId, MailType mailType, boolean html) {
        Objects.requireNonNull(accountSupplier, "发件账号提供器不能为空");
        long now = currentTimeMillisSupplier.getAsLong();
        purgeExpiredLocalStates(now);
        LocalResume localResume = resumeLocalDelivery(messageId, now);
        if (localResume.action() == LocalResumeAction.SKIP_SUCCESS) {
            log.info("邮件消息 {} 已在当前实例成功投递，跳过重复发送", messageId);
            return;
        }

        MailLog mailLog = MailLogBuilder.build(messageId, recipient, subject, content, mailType);
        LocalDateTime claimTime;
        if (localResume.action() == LocalResumeAction.RETRY_CLAIMED) {
            claimTime = localResume.claimTime();
            mailLog.setSendTime(claimTime);
        } else {
            claimTime = mailLog.getSendTime();
            MailDeliveryClaim claim = Objects.requireNonNull(
                    mailLogService.claimDelivery(mailLog, sendingLeaseSeconds),
                    "邮件投递占位结果不能为空");
            if (claim == MailDeliveryClaim.ALREADY_SUCCESS) {
                log.info("邮件消息 {} 已成功投递，跳过重复发送", messageId);
                return;
            }
            if (claim == MailDeliveryClaim.IN_PROGRESS) {
                throw new IllegalStateException("邮件消息正在由其他实例投递: " + messageId);
            }
            localDeliveryStates.put(messageId,
                    localState(LocalDeliveryOutcome.IN_FLIGHT, claimTime));
        }

        RuntimeException deliveryFailure = null;

        try {
            MailAccount account = Objects.requireNonNull(accountSupplier.get(), "发件账号不能为空");
            mailTransport.send(account, recipient, subject, content, html);
            mailLog.setSendStatus("SUCCESS");
            localDeliveryStates.put(messageId,
                    localState(LocalDeliveryOutcome.SUCCESS, claimTime));
        } catch (Exception exception) {
            mailLog.setSendStatus("FAILED");
            mailLog.setFailReason(exception.getMessage());
            deliveryFailure = new RuntimeException("邮件发送失败", exception);
            localDeliveryStates.put(messageId,
                    localState(LocalDeliveryOutcome.RETRYABLE_FAILURE, claimTime));
        }

        try {
            log.info("记录邮件日志: messageId={}, status={}", messageId, mailLog.getSendStatus());
            completeDeliveryWithRetry(
                    messageId, claimTime, mailLog.getSendStatus(), mailLog.getFailReason());
            // 数据库已经成为权威状态；仅数据库提交失败时才需要 JVM 内租约兜底。
            localDeliveryStates.remove(messageId);
        } catch (Exception persistenceFailure) {
            if (persistenceFailure instanceof DeliveryClaimLostException) {
                localDeliveryStates.remove(messageId);
            }
            if (deliveryFailure == null) {
                // SMTP 已确认成功。此时抛异常会让 MQ/调用方误判并重复发送邮件。
                log.error("邮件已发送，但日志落库失败: messageId={}", messageId, persistenceFailure);
            } else {
                deliveryFailure.addSuppressed(persistenceFailure);
                log.error("邮件发送和失败日志落库均失败: messageId={}", messageId, persistenceFailure);
            }
        }

        if (deliveryFailure != null) {
            throw deliveryFailure;
        }
    }

    private void completeDeliveryWithRetry(String messageId, LocalDateTime claimTime,
                                           String sendStatus, String failReason) {
        RuntimeException lastFailure = null;
        for (int attempt = 1; attempt <= COMPLETION_MAX_ATTEMPTS; attempt++) {
            try {
                if (!mailLogService.completeDelivery(
                        messageId, claimTime, sendStatus, failReason)) {
                    throw new DeliveryClaimLostException("邮件投递占用权已失效: " + messageId);
                }
                return;
            } catch (DeliveryClaimLostException exception) {
                throw exception;
            } catch (RuntimeException exception) {
                lastFailure = exception;
                if (attempt < COMPLETION_MAX_ATTEMPTS) {
                    pauseBeforeCompletionRetry(attempt);
                }
            }
        }
        throw new IllegalStateException(
                "邮件投递结果保存失败，已重试" + COMPLETION_MAX_ATTEMPTS + "次", lastFailure);
    }

    private void pauseBeforeCompletionRetry(int attempt) {
        try {
            Thread.sleep(COMPLETION_RETRY_INTERVAL_MILLIS * attempt);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("等待邮件投递结果重试时线程被中断", exception);
        }
    }

    private LocalResume resumeLocalDelivery(String messageId, long now) {
        while (true) {
            LocalDeliveryState state = localDeliveryStates.get(messageId);
            if (state == null) {
                return LocalResume.none();
            }
            if (state.expiresAtMillis() <= now) {
                localDeliveryStates.remove(messageId, state);
                continue;
            }
            if (state.outcome() == LocalDeliveryOutcome.SUCCESS) {
                return LocalResume.skipSuccess();
            }
            if (state.outcome() == LocalDeliveryOutcome.IN_FLIGHT) {
                throw new IllegalStateException("邮件消息正在当前实例投递: " + messageId);
            }

            LocalDeliveryState resumed = localState(
                    LocalDeliveryOutcome.IN_FLIGHT, state.claimTime());
            if (localDeliveryStates.replace(messageId, state, resumed)) {
                return LocalResume.retryClaimed(state.claimTime());
            }
        }
    }

    private LocalDeliveryState localState(LocalDeliveryOutcome outcome, LocalDateTime claimTime) {
        long now = currentTimeMillisSupplier.getAsLong();
        long ttlSeconds = outcome == LocalDeliveryOutcome.SUCCESS
                ? Math.max(SUCCESS_CACHE_TTL_SECONDS, sendingLeaseSeconds)
                : sendingLeaseSeconds;
        long ttlMillis = TimeUnit.SECONDS.toMillis(ttlSeconds);
        long expiresAt = ttlMillis >= Long.MAX_VALUE - now
                ? Long.MAX_VALUE
                : now + ttlMillis;
        return new LocalDeliveryState(outcome, claimTime, expiresAt);
    }

    private void purgeExpiredLocalStates(long now) {
        localDeliveryStates.entrySet().removeIf(entry -> entry.getValue().expiresAtMillis() <= now);
    }

    private enum LocalDeliveryOutcome {
        IN_FLIGHT,
        SUCCESS,
        RETRYABLE_FAILURE
    }

    private enum LocalResumeAction {
        NONE,
        SKIP_SUCCESS,
        RETRY_CLAIMED
    }

    private record LocalDeliveryState(LocalDeliveryOutcome outcome,
                                      LocalDateTime claimTime,
                                      long expiresAtMillis) {
    }

    private record LocalResume(LocalResumeAction action, LocalDateTime claimTime) {
        private static LocalResume none() {
            return new LocalResume(LocalResumeAction.NONE, null);
        }

        private static LocalResume skipSuccess() {
            return new LocalResume(LocalResumeAction.SKIP_SUCCESS, null);
        }

        private static LocalResume retryClaimed(LocalDateTime claimTime) {
            return new LocalResume(LocalResumeAction.RETRY_CLAIMED, claimTime);
        }
    }

    private static class DeliveryClaimLostException extends IllegalStateException {
        private DeliveryClaimLostException(String message) {
            super(message);
        }
    }
}
