package com.banquito.pos.exception;

public class CommunicationException extends RuntimeException {
    
    private final String service;
    private final String details;
    
    public CommunicationException(String service, String details) {
        super();
        this.service = service;
        this.details = details;
    }
    
    @Override
    public String getMessage() {
        return "Error de comunicación con el servicio: " + this.service + ". Detalles: " + this.details;
    }
} 