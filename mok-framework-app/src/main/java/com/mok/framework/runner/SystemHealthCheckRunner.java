package com.mok.framework.runner;

import cn.hutool.core.util.IdUtil;
import com.mok.framework.common.utils.LogUtils;
import com.mok.framework.model.dto.SystemCheckMailMessage;
import com.mok.framework.monitor.service.HealthCheckService;
import org.slf4j.Logger;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Component
@Order(1)
public class SystemHealthCheckRunner implements ApplicationRunner {

    private final static Logger log = LogUtils.getLogger(SystemHealthCheckRunner.class);

    private final HealthCheckService healthCheckService;
    private final RabbitTemplate rabbitTemplate;


    public SystemHealthCheckRunner(HealthCheckService healthCheckService,
                                   RabbitTemplate rabbitTemplate) {
        this.healthCheckService = healthCheckService;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        // 获取当前日期时间
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime systemStartTime = LocalDateTime.now();
        String emailContent = "系统启动成功于" + dateTimeFormatter.format(systemStartTime) + "\n";
        log.info("========== \uD83C\uDD97 系统启动成功 ==========");
        log.info("========== \uD83D\uDD1B 执行健康装填检查 - 开始 ==========");
        try {
            Map<String, Object> health = healthCheckService.performHealthCheck();
            String status = (String) health.get("status");

            if ("DOWN".equals(status)) {
                log.error("========== ❌ 系统健康检查失败: {}", health);
                // 可以发送告警邮件、钉钉消息等
            } else if ("WARNING".equals(status)) {
                log.warn("========== ⚠️ 系统健康检查警告: {}", health);
            } else {
                log.info("========== ✅ 系统健康检查正常: {}", health);
            }
        } catch (Exception e) {
            log.error("========== 健康检查任务执行失败", e);
        }
        LocalDateTime systemCheckTime = LocalDateTime.now();
        emailContent += "系统检查成功于" + dateTimeFormatter.format(systemCheckTime) + "\n";
        // 构建启动成功通知邮件
        SystemCheckMailMessage message = new SystemCheckMailMessage();
        message.setId(IdUtil.simpleUUID());
        message.setRecipient("jiangmok@qq.com");
        message.setSubject("mok-framework-启动成功通知");
        message.setContent("系统启动检查成功 : \n" + emailContent);
        // 异步发送邮件
//        rabbitTemplate.convertAndSend(
//                "system.check.mail.exchange",
//                "system.check.mail.routing",
//                message);
        log.info("========== SystemCheckMailMessage : {}",message.toString());
        log.info("========== \uD83D\uDD1A 执行健康装填检查 - 结束 ==========");
    }
}
