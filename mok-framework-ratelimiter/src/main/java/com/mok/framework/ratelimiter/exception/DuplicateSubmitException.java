package com.mok.framework.ratelimiter.exception;

/**
 * 重复提交异常
 * @author mok
 */
public class DuplicateSubmitException extends RuntimeException {
    
    /**
     * 错误码
     */
    private final String code;

    /**
     * 构造方法，只传入消息，使用默认错误码
     * @param message 异常信息
     */
    public DuplicateSubmitException(String message) {
        super(message);
        this.code = "DUPLICATE_SUBMIT";
    }

    /**
     * 构造方法，传入自定义错误码和消息
     * @param code 错误码
     * @param message 异常信息
     */
    public DuplicateSubmitException(String code, String message) {
        super(message);
        this.code = code;
    }
    
    public String getCode() {
        return code;
    }
}