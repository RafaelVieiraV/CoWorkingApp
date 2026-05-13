package com.example.u1proyecto_grupob.web.advice;

public class BusinessConflictException extends RuntimeException {
    public BusinessConflictException(String message) {
        super(message);
    }
}
