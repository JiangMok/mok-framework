package com.mok.baseframe.common;

import com.mok.baseframe.common.BusinessException;

public class FileUploadException extends BusinessException {
    
    public FileUploadException(String message) {
        super(message);
    }
    
    public FileUploadException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public FileUploadException(Integer code, String message) {
        super(code, message);
    }
    
    public FileUploadException(Integer code, String message, Throwable cause) {
        super(code, message, cause);
    }
}