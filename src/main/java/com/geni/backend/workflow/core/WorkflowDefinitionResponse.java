package com.geni.backend.workflow.core;

import com.geni.backend.workflow.enums.OnErrorStrategy;
import com.geni.backend.workflow.enums.WorkflowStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Value;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class WorkflowDefinitionResponse {

    UUID   id;
    String name;
    String description;

    WorkflowStatus status;

    TriggerResponse        trigger;
    List<StepResponse>     steps;    // flat list — tree shape encoded via parentStepId on each step

    Instant createdAt;
    Instant updatedAt;

}
