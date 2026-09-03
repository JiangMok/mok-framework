package com.mok.framework.monitor.service.impl;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HealthCheckServiceImplTest {

    @Test
    void diskStatusUsesHighestSeverity() {
        assertEquals("WARNING", HealthCheckServiceImpl.determineDiskStatus("UP", 91));
        assertEquals("DOWN", HealthCheckServiceImpl.determineDiskStatus("WARNING", 96));
        assertEquals("DOWN", HealthCheckServiceImpl.determineDiskStatus("DOWN", 92));
    }

    @Test
    void deadlockAlwaysMarksThreadHealthDown() {
        assertEquals("DOWN", HealthCheckServiceImpl.determineThreadStatus(10, 1));
        assertEquals("WARNING", HealthCheckServiceImpl.determineThreadStatus(501, 0));
        assertEquals("UP", HealthCheckServiceImpl.determineThreadStatus(10, 0));
    }
}

