package com.monitoreo.service;

import com.monitoreo.config.SensitiveDataFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Servicio de auditoría para registrar eventos relacionados con datos sensibles.
 * Proporciona trazabilidad completa de las operaciones de enmascaramiento.
 */
@Service
public class AuditService {
    
    private static final Logger auditLogger = LoggerFactory.getLogger("com.monitoreo.audit");
    private static final Logger securityLogger = LoggerFactory.getLogger("com.monitoreo.security");
    
    @Autowired
    private SensitiveDataFilter sensitiveDataFilter;
    
    /**
     * Registra la detección de datos sensibles
     */
    public void logSensitiveDataDetected(String dataType, String source, String context) {
        Map<String, Object> auditEvent = new HashMap<>();
        auditEvent.put("event_type", "SENSITIVE_DATA_DETECTED");
        auditEvent.put("timestamp", LocalDateTime.now());
        auditEvent.put("data_type", dataType);
        auditEvent.put("source", source);
        auditEvent.put("context", context);
        auditEvent.put("severity", "MEDIUM");
        
        auditLogger.info("Datos sensibles detectados: {}", auditEvent);
        securityLogger.warn("Datos sensibles detectados en {}: {}", source, dataType);
    }
    
    /**
     * Registra el enmascaramiento de datos sensibles
     */
    public void logDataMasked(String dataType, String originalValue, String maskedValue, String source) {
        Map<String, Object> auditEvent = new HashMap<>();
        auditEvent.put("event_type", "DATA_MASKED");
        auditEvent.put("timestamp", LocalDateTime.now());
        auditEvent.put("data_type", dataType);
        auditEvent.put("source", source);
        auditEvent.put("original_length", originalValue != null ? originalValue.length() : 0);
        auditEvent.put("masked_length", maskedValue != null ? maskedValue.length() : 0);
        auditEvent.put("masking_applied", true);
        
        auditLogger.info("Datos enmascarados: {}", auditEvent);
        securityLogger.debug("Datos enmascarados en {}: {} -> {}", source, dataType, maskedValue);
    }
    
    /**
     * Registra el acceso a datos sensibles
     */
    public void logSensitiveDataAccess(String userId, String dataType, String operation, String resource) {
        Map<String, Object> auditEvent = new HashMap<>();
        auditEvent.put("event_type", "SENSITIVE_DATA_ACCESS");
        auditEvent.put("timestamp", LocalDateTime.now());
        auditEvent.put("user_id", sensitiveDataFilter.maskUserId(userId));
        auditEvent.put("data_type", dataType);
        auditEvent.put("operation", operation);
        auditEvent.put("resource", resource);
        auditEvent.put("access_granted", true);
        
        auditLogger.info("Acceso a datos sensibles: {}", auditEvent);
        securityLogger.info("Usuario {} accedió a datos sensibles: {} en {}", 
                           sensitiveDataFilter.maskUserId(userId), dataType, resource);
    }
    
    /**
     * Registra intentos de acceso no autorizado a datos sensibles
     */
    public void logUnauthorizedAccess(String userId, String dataType, String operation, String resource, String reason) {
        Map<String, Object> auditEvent = new HashMap<>();
        auditEvent.put("event_type", "UNAUTHORIZED_ACCESS_ATTEMPT");
        auditEvent.put("timestamp", LocalDateTime.now());
        auditEvent.put("user_id", sensitiveDataFilter.maskUserId(userId));
        auditEvent.put("data_type", dataType);
        auditEvent.put("operation", operation);
        auditEvent.put("resource", resource);
        auditEvent.put("reason", reason);
        auditEvent.put("access_granted", false);
        auditEvent.put("severity", "HIGH");
        
        auditLogger.warn("Intento de acceso no autorizado: {}", auditEvent);
        securityLogger.error("Intento de acceso no autorizado: Usuario {} intentó acceder a {} en {} - Razón: {}", 
                            sensitiveDataFilter.maskUserId(userId), dataType, resource, reason);
    }
    
    /**
     * Registra la configuración de enmascaramiento
     */
    public void logMaskingConfiguration(String configType, String value, boolean enabled) {
        Map<String, Object> auditEvent = new HashMap<>();
        auditEvent.put("event_type", "MASKING_CONFIGURATION");
        auditEvent.put("timestamp", LocalDateTime.now());
        auditEvent.put("config_type", configType);
        auditEvent.put("value", sensitiveDataFilter.maskSensitiveData(value));
        auditEvent.put("enabled", enabled);
        
        auditLogger.info("Configuración de enmascaramiento: {}", auditEvent);
        securityLogger.info("Configuración de enmascaramiento actualizada: {} = {}", configType, enabled);
    }
    
    /**
     * Registra errores en el proceso de enmascaramiento
     */
    public void logMaskingError(String dataType, String error, String source) {
        Map<String, Object> auditEvent = new HashMap<>();
        auditEvent.put("event_type", "MASKING_ERROR");
        auditEvent.put("timestamp", LocalDateTime.now());
        auditEvent.put("data_type", dataType);
        auditEvent.put("error", error);
        auditEvent.put("source", source);
        auditEvent.put("severity", "HIGH");
        
        auditLogger.error("Error en enmascaramiento: {}", auditEvent);
        securityLogger.error("Error en enmascaramiento de {} en {}: {}", dataType, source, error);
    }
    
    /**
     * Registra el inicio de una sesión de auditoría
     */
    public void logAuditSessionStart(String sessionId, String userId, String ipAddress) {
        Map<String, Object> auditEvent = new HashMap<>();
        auditEvent.put("event_type", "AUDIT_SESSION_START");
        auditEvent.put("timestamp", LocalDateTime.now());
        auditEvent.put("session_id", sessionId);
        auditEvent.put("user_id", sensitiveDataFilter.maskUserId(userId));
        auditEvent.put("ip_address", sensitiveDataFilter.maskIP(ipAddress));
        
        auditLogger.info("Inicio de sesión de auditoría: {}", auditEvent);
        securityLogger.info("Sesión de auditoría iniciada: {} para usuario {}", 
                           sessionId, sensitiveDataFilter.maskUserId(userId));
    }
    
    /**
     * Registra el fin de una sesión de auditoría
     */
    public void logAuditSessionEnd(String sessionId, String userId, long duration) {
        Map<String, Object> auditEvent = new HashMap<>();
        auditEvent.put("event_type", "AUDIT_SESSION_END");
        auditEvent.put("timestamp", LocalDateTime.now());
        auditEvent.put("session_id", sessionId);
        auditEvent.put("user_id", sensitiveDataFilter.maskUserId(userId));
        auditEvent.put("duration_seconds", duration);
        
        auditLogger.info("Fin de sesión de auditoría: {}", auditEvent);
        securityLogger.info("Sesión de auditoría finalizada: {} para usuario {} (duración: {}s)", 
                           sessionId, sensitiveDataFilter.maskUserId(userId), duration);
    }
    
    /**
     * Registra eventos de cumplimiento normativo
     */
    public void logComplianceEvent(String regulation, String requirement, String action, String details) {
        Map<String, Object> auditEvent = new HashMap<>();
        auditEvent.put("event_type", "COMPLIANCE_EVENT");
        auditEvent.put("timestamp", LocalDateTime.now());
        auditEvent.put("regulation", regulation);
        auditEvent.put("requirement", requirement);
        auditEvent.put("action", action);
        auditEvent.put("details", sensitiveDataFilter.maskSensitiveData(details));
        
        auditLogger.info("Evento de cumplimiento: {}", auditEvent);
        securityLogger.info("Cumplimiento normativo: {} - {} - {}", regulation, requirement, action);
    }
    
    /**
     * Registra eventos de retención de datos
     */
    public void logDataRetentionEvent(String dataType, String action, String retentionPeriod, int recordCount) {
        Map<String, Object> auditEvent = new HashMap<>();
        auditEvent.put("event_type", "DATA_RETENTION_EVENT");
        auditEvent.put("timestamp", LocalDateTime.now());
        auditEvent.put("data_type", dataType);
        auditEvent.put("action", action);
        auditEvent.put("retention_period", retentionPeriod);
        auditEvent.put("record_count", recordCount);
        
        auditLogger.info("Evento de retención de datos: {}", auditEvent);
        securityLogger.info("Retención de datos: {} {} registros de {} (período: {})", 
                           action, recordCount, dataType, retentionPeriod);
    }
} 