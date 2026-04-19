package com.mok.framework.common;


import com.mok.framework.common.constant.ResponseCode;

import java.io.Serializable;
import java.util.Collections;

/**
 * @description: 全局统一返回
 **/
public class R<T> implements Serializable {
    private Integer code;
    private String msg;
    private T data;
    private Long timestamp;

    public R() {
        this.timestamp = System.currentTimeMillis();
    }

    public R(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    // =================== 成功响应 ===================
    public static <T> R<T> ok() {
        return new R<T>()
                .setCode(ResponseCode.SUCCESS)
                .setMsg(ResponseCode.SUCCESS_MSG)
                .setData(null);
    }

    public static <T> R<T> ok(T data) {
        return new R<T>()
                .setCode(ResponseCode.SUCCESS)
                .setMsg(ResponseCode.SUCCESS_MSG)
                .setData(data);
    }

    public static <T> R<T> ok(String msg, T data) {
        return new R<T>()
                .setCode(ResponseCode.SUCCESS)
                .setMsg(msg)
                .setData(data);
    }

    @SuppressWarnings("unchecked")
    private static <T> T emptyData() {
        return (T) Collections.emptyMap();
    }

    // =================== 失败响应 ===================
    public static <T> R<T> error() {
        return new R<T>()
                .setCode(ResponseCode.INTERNAL_SERVER_ERROR)
                .setMsg(ResponseCode.INTERNAL_SERVER_ERROR_MSG)
                .setData(emptyData());
    }

    public static <T> R<T> error(String msg) {
        return new R<T>()
                .setCode(ResponseCode.INTERNAL_SERVER_ERROR)
                .setMsg(msg)
                .setData(emptyData());
    }

    public static <T> R<T> error(Integer code, String msg) {
        return new R<T>()
                .setCode(code)
                .setMsg(msg)
                .setData(emptyData());
    }

    public static <T> R<T> error(Integer code, String msg, T data) {
        if (data == null) {
            data = emptyData();
        }
        return new R<T>()
                .setCode(code)
                .setMsg(msg)
                .setData(data);
    }

    // =================== 快速构建常用错误 ===================
    public static <T> R<T> badRequest(String msg) {
        return new R<T>()
                .setCode(ResponseCode.BAD_REQUEST)
                .setMsg(msg)
                .setData(emptyData());
    }

    public static <T> R<T> unauthorized(String msg) {
        return new R<T>()
                .setCode(ResponseCode.UNAUTHORIZED)
                .setMsg(msg)
                .setData(emptyData());
    }

    public static <T> R<T> forbidden(String msg) {
        return new R<T>()
                .setCode(ResponseCode.FORBIDDEN)
                .setMsg(msg)
                .setData(emptyData());
    }

    public static <T> R<T> notFound(String msg) {
        return new R<T>()
                .setCode(ResponseCode.NOT_FOUND)
                .setMsg(msg)
                .setData(emptyData());
    }

    public static <T> R<T> methodNotAllowed(String msg) {
        return new R<T>()
                .setCode(ResponseCode.METHOD_NOT_ALLOWED)
                .setMsg(msg)
                .setData(emptyData());
    }

    // =================== 业务错误 ===================
    public static <T> R<T> businessError(String msg) {
        return new R<T>()
                .setCode(ResponseCode.BUSINESS_ERROR)
                .setMsg(msg)
                .setData(emptyData());
    }

    public static <T> R<T> validationError(String msg) {
        return new R<T>()
                .setCode(ResponseCode.VALIDATION_ERROR)
                .setMsg(msg)
                .setData(emptyData());
    }

    public static <T> R<T> userNotFound() {
        return new R<T>()
                .setCode(ResponseCode.USER_NOT_FOUND)
                .setMsg(ResponseCode.USER_NOT_FOUND_MSG)
                .setData(emptyData());
    }

    public static <T> R<T> passwordError() {
        return new R<T>()
                .setCode(ResponseCode.PASSWORD_ERROR)
                .setMsg(ResponseCode.PASSWORD_ERROR_MSG)
                .setData(emptyData());
    }

    public static <T> R<T> tokenExpired() {
        return new R<T>()
                .setCode(ResponseCode.TOKEN_EXPIRED)
                .setMsg(ResponseCode.TOKEN_EXPIRED_MSG)
                .setData(emptyData());
    }

    public static <T> R<T> tokenInvalid() {
        return new R<T>()
                .setCode(ResponseCode.TOKEN_INVALID)
                .setMsg(ResponseCode.TOKEN_INVALID_MSG)
                .setData(emptyData());
    }

    // Getter 方法
    public Integer getCode() {
        return code;
    }

    // Setter 方法
    public R<T> setCode(Integer code) {
        this.code = code;
        return this;
    }

    public String getMsg() {
        return msg;
    }

    public R<T> setMsg(String msg) {
        this.msg = msg;
        return this;
    }

    public T getData() {
        return data;
    }

    public R<T> setData(T data) {
        this.data = data;
        return this;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public R<T> setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    // =================== 判断方法 ===================
    public boolean isSuccess() {
        return ResponseCode.SUCCESS.equals(this.code);
    }

    public boolean isError() {
        return !isSuccess();
    }

    @Override
    public String toString() {
        return "R{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                ", timestamp=" + timestamp +
                '}';
    }
}