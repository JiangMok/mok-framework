package com.mok.framework.runner;

import cn.hutool.core.util.IdUtil;
import com.mok.framework.common.constant.mq.SystemCheckMailMQConstant;
import com.mok.framework.common.utils.LogUtils;
import com.mok.framework.mail.util.HealthCheckMailBuilder;
import com.mok.framework.model.dto.SystemCheckMailMessage;
import com.mok.framework.monitor.service.HealthCheckService;
import org.slf4j.Logger;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Order(1)
public class SystemHealthCheckRunner implements ApplicationRunner {

    private final static Logger log = LogUtils.getLogger(SystemHealthCheckRunner.class);

    private final HealthCheckService healthCheckService;
    private final RabbitTemplate rabbitTemplate;
    private final HealthCheckMailBuilder mailBuilder;

    public SystemHealthCheckRunner(HealthCheckService healthCheckService,
                                   RabbitTemplate rabbitTemplate,
                                   HealthCheckMailBuilder mailBuilder) {
        this.healthCheckService = healthCheckService;
        this.rabbitTemplate = rabbitTemplate;
        this.mailBuilder = mailBuilder;
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("========== 🆗 系统启动成功 ==========");
        log.info("========== 🔛 执行健康装填检查 - 开始 ==========");

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
        SystemCheckMailMessage message = new SystemCheckMailMessage();
        message.setId(IdUtil.simpleUUID());
        message.setRecipient("jiangmok@qq.com");
        message.setSubject("mok-framework-系统启动健康报告");
        message.setContent(mailBuilder.buildHtmlMail(health, status));
        message.setHtml(true);

        rabbitTemplate.convertAndSend(
                SystemCheckMailMQConstant.SYSTEM_CHECK_MAIL_EXCHANGE,
                SystemCheckMailMQConstant.SYSTEM_CHECK_MAIL_ROUTING_KEY,
                message);
        log.info("========== 系统启动健康报告邮件已发送: status={}", status);
        log.info("========== 🔚 执行健康装填检查 - 结束 ==========");
    }
}
