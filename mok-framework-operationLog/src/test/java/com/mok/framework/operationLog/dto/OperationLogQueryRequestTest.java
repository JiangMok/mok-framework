package com.mok.framework.operationLog.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class OperationLogQueryRequestTest {

    @Test
    void shouldBuildTypedConditionsAndIgnoreBlankValues() {
        OperationLogQueryRequest request = new OperationLogQueryRequest();
        LocalDateTime startTime = LocalDateTime.of(2026, 8, 1, 0, 0);
        request.setStatus(2);
        request.setBusinessType(" ");
        request.setStartTime(startTime);

        assertThat(request.toConditions())
                .containsEntry("status", 2)
                .containsEntry("startTime", startTime)
                .doesNotContainKey("businessType");
    }
}

