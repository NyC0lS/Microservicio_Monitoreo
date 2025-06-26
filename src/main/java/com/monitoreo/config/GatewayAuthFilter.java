package com.monitoreo.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

/**
 * Filtro de autenticación que valida el header user-id enviado por el Gateway
 * Este filtro se ejecuta después del JwtAuthFilter del Gateway
 */
@Component
public class GatewayAuthFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(GatewayAuthFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) 
            throws ServletException, IOException {
        
        try {
            // Extraer user-id del header enviado por el Gateway
            String userId = extractUserId(request);
            
            if (userId != null && !userId.trim().isEmpty()) {
                logger.debug("GatewayAuthFilter - User ID encontrado: {}", userId);
                
                // Crear autenticación con el user-id
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(
                        userId, 
                        null, 
                        Collections.singletonList(new SimpleGrantedAuthority("USER"))
                    );
                
                // Establecer la autenticación en el contexto de seguridad
                SecurityContextHolder.getContext().setAuthentication(authentication);
                
                logger.debug("GatewayAuthFilter - Autenticación establecida para usuario: {}", userId);
                
            } else {
                logger.warn("GatewayAuthFilter - No se encontró user-id en el header");
            }
            
            // Continuar con la cadena de filtros
            filterChain.doFilter(request, response);
            
        } catch (Exception e) {
            logger.error("GatewayAuthFilter - Error durante la autenticación: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Extrae el user-id del header enviado por el Gateway
     */
    private String extractUserId(HttpServletRequest request) {
        // Intentar obtener del header user-id (que envía el Gateway)
        String userId = request.getHeader("user-id");
        
        // Si no está, intentar obtener del header X-User-ID
        if (userId == null || userId.trim().isEmpty()) {
            userId = request.getHeader("X-User-ID");
        }
        
        return userId;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        // No aplicar este filtro a endpoints de actuator
        String path = request.getRequestURI();
        return path.startsWith("/actuator/") || path.startsWith("/h2-console/");
    }
} 