package com.mok.framework.task.scheduler;

import com.mok.framework.common.utils.LogUtils;
import com.mok.framework.mail.service.MailService;
import com.mok.framework.mail.util.HealthCheckMailBuilder;
import com.mok.framework.model.enums.MailType;
import com.mok.framework.monitor.service.HealthCheckService;
import com.mok.framework.task.config.TimeConfig;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class HealthCheckTask {
    private static final Logger log = LogUtils.getLogger(HealthCheckTask.class);

    private final HealthCheckService healthCheckService;
    private final HealthCheckMailBuilder mailBuilder;
    private final MailService mailService;

    public HealthCheckTask(HealthCheckService healthCheckService,
                           HealthCheckMailBuilder mailBuilder,
                           MailService mailService) {
        this.healthCheckService = healthCheckService;
        this.mailBuilder = mailBuilder;
        this.mailService = mailService;
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
                String subject = "DOWN".equals(status)
                        ? "mok-framework-系统健康检查失败通知"
                        : "mok-framework-系统健康检查异常通知";
                String content = mailBuilder.buildHtmlMail(health, status);

                // 按邮件类型群发到所有订阅了 SYSTEM_CHECK 的收件人
                mailService.sendByMailType(MailType.SYSTEM_CHECK, subject, content, true);
            } else {
                log.info("系统健康检查正常: {}", health);
            }
        } catch (Exception e) {
            log.error("健康检查任务执行失败", e);
        }
    }
}
