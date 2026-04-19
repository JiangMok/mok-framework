package com.mok.framework.runner;

import com.mok.framework.common.utils.LogUtils;
import com.mok.framework.monitor.service.HealthCheckService;
import org.slf4j.Logger;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Order(1)
public class SystemRunner implements ApplicationRunner {

    private final static Logger log = LogUtils.getLogger(SystemRunner.class);

    private final HealthCheckService healthCheckService;

    public SystemRunner(HealthCheckService healthCheckService) {
        this.healthCheckService = healthCheckService;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("========== 系统启动成功 ==========");
        log.info("========== 执行健康装填检查 - 开始 ==========");
        try {
            Map<String, Object> health = healthCheckService.performHealthCheck();
            String status = (String) health.get("status");

            if ("DOWN".equals(status)) {
                log.error("❌ 系统健康检查失败: {}", health);
                // 可以发送告警邮件、钉钉消息等
            } else if ("WARNING".equals(status)) {
                log.warn("⚠️ 系统健康检查警告: {}", health);
            } else {
                log.info("✅ 系统健康检查正常: {}", health);
            }
        } catch (Exception e) {
            log.error("健康检查任务执行失败", e);
        }
        log.info("========== 执行健康装填检查 - 结束 ==========");
    }
}
