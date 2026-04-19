package com.mok.framework.monitor.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.cluster.HealthResponse;
import com.mok.framework.common.utils.LogUtils;
import com.mok.framework.monitor.service.HealthCheckService;
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
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
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
    private final ElasticsearchClient elasticsearchClient;
    private final RabbitTemplate rabbitTemplate;

    public HealthCheckServiceImpl(DataSource dataSource,
                                  JdbcTemplate jdbcTemplate,
                                  RedisConnectionFactory redisConnectionFactory,
                                  ElasticsearchClient elasticsearchClient,
                                  RabbitTemplate rabbitTemplate
    ) {
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
        this.redisConnectionFactory = redisConnectionFactory;
        this.elasticsearchClient = elasticsearchClient;
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
        HealthCheckResult elasticsearchResult = checkElasticsearch();
        healthInfo.put("elasticsearch", elasticsearchResult);

        // 检查rabbitmq
        HealthCheckResult rabbitmqResult = checkRabbitMQ();
        healthInfo.put("rabbitmq", rabbitmqResult);

        // 计算总体状态
        boolean allHealthy = dbResult.isUp()
                && redisResult.isUp()
                && memoryResult.isUp()
                && elasticsearchResult.isUp()
                && rabbitmqResult.isUp();
        healthInfo.put("status", allHealthy ? "UP" : "DOWN");
        healthInfo.put("application", "MOK-BaseFrame");
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
    private HealthCheckResult checkElasticsearch() {
        long startTime = System.currentTimeMillis();
        try {
            HealthResponse health = elasticsearchClient.cluster().health();
            long responseTime = System.currentTimeMillis() - startTime;

            Map<String, Object> details = new HashMap<>();
            details.put("clusterName", health.clusterName());
            details.put("status", health.status().jsonValue()); // green/yellow/red
            details.put("responseTime", responseTime + "ms");
            details.put("nodeCount", health.numberOfNodes());
            details.put("dataNodeCount", health.numberOfDataNodes());

            String healthStatus;
            if ("green".equals(health.status().jsonValue())) {
                healthStatus = "UP";
            } else if ("yellow".equals(health.status().jsonValue())) {
                healthStatus = "WARNING";
            } else {
                healthStatus = "DOWN";
            }

            return HealthCheckResult.builder()
                    .status(healthStatus)
                    .details(details)
                    .build();

        } catch (Exception e) {
            log.error("Elasticsearch 健康检查失败", e);
            return HealthCheckResult.builder()
                    .status("DOWN")
                    .details(Map.of("error", e.getMessage()))
                    .build();
        }
    }

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
