package com.mok.framework.common.utils;

import com.alibaba.fastjson2.JSON;
import com.mok.framework.common.R;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 响应工具类
 * <p>
 * 用于在非 Spring MVC 管理范围（如过滤器、拦截器）中直接返回统一的 JSON 响应结果。
 * 提供将 {@link R} 对象写入 {@link HttpServletResponse} 的便捷方法，
 * 并根据业务码自动映射 HTTP 状态码。
 * </p>
 *
 * @author JN
 * @date 2025/12/31 23:45
 */
public class ResponseUtils {

    private static final Logger log = LogUtils.getLogger(ResponseUtils.class);

    /**
     * 将统一响应对象写入 HttpServletResponse 并输出 JSON
     * <p>
     * 设置响应编码为 UTF-8，内容类型为 application/json，
     * 根据业务码映射 HTTP 状态码，最后将 R 对象序列化为 JSON 字符串写入响应流。
     * </p>
     *
     * @param response HttpServletResponse 对象，用于返回响应
     * @param result   统一响应对象 {@link R}，包含业务码、消息和数据
     */
    public static void writeJson(HttpServletResponse response, R<?> result) {
        // 设置响应的字符编码为UTF-8，确保中文等非ASCII字符正确显示
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        // 设置响应的内容类型为 application/json，告知客户端返回 JSON 格式数据
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        // 根据业务码设置 HTTP 响应状态码
        response.setStatus(getHttpStatus(result.getCode()));

        try {
            // 将 R 对象序列化为 JSON 字符串并写入响应输出流
            response.getWriter().write(JSON.toJSONString(result));
        } catch (IOException e) {
            // 网络连接问题、客户端断开连接等 IO 异常，记录日志但不重新抛出
            log.error("写入响应失败", e);
        }
    }

    /**
     * 根据业务码获取对应的 HTTP 状态码
     * <p>
     * 映射规则：
     * <ul>
     *     <li>业务码 400/401/403/404/405/500/503 → 对应 HTTP 状态码</li>
     *     <li>业务码 ≥ 1000（业务错误码）→ 返回 200，错误信息放在响应体中</li>
     *     <li>其他情况 → 返回 500</li>
     * </ul>
     * </p>
     *
     * @param code 业务码，可能为 null
     * @return 对应的 HTTP 状态码，默认为 500
     */
    private static int getHttpStatus(Integer code) {
        if (code == null) {
            return 500;
        }

        switch (code) {
            case 400:
                return 400;  // 客户端请求语法错误
            case 401:
                return 401;  // 需要身份验证
            case 403:
                return 403;  // 服务器理解请求但拒绝执行
            case 404:
                return 404;  // 请求资源不存在
            case 405:
                return 405;  // 请求方法不被允许
            case 500:
                return 500;  // 服务器内部错误
            case 503:
                return 503;  // 服务暂时不可用
            default:
                // 业务错误码（>=1000）统一返回 200，错误信息在响应体中携带
                if (code >= 1000) {
                    return 200;
                }
                return 500;
        }
    }

    /**
     * 快速返回成功响应，将数据写入 HttpServletResponse
     *
     * @param response HttpServletResponse 对象
     * @param data     成功响应的数据（可为 null）
     */
    public static void writeSuccess(HttpServletResponse response, Object data) {
        writeJson(response, R.ok(data));
    }

    /**
     * 快速返回错误响应，写入 HttpServletResponse
     *
     * @param response HttpServletResponse 对象
     * @param code     业务错误码
     * @param msg      错误消息
     */
    public static void writeError(HttpServletResponse response, Integer code, String msg) {
        writeJson(response, R.error(code, msg));
    }

    /**
     * 快速返回 400 错误响应（客户端请求错误）
     *
     * @param response HttpServletResponse 对象
     * @param msg      错误消息
     */
    public static void writeBadRequest(HttpServletResponse response, String msg) {
        writeJson(response, R.badRequest(msg));
    }

    /**
     * 快速返回 401 错误响应（未认证）
     *
     * @param response HttpServletResponse 对象
     * @param msg      错误消息
     */
    public static void writeUnauthorized(HttpServletResponse response, String msg) {
        writeJson(response, R.unauthorized(msg));
    }

    /**
     * 快速返回 403 错误响应（无权限）
     *
     * @param response HttpServletResponse 对象
     * @param msg      错误消息
     */
    public static void writeForbidden(HttpServletResponse response, String msg) {
        writeJson(response, R.forbidden(msg));
    }
}