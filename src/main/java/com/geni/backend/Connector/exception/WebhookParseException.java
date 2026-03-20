package com.geni.backend.Connector.exception;

public class WebhookParseException extends RuntimeException {
  public WebhookParseException(String message,Exception e) {
    super(message , e);
  }

  public WebhookParseException(String message) {
    super(message);
  }
}
