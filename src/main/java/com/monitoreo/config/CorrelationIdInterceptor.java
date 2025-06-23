package com.monitoreo.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Interceptor para propagar Correlation ID en llamadas HTTP salientes
 * Asegura que el correlation-id se incluya en todas las peticiones a otros microservicios
 */
@Component
public class CorrelationIdInterceptor implements ClientHttpRequestInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(CorrelationIdInterceptor.class);

    @Override
    public ClientHttpResponse intercept(HttpRequest request, 
                                      byte[] body, 
                                      ClientHttpRequestExecution execution) 
            throws IOException {
        
        // Obtener correlation-id del MDC
        String correlationId = CorrelationIdFilter.getCurrentCorrelationId();
        String requestId = CorrelationIdFilter.getCurrentRequestId();
        String userId = CorrelationIdFilter.getCurrentUserId();
        String sessionId = CorrelationIdFilter.getCurrentSessionId();
        
        // Agregar headers de trazabilidad
        if (correlationId != null) {
            request.getHeaders().add(CorrelationIdFilter.CORRELATION_ID_HEADER, correlationId);
            logger.debug("Propagando correlation-id: {} en petición a: {}", correlationId, request.getURI());
        }
        
        if (requestId != null) {
            request.getHeaders().add("X-Request-ID", requestId);
        }
        
        if (userId != null) {
            request.getHeaders().add("X-User-ID", userId);
        }
        
        if (sessionId != null) {
            request.getHeaders().add("X-Session-ID", sessionId);
        }
        
        // Agregar timestamp de la petición
        request.getHeaders().add("X-Request-Timestamp", String.valueOf(System.currentTimeMillis()));
        
        // Ejecutar la petición
        ClientHttpResponse response = execution.execute(request, body);
        
        // Log de la respuesta
        logger.debug("Respuesta recibida para correlation-id: {} - Status: {}", 
                    correlationId, response.getStatusCode());
        
        return response;
    }
} 