package com.geni.backend.workflow.enums;

public enum WorkflowRunStatus {
    RUNNING,   // currently executing
    SUCCESS,   // all steps completed successfully
    FAILED,    // at least one step failed with onError = STOP
    PARTIAL    // some steps failed with onError = CONTINUE, others succeeded
}