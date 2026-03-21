package com.geni.backend.workflow.enums;

public enum OnErrorStrategy {
    STOP,     // abort run, mark WorkflowRun as FAILED
    CONTINUE, // log error, proceed to next step
    RETRY     // retry according to RetryConfig, then STOP if exhausted
}
