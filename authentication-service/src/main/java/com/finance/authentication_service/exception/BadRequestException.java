package com.finance.authentication_service.exception;

public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
        System.out.println("I am in bad request");
    }

    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
