package com.monitoreo.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Controlador de prueba para demostrar logging estructurado en JSON
 */
@RestController
@RequestMapping("/api/test/logging")
@CrossOrigin(origins = "*")
public class LoggingTestController {

    private static final Logger logger = LoggerFactory.getLogger(LoggingTestController.class);

    /**
     * Prueba de logging básico
     */
    @GetMapping("/basic")
    public ResponseEntity<Map<String, Object>> testBasicLogging() {
        logger.info("Iniciando prueba de logging básico");
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Logging básico funcionando");
        response.put("timestamp", LocalDateTime.now());
        response.put("level", "INFO");
        
        logger.info("Prueba de logging básico completada exitosamente");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Prueba de logging con MDC (Mapped Diagnostic Context)
     */
    @GetMapping("/mdc")
    public ResponseEntity<Map<String, Object>> testMDCLogging() {
        // Configurar contexto MDC
        MDC.put("userId", "user123");
        MDC.put("sessionId", "sess_abc456");
        MDC.put("requestId", "req_789");
        MDC.put("endpoint", "/api/test/logging/mdc");
        
        logger.info("Iniciando prueba de logging con MDC");
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Logging con MDC funcionando");
        response.put("timestamp", LocalDateTime.now());
        response.put("mdc", "Configurado con userId, sessionId, requestId");
        
        logger.info("Prueba de logging con MDC completada exitosamente");
        
        // Limpiar MDC
        MDC.clear();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Prueba de logging con diferentes niveles
     */
    @GetMapping("/levels")
    public ResponseEntity<Map<String, Object>> testLogLevels() {
        logger.trace("Este es un mensaje TRACE");
        logger.debug("Este es un mensaje DEBUG");
        logger.info("Este es un mensaje INFO");
        logger.warn("Este es un mensaje WARN");
        logger.error("Este es un mensaje ERROR");
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Prueba de diferentes niveles de logging");
        response.put("timestamp", LocalDateTime.now());
        response.put("levels", "TRACE, DEBUG, INFO, WARN, ERROR");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Prueba de logging con excepciones
     */
    @GetMapping("/exception")
    public ResponseEntity<Map<String, Object>> testExceptionLogging() {
        try {
            // Simular una excepción
            throw new RuntimeException("Error simulado para prueba de logging");
        } catch (Exception e) {
            logger.error("Error capturado en prueba de logging", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Excepción capturada y loggeada");
            response.put("timestamp", LocalDateTime.now());
            response.put("exception", e.getMessage());
            
            return ResponseEntity.ok(response);
        }
    }

    /**
     * Prueba de logging con datos estructurados
     */
    @PostMapping("/structured")
    public ResponseEntity<Map<String, Object>> testStructuredLogging(@RequestBody Map<String, Object> data) {
        MDC.put("operation", "structured_logging");
        MDC.put("dataType", "user_data");
        
        logger.info("Procesando datos estructurados: {}", data);
        
        // Simular procesamiento
        data.put("processed", true);
        data.put("processedAt", LocalDateTime.now());
        
        logger.info("Datos procesados exitosamente: {}", data);
        
        MDC.clear();
        
        return ResponseEntity.ok(data);
    }

    /**
     * Prueba de logging de seguridad
     */
    @PostMapping("/security")
    public ResponseEntity<Map<String, Object>> testSecurityLogging(@RequestBody Map<String, Object> credentials) {
        // Simular datos sensibles
        String username = (String) credentials.get("username");
        String password = (String) credentials.get("password");
        
        logger.info("Intento de autenticación para usuario: {}", username);
        logger.debug("Datos de autenticación recibidos (password enmascarado)");
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Log de seguridad generado");
        response.put("timestamp", LocalDateTime.now());
        response.put("user", username);
        response.put("status", "logged");
        
        return ResponseEntity.ok(response);
    }
} 