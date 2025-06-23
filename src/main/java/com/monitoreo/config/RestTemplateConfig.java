package com.monitoreo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

/**
 * Configuración de RestTemplate con interceptores para correlation-id
 */
@Configuration
public class RestTemplateConfig {

    @Autowired
    private CorrelationIdInterceptor correlationIdInterceptor;

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000); // 5 segundos
        factory.setReadTimeout(10000);   // 10 segundos
        
        RestTemplate restTemplate = new RestTemplate(new BufferingClientHttpRequestFactory(factory));
        
        // Agregar el interceptor de correlation-id
        restTemplate.setInterceptors(Collections.singletonList(correlationIdInterceptor));
        
        return restTemplate;
    }
} 