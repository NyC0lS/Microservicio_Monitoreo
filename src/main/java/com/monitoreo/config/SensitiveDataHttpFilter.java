package com.monitoreo.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

/**
 * Filtro HTTP para enmascarar datos sensibles en solicitudes y respuestas.
 * Se ejecuta antes de otros filtros para proteger información sensible.
 */
@Component
@Order(1)
public class SensitiveDataHttpFilter implements Filter {
    
    private static final Logger logger = LoggerFactory.getLogger(SensitiveDataHttpFilter.class);
    
    @Autowired
    private SensitiveDataFilter sensitiveDataFilter;
    
    // Headers sensibles que deben ser enmascarados
    private static final List<String> SENSITIVE_HEADERS = Arrays.asList(
        "authorization",
        "x-api-key",
        "x-auth-token",
        "cookie",
        "set-cookie"
    );
    
    // Parámetros de consulta sensibles
    private static final List<String> SENSITIVE_QUERY_PARAMS = Arrays.asList(
        "password",
        "token",
        "api_key",
        "secret",
        "key"
    );
    
    // Rutas que contienen datos sensibles
    private static final List<String> SENSITIVE_PATHS = Arrays.asList(
        "/api/auth",
        "/api/login",
        "/api/register",
        "/api/payment",
        "/api/credit-card"
    );
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        // Crear wrappers para enmascarar datos sensibles
        SensitiveDataRequestWrapper requestWrapper = new SensitiveDataRequestWrapper(httpRequest, sensitiveDataFilter);
        SensitiveDataResponseWrapper responseWrapper = new SensitiveDataResponseWrapper(httpResponse, sensitiveDataFilter);
        
        try {
            // Log de la solicitud enmascarada
            logMaskedRequest(requestWrapper);
            
            // Continuar con la cadena de filtros
            chain.doFilter(requestWrapper, responseWrapper);
            
            // Log de la respuesta enmascarada
            logMaskedResponse(responseWrapper);
            
        } catch (Exception e) {
            logger.error("Error en el filtro de datos sensibles: {}", e.getMessage());
            throw e;
        }
    }
    
    /**
     * Registra la solicitud con datos enmascarados
     */
    private void logMaskedRequest(SensitiveDataRequestWrapper requestWrapper) {
        String method = requestWrapper.getMethod();
        String uri = requestWrapper.getRequestURI();
        String queryString = requestWrapper.getQueryString();
        
        // Enmascarar URI si contiene datos sensibles
        String maskedUri = sensitiveDataFilter.maskSensitiveData(uri);
        String maskedQuery = queryString != null ? sensitiveDataFilter.maskSensitiveData(queryString) : null;
        
        logger.info("Solicitud HTTP: {} {} {}", method, maskedUri, 
                   maskedQuery != null ? "?" + maskedQuery : "");
        
        // Log de headers sensibles (enmascarados)
        for (String headerName : SENSITIVE_HEADERS) {
            String headerValue = requestWrapper.getHeader(headerName);
            if (headerValue != null) {
                logger.debug("Header {}: {}", headerName, sensitiveDataFilter.maskSensitiveData(headerValue));
            }
        }
    }
    
    /**
     * Registra la respuesta con datos enmascarados
     */
    private void logMaskedResponse(SensitiveDataResponseWrapper responseWrapper) {
        int status = responseWrapper.getStatus();
        String contentType = responseWrapper.getContentType();
        
        logger.info("Respuesta HTTP: {} - Content-Type: {}", status, contentType);
        
        // Log del cuerpo de la respuesta si contiene datos sensibles
        String responseBody = responseWrapper.getResponseBody();
        if (responseBody != null && sensitiveDataFilter.containsSensitiveData(responseBody)) {
            String maskedBody = sensitiveDataFilter.maskSensitiveData(responseBody);
            logger.debug("Respuesta enmascarada: {}", maskedBody);
        }
    }
    
    /**
     * Wrapper para enmascarar datos sensibles en solicitudes HTTP
     */
    public static class SensitiveDataRequestWrapper extends jakarta.servlet.http.HttpServletRequestWrapper {
        
        private final SensitiveDataFilter filter;
        
        public SensitiveDataRequestWrapper(HttpServletRequest request, SensitiveDataFilter filter) {
            super(request);
            this.filter = filter;
        }
        
        @Override
        public String getHeader(String name) {
            String value = super.getHeader(name);
            if (value != null && SENSITIVE_HEADERS.contains(name.toLowerCase())) {
                return filter.maskSensitiveData(value);
            }
            return value;
        }
        
        @Override
        public String getParameter(String name) {
            String value = super.getParameter(name);
            if (value != null && SENSITIVE_QUERY_PARAMS.contains(name.toLowerCase())) {
                return filter.maskSensitiveData(value);
            }
            return value;
        }
        
        @Override
        public String[] getParameterValues(String name) {
            String[] values = super.getParameterValues(name);
            if (values != null && SENSITIVE_QUERY_PARAMS.contains(name.toLowerCase())) {
                String[] maskedValues = new String[values.length];
                for (int i = 0; i < values.length; i++) {
                    maskedValues[i] = filter.maskSensitiveData(values[i]);
                }
                return maskedValues;
            }
            return values;
        }
    }
    
    /**
     * Wrapper para enmascarar datos sensibles en respuestas HTTP
     */
    public static class SensitiveDataResponseWrapper extends jakarta.servlet.http.HttpServletResponseWrapper {
        
        private final SensitiveDataFilter filter;
        private final StringBuilder responseBody = new StringBuilder();
        private final HttpServletResponse originalResponse;
        
        public SensitiveDataResponseWrapper(HttpServletResponse response, SensitiveDataFilter filter) {
            super(response);
            this.filter = filter;
            this.originalResponse = response;
        }
        
        @Override
        public ServletOutputStream getOutputStream() throws IOException {
            return new ServletOutputStream() {
                @Override
                public void write(int b) throws IOException {
                    responseBody.append((char) b);
                    originalResponse.getOutputStream().write(b);
                }
                
                @Override
                public boolean isReady() {
                    return true;
                }
                
                @Override
                public void setWriteListener(WriteListener writeListener) {
                    // No implementado
                }
            };
        }
        
        @Override
        public PrintWriter getWriter() throws IOException {
            return new PrintWriter(new OutputStreamWriter(getOutputStream()));
        }
        
        /**
         * Obtiene el cuerpo de la respuesta
         */
        public String getResponseBody() {
            return responseBody.toString();
        }
        
        /**
         * Obtiene el cuerpo de la respuesta enmascarado
         */
        public String getMaskedResponseBody() {
            return filter.maskSensitiveData(responseBody.toString());
        }
    }
} 