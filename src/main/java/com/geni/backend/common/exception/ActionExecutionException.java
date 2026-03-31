package com.geni.backend.common.exception;


/**
 * Thrown by an ActionExecutor when the external API call fails.
 * Caught by the engine and handled according to the step's onError strategy.
 */
public class ActionExecutionException extends RuntimeException {

    private final boolean retryable;

    public ActionExecutionException(String message, boolean retryable) {
        super(message);
        this.retryable = retryable;
    }

    public ActionExecutionException(String message, Throwable cause, boolean retryable) {
        super(message, cause);
        this.retryable = retryable;
    }

    /**
     * true  = transient failure (network timeout, rate limit, 5xx) — worth retrying
     * false = permanent failure (invalid credentials, 4xx, bad input) — retry won't help
     */
    public boolean isRetryable() {
        return retryable;
    }
}
