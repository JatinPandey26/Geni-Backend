package com.geni.backend.common.exception;

public class WorkflowValidationException extends RuntimeException {
    public WorkflowValidationException(String message) {
        super(message);
    }
}
