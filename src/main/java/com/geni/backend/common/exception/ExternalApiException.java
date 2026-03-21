package com.geni.backend.common.exception;

import java.io.IOException;

// ExternalApiException.java
public class ExternalApiException extends RuntimeException {
    private final String provider;
    private final int    statusCode;

    public ExternalApiException(String provider, int statusCode, String body) {
        super(provider + " API error " + statusCode + ": " + body);
        this.provider   = provider;
        this.statusCode = statusCode;
    }

    public ExternalApiException(String provider, IOException cause) {
        super(provider + " API connection failed", cause);
        this.provider   = provider;
        this.statusCode = -1;
    }

    public boolean isUnauthorized() { return statusCode == 401; }
    public boolean isRateLimited()  { return statusCode == 429; }
}
