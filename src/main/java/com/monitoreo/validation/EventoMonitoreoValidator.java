package com.monitoreo.validation;

import com.monitoreo.dto.EventoMonitoreoRequest;
import com.monitoreo.exception.InvalidEventoException;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Validador personalizado para eventos de monitoreo
 */
@Component
public class EventoMonitoreoValidator implements Validator {

    private static final int MAX_METADATA_SIZE = 50000; // 50KB
    private static final int MAX_METADATA_KEYS = 100;

    @Override
    public boolean supports(Class<?> clazz) {
        return EventoMonitoreoRequest.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        EventoMonitoreoRequest evento = (EventoMonitoreoRequest) target;

        // Validaciones adicionales específicas para monitoreo
        validateEventType(evento.getEventType(), errors);
        validateMessage(evento.getMessage(), errors);
        validateTimestamp(evento.getTimestamp(), errors);
        validateLevel(evento.getLevel(), errors);
        validateMetadata(evento.getMetadata(), errors);
        validateServiceName(evento.getServiceName(), errors);
    }

    private void validateEventType(String eventType, Errors errors) {
        if (eventType != null) {
            // Validar que no contenga caracteres especiales peligrosos
            if (eventType.contains("<") || eventType.contains(">") || eventType.contains("&")) {
                errors.rejectValue("eventType", "invalid.eventType", 
                    "El tipo de evento no puede contener caracteres especiales");
            }

            // Validar que no sea demasiado corto
            if (eventType.trim().length() < 2) {
                errors.rejectValue("eventType", "invalid.eventType", 
                    "El tipo de evento debe tener al menos 2 caracteres");
            }
        }
    }

    private void validateMessage(String message, Errors errors) {
        if (message != null) {
            // Validar que no esté vacío después de trim
            if (message.trim().isEmpty()) {
                errors.rejectValue("message", "invalid.message", 
                    "El mensaje no puede estar vacío");
            }

            // Validar que no contenga scripts maliciosos
            if (message.toLowerCase().contains("<script>") || 
                message.toLowerCase().contains("javascript:")) {
                errors.rejectValue("message", "invalid.message", 
                    "El mensaje no puede contener scripts");
            }
        }
    }

    private void validateTimestamp(LocalDateTime timestamp, Errors errors) {
        if (timestamp != null) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime maxPast = now.minusDays(30); // No más de 30 días en el pasado

            if (timestamp.isAfter(now)) {
                errors.rejectValue("timestamp", "invalid.timestamp", 
                    "El timestamp no puede ser futuro");
            }

            if (timestamp.isBefore(maxPast)) {
                errors.rejectValue("timestamp", "invalid.timestamp", 
                    "El timestamp no puede ser más de 30 días en el pasado");
            }
        }
    }

    private void validateLevel(String level, Errors errors) {
        if (level != null) {
            String[] validLevels = {"INFO", "WARN", "ERROR", "DEBUG", "TRACE"};
            boolean isValid = false;
            
            for (String validLevel : validLevels) {
                if (validLevel.equals(level)) {
                    isValid = true;
                    break;
                }
            }

            if (!isValid) {
                errors.rejectValue("level", "invalid.level", 
                    "El nivel debe ser INFO, WARN, ERROR, DEBUG o TRACE");
            }
        }
    }

    private void validateMetadata(Map<String, Object> metadata, Errors errors) {
        if (metadata != null) {
            // Validar tamaño total
            int totalSize = metadata.toString().getBytes().length;
            if (totalSize > MAX_METADATA_SIZE) {
                errors.rejectValue("metadata", "invalid.metadata", 
                    "Los metadatos no pueden exceder " + MAX_METADATA_SIZE + " bytes");
            }

            // Validar número de claves
            if (metadata.size() > MAX_METADATA_KEYS) {
                errors.rejectValue("metadata", "invalid.metadata", 
                    "Los metadatos no pueden tener más de " + MAX_METADATA_KEYS + " claves");
            }

            // Validar claves individuales
            for (Map.Entry<String, Object> entry : metadata.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();

                if (key == null || key.trim().isEmpty()) {
                    errors.rejectValue("metadata", "invalid.metadata", 
                        "Las claves de metadatos no pueden estar vacías");
                }

                if (key.length() > 100) {
                    errors.rejectValue("metadata", "invalid.metadata", 
                        "Las claves de metadatos no pueden exceder 100 caracteres");
                }

                if (value != null && value.toString().length() > 1000) {
                    errors.rejectValue("metadata", "invalid.metadata", 
                        "Los valores de metadatos no pueden exceder 1000 caracteres");
                }
            }
        }
    }

    private void validateServiceName(String serviceName, Errors errors) {
        if (serviceName != null && !serviceName.trim().isEmpty()) {
            // Validar formato del nombre del servicio
            if (!serviceName.matches("^[a-zA-Z0-9_-]+$")) {
                errors.rejectValue("serviceName", "invalid.serviceName", 
                    "El nombre del servicio solo puede contener letras, números, guiones y guiones bajos");
            }

            if (serviceName.length() > 100) {
                errors.rejectValue("serviceName", "invalid.serviceName", 
                    "El nombre del servicio no puede exceder 100 caracteres");
            }
        }
    }

    /**
     * Método para validar y lanzar excepción si hay errores
     */
    public void validateAndThrow(EventoMonitoreoRequest evento) {
        org.springframework.validation.BeanPropertyBindingResult errors = 
            new org.springframework.validation.BeanPropertyBindingResult(evento, "evento");
        
        validate(evento, errors);
        
        if (errors.hasErrors()) {
            StringBuilder message = new StringBuilder("Evento inválido: ");
            errors.getAllErrors().forEach(error -> 
                message.append(error.getDefaultMessage()).append("; "));
            
            throw new InvalidEventoException(message.toString());
        }
    }
} 