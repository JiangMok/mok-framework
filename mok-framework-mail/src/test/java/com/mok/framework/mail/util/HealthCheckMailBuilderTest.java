package com.mok.framework.mail.util;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class HealthCheckMailBuilderTest {

    @Test
    void includesInstanceIdInHealthAlertBody() {
        String html = new HealthCheckMailBuilder().buildHtmlMail(
                Map.of(
                        "application", "mok-framework",
                        "version", "1.0.0",
                        "instanceId", "mok-framework-node-a-8080"),
                "DOWN");

        assertThat(html).contains("<strong>实例：</strong>mok-framework-node-a-8080");
    }
}
