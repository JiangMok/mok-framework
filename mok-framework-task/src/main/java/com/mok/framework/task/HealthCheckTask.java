package com.mok.framewoek.task;

import cn.hutool.core.util.IdUtil;
import com.mok.framework.common.utils.LogUtils;
import com.mok.framework.model.dto.SystemCheckMailMessage;
import com.mok.framework.monitor.service.HealthCheckService;
import org.slf4j.Logger;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @description: 定时健康检查
 * @author: JN
 * @date: 2026/1/6 14:57
 * @param:
 * @return:
 **/

@Component
public class HealthCheckTask {
    private static final Logger log = LogUtils.getLogger(HealthCheckTask.class);

    private final HealthCheckService healthCheckService;
    private final RabbitTemplate rabbitTemplate;

    public HealthCheckTask(HealthCheckService healthCheckService,
                           RabbitTemplate rabbitTemplate) {
        this.healthCheckService = healthCheckService;
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * 每5分钟执行一次健康检查
     */
    @Scheduled(fixedRate = 300000) // 例:60秒 60*1000=60000
    public void scheduledHealthCheck() {
        SystemCheckMailMessage message = new SystemCheckMailMessage();
        message.setId(IdUtil.simpleUUID());
        message.setRecipient("jiangmok@qq.com");
        message.setSubject("mok-framework-系统健康检查失败通知");
        message.setContent("系统健康检查失败 : \n" + "你好你好你好你好你好你好你好你好你好");
        // 异步发送邮件
        rabbitTemplate.convertAndSend(
                "system.check.mail.exchange",
                "system.check.mail.routing",
                message);
        try {
            Map<String, Object> health = healthCheckService.performHealthCheck();
            String status = (String) health.get("status");

            if ("DOWN".equals(status)) {
                log.error("❌ 系统健康检查失败: {}", health);
                // 可以发送告警邮件、钉钉消息等
//                SystemCheckMailMessage message = new SystemCheckMailMessage();
//                message.setId(IdUtil.simpleUUID());
//                message.setRecipient("jiangmok@qq.com");
//                message.setSubject("mok-framework-系统健康检查失败通知");
//                message.setContent("系统健康检查失败 : \n" + health);
//                // 异步发送邮件
//                rabbitTemplate.convertAndSend(
//                        "system.check.mail.exchange",
//                        "system.check.mail.routing",
//                        message);
            } else if ("WARNING".equals(status)) {
                log.warn("⚠️ 系统健康检查警告: {}", health);
            } else {
                log.info("✅ 系统健康检查正常: {}", health);
            }
        } catch (Exception e) {
            log.error("健康检查任务执行失败", e);
        }
    }
}