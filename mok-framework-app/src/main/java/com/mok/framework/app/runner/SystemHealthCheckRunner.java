package com.mok.framework.app.runner;

import com.mok.framework.app.config.SystemStartConfig;
import com.mok.framework.common.utils.LogUtils;
import com.mok.framework.mail.service.MailService;
import com.mok.framework.mail.util.HealthCheckMailBuilder;
import com.mok.framework.model.enums.MailType;
import com.mok.framework.monitor.service.HealthCheckService;
import com.mok.framework.monitor.service.impl.HealthCheckServiceImpl;
import com.mok.framework.mq.service.SystemCheckMailPublisher;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Order(2)
public class SystemHealthCheckRunner implements ApplicationRunner {

    private final static Logger log = LogUtils.getLogger(SystemHealthCheckRunner.class);

    private final HealthCheckService healthCheckService;
    private final SystemCheckMailPublisher mailPublisher;
    private final MailService mailService;
    private final HealthCheckMailBuilder mailBuilder;
    private final SystemStartConfig systemStartConfig;
    private final String instanceId;

    public SystemHealthCheckRunner(HealthCheckService healthCheckService,
                                   SystemCheckMailPublisher mailPublisher,
                                   MailService mailService,
                                   HealthCheckMailBuilder mailBuilder,
                                   SystemStartConfig systemStartConfig,
                                   @Value("${spring.application.name:mok-framework}") String applicationName,
                                   @Value("${HOSTNAME:${COMPUTERNAME:local}}") String hostName,
                                   @Value("${server.port:8080}") String serverPort) {
        this.healthCheckService = healthCheckService;
        this.mailPublisher = mailPublisher;
        this.mailService = mailService;
        this.mailBuilder = mailBuilder;
        this.systemStartConfig = systemStartConfig;
        this.instanceId = sanitizeKeyPart(applicationName + '-' + hostName + '-' + serverPort);
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("========== 🆗 系统启动成功 ==========");
        log.info("========== 🔛 执行健康检查 - 开始 ==========");

        Map<String, Object> health;
        try {
            health = healthCheckService.performHealthCheck();
            String status = (String) health.get("status");

            if ("DOWN".equals(status)) {
                log.error("========== ❌ 系统健康检查失败: {}", health);
            } else if ("WARNING".equals(status)) {
                log.warn("========== ⚠️ 系统健康检查警告: {}", health);
            } else {
                log.info("========== ✅ 系统健康检查正常: {}", health);
            }
        } catch (Exception e) {
            log.error("========== 健康检查任务执行失败", e);
            return;
        }

        String status = String.valueOf(health.getOrDefault("status", "UP"));
        String subject = '[' + instanceId + "] mok-framework-系统启动健康报告";
        Map<String, Object> mailHealth = new HashMap<>(health);
        mailHealth.put("instanceId", instanceId);
        String content = mailBuilder.buildHtmlMail(mailHealth, status);
        // 按邮件类型群发到所有订阅了 SYSTEM_CHECK 的收件人
        log.info("========== 系统启动健康邮件配置:{}",
                systemStartConfig.getSystemStartCheckMail() ? "启用":"停用");
        if(systemStartConfig.getSystemStartCheckMail()){
            try {
                int recipientCount = sendNotification(health, subject, content);
                log.info("========== 系统启动健康报告邮件已处理: status={}, count={}",
                        status, recipientCount);
            } catch (Exception e) {
                // 健康邮件失败不应阻止应用完成启动
                log.error("========== 系统启动健康报告邮件发布失败", e);
            }
        }
        log.info("========== 🔚 执行健康检查 - 结束 ==========");
    }

    private int sendNotification(Map<String, Object> health, String subject, String content) {
        Object rabbitResult = health.get("rabbitmq");
        if (rabbitResult instanceof HealthCheckServiceImpl.HealthCheckResult result
                && !result.isUp()) {
            log.warn("========== RabbitMQ 不可用，启动报告降级为 SMTP 直发");
            return mailService.sendByMailType(MailType.SYSTEM_CHECK, subject, content, true);
        }
        return mailPublisher.publish(subject, content, true);
    }

    private String sanitizeKeyPart(String value) {
        return value == null ? "unknown" : value.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
