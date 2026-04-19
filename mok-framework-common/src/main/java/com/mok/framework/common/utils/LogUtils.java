package com.mok.framework.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 日志工具类
 * <p>
 * 提供快速获取 SLF4J Logger 实例的方法，以及常用的日志级别判断方法。
 * 使用此类可以简化日志对象的创建和级别检查代码。
 * </p>
 */
public class LogUtils {
    private static final Logger log = LogUtils.getLogger(LogUtils.class);

    /**
     * 快速获取指定类的 Logger 实例
     *
     * @param clazz 需要记录日志的类
     * @return 对应类的 Logger 对象
     */
    public static Logger getLogger(Class<?> clazz) {
        return LoggerFactory.getLogger(clazz);
    }

    /**
     * 判断指定类的 TRACE 级别日志是否启用
     *
     * @param clazz 需要检查的类
     * @return true 表示 TRACE 级别已启用，否则 false
     */
    public static boolean isTraceEnabled(Class<?> clazz) {
        return getLogger(clazz).isTraceEnabled();
    }

    /**
     * 判断指定类的 DEBUG 级别日志是否启用
     *
     * @param clazz 需要检查的类
     * @return true 表示 DEBUG 级别已启用，否则 false
     */
    public static boolean isDebugEnabled(Class<?> clazz) {
        return getLogger(clazz).isDebugEnabled();
    }

    /**
     * 判断指定类的 INFO 级别日志是否启用
     *
     * @param clazz 需要检查的类
     * @return true 表示 INFO 级别已启用，否则 false
     */
    public static boolean isInfoEnabled(Class<?> clazz) {
        return getLogger(clazz).isInfoEnabled();
    }

    /**
     * 判断指定类的 WARN 级别日志是否启用
     *
     * @param clazz 需要检查的类
     * @return true 表示 WARN 级别已启用，否则 false
     */
    public static boolean isWarnEnabled(Class<?> clazz) {
        return getLogger(clazz).isWarnEnabled();
    }

    /**
     * 判断指定类的 ERROR 级别日志是否启用
     *
     * @param clazz 需要检查的类
     * @return true 表示 ERROR 级别已启用，否则 false
     */
    public static boolean isErrorEnabled(Class<?> clazz) {
        return getLogger(clazz).isErrorEnabled();
    }
}