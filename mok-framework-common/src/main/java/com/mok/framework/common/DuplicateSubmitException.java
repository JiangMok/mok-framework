package com.mok.framework.common;

/**
 * 放重复提交注解的异常
 */
public class DuplicateSubmitException extends BusinessException {
    public DuplicateSubmitException(String message) {
        super(message);
    }
}
