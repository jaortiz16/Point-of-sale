package com.banquito.pos.exception;

public class ValidationException extends RuntimeException {
    
    private final String message;
    
    public ValidationException(String message) {
        super();
        this.message = message;
    }
    
    @Override
    public String getMessage() {
        return this.message;
    }
} 