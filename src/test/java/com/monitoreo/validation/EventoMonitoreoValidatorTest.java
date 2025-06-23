package com.monitoreo.validation;

import com.monitoreo.dto.EventoMonitoreoRequest;
import com.monitoreo.exception.InvalidEventoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EventoMonitoreoValidatorTest {

    private EventoMonitoreoValidator validator;

    @BeforeEach
    void setUp() {
        validator = new EventoMonitoreoValidator();
    }

    private EventoMonitoreoRequest createValidRequest() {
        EventoMonitoreoRequest request = new EventoMonitoreoRequest();
        request.setEventType("VALID_EVENT");
        request.setMessage("This is a valid message.");
        request.setTimestamp(LocalDateTime.now().minusDays(1));
        request.setLevel("INFO");
        request.setServiceName("valid-service");
        request.setMetadata(Collections.singletonMap("key", "value"));
        return request;
    }

    @Test
    void validateAndThrow_withValidRequest_shouldNotThrowException() {
        EventoMonitoreoRequest request = createValidRequest();
        assertDoesNotThrow(() -> validator.validateAndThrow(request));
    }

    @Test
    void validateAndThrow_withInvalidEventType_shouldThrowException() {
        EventoMonitoreoRequest request = createValidRequest();
        request.setEventType("<script>");
        assertThrows(InvalidEventoException.class, () -> validator.validateAndThrow(request));
    }

    @Test
    void validateAndThrow_withEmptyMessage_shouldThrowException() {
        EventoMonitoreoRequest request = createValidRequest();
        request.setMessage("   "); // Empty after trim
        assertThrows(InvalidEventoException.class, () -> validator.validateAndThrow(request));
    }

    @Test
    void validateAndThrow_withFutureTimestamp_shouldThrowException() {
        EventoMonitoreoRequest request = createValidRequest();
        request.setTimestamp(LocalDateTime.now().plusDays(1));
        assertThrows(InvalidEventoException.class, () -> validator.validateAndThrow(request));
    }
    
    @Test
    void validateAndThrow_withOldTimestamp_shouldThrowException() {
        EventoMonitoreoRequest request = createValidRequest();
        request.setTimestamp(LocalDateTime.now().minusDays(31));
        assertThrows(InvalidEventoException.class, () -> validator.validateAndThrow(request));
    }

    @Test
    void validateAndThrow_withInvalidLevel_shouldThrowException() {
        EventoMonitoreoRequest request = createValidRequest();
        request.setLevel("INVALID");
        assertThrows(InvalidEventoException.class, () -> validator.validateAndThrow(request));
    }
    
    @Test
    void validateAndThrow_withTooManyMetadataKeys_shouldThrowException() {
        EventoMonitoreoRequest request = createValidRequest();
        Map<String, Object> metadata = new HashMap<>();
        for (int i = 0; i < 101; i++) {
            metadata.put("key" + i, "value");
        }
        request.setMetadata(metadata);
        assertThrows(InvalidEventoException.class, () -> validator.validateAndThrow(request));
    }

    @Test
    void validateAndThrow_withInvalidServiceName_shouldThrowException() {
        EventoMonitoreoRequest request = createValidRequest();
        request.setServiceName("invalid service name");
        assertThrows(InvalidEventoException.class, () -> validator.validateAndThrow(request));
    }
} 