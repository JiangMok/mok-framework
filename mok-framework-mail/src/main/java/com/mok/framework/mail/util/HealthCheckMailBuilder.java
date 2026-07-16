package com.mok.framework.mail.util;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map;

/**
 * 健康检查邮件 HTML 构建器
 * 兼容 HealthCheckResult POJO 和 Map 两种数据格式（通过反射提取 status / details）
 *
 * @author: mok
 * @date: 2026/7/16
 */
@Component
public class HealthCheckMailBuilder {

    private static final String[] COMP_ORDER = {
            "database", "redis", "memory", "rabbitmq", "cpu", "threads", "gc", "disk", "connectionPool"
    };
    private static final String[] COMP_NAMES = {
            "数据库", "Redis 缓存", "内存使用", "RabbitMQ", "CPU", "线程", "垃圾回收", "磁盘", "连接池"
    };

    /**
     * 构建 HTML 格式的健康检查邮件
     *
     * @param health        健康检查结果 Map
     * @param overallStatus 整体状态: UP / DOWN / WARNING
     * @return HTML 邮件正文
     */
    public String buildHtmlMail(Map<String, Object> health, String overallStatus) {
        String timestamp = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now());
        String appName = String.valueOf(health.getOrDefault("application", "MOK-Framework"));
        String version = String.valueOf(health.getOrDefault("version", "--"));

        // Banner 三色：UP=绿, WARNING=黄, DOWN=红
        String bannerBg, bannerIcon, bannerTitle;
        if ("DOWN".equals(overallStatus)) {
            bannerBg = "#dc2626";
            bannerIcon = "&#10060;";
            bannerTitle = "系统健康检查失败";
        } else if ("WARNING".equals(overallStatus)) {
            bannerBg = "#d97706";
            bannerIcon = "&#9888;&#65039;";
            bannerTitle = "系统健康检查异常";
        } else {
            bannerBg = "#16a34a";
            bannerIcon = "&#9989;";
            bannerTitle = "系统健康检查正常";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><meta charset=\"UTF-8\"></head><body style=\"margin:0;padding:0;background:#f0f2f5;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',sans-serif;\">");
        sb.append("<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\"><tr><td align=\"center\" style=\"padding:24px;\">");
        sb.append("<table width=\"620\" cellpadding=\"0\" cellspacing=\"0\" style=\"background:#fff;border-radius:8px;overflow:hidden;box-shadow:0 2px 12px rgba(0,0,0,0.08);\">");

        // ==== Banner ====
        sb.append("<tr><td style=\"background:").append(bannerBg).append(";padding:24px 28px;text-align:center;\">");
        sb.append("<p style=\"margin:0;font-size:36px;\">").append(bannerIcon).append("</p>");
        sb.append("<h2 style=\"margin:8px 0 0;color:#fff;font-size:20px;font-weight:700;\">").append(bannerTitle).append("</h2>");
        sb.append("<p style=\"margin:6px 0 0;color:rgba(255,255,255,0.8);font-size:13px;\">").append(appName).append(" v").append(version).append("</p>");
        sb.append("</td></tr>");

        // ==== 概览 ====
        sb.append("<tr><td style=\"padding:20px 28px 8px;\">");
        sb.append("<p style=\"margin:0;font-size:14px;color:#333;\"><strong>检测时间：</strong>").append(timestamp).append("</p>");
        sb.append("<p style=\"margin:4px 0 0;font-size:14px;color:#333;\"><strong>整体状态：</strong><span style=\"color:").append(bannerBg).append(";font-weight:700;\">").append(overallStatus).append("</span></p>");
        sb.append("</td></tr>");

        // ==== 组件表格 ====
        sb.append("<tr><td style=\"padding:16px 28px 24px;\">");
        sb.append("<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"border-collapse:collapse;font-size:13px;\">");
        sb.append("<thead><tr style=\"background:#f5f7fa;\">");
        sb.append("<th style=\"padding:10px 14px;text-align:left;border-bottom:2px solid #e8e8e8;color:#666;font-weight:600;\">组件</th>");
        sb.append("<th style=\"padding:10px 14px;text-align:center;border-bottom:2px solid #e8e8e8;color:#666;font-weight:600;\">状态</th>");
        sb.append("<th style=\"padding:10px 14px;text-align:left;border-bottom:2px solid #e8e8e8;color:#666;font-weight:600;\">详情</th>");
        sb.append("</tr></thead><tbody>");

        for (int i = 0; i < COMP_ORDER.length; i++) {
            Object obj = health.get(COMP_ORDER[i]);
            if (obj == null) continue;

            String compStatus = extractStatus(obj);
            Map<String, Object> details = extractDetails(obj);
            String dotColor = "UP".equals(compStatus) ? "#16a34a" : "WARNING".equals(compStatus) ? "#d97706" : "#dc2626";
            String rowBg = (i % 2 == 0) ? "#fafafa" : "#fff";

            sb.append("<tr style=\"background:").append(rowBg).append(";\">");
            sb.append("<td style=\"padding:10px 14px;font-weight:500;color:#333;\">").append(COMP_NAMES[i]).append("</td>");
            sb.append("<td style=\"padding:10px 14px;text-align:center;\"><span style=\"display:inline-block;width:8px;height:8px;border-radius:50%;background:").append(dotColor).append(";margin-right:6px;\"></span><span style=\"color:").append(dotColor).append(";font-weight:600;\">").append(compStatus).append("</span></td>");
            sb.append("<td style=\"padding:10px 14px;color:#555;font-size:12px;\">").append(formatCompDetail(details)).append("</td>");
            sb.append("</tr>");
        }

        sb.append("</tbody></table></td></tr>");

        // ==== 页脚 ====
        sb.append("<tr><td style=\"padding:16px 28px;background:#f5f7fa;text-align:center;font-size:11px;color:#999;border-top:1px solid #e8e8e8;\">");
        sb.append("<p style=\"margin:0;\">此邮件由 MOK-Framework 自动发送，请勿回复</p>");
        sb.append("<p style=\"margin:4px 0 0;\">").append(timestamp).append("</p>");
        sb.append("</td></tr>");

        sb.append("</table></td></tr></table></body></html>");
        return sb.toString();
    }

    // ==================== 反射工具：兼容 Map 和 HealthCheckResult POJO ====================

    @SuppressWarnings("unchecked")
    private static String extractStatus(Object obj) {
        if (obj instanceof Map) {
            Object s = ((Map<String, Object>) obj).getOrDefault("status", "UNKNOWN");
            return String.valueOf(s);
        }
        try {
            Object status = obj.getClass().getMethod("getStatus").invoke(obj);
            return status != null ? String.valueOf(status) : "UNKNOWN";
        } catch (Exception e) {
            return "UNKNOWN";
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> extractDetails(Object obj) {
        if (obj instanceof Map) {
            Object d = ((Map<String, Object>) obj).get("details");
            return d instanceof Map ? (Map<String, Object>) d : Collections.emptyMap();
        }
        try {
            Object d = obj.getClass().getMethod("getDetails").invoke(obj);
            return d instanceof Map ? (Map<String, Object>) d : Collections.emptyMap();
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

    // ==================== 详情摘要 ====================

    private String formatCompDetail(Map<String, Object> details) {
        if (details == null || details.isEmpty()) return "--";

        if (details.containsKey("responseTime")) return "响应 " + details.get("responseTime");
        if (details.containsKey("usedPercentage")) return "使用率 " + details.get("usedPercentage");
        if (details.containsKey("loadAverage")) return "负载 " + details.get("loadAverage");
        if (details.containsKey("totalCollections")) return "回收 " + details.get("totalCollections") + " 次";

        // 连接池
        if (details.containsKey("active") && details.containsKey("idle"))
            return "活跃 " + details.get("active") + " / 空闲 " + details.get("idle");

        // 磁盘 — 显示第一个分区
        for (Object v : details.values()) {
            if (v instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> diskInfo = (Map<String, Object>) v;
                return "使用 " + diskInfo.getOrDefault("usedPercent", "--");
            }
        }
        return "--";
    }
}
