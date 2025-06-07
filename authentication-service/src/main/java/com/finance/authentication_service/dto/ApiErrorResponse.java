package com.finance.authentication_service.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.util.Map;

@Data
@NoArgsConstructor
public class ApiErrorResponse<T> {
    private HttpStatus status;
    private String message;
    private Object errors;

    public ApiErrorResponse(HttpStatus status, String message, String errors){
        this.status = status;
        this.message = message;
        this.errors = errors;
    }

    public ApiErrorResponse(HttpStatus status, String message, Map<String,String> errors){
        this.status = status;
        this.message = message;
        this.errors = errors;
    }
}
