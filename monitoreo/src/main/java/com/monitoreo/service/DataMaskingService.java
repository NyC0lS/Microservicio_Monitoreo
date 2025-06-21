package com.monitoreo.service;

import com.monitoreo.config.SensitiveDataFilter;
import com.monitoreo.model.EventoMonitoreo;
import com.monitoreo.service.MetricsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Servicio para enmascarar datos sensibles en modelos de dominio y respuestas de API.
 * Proporciona métodos específicos para diferentes tipos de entidades de monitoreo.
 */
@Service
public class DataMaskingService {
    
    private static final Logger logger = LoggerFactory.getLogger(DataMaskingService.class);
    
    @Autowired
    private SensitiveDataFilter sensitiveDataFilter;
    
    @Autowired
    private MetricsService metricsService;
    
    /**
     * Enmascara datos sensibles en un evento de monitoreo
     */
    public EventoMonitoreo maskEventoMonitoreo(EventoMonitoreo evento) {
        if (evento == null) {
            return null;
        }
        
        EventoMonitoreo maskedEvento = new EventoMonitoreo();
        
        // Copiar datos básicos
        maskedEvento.setId(evento.getId());
        maskedEvento.setEventType(evento.getEventType());
        maskedEvento.setServiceName(evento.getServiceName());
        maskedEvento.setLevel(evento.getLevel());
        maskedEvento.setTimestamp(evento.getTimestamp());
        maskedEvento.setSessionId(evento.getSessionId());
        
        // Enmascarar datos sensibles
        maskedEvento.setUserId(sensitiveDataFilter.maskUserId(evento.getUserId()));
        
        // Enmascarar mensaje si contiene datos sensibles
        if (evento.getMessage() != null && sensitiveDataFilter.containsSensitiveData(evento.getMessage())) {
            maskedEvento.setMessage(sensitiveDataFilter.maskSensitiveData(evento.getMessage()));
            metricsService.incrementarDatosSensiblesDetectados();
            metricsService.incrementarDatosEnmascarados();
        } else {
            maskedEvento.setMessage(evento.getMessage());
        }
        
        // Enmascarar metadata si contiene datos sensibles
        if (evento.getMetadata() != null && sensitiveDataFilter.containsSensitiveData(evento.getMetadata())) {
            maskedEvento.setMetadata(sensitiveDataFilter.maskSensitiveData(evento.getMetadata()));
            metricsService.incrementarDatosSensiblesDetectados();
            metricsService.incrementarDatosEnmascarados();
        } else {
            maskedEvento.setMetadata(evento.getMetadata());
        }
        
        logger.debug("Evento de monitoreo enmascarado: ID={}, Tipo={}", 
                    maskedEvento.getId(), maskedEvento.getEventType());
        
        return maskedEvento;
    }
    
    /**
     * Enmascara un monto monetario para mostrar solo el rango
     */
    public String maskAmount(BigDecimal amount) {
        if (amount == null) {
            return null;
        }
        return sensitiveDataFilter.maskAmount(amount.toString());
    }
    
    /**
     * Enmascara un mensaje de log
     */
    public String maskLogMessage(String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }
        
        if (sensitiveDataFilter.containsSensitiveData(message)) {
            metricsService.incrementarDatosSensiblesDetectados();
            metricsService.incrementarDatosEnmascarados();
        }
        
        return sensitiveDataFilter.maskSensitiveData(message);
    }
    
    /**
     * Enmascara un mapa de metadatos
     */
    public Map<String, String> maskMetadata(Map<String, String> metadata) {
        if (metadata == null) {
            return null;
        }
        
        Map<String, String> maskedMetadata = new HashMap<>();
        for (Map.Entry<String, String> entry : metadata.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            
            // Enmascarar valores sensibles
            if (sensitiveDataFilter.containsSensitiveData(value)) {
                maskedMetadata.put(key, sensitiveDataFilter.maskSensitiveData(value));
            } else {
                maskedMetadata.put(key, value);
            }
        }
        
        return maskedMetadata;
    }
    
    /**
     * Verifica si un objeto contiene datos sensibles
     */
    public boolean containsSensitiveData(Object obj) {
        if (obj == null) {
            return false;
        }
        
        if (obj instanceof String) {
            return sensitiveDataFilter.containsSensitiveData((String) obj);
        }
        
        if (obj instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) obj;
            for (Object value : map.values()) {
                if (containsSensitiveData(value)) {
                    return true;
                }
            }
        }
        
        return false;
    }
} 