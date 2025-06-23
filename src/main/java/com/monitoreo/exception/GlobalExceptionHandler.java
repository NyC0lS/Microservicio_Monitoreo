package com.monitoreo.exception;

import com.monitoreo.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Manejador global de excepciones para el sistema de monitoreo
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Maneja excepciones de validación de Bean Validation
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {
        
        logger.warn("Error de validación detectado: {}", ex.getMessage());
        
        List<String> details = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "VALIDATION_ERROR",
                "Error de validación en los datos de entrada",
                request.getDescription(false),
                details
        );

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Maneja excepciones personalizadas de monitoreo
     */
    @ExceptionHandler(MonitoreoException.class)
    public ResponseEntity<ErrorResponse> handleMonitoreoException(
            MonitoreoException ex, WebRequest request) {
        
        logger.error("Excepción de monitoreo: {} - {}", ex.getErrorCode(), ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ex.getErrorCode(),
                ex.getMessage(),
                request.getDescription(false)
        );

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Maneja excepciones de eventos no encontrados
     */
    @ExceptionHandler(EventoNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEventoNotFoundException(
            EventoNotFoundException ex, WebRequest request) {
        
        logger.warn("Evento no encontrado: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                ex.getErrorCode(),
                ex.getMessage(),
                request.getDescription(false)
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * Maneja excepciones de eventos inválidos
     */
    @ExceptionHandler(InvalidEventoException.class)
    public ResponseEntity<ErrorResponse> handleInvalidEventoException(
            InvalidEventoException ex, WebRequest request) {
        
        logger.warn("Evento inválido: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ex.getErrorCode(),
                ex.getMessage(),
                request.getDescription(false)
        );

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Maneja excepciones de IllegalArgumentException
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {
        
        logger.warn("Argumento ilegal: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "INVALID_ARGUMENT",
                ex.getMessage(),
                request.getDescription(false)
        );

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Maneja excepciones de NullPointerException
     */
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ErrorResponse> handleNullPointerException(
            NullPointerException ex, WebRequest request) {
        
        logger.error("NullPointerException detectada: {}", ex.getMessage(), ex);
        
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "NULL_POINTER_ERROR",
                "Error interno del servidor: referencia nula",
                request.getDescription(false)
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * Maneja excepciones generales no manejadas
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, WebRequest request) {
        
        logger.error("Excepción no manejada: {}", ex.getMessage(), ex);
        
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "INTERNAL_SERVER_ERROR",
                "Error interno del servidor",
                request.getDescription(false)
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * Maneja errores de base de datos
     */
    @ExceptionHandler(org.springframework.dao.DataAccessException.class)
    public ResponseEntity<ErrorResponse> handleDataAccessException(
            org.springframework.dao.DataAccessException ex, WebRequest request) {
        
        logger.error("Error de acceso a datos: {}", ex.getMessage(), ex);
        
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                "DATABASE_ERROR",
                "Error de acceso a la base de datos",
                request.getDescription(false)
        );

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
    }

    /**
     * Maneja errores de timeout
     */
    @ExceptionHandler(org.springframework.dao.QueryTimeoutException.class)
    public ResponseEntity<ErrorResponse> handleQueryTimeoutException(
            org.springframework.dao.QueryTimeoutException ex, WebRequest request) {
        
        logger.warn("Timeout en consulta: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.REQUEST_TIMEOUT.value(),
                "QUERY_TIMEOUT",
                "Timeout en la consulta de base de datos",
                request.getDescription(false)
        );

        return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body(errorResponse);
    }
} 