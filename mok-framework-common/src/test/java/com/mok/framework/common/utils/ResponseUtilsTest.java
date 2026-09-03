package com.mok.framework.common.utils;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ResponseUtilsTest {

    @Test
    void writeSuccessUsesHttpOk() {
        MockHttpServletResponse response = new MockHttpServletResponse();

        ResponseUtils.writeSuccess(response, "ok");

        assertEquals(200, response.getStatus());
    }

    @Test
    void writeErrorUsesMatchingHttpStatus() {
        MockHttpServletResponse response = new MockHttpServletResponse();

        ResponseUtils.writeError(response, 403, "forbidden");

        assertEquals(403, response.getStatus());
    }
}

