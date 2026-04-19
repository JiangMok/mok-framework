package com.mok.framework.common;

import java.util.Objects;

/**
 * @description: 业务异常类
 * @author: JN
 * @date: 2025/12/31
 */
public class BusinessException extends RuntimeException {

    private Integer code;

    // 构造方法
    public BusinessException() {
        super();
    }

    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.code = 500;
    }

    public BusinessException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    // Builder 模式
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Integer code = 500;
        private String message;
        private Throwable cause;

        public Builder code(Integer code) {
            this.code = code;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder cause(Throwable cause) {
            this.cause = cause;
            return this;
        }

        public BusinessException build() {
            if (cause != null) {
                return new BusinessException(code, message, cause);
            }
            return new BusinessException(code, message);
        }
    }

    // Getter 和 Setter 方法
    public Integer getCode() {
        return code;
    }

    public BusinessException setCode(Integer code) {
        this.code = code;
        return this;
    }

    // 重写 toString 方法
    @Override
    public String toString() {
        return "BusinessException{" +
                "code=" + code +
                ", message='" + getMessage() + '\'' +
                '}';
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
        if (!super.equals(o)) {
            return false;
        }
        BusinessException that = (BusinessException) o;
        return Objects.equals(code, that.code);
    }

    // hashCode 方法
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), code);
    }
}