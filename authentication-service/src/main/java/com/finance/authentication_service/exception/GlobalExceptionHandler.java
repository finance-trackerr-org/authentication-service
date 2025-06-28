package com.finance.authentication_service.exception;

import com.finance.authentication_service.dto.ApiErrorResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse<Object>> handleGenericException(Exception ex) {
        System.out.println(ex);
        ApiErrorResponse<Object> response = new ApiErrorResponse<>(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Something went wrong",
                ex.getMessage()
        );
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse<Object>> handleNotFound(ResourceNotFoundException ex) {
        ApiErrorResponse<Object> response = new ApiErrorResponse<>(
                HttpStatus.NOT_FOUND,
                "Resource Not Found",
                ex.getMessage()
        );
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiErrorResponse<Object>> handleBadRequest(BadRequestException ex) {
        ApiErrorResponse<Object> response = new ApiErrorResponse<>(
                HttpStatus.NOT_FOUND,
                "Bad Request",
                ex.getMessage()
        );
        System.out.println("Noooooooooo");
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<ApiErrorResponse<Object>> handleUnauthorized(UnauthorizedAccessException ex) {
        ApiErrorResponse<Object> response = new ApiErrorResponse<>(
                HttpStatus.NOT_FOUND,
                "Unauthorized",
                ex.getMessage()
        );
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationExceptions(MethodArgumentNotValidException ex) {
        ArrayList<String> errors = new ArrayList<>();

        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.add(error.getDefaultMessage())
        );

        ApiErrorResponse<Object> response = new ApiErrorResponse<>(
                HttpStatus.BAD_REQUEST,
                "Validation failed",
                errors
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolations(ConstraintViolationException ex) {
        ArrayList<String> errors = new ArrayList<>();
        ex.getConstraintViolations().forEach(violation ->
                errors.add(violation.getMessage())
        );

        ApiErrorResponse<Object> response = new ApiErrorResponse<>(
                HttpStatus.BAD_REQUEST,
                "Validation failed",
                errors
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}
