package com.mok.framework.task.config;

/**
 * 定时任务时间常量配置
 * 单位：毫秒
 *
 * @author mok
 * @date 2026/4/12
 */
public final class TimeConfig {

    /*
    使用示例
    @Scheduled(fixedRate = TimeConfig.HEALTH_CHECK_INTERVAL)
    public void scheduledHealthCheck() {
        // 每5分钟执行一次
    }

    @Scheduled(initialDelay = TimeConfig.INITIAL_DELAY_30S, fixedDelay = TimeConfig.ONE_MINUTE)
    public void scheduledTaskWithDelay() {
        // 启动后30秒首次执行，之后每次执行完毕间隔1分钟再执行
    }

     */


    // 私有构造器，禁止实例化
    private TimeConfig() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    // ==================== 基础时间单位 ====================
    /**
     * 1秒
     */
    public static final long ONE_SECOND = 1000L;
    /**
     * 1分钟
     */
    public static final long ONE_MINUTE = 60 * ONE_SECOND;
    /**
     * 5分钟
     */
    public static final long FIVE_MINUTES = 5 * ONE_MINUTE;
    /**
     * 10分钟
     */
    public static final long TEN_MINUTES = 10 * ONE_MINUTE;
    /**
     * 30分钟
     */
    public static final long THIRTY_MINUTES = 30 * ONE_MINUTE;
    /**
     * 1小时
     */
    public static final long ONE_HOUR = 60 * ONE_MINUTE;
    /**
     * 2小时
     */
    public static final long TWO_HOURS = 2 * ONE_HOUR;
    /**
     * 6小时
     */
    public static final long SIX_HOURS = 6 * ONE_HOUR;
    /**
     * 12小时
     */
    public static final long TWELVE_HOURS = 12 * ONE_HOUR;
    /**
     * 1天
     */
    public static final long ONE_DAY = 24 * ONE_HOUR;

    // ==================== 常用定时间隔 ====================
    /**
     * 30秒（快速轮询）
     */
    public static final long THIRTY_SECONDS = 30 * ONE_SECOND;
    /**
     * 3分钟
     */
    public static final long THREE_MINUTES = 3 * ONE_MINUTE;
    /**
     * 15分钟
     */
    public static final long FIFTEEN_MINUTES = 15 * ONE_MINUTE;
    /**
     * 45分钟
     */
    public static final long FORTY_FIVE_MINUTES = 45 * ONE_MINUTE;
    /**
     * 2小时
     */
    public static final long TWO_HOUR = TWO_HOURS; // 别名
    /**
     * 4小时
     */
    public static final long FOUR_HOURS = 4 * ONE_HOUR;
    /**
     * 8小时
     */
    public static final long EIGHT_HOURS = 8 * ONE_HOUR;

    // ==================== 固定延迟/固定频率专用 ====================
    /**
     * 系统启动后延迟5秒执行（initialDelay）
     */
    public static final long INITIAL_DELAY_5S = 5 * ONE_SECOND;
    /**
     * 系统启动后延迟10秒执行
     */
    public static final long INITIAL_DELAY_10S = 10 * ONE_SECOND;
    /**
     * 系统启动后延迟30秒执行
     */
    public static final long INITIAL_DELAY_30S = 30 * ONE_SECOND;
    /**
     * 系统启动后延迟1分钟执行
     */
    public static final long INITIAL_DELAY_1M = ONE_MINUTE;

    // ==================== 便捷组合 ====================
    /**
     * 健康检查间隔（10分钟）
     */
    public static final long HEALTH_CHECK_INTERVAL = FIVE_MINUTES * 2;
    /**
     * 缓存刷新间隔（10分钟）
     */
    public static final long CACHE_REFRESH_INTERVAL = TEN_MINUTES;
    /**
     * 数据同步间隔（30分钟）
     */
    public static final long DATA_SYNC_INTERVAL = THIRTY_MINUTES;
    /**
     * 日报生成时间（1小时）
     */
    public static final long DAILY_REPORT_INTERVAL = ONE_HOUR;
    /**
     * 凌晨清理任务（1天）
     */
    public static final long DAILY_CLEAN_INTERVAL = ONE_DAY;

    // ==================== 辅助工具方法（可选） ====================

    /**
     * 格式化毫秒值为可读字符串
     * 例如：300000 -> "5 minutes"
     */
    public static String format(long millis) {
        if (millis < ONE_MINUTE) {
            return millis / ONE_SECOND + " seconds";
        }
        if (millis < ONE_HOUR) {
            return millis / ONE_MINUTE + " minutes";
        }
        if (millis < ONE_DAY) {
            return millis / ONE_HOUR + " hours";
        }
        return millis / ONE_DAY + " days";
    }
}