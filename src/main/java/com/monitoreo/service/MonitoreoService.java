package com.monitoreo.service;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Servicio principal para la lógica de negocio del monitoreo
 */
@Service
public class MonitoreoService implements HealthIndicator {

    private static final Logger logger = LoggerFactory.getLogger(MonitoreoService.class);

    /**
     * Verifica el estado de salud del servicio
     */
    public Map<String, Object> checkHealth() {
        logger.debug("Verificando estado de salud del servicio");
        
        Map<String, Object> healthStatus = new HashMap<>();
        healthStatus.put("status", "UP");
        healthStatus.put("timestamp", LocalDateTime.now());
        healthStatus.put("service", "monitoreo_loggin");
        healthStatus.put("version", "1.0.0");
        
        return healthStatus;
    }

    /**
     * Obtiene información del sistema
     */
    public Map<String, Object> getSystemInfo() {
        logger.debug("Obteniendo información del sistema");
        
        Map<String, Object> systemInfo = new HashMap<>();
        systemInfo.put("javaVersion", System.getProperty("java.version"));
        systemInfo.put("osName", System.getProperty("os.name"));
        systemInfo.put("osVersion", System.getProperty("os.version"));
        systemInfo.put("userName", System.getProperty("user.name"));
        systemInfo.put("timestamp", LocalDateTime.now());
        
        return systemInfo;
    }

    /**
     * Registra un evento de monitoreo
     */
    public void logEvent(String eventType, String message, Map<String, Object> metadata) {
        logger.info("Evento de monitoreo: {} - {}", eventType, message);
        
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", eventType);
        event.put("message", message);
        event.put("timestamp", LocalDateTime.now());
        event.put("metadata", metadata);
        
        // Aquí se podría agregar lógica para persistir el evento
        logger.debug("Evento registrado: {}", event);
    }

    /**
     * Implementación del HealthIndicator para Actuator
     */
    @Override
    public Health health() {
        try {
            Map<String, Object> healthStatus = checkHealth();
            return Health.up()
                    .withDetails(healthStatus)
                    .build();
        } catch (Exception e) {
            logger.error("Error al verificar la salud del servicio", e);
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
} 