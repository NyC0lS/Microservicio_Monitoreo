package com.monitoreo.config;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

/**
 * Filtro para manejar Correlation ID en las peticiones HTTP
 * Genera un nuevo correlation-id si no existe, o extrae el existente del header
 */
@Component
public class CorrelationIdFilter extends OncePerRequestFilter {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    public static final String REQUEST_ID_HEADER = "X-Request-ID";
    public static final String CORRELATION_ID_MDC_KEY = "correlationId";
    public static final String REQUEST_ID_MDC_KEY = "requestId";
    public static final String USER_ID_MDC_KEY = "userId";
    public static final String SESSION_ID_MDC_KEY = "sessionId";

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) 
            throws ServletException, IOException {
        
        try {
            // Extraer o generar correlation-id
            String correlationId = extractOrGenerateCorrelationId(request);
            
            // Extraer otros identificadores útiles
            String requestId = extractRequestId(request);
            String userId = extractUserId(request);
            String sessionId = extractSessionId(request);
            
            // Agregar correlation-id al header de respuesta
            response.addHeader(CORRELATION_ID_HEADER, correlationId);
            
            // Configurar MDC con todos los identificadores
            MDC.put(CORRELATION_ID_MDC_KEY, correlationId);
            MDC.put(REQUEST_ID_MDC_KEY, requestId);
            
            if (userId != null) {
                MDC.put(USER_ID_MDC_KEY, userId);
            }
            
            if (sessionId != null) {
                MDC.put(SESSION_ID_MDC_KEY, sessionId);
            }
            
            // Agregar información adicional al MDC
            MDC.put("method", request.getMethod());
            MDC.put("uri", request.getRequestURI());
            MDC.put("remoteAddr", getClientIpAddress(request));
            MDC.put("userAgent", request.getHeader("User-Agent"));
            
            // Continuar con la cadena de filtros
            filterChain.doFilter(request, response);
            
        } finally {
            // Limpiar MDC al finalizar la petición
            MDC.clear();
        }
    }

    /**
     * Extrae el correlation-id del header o genera uno nuevo
     */
    private String extractOrGenerateCorrelationId(HttpServletRequest request) {
        String correlationId = request.getHeader(CORRELATION_ID_HEADER);
        
        if (correlationId == null || correlationId.trim().isEmpty()) {
            correlationId = generateCorrelationId();
        }
        
        return correlationId;
    }

    /**
     * Genera un nuevo correlation-id único
     */
    private String generateCorrelationId() {
        return "corr-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    /**
     * Extrae el request-id del header o genera uno nuevo
     */
    private String extractRequestId(HttpServletRequest request) {
        String requestId = request.getHeader(REQUEST_ID_HEADER);
        
        if (requestId == null || requestId.trim().isEmpty()) {
            requestId = "req-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        }
        
        return requestId;
    }

    /**
     * Extrae el user-id del header o de la sesión
     */
    private String extractUserId(HttpServletRequest request) {
        // Intentar obtener del header primero
        String userId = request.getHeader("X-User-ID");
        
        if (userId == null || userId.trim().isEmpty()) {
            // Intentar obtener de la sesión
            if (request.getSession(false) != null) {
                userId = (String) request.getSession().getAttribute("userId");
            }
        }
        
        return userId;
    }

    /**
     * Extrae el session-id
     */
    private String extractSessionId(HttpServletRequest request) {
        if (request.getSession(false) != null) {
            return request.getSession().getId();
        }
        return null;
    }

    /**
     * Obtiene la dirección IP real del cliente
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }

    /**
     * Obtiene el correlation-id actual del MDC
     */
    public static String getCurrentCorrelationId() {
        return MDC.get(CORRELATION_ID_MDC_KEY);
    }

    /**
     * Obtiene el request-id actual del MDC
     */
    public static String getCurrentRequestId() {
        return MDC.get(REQUEST_ID_MDC_KEY);
    }

    /**
     * Obtiene el user-id actual del MDC
     */
    public static String getCurrentUserId() {
        return MDC.get(USER_ID_MDC_KEY);
    }

    /**
     * Obtiene el session-id actual del MDC
     */
    public static String getCurrentSessionId() {
        return MDC.get(SESSION_ID_MDC_KEY);
    }
} 