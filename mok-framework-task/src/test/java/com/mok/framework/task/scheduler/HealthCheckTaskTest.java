package com.mok.framework.task.scheduler;

import com.mok.framework.mail.service.MailService;
import com.mok.framework.mail.util.HealthCheckMailBuilder;
import com.mok.framework.monitor.service.HealthCheckService;
import com.mok.framework.monitor.service.impl.HealthCheckServiceImpl;
import com.mok.framework.mq.service.SystemCheckMailPublisher;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class HealthCheckTaskTest {

    @Test
    void failedRetryReusesAlertIdAndRecoveryCreatesANewEvent() {
        HealthCheckService healthService = mock(HealthCheckService.class);
        HealthCheckMailBuilder mailBuilder = mock(HealthCheckMailBuilder.class);
        SystemCheckMailPublisher publisher = mock(SystemCheckMailPublisher.class);
        when(healthService.performHealthCheck()).thenReturn(down(), down(), up(), down());
        when(mailBuilder.buildHtmlMail(org.mockito.ArgumentMatchers.anyMap(), anyString()))
                .thenReturn("content");
        when(publisher.publish(anyString(), anyString(), anyString(), eq(true)))
                .thenThrow(new IllegalStateException("broker unavailable"))
                .thenReturn(1, 1);
        HealthCheckTask task = task(healthService, mailBuilder, publisher);

        task.scheduledHealthCheck();
        task.scheduledHealthCheck();
        task.scheduledHealthCheck();
        task.scheduledHealthCheck();

        ArgumentCaptor<String> alertIds = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> subjects = ArgumentCaptor.forClass(String.class);
        verify(publisher, times(3)).publish(
                alertIds.capture(), subjects.capture(), anyString(), eq(true));
        List<String> values = alertIds.getAllValues();
        assertThat(values.get(0)).isEqualTo(values.get(1));
        assertThat(values.get(2)).isNotEqualTo(values.get(1));
        assertThat(subjects.getAllValues())
                .allMatch(subject -> subject.startsWith("[mok-framework-test-host-8080]"));
    }

    @Test
    void mailBuildFailureReleasesTheEventLock() {
        HealthCheckService healthService = mock(HealthCheckService.class);
        HealthCheckMailBuilder mailBuilder = mock(HealthCheckMailBuilder.class);
        SystemCheckMailPublisher publisher = mock(SystemCheckMailPublisher.class);
        when(healthService.performHealthCheck()).thenReturn(down());
        when(mailBuilder.buildHtmlMail(org.mockito.ArgumentMatchers.anyMap(), anyString()))
                .thenThrow(new IllegalStateException("render failed"))
                .thenReturn("content");
        when(publisher.publish(anyString(), anyString(), anyString(), eq(true))).thenReturn(1);
        HealthCheckTask task = task(healthService, mailBuilder, publisher);

        task.scheduledHealthCheck();
        task.scheduledHealthCheck();

        verify(mailBuilder, times(2))
                .buildHtmlMail(org.mockito.ArgumentMatchers.anyMap(), eq("DOWN"));
        verify(publisher).publish(anyString(), anyString(), eq("content"), eq(true));
    }

    @Test
    void successfulDeliveryStartsCooldownForTheSameEvent() {
        HealthCheckService healthService = mock(HealthCheckService.class);
        HealthCheckMailBuilder mailBuilder = mock(HealthCheckMailBuilder.class);
        SystemCheckMailPublisher publisher = mock(SystemCheckMailPublisher.class);
        when(healthService.performHealthCheck()).thenReturn(down());
        when(mailBuilder.buildHtmlMail(org.mockito.ArgumentMatchers.anyMap(), anyString()))
                .thenReturn("content");
        when(publisher.publish(anyString(), anyString(), anyString(), eq(true))).thenReturn(1);
        HealthCheckTask task = task(healthService, mailBuilder, publisher);

        task.scheduledHealthCheck();
        task.scheduledHealthCheck();

        verify(publisher).publish(anyString(), anyString(), anyString(), eq(true));
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void recoveryDeletesOnlyThisInstancesActiveEventLock() {
        HealthCheckService healthService = mock(HealthCheckService.class);
        HealthCheckMailBuilder mailBuilder = mock(HealthCheckMailBuilder.class);
        SystemCheckMailPublisher publisher = mock(SystemCheckMailPublisher.class);
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        String activeState = "DOWN:_database_DOWN_|alert-1";
        when(redisTemplate.execute(
                any(RedisScript.class), anyList(), any(Object[].class)))
                .thenAnswer(invocation -> {
                    RedisScript<?> script = invocation.getArgument(0);
                    return String.class.equals(script.getResultType()) ? activeState : 1L;
                });
        when(healthService.performHealthCheck()).thenReturn(down(), up());
        when(mailBuilder.buildHtmlMail(org.mockito.ArgumentMatchers.anyMap(), anyString()))
                .thenReturn("content");
        when(publisher.publish(anyString(), anyString(), anyString(), eq(true))).thenReturn(1);
        HealthCheckTask task = new HealthCheckTask(
                healthService, mailBuilder, publisher, mock(MailService.class), redisTemplate,
                1800, "mok-framework", "test-host", "8080");

        task.scheduledHealthCheck();
        task.scheduledHealthCheck();

        verify(redisTemplate, times(2)).execute(
                any(RedisScript.class),
                eq(List.of("monitor:health:alert:mok-framework-test-host-8080:active")),
                any(Object[].class));
        verify(redisTemplate).delete(List.of(
                "monitor:health:alert:mok-framework-test-host-8080:event:alert-1:in-flight",
                "monitor:health:alert:mok-framework-test-host-8080:event:alert-1:cooldown"));
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void redisRecoveryKeepsTheFallbackAlertId() {
        HealthCheckService healthService = mock(HealthCheckService.class);
        HealthCheckMailBuilder mailBuilder = mock(HealthCheckMailBuilder.class);
        SystemCheckMailPublisher publisher = mock(SystemCheckMailPublisher.class);
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        AtomicBoolean redisRecovered = new AtomicBoolean(false);
        AtomicInteger activeStateCalls = new AtomicInteger();
        when(redisTemplate.execute(
                any(RedisScript.class), anyList(), any(Object[].class)))
                .thenAnswer(invocation -> {
                    RedisScript<?> script = invocation.getArgument(0);
                    if (!String.class.equals(script.getResultType())) {
                        return redisRecovered.get() ? 1L : null;
                    }
                    if (activeStateCalls.getAndIncrement() == 0) {
                        return null;
                    }
                    redisRecovered.set(true);
                    Object[] arguments = invocation.getArgument(2);
                    return arguments[0] + "|" + arguments[1];
                });
        when(healthService.performHealthCheck()).thenReturn(down());
        when(mailBuilder.buildHtmlMail(org.mockito.ArgumentMatchers.anyMap(), anyString()))
                .thenReturn("content");
        when(publisher.publish(anyString(), anyString(), anyString(), eq(true)))
                .thenThrow(new IllegalStateException("broker unavailable"))
                .thenReturn(1);
        HealthCheckTask task = new HealthCheckTask(
                healthService, mailBuilder, publisher, mock(MailService.class), redisTemplate,
                1800, "mok-framework", "test-host", "8080");

        task.scheduledHealthCheck();
        task.scheduledHealthCheck();

        ArgumentCaptor<String> alertIds = ArgumentCaptor.forClass(String.class);
        verify(publisher, times(2)).publish(
                alertIds.capture(), anyString(), anyString(), eq(true));
        assertThat(alertIds.getAllValues().get(0))
                .isEqualTo(alertIds.getAllValues().get(1));
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void failedRecoveryCleanupForcesANewAlertAfterRedisReturns() {
        HealthCheckService healthService = mock(HealthCheckService.class);
        HealthCheckMailBuilder mailBuilder = mock(HealthCheckMailBuilder.class);
        SystemCheckMailPublisher publisher = mock(SystemCheckMailPublisher.class);
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        AtomicInteger activeStateCalls = new AtomicInteger();
        when(redisTemplate.execute(
                any(RedisScript.class), anyList(), any(Object[].class)))
                .thenAnswer(invocation -> {
                    RedisScript<?> script = invocation.getArgument(0);
                    if (!String.class.equals(script.getResultType())) {
                        return 1L;
                    }
                    int call = activeStateCalls.getAndIncrement();
                    if (call == 0) {
                        return "DOWN:_database_DOWN_|old-alert";
                    }
                    if (call == 1) {
                        throw new IllegalStateException("redis unavailable during recovery");
                    }
                    Object[] arguments = invocation.getArgument(2);
                    return "1".equals(arguments[3])
                            ? arguments[0] + "|" + arguments[1]
                            : "DOWN:_database_DOWN_|old-alert";
                });
        when(healthService.performHealthCheck()).thenReturn(down(), up(), down());
        when(mailBuilder.buildHtmlMail(org.mockito.ArgumentMatchers.anyMap(), anyString()))
                .thenReturn("content");
        when(publisher.publish(anyString(), anyString(), anyString(), eq(true))).thenReturn(1);
        HealthCheckTask task = new HealthCheckTask(
                healthService, mailBuilder, publisher, mock(MailService.class), redisTemplate,
                1800, "mok-framework", "test-host", "8080");

        task.scheduledHealthCheck();
        task.scheduledHealthCheck();
        task.scheduledHealthCheck();

        ArgumentCaptor<String> alertIds = ArgumentCaptor.forClass(String.class);
        verify(publisher, times(2)).publish(
                alertIds.capture(), anyString(), anyString(), eq(true));
        assertThat(alertIds.getAllValues()).hasSize(2);
        assertThat(alertIds.getAllValues().get(0)).isEqualTo("old-alert");
        assertThat(alertIds.getAllValues().get(1)).isNotEqualTo("old-alert");
    }

    private HealthCheckTask task(HealthCheckService healthService,
                                 HealthCheckMailBuilder mailBuilder,
                                 SystemCheckMailPublisher publisher) {
        return new HealthCheckTask(
                healthService,
                mailBuilder,
                publisher,
                mock(MailService.class),
                mock(StringRedisTemplate.class),
                1800,
                "mok-framework",
                "test-host",
                "8080");
    }

    private Map<String, Object> down() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "DOWN");
        health.put("database", new HealthCheckServiceImpl.HealthCheckResult("DOWN", Map.of()));
        health.put("rabbitmq", new HealthCheckServiceImpl.HealthCheckResult("UP", Map.of()));
        return health;
    }

    private Map<String, Object> up() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("database", new HealthCheckServiceImpl.HealthCheckResult("UP", Map.of()));
        health.put("rabbitmq", new HealthCheckServiceImpl.HealthCheckResult("UP", Map.of()));
        return health;
    }
}
