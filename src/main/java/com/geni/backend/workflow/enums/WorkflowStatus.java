package com.geni.backend.workflow.enums;

public enum WorkflowStatus {
    DRAFT,    // being built, never executed
    ACTIVE,   // live — poller / webhook will fire it
    PAUSED,   // user-paused, no executions
    ARCHIVED  // soft-deleted
}