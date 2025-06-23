package com.monitoreo.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MonitoreoServiceTest {

    private MonitoreoService monitoreoService;

    @BeforeEach
    void setUp() {
        monitoreoService = new MonitoreoService();
    }

    @Test
    void checkHealth() {
        Map<String, Object> health = monitoreoService.checkHealth();
        assertNotNull(health);
        assertEquals("UP", health.get("status"));
        assertTrue(health.containsKey("timestamp"));
    }

    @Test
    void getSystemInfo() {
        Map<String, Object> systemInfo = monitoreoService.getSystemInfo();
        assertNotNull(systemInfo);
        assertTrue(systemInfo.containsKey("javaVersion"));
        assertTrue(systemInfo.containsKey("osName"));
        assertTrue(systemInfo.containsKey("osVersion"));
        assertTrue(systemInfo.containsKey("userName"));
        assertTrue(systemInfo.containsKey("timestamp"));
    }

    @Test
    void logEvent() {
        assertDoesNotThrow(() -> monitoreoService.logEvent("TEST_EVENT", "This is a test event", null));
    }

    @Test
    void health() {
        Health health = monitoreoService.health();
        assertEquals(Status.UP, health.getStatus());
        assertNotNull(health.getDetails());
        assertTrue(health.getDetails().containsKey("status"));
        assertEquals("UP", health.getDetails().get("status"));
    }
} 