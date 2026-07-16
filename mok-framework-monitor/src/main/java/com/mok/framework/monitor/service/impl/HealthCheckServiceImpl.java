package com.mok.framework.monitor.service.impl;

import com.mok.framework.common.utils.LogUtils;
import com.mok.framework.monitor.service.HealthCheckService;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.File;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @description:
 * @author: JN
 * @date: 2026/1/6
 */

@Service

public class HealthCheckServiceImpl implements HealthCheckService {
    private static final Logger log = LogUtils.getLogger(HealthCheckServiceImpl.class);
    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;
    private final RedisConnectionFactory redisConnectionFactory;
//    private final ElasticsearchClient elasticsearchClient;
    private final RabbitTemplate rabbitTemplate;

    public HealthCheckServiceImpl(DataSource dataSource,
                                  JdbcTemplate jdbcTemplate,
                                  RedisConnectionFactory redisConnectionFactory,
//                                  ElasticsearchClient elasticsearchClient,
                                  RabbitTemplate rabbitTemplate
    ) {
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
        this.redisConnectionFactory = redisConnectionFactory;
//        this.elasticsearchClient = elasticsearchClient;
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * 执行完整的健康检查
     */
    @Override
    public Map<String, Object> performHealthCheck() {
        Map<String, Object> healthInfo = new HashMap<>();
        healthInfo.put("timestamp", System.currentTimeMillis());

        // 检查数据库连接
        HealthCheckResult dbResult = checkDatabase();
        healthInfo.put("database", dbResult);

        // 检查Redis连接
        HealthCheckResult redisResult = checkRedis();
        healthInfo.put("redis", redisResult);

        // 检查应用内存
        HealthCheckResult memoryResult = checkMemory();
        healthInfo.put("memory", memoryResult);

        // 检查ES数据库
//        HealthCheckResult elasticsearchResult = checkElasticsearch();
//        healthInfo.put("elasticsearch", elasticsearchResult);

        // 检查rabbitmq
        HealthCheckResult rabbitmqResult = checkRabbitMQ();
        healthInfo.put("rabbitmq", rabbitmqResult);

        // 检查 CPU
        HealthCheckResult cpuResult = checkCpu();
        healthInfo.put("cpu", cpuResult);

        // 检查线程
        HealthCheckResult threadsResult = checkThreads();
        healthInfo.put("threads", threadsResult);

        // 检查 GC
        HealthCheckResult gcResult = checkGc();
        healthInfo.put("gc", gcResult);

        // 检查磁盘
        HealthCheckResult diskResult = checkDisk();
        healthInfo.put("disk", diskResult);

        // 检查连接池
        HealthCheckResult poolResult = checkConnectionPool();
        healthInfo.put("connectionPool", poolResult);

        // 计算总体状态：有 DOWN → DOWN，有 WARNING 但无 DOWN → WARNING，否则 UP
        HealthCheckResult[] results = {
                redisResult, memoryResult, rabbitmqResult,
                cpuResult, threadsResult, gcResult, diskResult, poolResult
        };
        boolean hasDown = false;
        boolean hasWarning = false;
        for (HealthCheckResult r : results) {
            if (!r.isUp()) hasDown = true;
            else if ("WARNING".equals(r.getStatus())) hasWarning = true;
        }
        String overall;
        if (hasDown) overall = "DOWN";
        else if (hasWarning) overall = "WARNING";
        else overall = "UP";
        healthInfo.put("status", overall);
        healthInfo.put("application", "MOK-Framework");
        healthInfo.put("version", "1.1.0");

        return healthInfo;
    }

    /**
     * 检查数据库连接
     */
    private HealthCheckResult checkDatabase() {
        long startTime = System.currentTimeMillis();
        try {
            // 1. 检查连接池
            try (Connection connection = dataSource.getConnection()) {
                // 5秒超时
                boolean isValid = connection.isValid(5);
                long responseTime = System.currentTimeMillis() - startTime;

                // 2. 执行简单查询
                String version = jdbcTemplate.queryForObject(
                        "SELECT VERSION()", String.class
                );
                int userCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sys_user WHERE is_deleted = 0", Integer.class);

                return HealthCheckResult.builder()
                        .status("UP")
                        .details(Map.of(
                                "version", version,
                                "userCount", userCount,
                                "responseTime", responseTime + "ms",
                                "connection", "Valid"
                        ))
                        .build();
            }
        } catch (SQLException e) {
            log.error("数据库健康检查失败", e);
            return HealthCheckResult.builder()
                    .status("DOWN")
                    .details(Map.of("error", e.getMessage()))
                    .build();
        } catch (Exception e) {
            log.error("数据库查询失败", e);
            return HealthCheckResult.builder()
                    .status("DOWN")
                    .details(Map.of("error", "数据库查询失败: " + e.getMessage()))
                    .build();
        }
    }

    /**
     * 检查 Redis 连接
     */
    private HealthCheckResult checkRedis() {
        long startTime = System.currentTimeMillis();
        try {
            RedisConnection connection = redisConnectionFactory.getConnection();
            try {
                // 执行 PING 命令
                String pong = connection.ping();
                long responseTime = System.currentTimeMillis() - startTime;

                // 获取 Redis 信息
                String info = String.valueOf(connection.info("server"));

                return HealthCheckResult.builder()
                        .status("UP")
                        .details(Map.of(
                                "response", pong,
                                "responseTime", responseTime + "ms",
                                "info", "Connected"
                        ))
                        .build();
            } finally {
                connection.close();
            }
        } catch (DataAccessException e) {
            log.error("Redis健康检查失败", e);
            return HealthCheckResult.builder()
                    .status("DOWN")
                    .details(Map.of("error", e.getMessage()))
                    .build();
        }
    }

    /**
     * 检查内存使用情况
     */
    private HealthCheckResult checkMemory() {
        Runtime runtime = Runtime.getRuntime();

        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;

        double usedPercentage = (double) usedMemory / maxMemory * 100;

        Map<String, Object> details = new HashMap<>();
        details.put("max", formatBytes(maxMemory));
        details.put("total", formatBytes(totalMemory));
        details.put("used", formatBytes(usedMemory));
        details.put("free", formatBytes(freeMemory));
        details.put("usedPercentage", String.format("%.2f%%", usedPercentage));

        // 如果内存使用超过90%，标记为警告
        String status = usedPercentage > 90 ? "WARNING" : "UP";

        return HealthCheckResult.builder()
                .status(status)
                .details(details)
                .build();
    }


    /**
     * 检查 Elasticsearch 连接
     */
//    private HealthCheckResult checkElasticsearch() {
//        long startTime = System.currentTimeMillis();
//        try {
//            HealthResponse health = elasticsearchClient.cluster().health();
//            long responseTime = System.currentTimeMillis() - startTime;
//
//            Map<String, Object> details = new HashMap<>();
//            details.put("clusterName", health.clusterName());
//            details.put("status", health.status().jsonValue()); // green/yellow/red
//            details.put("responseTime", responseTime + "ms");
//            details.put("nodeCount", health.numberOfNodes());
//            details.put("dataNodeCount", health.numberOfDataNodes());
//
//            String healthStatus;
//            if ("green".equals(health.status().jsonValue())) {
//                healthStatus = "UP";
//            } else if ("yellow".equals(health.status().jsonValue())) {
//                healthStatus = "WARNING";
//            } else {
//                healthStatus = "DOWN";
//            }
//
//            return HealthCheckResult.builder()
//                    .status(healthStatus)
//                    .details(details)
//                    .build();
//
//        } catch (Exception e) {
//            log.error("Elasticsearch 健康检查失败", e);
//            return HealthCheckResult.builder()
//                    .status("DOWN")
//                    .details(Map.of("error", e.getMessage()))
//                    .build();
//        }
//    }

    /**
     * 检查 RabbitMQ 连接
     */
    private HealthCheckResult checkRabbitMQ() {
        long startTime = System.currentTimeMillis();
        try {
            // 方式1：通过 execute 发送一个空操作，如果连接正常则不会抛异常
            rabbitTemplate.execute(channel -> {
                // 什么都不做，仅用于测试连接是否可用
                return true;
            });
            long responseTime = System.currentTimeMillis() - startTime;

            // 获取连接工厂的基本信息（CachingConnectionFactory）
            CachingConnectionFactory connectionFactory =
                    (CachingConnectionFactory) rabbitTemplate.getConnectionFactory();
            String host = connectionFactory.getHost();
            int port = connectionFactory.getPort();
            String virtualHost = connectionFactory.getVirtualHost();

            Map<String, Object> details = new HashMap<>();
            details.put("host", host);
            details.put("port", port);
            details.put("virtualHost", virtualHost);
            details.put("responseTime", responseTime + "ms");
            details.put("connectionStatus", "Connected");

            return HealthCheckResult.builder()
                    .status("UP")
                    .details(details)
                    .build();

        } catch (AmqpException e) {
            log.error("RabbitMQ 健康检查失败", e);
            return HealthCheckResult.builder()
                    .status("DOWN")
                    .details(Map.of("error", e.getMessage()))
                    .build();
        } catch (Exception e) {
            log.error("RabbitMQ 健康检查异常", e);
            return HealthCheckResult.builder()
                    .status("DOWN")
                    .details(Map.of("error", e.getMessage()))
                    .build();
        }
    }

    /**
     * 检查 CPU 使用情况
     */
    private HealthCheckResult checkCpu() {
        try {
            OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            int processors = Runtime.getRuntime().availableProcessors();
            double loadAvg = osBean.getSystemLoadAverage();

            Map<String, Object> details = new HashMap<>();
            details.put("processors", processors);
            details.put("loadAverage", String.format("%.2f", loadAvg >= 0 ? loadAvg : 0));
            details.put("loadPerProcessor", String.format("%.2f%%",
                    loadAvg >= 0 ? (loadAvg / processors) * 100 : 0));

            // 负载超过核数的 80% 视为警告
            String status = (loadAvg >= 0 && loadAvg > processors * 0.8) ? "WARNING" : "UP";

            return HealthCheckResult.builder()
                    .status(status)
                    .details(details)
                    .build();
        } catch (Exception e) {
            log.error("CPU 健康检查失败", e);
            return HealthCheckResult.builder()
                    .status("DOWN")
                    .details(Map.of("error", e.getMessage()))
                    .build();
        }
    }

    /**
     * 检查线程使用情况
     */
    private HealthCheckResult checkThreads() {
        try {
            ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
            int activeThreads = threadBean.getThreadCount();
            int peakThreads = threadBean.getPeakThreadCount();
            long totalStarted = threadBean.getTotalStartedThreadCount();
            int daemonThreads = threadBean.getDaemonThreadCount();

            Map<String, Object> details = new HashMap<>();
            details.put("active", activeThreads);
            details.put("peak", peakThreads);
            details.put("daemon", daemonThreads);
            details.put("totalStarted", totalStarted);
            details.put("deadlocked", threadBean.findDeadlockedThreads() != null
                    ? threadBean.findDeadlockedThreads().length : 0);

            // 活跃线程超过 500 视为警告
            String status = activeThreads > 500 ? "WARNING" : "UP";

            return HealthCheckResult.builder()
                    .status(status)
                    .details(details)
                    .build();
        } catch (Exception e) {
            log.error("线程健康检查失败", e);
            return HealthCheckResult.builder()
                    .status("DOWN")
                    .details(Map.of("error", e.getMessage()))
                    .build();
        }
    }

    /**
     * 检查 GC 情况
     */
    private HealthCheckResult checkGc() {
        try {
            List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
            long totalGcCount = 0;
            long totalGcTime = 0;

            Map<String, Object> details = new HashMap<>();
            for (GarbageCollectorMXBean gcBean : gcBeans) {
                totalGcCount += gcBean.getCollectionCount();
                totalGcTime += gcBean.getCollectionTime();
                details.put(gcBean.getName() + "Count", gcBean.getCollectionCount());
                details.put(gcBean.getName() + "Time", gcBean.getCollectionTime() + "ms");
            }
            details.put("totalCollections", totalGcCount);
            details.put("totalTime", totalGcTime + "ms");

            String status = "UP";

            return HealthCheckResult.builder()
                    .status(status)
                    .details(details)
                    .build();
        } catch (Exception e) {
            log.error("GC 健康检查失败", e);
            return HealthCheckResult.builder()
                    .status("DOWN")
                    .details(Map.of("error", e.getMessage()))
                    .build();
        }
    }

    /**
     * 检查磁盘空间
     */
    private HealthCheckResult checkDisk() {
        try {
            File[] roots = File.listRoots();
            Map<String, Object> details = new HashMap<>();
            String worstStatus = "UP";

            for (File root : roots) {
                long total = root.getTotalSpace();
                long free = root.getFreeSpace();
                long usable = root.getUsableSpace();
                double usedPercent = total > 0 ? ((double) (total - free) / total) * 100 : 0;

                Map<String, Object> diskInfo = new HashMap<>();
                diskInfo.put("total", formatBytes(total));
                diskInfo.put("free", formatBytes(free));
                diskInfo.put("usable", formatBytes(usable));
                diskInfo.put("usedPercent", String.format("%.1f%%", usedPercent));

                details.put(root.getPath(), diskInfo);

                // 使用率超过 90% 视为警告
                if (usedPercent > 90) {
                    worstStatus = "WARNING";
                } else if (usedPercent > 95) {
                    worstStatus = "DOWN";
                }
            }

            return HealthCheckResult.builder()
                    .status(worstStatus)
                    .details(details)
                    .build();
        } catch (Exception e) {
            log.error("磁盘健康检查失败", e);
            return HealthCheckResult.builder()
                    .status("DOWN")
                    .details(Map.of("error", e.getMessage()))
                    .build();
        }
    }

    /**
     * 检查数据库连接池状态 (HikariCP)
     */
    private HealthCheckResult checkConnectionPool() {
        try {
            if (dataSource instanceof HikariDataSource) {
                HikariDataSource hikariDs = (HikariDataSource) dataSource;
                com.zaxxer.hikari.HikariPoolMXBean poolBean = hikariDs.getHikariPoolMXBean();

                Map<String, Object> details = new HashMap<>();
                details.put("active", poolBean.getActiveConnections());
                details.put("idle", poolBean.getIdleConnections());
                details.put("total", poolBean.getTotalConnections());
                details.put("pending", poolBean.getThreadsAwaitingConnection());
                details.put("maxPoolSize", hikariDs.getMaximumPoolSize());
                details.put("connectionTimeout", hikariDs.getConnectionTimeout() + "ms");

                // 等待线程 > 0 视为警告
                String status = poolBean.getThreadsAwaitingConnection() > 0 ? "WARNING" : "UP";

                return HealthCheckResult.builder()
                        .status(status)
                        .details(details)
                        .build();
            } else {
                return HealthCheckResult.builder()
                        .status("UP")
                        .details(Map.of("info", "非 HikariCP 数据源，跳过连接池检查"))
                        .build();
            }
        } catch (Exception e) {
            log.error("连接池健康检查失败", e);
            return HealthCheckResult.builder()
                    .status("DOWN")
                    .details(Map.of("error", e.getMessage()))
                    .build();
        }
    }

    /**
     * 格式化字节数
     */
    private String formatBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        }
        if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        }
        return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }

    /**
     * 健康检查结果内部类
     */
    public static class HealthCheckResult {
        private String status;
        private Map<String, Object> details;

        // 构造方法
        public HealthCheckResult() {
        }

        public HealthCheckResult(String status, Map<String, Object> details) {
            this.status = status;
            this.details = details;
        }

        // Builder 模式
        public static Builder builder() {
            return new Builder();
        }

        // Getter 和 Setter 方法
        public String getStatus() {
            return status;
        }

        public HealthCheckResult setStatus(String status) {
            this.status = status;
            return this;
        }

        public Map<String, Object> getDetails() {
            return details;
        }

        public HealthCheckResult setDetails(Map<String, Object> details) {
            this.details = details;
            return this;
        }

        // 自定义方法
        public boolean isUp() {
            return "UP".equals(status) || "WARNING".equals(status);
        }

        // equals 方法
        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            HealthCheckResult that = (HealthCheckResult) o;
            return java.util.Objects.equals(status, that.status) &&
                    java.util.Objects.equals(details, that.details);
        }

        // hashCode 方法
        @Override
        public int hashCode() {
            return java.util.Objects.hash(status, details);
        }

        // toString 方法
        @Override
        public String toString() {
            return "HealthCheckResult{" +
                    "status='" + status + '\'' +
                    ", details=" + details +
                    '}';
        }

        public static class Builder {
            private String status;
            private Map<String, Object> details;

            public Builder status(String status) {
                this.status = status;
                return this;
            }

            public Builder details(Map<String, Object> details) {
                this.details = details;
                return this;
            }

            public HealthCheckResult build() {
                return new HealthCheckResult(status, details);
            }
        }
    }
}
