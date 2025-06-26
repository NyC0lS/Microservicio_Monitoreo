package com.monitoreo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.MapSerializer;
import com.fasterxml.jackson.databind.deser.std.MapDeserializer;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;

/**
 * Entidad JPA para representar eventos de monitoreo
 */
@Entity
@Table(name = "eventos_monitoreo")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventoMonitoreo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "El tipo de evento es obligatorio")
    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;
    
    @NotBlank(message = "El mensaje es obligatorio")
    @Column(name = "message", nullable = false, length = 1000)
    private String message;
    
    @CreationTimestamp
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;
    
    @Column(name = "service_name", length = 100)
    private String serviceName;
    
    @Column(name = "level", length = 20)
    private String level;
    
    @Column(name = "metadata", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<String, Object> metadata = new HashMap<>();
    
    @Column(name = "user_id", length = 100)
    private String userId;
    
    @Column(name = "session_id", length = 100)
    private String sessionId;

    // Constructores adicionales
    public EventoMonitoreo(String eventType, String message) {
        this();
        this.eventType = eventType;
        this.message = message;
    }

    public EventoMonitoreo(String eventType, String message, String level) {
        this(eventType, message);
        this.level = level;
    }

    // Métodos de utilidad para metadata
    public void addMetadata(String key, Object value) {
        if (this.metadata == null) {
            this.metadata = new HashMap<>();
        }
        this.metadata.put(key, value);
    }

    @Override
    public String toString() {
        return "EventoMonitoreo{" +
                "id=" + id +
                ", eventType='" + eventType + '\'' +
                ", message='" + message + '\'' +
                ", timestamp=" + timestamp +
                ", serviceName='" + serviceName + '\'' +
                ", level='" + level + '\'' +
                ", userId='" + userId + '\'' +
                ", sessionId='" + sessionId + '\'' +
                '}';
    }
} 