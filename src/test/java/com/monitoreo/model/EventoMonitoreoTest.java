package com.monitoreo.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class EventoMonitoreoTest {

    @Test
    void testNoArgsConstructor() {
        EventoMonitoreo evento = new EventoMonitoreo();
        assertNotNull(evento);
    }

    @Test
    void testAllArgsConstructor() {
        LocalDateTime now = LocalDateTime.now();
        EventoMonitoreo evento = new EventoMonitoreo(1L, "TEST_EVENT", "Test message", now, "test-service", "INFO", "{}", "user1", "session1");
        
        assertEquals(1L, evento.getId());
        assertEquals("TEST_EVENT", evento.getEventType());
        assertEquals("Test message", evento.getMessage());
        assertEquals(now, evento.getTimestamp());
        assertEquals("test-service", evento.getServiceName());
        assertEquals("INFO", evento.getLevel());
        assertEquals("{}", evento.getMetadata());
        assertEquals("user1", evento.getUserId());
        assertEquals("session1", evento.getSessionId());
    }

    @Test
    void testCustomConstructors() {
        EventoMonitoreo evento1 = new EventoMonitoreo("EVENT1", "Message 1");
        assertEquals("EVENT1", evento1.getEventType());
        assertEquals("Message 1", evento1.getMessage());

        EventoMonitoreo evento2 = new EventoMonitoreo("EVENT2", "Message 2", "WARN");
        assertEquals("EVENT2", evento2.getEventType());
        assertEquals("Message 2", evento2.getMessage());
        assertEquals("WARN", evento2.getLevel());
    }

    @Test
    void testGettersAndSetters() {
        EventoMonitoreo evento = new EventoMonitoreo();
        LocalDateTime now = LocalDateTime.now();

        evento.setId(2L);
        evento.setEventType("SETTER_EVENT");
        evento.setMessage("Setter message");
        evento.setTimestamp(now);
        evento.setServiceName("setter-service");
        evento.setLevel("ERROR");
        evento.setMetadata("{\"key\":\"value\"}");
        evento.setUserId("user2");
        evento.setSessionId("session2");

        assertEquals(2L, evento.getId());
        assertEquals("SETTER_EVENT", evento.getEventType());
        assertEquals("Setter message", evento.getMessage());
        assertEquals(now, evento.getTimestamp());
        assertEquals("setter-service", evento.getServiceName());
        assertEquals("ERROR", evento.getLevel());
        assertEquals("{\"key\":\"value\"}", evento.getMetadata());
        assertEquals("user2", evento.getUserId());
        assertEquals("session2", evento.getSessionId());
    }
    
    @Test
    void testAddMetadata() {
        EventoMonitoreo evento = new EventoMonitoreo();
        evento.addMetadata("key1", "value1");
        assertEquals("key1:value1;", evento.getMetadata());
        
        evento.addMetadata("key2", 123);
        assertEquals("key1:value1;key2:123;", evento.getMetadata());
    }
    
    @Test
    void testToString() {
        EventoMonitoreo evento = new EventoMonitoreo("TO_STRING", "Test message", "DEBUG");
        String aString = evento.toString();
        
        assertTrue(aString.contains("eventType='TO_STRING'"));
        assertTrue(aString.contains("message='Test message'"));
        assertTrue(aString.contains("level='DEBUG'"));
    }
} 