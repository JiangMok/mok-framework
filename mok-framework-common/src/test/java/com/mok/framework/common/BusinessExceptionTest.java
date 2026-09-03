package com.mok.framework.common;

import com.mok.framework.common.constant.ResponseCode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BusinessExceptionTest {

    @Test
    void defaultExceptionUsesBusinessErrorCode() {
        BusinessException exception = new BusinessException("业务失败");

        assertEquals(ResponseCode.BUSINESS_ERROR, exception.getCode());
    }
}

