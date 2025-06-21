package com.monitoreo.exception;

/**
 * Excepción lanzada cuando un evento de monitoreo es inválido
 */
public class InvalidEventoException extends MonitoreoException {

    public InvalidEventoException(String message) {
        super("INVALID_EVENTO", message);
    }

    public InvalidEventoException(String field, String reason) {
        super("INVALID_EVENTO", "Campo '" + field + "' inválido: " + reason);
    }

    public InvalidEventoException(String message, Throwable cause) {
        super("INVALID_EVENTO", message, cause);
    }
} 