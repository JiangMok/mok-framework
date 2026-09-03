package com.mok.framework.task.scheduler;

import cn.hutool.core.util.IdUtil;
import com.mok.framework.common.utils.LogUtils;
import com.mok.framework.mail.service.MailService;
import com.mok.framework.mail.util.HealthCheckMailBuilder;
import com.mok.framework.model.enums.MailType;
import com.mok.framework.monitor.service.HealthCheckService;
import com.mok.framework.monitor.service.impl.HealthCheckServiceImpl;
import com.mok.framework.mq.service.SystemCheckMailPublisher;
import com.mok.framework.task.config.TimeConfig;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class HealthCheckTask {
    private static final Logger log = LogUtils.getLogger(HealthCheckTask.class);
    private static final String ALERT_KEY_PREFIX = "monitor:health:alert:";
    private static final String STATE_SEPARATOR = "|";
    private static final long ALERT_IN_FLIGHT_SECONDS = 60;
    private static final RedisScript<String> GET_OR_CREATE_ACTIVE_ALERT_SCRIPT = RedisScript.of(
            "local current = redis.call('GET', KEYS[1]); " +
                    "local prefix = ARGV[1] .. '|'; " +
                    "if ARGV[4] ~= '1' and current " +
                    "and string.sub(current, 1, string.len(prefix)) == prefix then " +
                    "redis.call('EXPIRE', KEYS[1], ARGV[3]); return current; end; " +
                    "local next = ARGV[1] .. '|' .. ARGV[2]; " +
                    "redis.call('SET', KEYS[1], next, 'EX', ARGV[3]); return next;",
            String.class);
    private static final RedisScript<String> GET_AND_DELETE_ACTIVE_ALERT_SCRIPT = RedisScript.of(
            "local current = redis.call('GET', KEYS[1]); " +
                    "if current then redis.call('DEL', KEYS[1]); end; return current;",
            String.class);
    private static final RedisScript<Long> TRY_ACQUIRE_IN_FLIGHT_SCRIPT = RedisScript.of(
            "if redis.call('EXISTS', KEYS[2]) == 1 then return 0; end; " +
                    "local acquired = redis.call('SET', KEYS[1], ARGV[1], 'NX', 'EX', ARGV[2]); " +
                    "if acquired then return 1; end; return 0;",
            Long.class);
    private static final RedisScript<Long> COMPLETE_ALERT_SCRIPT = RedisScript.of(
            "if redis.call('GET', KEYS[1]) == ARGV[1] then redis.call('DEL', KEYS[1]); end; " +
                    "redis.call('SET', KEYS[2], ARGV[1], 'EX', ARGV[2]); return 1;",
            Long.class);
    private static final RedisScript<Long> RELEASE_IN_FLIGHT_SCRIPT = RedisScript.of(
            "if redis.call('GET', KEYS[1]) == ARGV[1] then " +
                    "return redis.call('DEL', KEYS[1]); end; return 0;",
            Long.class);

    private final HealthCheckService healthCheckService;
    private final HealthCheckMailBuilder mailBuilder;
    private final SystemCheckMailPublisher mailPublisher;
    private final MailService mailService;
    private final StringRedisTemplate redisTemplate;
    private final long alertCooldownSeconds;
    private final long activeStateTtlSeconds;
    private final String instanceId;
    private final String activeAlertStateKey;
    private final AtomicReference<AlertContext> fallbackActiveAlert = new AtomicReference<>();
    private final AtomicBoolean recoveryTombstone = new AtomicBoolean(false);
    private final Object fallbackLockMonitor = new Object();
    private String fallbackInFlightAlertId;
    private long fallbackInFlightUntil;
    private String fallbackCooldownAlertId;
    private long fallbackCooldownUntil;

    public HealthCheckTask(HealthCheckService healthCheckService,
                           HealthCheckMailBuilder mailBuilder,
                           SystemCheckMailPublisher mailPublisher,
                           MailService mailService,
                           StringRedisTemplate redisTemplate,
                           @Value("${mok.task.health-check.alert-cooldown-seconds:1800}")
                           long alertCooldownSeconds,
                           @Value("${spring.application.name:mok-framework}") String applicationName,
                           @Value("${HOSTNAME:${COMPUTERNAME:local}}") String hostName,
                           @Value("${server.port:8080}") String serverPort) {
        this.healthCheckService = healthCheckService;
        this.mailBuilder = mailBuilder;
        this.mailPublisher = mailPublisher;
        this.mailService = mailService;
        this.redisTemplate = redisTemplate;
        this.alertCooldownSeconds = Math.max(alertCooldownSeconds, 60);
        long doubledCooldown = this.alertCooldownSeconds > Integer.MAX_VALUE / 2L
                ? Integer.MAX_VALUE
                : this.alertCooldownSeconds * 2;
        this.activeStateTtlSeconds = Math.max(doubledCooldown, 600);
        this.instanceId = sanitizeKeyPart(applicationName + '-' + hostName + '-' + serverPort);
        this.activeAlertStateKey = ALERT_KEY_PREFIX + instanceId + ":active";
    }

    @PostConstruct
    public void init() {
        System.out.println("========== HealthCheckTask Bean 已初始化");
    }

    @Scheduled(fixedRate = TimeConfig.FIVE_MINUTES)
    public void scheduledHealthCheck() {
        try {
            Map<String, Object> health = healthCheckService.performHealthCheck();
            String status = (String) health.get("status");

            if (!"UP".equals(status)) {
                log.warn("系统健康检查异常 ({}): {}", status, health);
                AlertContext alertContext = getOrCreateAlertContext(health, status);
                if (!tryAcquireInFlight(alertContext)) {
                    log.info("健康状态 {} 仍在通知冷却期，本次跳过邮件", status);
                    return;
                }

                try {
                    String subject = '[' + instanceId + "] " + ("DOWN".equals(status)
                            ? "mok-framework-系统健康检查失败通知"
                            : "mok-framework-系统健康检查异常通知");
                    Map<String, Object> mailHealth = new HashMap<>(health);
                    mailHealth.put("instanceId", instanceId);
                    String content = mailBuilder.buildHtmlMail(mailHealth, status);
                    sendNotification(health, subject, content, alertContext.alertId());
                    completeAlert(alertContext);
                } catch (Exception e) {
                    releaseInFlight(alertContext);
                    throw e;
                }
            } else {
                clearActiveAlertState();
                log.info("系统健康检查正常: {}", health);
            }
        } catch (Exception e) {
            log.error("健康检查任务执行失败", e);
        }
    }

    private void sendNotification(Map<String, Object> health, String subject,
                                  String content, String alertId) {
        Object rabbitResult = health.get("rabbitmq");
        int recipientCount;
        if (rabbitResult instanceof HealthCheckServiceImpl.HealthCheckResult result
                && !result.isUp()) {
            // RabbitMQ 自身故障时必须绕过消息队列，否则无法发出故障通知
            log.warn("RabbitMQ 不可用，健康告警降级为 SMTP 直发");
            recipientCount = mailService.sendByMailType(
                    MailType.SYSTEM_CHECK, subject, content, true, alertId);
        } else {
            recipientCount = mailPublisher.publish(alertId, subject, content, true);
        }
        if (recipientCount <= 0) {
            throw new IllegalStateException("未配置系统健康告警收件人");
        }
    }

    private boolean tryAcquireInFlight(AlertContext alertContext) {
        if (isFallbackCooldownActive(alertContext.alertId())) {
            return false;
        }
        try {
            Long acquired = redisTemplate.execute(
                    TRY_ACQUIRE_IN_FLIGHT_SCRIPT,
                    List.of(alertContext.inFlightKey(), alertContext.cooldownKey()),
                    alertContext.alertId(),
                    String.valueOf(ALERT_IN_FLIGHT_SECONDS));
            if (acquired == null) {
                throw new IllegalStateException("健康告警发送锁获取失败");
            }
            if (acquired == 1L) {
                markFallbackInFlight(alertContext.alertId());
                return true;
            }
            return false;
        } catch (Exception e) {
            // Redis 本身异常时使用进程内的短发送锁与成功冷却。
            log.warn("健康告警发送锁使用 Redis 失败，降级为进程内状态: {}", e.getMessage());
            return tryAcquireFallbackInFlight(alertContext.alertId());
        }
    }

    private AlertContext getOrCreateAlertContext(Map<String, Object> health, String overallStatus) {
        String fingerprint = buildFailureFingerprint(health, overallStatus);
        AlertContext localContext = fallbackActiveAlert.get();
        String candidateAlertId = localContext != null
                && fingerprint.equals(localContext.fingerprint())
                ? localContext.alertId()
                : IdUtil.simpleUUID();
        boolean forceNewEvent = recoveryTombstone.get();
        try {
            String state = redisTemplate.execute(
                    GET_OR_CREATE_ACTIVE_ALERT_SCRIPT,
                    List.of(activeAlertStateKey),
                    fingerprint,
                    candidateAlertId,
                    String.valueOf(activeStateTtlSeconds),
                    forceNewEvent ? "1" : "0");
            if (!StringUtils.hasText(state)) {
                throw new IllegalStateException("健康告警活动状态创建失败");
            }
            AlertContext context = parseAlertContext(state);
            // 同步保留本地镜像，Redis 在后续轮次暂时不可用时仍复用同一 alertId。
            fallbackActiveAlert.set(context);
            if (forceNewEvent) {
                recoveryTombstone.compareAndSet(true, false);
            }
            return context;
        } catch (Exception e) {
            log.warn("健康告警活动状态使用 Redis 失败，降级为进程内状态: {}", e.getMessage());
            while (true) {
                AlertContext current = fallbackActiveAlert.get();
                if (current != null && fingerprint.equals(current.fingerprint())) {
                    return current;
                }
                AlertContext replacement = createAlertContext(fingerprint, candidateAlertId);
                if (fallbackActiveAlert.compareAndSet(current, replacement)) {
                    return replacement;
                }
            }
        }
    }

    private String buildFailureFingerprint(Map<String, Object> health, String overallStatus) {
        Map<String, String> failedComponents = new TreeMap<>();
        for (Map.Entry<String, Object> entry : health.entrySet()) {
            if (entry.getValue() instanceof HealthCheckServiceImpl.HealthCheckResult result
                    && !"UP".equals(result.getStatus())) {
                failedComponents.put(entry.getKey(), result.getStatus());
            }
        }
        String fingerprint = failedComponents.isEmpty()
                ? overallStatus
                : failedComponents.toString();
        return overallStatus + ':' + sanitizeKeyPart(fingerprint);
    }

    private AlertContext parseAlertContext(String state) {
        int separatorIndex = state.lastIndexOf(STATE_SEPARATOR);
        if (separatorIndex <= 0 || separatorIndex == state.length() - 1) {
            throw new IllegalStateException("健康告警活动状态格式错误");
        }
        return createAlertContext(
                state.substring(0, separatorIndex),
                state.substring(separatorIndex + 1));
    }

    private AlertContext createAlertContext(String fingerprint, String alertId) {
        String eventKeyPrefix = ALERT_KEY_PREFIX + instanceId + ":event:" + alertId;
        return new AlertContext(
                fingerprint,
                alertId,
                eventKeyPrefix + ":in-flight",
                eventKeyPrefix + ":cooldown");
    }

    private void clearActiveAlertState() {
        try {
            String state = redisTemplate.execute(
                    GET_AND_DELETE_ACTIVE_ALERT_SCRIPT,
                    List.of(activeAlertStateKey));
            if (StringUtils.hasText(state)) {
                AlertContext context = parseAlertContext(state);
                redisTemplate.delete(List.of(context.inFlightKey(), context.cooldownKey()));
            }
            recoveryTombstone.set(false);
        } catch (Exception e) {
            // Redis 中可能仍保留旧成功事件；恢复后必须强制覆盖为新的 alertId。
            recoveryTombstone.set(true);
            log.debug("清理健康告警活动状态失败: {}", e.getMessage());
        }
        fallbackActiveAlert.set(null);
        clearFallbackDeliveryState();
    }

    private void completeAlert(AlertContext alertContext) {
        try {
            Long completed = redisTemplate.execute(
                    COMPLETE_ALERT_SCRIPT,
                    List.of(alertContext.inFlightKey(), alertContext.cooldownKey()),
                    alertContext.alertId(),
                    String.valueOf(alertCooldownSeconds));
            if (completed == null || completed != 1L) {
                throw new IllegalStateException("健康告警冷却状态保存失败");
            }
        } catch (Exception e) {
            log.warn("健康告警已发送，但 Redis 冷却状态保存失败: {}", e.getMessage());
        }
        markFallbackSuccess(alertContext.alertId());
    }

    private void releaseInFlight(AlertContext alertContext) {
        try {
            redisTemplate.execute(
                    RELEASE_IN_FLIGHT_SCRIPT,
                    List.of(alertContext.inFlightKey()),
                    alertContext.alertId());
        } catch (Exception e) {
            log.debug("释放健康告警发送锁失败: {}", e.getMessage());
        }
        synchronized (fallbackLockMonitor) {
            if (alertContext.alertId().equals(fallbackInFlightAlertId)) {
                fallbackInFlightAlertId = null;
                fallbackInFlightUntil = 0;
            }
        }
    }

    private boolean isFallbackCooldownActive(String alertId) {
        synchronized (fallbackLockMonitor) {
            return alertId.equals(fallbackCooldownAlertId)
                    && System.currentTimeMillis() < fallbackCooldownUntil;
        }
    }

    private boolean tryAcquireFallbackInFlight(String alertId) {
        synchronized (fallbackLockMonitor) {
            long now = System.currentTimeMillis();
            if (alertId.equals(fallbackCooldownAlertId) && now < fallbackCooldownUntil) {
                return false;
            }
            if (fallbackInFlightAlertId != null && now < fallbackInFlightUntil) {
                return false;
            }
            fallbackInFlightAlertId = alertId;
            fallbackInFlightUntil = deadlineAfter(now, ALERT_IN_FLIGHT_SECONDS);
            return true;
        }
    }

    private void markFallbackInFlight(String alertId) {
        synchronized (fallbackLockMonitor) {
            long now = System.currentTimeMillis();
            fallbackInFlightAlertId = alertId;
            fallbackInFlightUntil = deadlineAfter(now, ALERT_IN_FLIGHT_SECONDS);
        }
    }

    private void markFallbackSuccess(String alertId) {
        synchronized (fallbackLockMonitor) {
            long now = System.currentTimeMillis();
            if (alertId.equals(fallbackInFlightAlertId)) {
                fallbackInFlightAlertId = null;
                fallbackInFlightUntil = 0;
            }
            fallbackCooldownAlertId = alertId;
            fallbackCooldownUntil = deadlineAfter(now, alertCooldownSeconds);
        }
    }

    private void clearFallbackDeliveryState() {
        synchronized (fallbackLockMonitor) {
            fallbackInFlightAlertId = null;
            fallbackInFlightUntil = 0;
            fallbackCooldownAlertId = null;
            fallbackCooldownUntil = 0;
        }
    }

    private long deadlineAfter(long now, long seconds) {
        long durationMillis = TimeUnit.SECONDS.toMillis(seconds);
        return durationMillis >= Long.MAX_VALUE - now ? Long.MAX_VALUE : now + durationMillis;
    }

    private String sanitizeKeyPart(String value) {
        return value == null ? "unknown" : value.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private record AlertContext(String fingerprint, String alertId,
                                String inFlightKey, String cooldownKey) {
    }
}
