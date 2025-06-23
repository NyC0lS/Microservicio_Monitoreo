package com.monitoreo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.monitoreo.config.SensitiveDataFilter;
import com.monitoreo.model.EventoMonitoreo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataMaskingServiceTest {

    @Mock
    private SensitiveDataFilter sensitiveDataFilter;

    @Mock
    private MetricsService metricsService;

    @InjectMocks
    private DataMaskingService dataMaskingService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void maskEventoMonitoreo_withSensitiveData() throws JsonProcessingException {
        EventoMonitoreo evento = new EventoMonitoreo();
        evento.setUserId("12345");
        evento.setMessage("Credit card: 1234-5678-9012-3456");
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("account", "987654321");
        String metadataString = objectMapper.writeValueAsString(metadata);
        evento.setMetadata(metadataString);

        when(sensitiveDataFilter.maskUserId(anyString())).thenReturn("12***");
        when(sensitiveDataFilter.containsSensitiveData(evento.getMessage())).thenReturn(true);
        when(sensitiveDataFilter.maskSensitiveData(evento.getMessage())).thenReturn("Credit card: ****");
        when(sensitiveDataFilter.containsSensitiveData(evento.getMetadata())).thenReturn(true);
        when(sensitiveDataFilter.maskSensitiveData(evento.getMetadata())).thenReturn("masked_metadata");


        EventoMonitoreo maskedEvento = dataMaskingService.maskEventoMonitoreo(evento);

        assertEquals("12***", maskedEvento.getUserId());
        assertEquals("Credit card: ****", maskedEvento.getMessage());
        assertEquals("masked_metadata", maskedEvento.getMetadata());
        verify(metricsService, times(2)).incrementarDatosSensiblesDetectados();
        verify(metricsService, times(2)).incrementarDatosEnmascarados();
    }

    @Test
    void maskEventoMonitoreo_withoutSensitiveData() {
        EventoMonitoreo evento = new EventoMonitoreo();
        evento.setUserId("user-normal");
        evento.setMessage("Normal message");

        when(sensitiveDataFilter.maskUserId(anyString())).thenReturn("user-normal");
        when(sensitiveDataFilter.containsSensitiveData(anyString())).thenReturn(false);

        EventoMonitoreo maskedEvento = dataMaskingService.maskEventoMonitoreo(evento);

        assertEquals("user-normal", maskedEvento.getUserId());
        assertEquals("Normal message", maskedEvento.getMessage());
        assertNull(maskedEvento.getMetadata());
        verify(metricsService, never()).incrementarDatosSensiblesDetectados();
        verify(metricsService, never()).incrementarDatosEnmascarados();
    }

    @Test
    void maskAmount() {
        when(sensitiveDataFilter.maskAmount("123.45")).thenReturn("1**-***");
        String masked = dataMaskingService.maskAmount(new BigDecimal("123.45"));
        assertEquals("1**-***", masked);
    }

    @Test
    void maskLogMessage_withSensitiveData() {
        String message = "Sensitive info here";
        when(sensitiveDataFilter.containsSensitiveData(message)).thenReturn(true);
        when(sensitiveDataFilter.maskSensitiveData(message)).thenReturn("*****");

        String maskedMessage = dataMaskingService.maskLogMessage(message);

        assertEquals("*****", maskedMessage);
        verify(metricsService, times(1)).incrementarDatosSensiblesDetectados();
        verify(metricsService, times(1)).incrementarDatosEnmascarados();
    }

    @Test
    void maskMetadata() {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("sensitive_key", "sensitive_value");
        metadata.put("normal_key", "normal_value");

        when(sensitiveDataFilter.containsSensitiveData("sensitive_value")).thenReturn(true);
        when(sensitiveDataFilter.maskSensitiveData("sensitive_value")).thenReturn("*****");
        when(sensitiveDataFilter.containsSensitiveData("normal_value")).thenReturn(false);

        Map<String, String> maskedMetadata = dataMaskingService.maskMetadata(metadata);

        assertEquals("*****", maskedMetadata.get("sensitive_key"));
        assertEquals("normal_value", maskedMetadata.get("normal_key"));
    }
} 