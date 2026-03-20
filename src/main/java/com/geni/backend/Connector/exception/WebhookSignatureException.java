package com.geni.backend.Connector.exception;

public class WebhookSignatureException extends RuntimeException {
    public WebhookSignatureException(String message) {
        super(message);
    }
}
