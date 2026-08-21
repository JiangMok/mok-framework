package com.mok.framework.monitor.controller;

import com.mok.framework.common.R;
import top.jiangmok.operationlog.annotation.OperationLog;
import top.jiangmok.operationlog.enums.BusinessType;
import com.mok.framework.monitor.service.HealthCheckService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/system")
@Tag(name = "系统管理-监控", description = "系统管理-监控相关接口")
public class MonitorController {

    private final HealthCheckService healthCheckService;

    public MonitorController(HealthCheckService healthCheckService) {
        this.healthCheckService = healthCheckService;
    }

    /**
     * @description: 获取系统信息
     * @author: JN
     * @date: 2026/1/6 14:40
     * @param: []
     * @return: com.mok.framework.common.R<java.util.Map < java.lang.String, java.lang.Object>>
     **/
    @Operation(summary = "获取系统信息")
    @OperationLog(title = "获取系统信息", businessType = BusinessType.QUERY)
    @GetMapping("/info")
    public R<Map<String, Object>> getSystemInfo() {
        Map<String, Object> info = Map.of(
                "appName", "MOK-framework",
                "version", "1.1.0",
                "javaVersion", System.getProperty("java.version"),
                "osName", System.getProperty("os.name"),
                "osArch", System.getProperty("os.arch"),
                "userHome", System.getProperty("user.home"),
                "timestamp", System.currentTimeMillis(),
                "upTime", getUptime()
        );

        return R.ok(info);
    }

    /**
     * @description: 健康检查
     * @author: JN
     * @date: 2026/1/6 14:55
     * @param: []
     * @return: com.mok.framework.common.R<java.util.Map < java.lang.String, java.lang.Object>>
     **/
    @Operation(summary = "健康检查")
    @GetMapping("/health")
    @OperationLog(title = "系统监控", businessType = BusinessType.QUERY, saveResponseData = false)
    public R<Map<String, Object>> healthCheck() {
        Map<String, Object> health = healthCheckService.performHealthCheck();
        return R.ok(health);
    }

    /**
     * @description: 获取应用运行时间
     * @author: JN
     * @date: 2026/1/6 14:55
     * @param: []
     * @return: java.lang.String
     **/
    private String getUptime() {
        long uptimeMillis = ManagementFactory.getRuntimeMXBean().getUptime();
        return formatUptime(uptimeMillis);
    }

    /**
     * @description: 格式化运行时间
     * @author: JN
     * @date: 2026/1/6 14:55
     * @param: [millis]
     * @return: java.lang.String
     **/
    private String formatUptime(long millis) {
        long days = TimeUnit.MILLISECONDS.toDays(millis);
        long hours = TimeUnit.MILLISECONDS.toHours(millis) % 24;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0) {
            sb.append(days).append("天 ");
        }
        if (hours > 0) {
            sb.append(hours).append("小时 ");
        }
        if (minutes > 0) {
            sb.append(minutes).append("分钟 ");
        }
        sb.append(seconds).append("秒");

        return sb.toString();
    }
}
