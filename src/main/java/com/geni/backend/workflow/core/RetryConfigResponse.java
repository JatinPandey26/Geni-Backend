package com.geni.backend.workflow.core;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Value;

@Data
@Builder
@AllArgsConstructor
public class RetryConfigResponse {
    public int                          maxAttempts;
    public long                         delayMs;
    public RetryConfig.BackoffStrategy  backoff;
}