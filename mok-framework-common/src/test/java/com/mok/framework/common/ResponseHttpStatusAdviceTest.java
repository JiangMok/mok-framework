package com.mok.framework.common;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpResponse;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ResponseHttpStatusAdviceTest {

    @Test
    void standardErrorCodeUpdatesRealHttpStatus() {
        ServerHttpResponse response = mock(ServerHttpResponse.class);
        ResponseHttpStatusAdvice advice = new ResponseHttpStatusAdvice();

        advice.beforeBodyWrite(
                R.forbidden("无权访问"), null, null, null, null, response);

        verify(response).setStatusCode(HttpStatus.FORBIDDEN);
    }

    @Test
    void businessErrorKeepsHttpOk() {
        ServerHttpResponse response = mock(ServerHttpResponse.class);
        ResponseHttpStatusAdvice advice = new ResponseHttpStatusAdvice();

        advice.beforeBodyWrite(
                R.businessError("业务失败"), null, null, null, null, response);

        verify(response).setStatusCode(HttpStatus.OK);
    }
}

