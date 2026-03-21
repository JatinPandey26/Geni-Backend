package com.geni.backend.workflow.core;


import jakarta.persistence.*;

@Embeddable
public class RetryConfig {

    @Column(name = "retry_max_attempts")
    private Integer maxAttempts = 3;

    @Column(name = "retry_delay_ms")
    private Long delayMs = 2000L;

    @Enumerated(EnumType.STRING)
    @Column(name = "retry_backoff")
    private BackoffStrategy backoff = BackoffStrategy.FIXED;

    public enum BackoffStrategy {
        FIXED,        // same delay every attempt
        EXPONENTIAL   // delay doubles each attempt: delayMs, 2×, 4×, 8×…
    }

    public Integer getMaxAttempts()               { return maxAttempts; }
    public void setMaxAttempts(Integer n)         { this.maxAttempts = n; }
    public Long getDelayMs()                      { return delayMs; }
    public void setDelayMs(Long ms)               { this.delayMs = ms; }
    public BackoffStrategy getBackoff()           { return backoff; }
    public void setBackoff(BackoffStrategy b)     { this.backoff = b; }
}

