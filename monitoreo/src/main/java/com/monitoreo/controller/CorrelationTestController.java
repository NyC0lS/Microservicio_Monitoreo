package com.monitoreo.controller;

import com.monitoreo.config.CorrelationIdFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador para probar la propagación de Correlation ID
 */
@RestController
@RequestMapping("/api/test/correlation")
@CrossOrigin(origins = "*")
public class CorrelationTestController {

    private static final Logger logger = LoggerFactory.getLogger(CorrelationTestController.class);

    @Autowired
    private RestTemplate restTemplate;

    /**
     * GET - Probar correlation-id en logs
     */
    @GetMapping("/logs")
    public ResponseEntity<Map<String, Object>> testCorrelationInLogs() {
        String correlationId = CorrelationIdFilter.getCurrentCorrelationId();
        String requestId = CorrelationIdFilter.getCurrentRequestId();
        String userId = CorrelationIdFilter.getCurrentUserId();
        String sessionId = CorrelationIdFilter.getCurrentSessionId();

        logger.debug("Log de DEBUG - CorrelationId: {}, RequestId: {}", correlationId, requestId);
        logger.info("Log de INFO - CorrelationId: {}, RequestId: {}", correlationId, requestId);
        logger.warn("Log de WARN - CorrelationId: {}, RequestId: {}", correlationId, requestId);
        logger.error("Log de ERROR - CorrelationId: {}, RequestId: {}", correlationId, requestId);

        Map<String, Object> response = new HashMap<>();
        response.put("correlationId", correlationId);
        response.put("requestId", requestId);
        response.put("userId", userId);
        response.put("sessionId", sessionId);
        response.put("message", "Logs generados con correlation-id");
        response.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.ok(response);
    }

    /**
     * POST - Probar propagación de correlation-id a otro servicio
     */
    @PostMapping("/propagate")
    public ResponseEntity<Map<String, Object>> testCorrelationPropagation(
            @RequestParam(defaultValue = "http://localhost:8080") String targetUrl) {
        
        String correlationId = CorrelationIdFilter.getCurrentCorrelationId();
        String requestId = CorrelationIdFilter.getCurrentRequestId();

        logger.info("Iniciando prueba de propagación - CorrelationId: {}, RequestId: {}, Target: {}", 
                   correlationId, requestId, targetUrl);

        try {
            // Hacer una petición HTTP a otro servicio
            String testEndpoint = targetUrl + "/api/test/correlation/logs";
            ResponseEntity<Map> response = restTemplate.getForEntity(testEndpoint, Map.class);

            logger.info("Respuesta recibida - CorrelationId: {}, RequestId: {}, Status: {}", 
                       correlationId, requestId, response.getStatusCode());

            Map<String, Object> result = new HashMap<>();
            result.put("correlationId", correlationId);
            result.put("requestId", requestId);
            result.put("targetUrl", testEndpoint);
            result.put("responseStatus", response.getStatusCode().toString());
            result.put("responseBody", response.getBody());
            result.put("propagationSuccessful", true);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            logger.error("Error en propagación - CorrelationId: {}, RequestId: {}, Error: {}", 
                        correlationId, requestId, e.getMessage(), e);

            Map<String, Object> result = new HashMap<>();
            result.put("correlationId", correlationId);
            result.put("requestId", requestId);
            result.put("targetUrl", targetUrl);
            result.put("error", e.getMessage());
            result.put("propagationSuccessful", false);

            return ResponseEntity.status(500).body(result);
        }
    }

    /**
     * GET - Obtener información del correlation-id actual
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getCorrelationInfo() {
        String correlationId = CorrelationIdFilter.getCurrentCorrelationId();
        String requestId = CorrelationIdFilter.getCurrentRequestId();
        String userId = CorrelationIdFilter.getCurrentUserId();
        String sessionId = CorrelationIdFilter.getCurrentSessionId();

        Map<String, Object> info = new HashMap<>();
        info.put("correlationId", correlationId);
        info.put("requestId", requestId);
        info.put("userId", userId);
        info.put("sessionId", sessionId);
        info.put("timestamp", System.currentTimeMillis());
        info.put("headers", Map.of(
            "X-Correlation-ID", correlationId,
            "X-Request-ID", requestId,
            "X-User-ID", userId != null ? userId : "N/A",
            "X-Session-ID", sessionId != null ? sessionId : "N/A"
        ));

        logger.info("Información de correlation obtenida - CorrelationId: {}, RequestId: {}", 
                   correlationId, requestId);

        return ResponseEntity.ok(info);
    }

    /**
     * POST - Simular petición con correlation-id personalizado
     */
    @PostMapping("/custom")
    public ResponseEntity<Map<String, Object>> testCustomCorrelation(
            @RequestParam(required = false) String correlationId,
            @RequestParam(required = false) String requestId,
            @RequestParam(required = false) String userId) {
        
        String currentCorrelationId = CorrelationIdFilter.getCurrentCorrelationId();
        String currentRequestId = CorrelationIdFilter.getCurrentRequestId();

        logger.info("Prueba con correlation personalizado - Current: {}, Custom: {}", 
                   currentCorrelationId, correlationId);

        Map<String, Object> result = new HashMap<>();
        result.put("currentCorrelationId", currentCorrelationId);
        result.put("currentRequestId", currentRequestId);
        result.put("customCorrelationId", correlationId);
        result.put("customRequestId", requestId);
        result.put("customUserId", userId);
        result.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.ok(result);
    }

    /**
     * GET - Probar logs con diferentes niveles y correlation-id
     */
    @GetMapping("/levels")
    public ResponseEntity<Map<String, Object>> testLogLevels() {
        String correlationId = CorrelationIdFilter.getCurrentCorrelationId();
        String requestId = CorrelationIdFilter.getCurrentRequestId();

        // Generar logs con diferentes niveles
        logger.trace("TRACE - CorrelationId: {}, RequestId: {}", correlationId, requestId);
        logger.debug("DEBUG - CorrelationId: {}, RequestId: {}", correlationId, requestId);
        logger.info("INFO - CorrelationId: {}, RequestId: {}", correlationId, requestId);
        logger.warn("WARN - CorrelationId: {}, RequestId: {}", correlationId, requestId);
        logger.error("ERROR - CorrelationId: {}, RequestId: {}", correlationId, requestId);

        Map<String, Object> result = new HashMap<>();
        result.put("correlationId", correlationId);
        result.put("requestId", requestId);
        result.put("message", "Logs de diferentes niveles generados");
        result.put("levels", new String[]{"TRACE", "DEBUG", "INFO", "WARN", "ERROR"});
        result.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.ok(result);
    }
} 