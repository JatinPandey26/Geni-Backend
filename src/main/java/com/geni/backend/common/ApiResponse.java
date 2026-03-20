package com.geni.backend.common;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

// ApiResponse.java
@Value
@Builder
public class ApiResponse<T> {

    boolean success;
    T       data;
    String  error;
    int     statusCode;
    Instant timestamp;

    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .statusCode(200)
                .timestamp(Instant.now())
                .build();
    }

    public static <T> ApiResponse<T> created(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .statusCode(201)
                .timestamp(Instant.now())
                .build();
    }

    public static ApiResponse<Void> noContent() {
        return ApiResponse.<Void>builder()
                .success(true)
                .statusCode(204)
                .timestamp(Instant.now())
                .build();
    }

    public static ApiResponse<Void> error(int statusCode, String message) {
        return ApiResponse.<Void>builder()
                .success(false)
                .error(message)
                .statusCode(statusCode)
                .timestamp(Instant.now())
                .build();
    }
}
