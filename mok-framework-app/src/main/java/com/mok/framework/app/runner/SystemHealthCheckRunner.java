package com.mok.framework.app.runner;

import com.mok.framework.app.config.SystemStartConfig;
import com.mok.framework.common.utils.LogUtils;
import com.mok.framework.mail.service.MailService;
import com.mok.framework.mail.util.HealthCheckMailBuilder;
import com.mok.framework.model.enums.MailType;
import com.mok.framework.monitor.service.HealthCheckService;
import org.slf4j.Logger;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Order(2)
public class SystemHealthCheckRunner implements ApplicationRunner {

    private final static Logger log = LogUtils.getLogger(SystemHealthCheckRunner.class);

    private final HealthCheckService healthCheckService;
    private final MailService mailService;
    private final HealthCheckMailBuilder mailBuilder;
    private final SystemStartConfig systemStartConfig;

    public SystemHealthCheckRunner(HealthCheckService healthCheckService,
                                   MailService mailService,
                                   HealthCheckMailBuilder mailBuilder,
                                   SystemStartConfig systemStartConfig) {
        this.healthCheckService = healthCheckService;
        this.mailService = mailService;
        this.mailBuilder = mailBuilder;
        this.systemStartConfig = systemStartConfig;
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
        String subject = "mok-framework-系统启动健康报告";
        String content = mailBuilder.buildHtmlMail(health, status);
        // 按邮件类型群发到所有订阅了 SYSTEM_CHECK 的收件人
        log.info("========== 系统启动健康邮件配置:{}",
                systemStartConfig.getSystemStartCheckMail() ? "启用":"停用");
        if(systemStartConfig.getSystemStartCheckMail()){
            mailService.sendByMailType(MailType.SYSTEM_CHECK, subject, content, true);
            log.info("========== 系统启动健康报告邮件已发送: status={}", status);
        }
        log.info("========== 🔚 执行健康检查 - 结束 ==========");
    }
}
