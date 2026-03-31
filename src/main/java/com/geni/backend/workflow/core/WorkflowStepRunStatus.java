package com.geni.backend.workflow.core;

public enum WorkflowStepRunStatus {
    RUNNING,  // currently executing
    SUCCESS,  // executor returned successfully
    FAILED,   // executor threw or returned error
    SKIPPED   // edge condition evaluated to false — step was not executed
}
