package com.geni.backend.common;

import com.geni.backend.Connector.exception.CredentialValidationException;
import com.geni.backend.Connector.exception.UnknownConnectorException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UnknownConnectorException.class)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ApiResponse<Void> handle(UnknownConnectorException ex) {
        return ApiResponse.error(400, ex.getMessage());
    }

//    @ExceptionHandler(NotFoundException.class)
//    @ResponseStatus(HttpStatus.NOT_FOUND)
//    public ApiResponse<Void> handle(NotFoundException ex) {
//        return ApiResponse.error(404, ex.getMessage());
//    }

    @ExceptionHandler(CredentialValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handle(CredentialValidationException ex) {
        return ApiResponse.error(400, String.join(", ", ex.getErrors()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handle(IllegalArgumentException ex) {
        return ApiResponse.error(400, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handle(Exception ex) {
        return ApiResponse.error(500, "Internal server error: " + ex.getMessage());
    }
}