package com.geni.backend.workflow.core;

import com.geni.backend.workflow.enums.WorkflowStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UpdateStatusRequest {

    @NotNull
    WorkflowStatus status;
}

