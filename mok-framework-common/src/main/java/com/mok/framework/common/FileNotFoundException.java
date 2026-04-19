package com.mok.baseframe.common;

import com.mok.baseframe.common.constant.ResponseCode;

public class FileNotFoundException extends BusinessException {
    
    public FileNotFoundException(String fileId) {
        super(ResponseCode.NOT_FOUND, String.format("文件[%s]不存在或已被删除", fileId));
    }
    
    public FileNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}