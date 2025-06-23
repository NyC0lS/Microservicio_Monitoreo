package com.monitoreo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class LoggingTestControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new LoggingTestController()).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testBasicLogging() throws Exception {
        mockMvc.perform(get("/api/test/logging/basic"))
                .andExpect(status().isOk());
    }

    @Test
    void testMDCLogging() throws Exception {
        mockMvc.perform(get("/api/test/logging/mdc"))
                .andExpect(status().isOk());
    }

    @Test
    void testLogLevels() throws Exception {
        mockMvc.perform(get("/api/test/logging/levels"))
                .andExpect(status().isOk());
    }

    @Test
    void testExceptionLogging() throws Exception {
        mockMvc.perform(get("/api/test/logging/exception"))
                .andExpect(status().isOk());
    }

    @Test
    void testStructuredLogging() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("key", "value");

        mockMvc.perform(post("/api/test/logging/structured")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());
    }

    @Test
    void testSecurityLogging() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("username", "testuser");
        body.put("password", "password123");

        mockMvc.perform(post("/api/test/logging/security")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());
    }
} 