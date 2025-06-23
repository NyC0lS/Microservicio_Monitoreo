package com.monitoreo.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO para crear eventos de monitoreo con validaciones
 */
public class EventoMonitoreoRequest {

    @NotBlank(message = "El tipo de evento es obligatorio")
    @Size(max = 100, message = "El tipo de evento no puede exceder 100 caracteres")
    private String eventType;

    @NotBlank(message = "El mensaje es obligatorio")
    @Size(max = 1000, message = "El mensaje no puede exceder 1000 caracteres")
    private String message;

    @NotNull(message = "El timestamp es obligatorio")
    @PastOrPresent(message = "El timestamp no puede ser futuro")
    private LocalDateTime timestamp;

    @Size(max = 100, message = "El nombre del servicio no puede exceder 100 caracteres")
    private String serviceName;

    @Pattern(regexp = "^(INFO|WARN|ERROR|DEBUG|TRACE)$", message = "El nivel debe ser INFO, WARN, ERROR, DEBUG o TRACE")
    private String level = "INFO";

    @Size(max = 100, message = "El ID de usuario no puede exceder 100 caracteres")
    private String userId;

    @Size(max = 100, message = "El ID de sesión no puede exceder 100 caracteres")
    private String sessionId;

    @Size(max = 50000, message = "Los metadatos no pueden exceder 50KB")
    private Map<String, Object> metadata;

    // Constructores
    public EventoMonitoreoRequest() {}

    public EventoMonitoreoRequest(String eventType, String message) {
        this.eventType = eventType;
        this.message = message;
        this.timestamp = LocalDateTime.now();
        this.level = "INFO";
    }

    // Getters y Setters
    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    @Override
    public String toString() {
        return "EventoMonitoreoRequest{" +
                "eventType='" + eventType + '\'' +
                ", message='" + message + '\'' +
                ", timestamp=" + timestamp +
                ", serviceName='" + serviceName + '\'' +
                ", level='" + level + '\'' +
                ", userId='" + userId + '\'' +
                ", sessionId='" + sessionId + '\'' +
                ", metadata=" + metadata +
                '}';
    }
} 