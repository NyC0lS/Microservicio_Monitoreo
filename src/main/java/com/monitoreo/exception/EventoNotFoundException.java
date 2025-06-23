package com.monitoreo.exception;

/**
 * Excepción lanzada cuando no se encuentra un evento de monitoreo
 */
public class EventoNotFoundException extends MonitoreoException {

    public EventoNotFoundException(String message) {
        super("EVENTO_NOT_FOUND", message);
    }

    public EventoNotFoundException(Long eventoId) {
        super("EVENTO_NOT_FOUND", "Evento con ID " + eventoId + " no encontrado");
    }

    public EventoNotFoundException(String message, Throwable cause) {
        super("EVENTO_NOT_FOUND", message, cause);
    }
} 