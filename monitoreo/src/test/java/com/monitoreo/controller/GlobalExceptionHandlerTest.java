package com.monitoreo.controller;

import com.monitoreo.exception.EventoNotFoundException;
import com.monitoreo.exception.GlobalExceptionHandler;
import com.monitoreo.exception.InvalidEventoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler globalExceptionHandler;
    private WebRequest webRequest;

    @BeforeEach
    void setUp() {
        globalExceptionHandler = new GlobalExceptionHandler();
        webRequest = mock(WebRequest.class);
    }

    @Test
    void handleEventoNotFoundException() {
        EventoNotFoundException ex = new EventoNotFoundException("Evento no encontrado");
        ResponseEntity<?> responseEntity = globalExceptionHandler.handleEventoNotFoundException(ex, webRequest);
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
    }

    @Test
    void handleInvalidEventoException() {
        InvalidEventoException ex = new InvalidEventoException("Evento inválido");
        ResponseEntity<?> responseEntity = globalExceptionHandler.handleInvalidEventoException(ex, webRequest);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }

    @Test
    void handleAllExceptions() {
        Exception ex = new Exception("Error inesperado");
        ResponseEntity<?> responseEntity = globalExceptionHandler.handleGenericException(ex, webRequest);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
    }
} 