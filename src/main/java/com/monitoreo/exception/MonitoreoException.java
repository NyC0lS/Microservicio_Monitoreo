package com.monitoreo.exception;

/**
 * Excepción base para el sistema de monitoreo
 */
public class MonitoreoException extends RuntimeException {

    private final String errorCode;

    public MonitoreoException(String message) {
        super(message);
        this.errorCode = "MONITOREO_ERROR";
    }

    public MonitoreoException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "MONITOREO_ERROR";
    }

    public MonitoreoException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public MonitoreoException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
} 