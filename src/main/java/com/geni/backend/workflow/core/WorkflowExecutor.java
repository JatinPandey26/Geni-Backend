package com.geni.backend.workflow.core;

import com.geni.backend.common.TriggerPayload;
import com.geni.backend.common.exception.ActionExecutionException;
import com.geni.backend.integration.Integration;
import com.geni.backend.integration.Service.IntegrationService;
import com.geni.backend.trigger.core.TriggerSource;
import com.geni.backend.workflow.enums.OnErrorStrategy;
import com.geni.backend.workflow.enums.WorkflowRunStatus;
import com.geni.backend.workflow.repository.WorkflowRunRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.lang.Thread.sleep;

@Component
@RequiredArgsConstructor
@Slf4j
public class WorkflowExecutor {

    private final ObjectMapper objectMapper;
    private final FieldMappingResolver fieldMappingResolver;
    private final ActionHandlerRegistry actionHandlerRegistry;
    private final IntegrationService integrationService;
    private final ConditionEvaluator conditionEvaluator;
    private final WorkflowRunRepository runRepo;

    public WorkflowRun execute(WorkflowDefinition workflow, Map<String,Object> triggerPayloadMap) {

        log.info("Starting workflow execution: {}", workflow.getName());
        WorkflowRun run = createRun(workflow, triggerPayloadMap, TriggerSource.WEBHOOK, null, null);

        ExecutionContext context = new ExecutionContext(triggerPayloadMap);

        log.debug("Initial trigger payload: {} fields", triggerPayloadMap.size());
        log.debug("Initial execution context: {}" , context);

        runTree(workflow, run, context);
        return finalizeRun(run);
    }

    public void execute(WorkflowDefinition workflow, TriggerPayload triggerPayload) {
        Map <String, Object> triggerPayloadMap = parseToMap(triggerPayload);
        execute(workflow, triggerPayloadMap);
    }

    public void execute(List<WorkflowDefinition> workflows, TriggerPayload triggerPayload) {
        Map <String, Object> triggerPayloadMap = parseToMap(triggerPayload);
        for (WorkflowDefinition workflow : workflows) {
            execute(workflow, triggerPayloadMap);
        }
    }

    /**
     * Finds all root steps and executes each one.
     * Root steps run sequentially in this implementation.
     * For true parallelism in v2: wrap in CompletableFuture.allOf().
     */
    private void runTree(WorkflowDefinition wf, WorkflowRun run,
                         ExecutionContext context) {
        // build step lookup: clientId → WorkflowStep
        Map<UUID, WorkflowStep> stepMap = wf.getSteps().stream()
                .collect(Collectors.toMap(WorkflowStep::getId, s -> s));

        // build children index: parentClientId → [children]
        Map<UUID, List<WorkflowStep>> childrenIndex = buildChildrenIndex(wf.getSteps());

        // find roots — steps with no parent
        List<WorkflowStep> roots = wf.getSteps().stream()
                .filter(s -> s.getParentStepId() == null)
                .toList();

        log.info("Executing {} root steps for run {}", roots.size(), run.getId());

        for (WorkflowStep root : roots) {
            executeStep(root, run, context, childrenIndex);
        }
    }

    private void executeStep(WorkflowStep step,
                             WorkflowRun run,
                             ExecutionContext context,
                             Map<UUID, List<WorkflowStep>> childrenIndex) {

        log.info("Executing step '{}' ({})", step.getName(), step.getId());

        // 1. Create step run record — saved as part of run
        WorkflowStepRun stepRun = new WorkflowStepRun();
        stepRun.setStepId(step.getId());
        stepRun.setStepName(step.getName());
        stepRun.setActionDefinitionId(step.getActionDefinitionId());
        stepRun.setStatus(WorkflowStepRunStatus.RUNNING);
        stepRun.setStartedAt(Instant.now());
        run.addStepRun(stepRun);

        //2. Resolve inputs
        Map<String, Object> resolvedInputs;
        if (context.hasOverride(step.getId())) {
            resolvedInputs = context.getOverride(step.getId());
            stepRun.setInputOverridden(true);
            log.debug("Using overridden inputs for step '{}'", step.getName());
        } else {
            resolvedInputs = fieldMappingResolver.resolve(step.getFieldMappings(), context);
        }
        stepRun.setResolvedInputs(resolvedInputs);

        // 3. Execute with retry support
        Map<String, Object> output = executeWithRetry(step, stepRun, resolvedInputs,context);
        runRepo.save(run); // persist step run status and output before processing children

        // 4. Handle result
        if (output != null) {
            context.addStepOutput(step.getId(), output);
            log.info("Step '{}' succeeded", step.getName());

            // 5. Find and evaluate children
            List<WorkflowStep> children = childrenIndex.getOrDefault(
                    step.getId(), List.of());

            for (WorkflowStep child : children) {
                boolean conditionPasses = conditionEvaluator.evaluate(
                        child.getEdgeCondition(), context);

                if (conditionPasses) {
                    log.debug("Edge condition PASSED for child '{}' — executing",
                            child.getName());
                    executeStep(child, run, context, childrenIndex);
                } else {
                    log.debug("Edge condition FAILED for child '{}' — skipping",
                            child.getName());
                    // record skipped step
                    WorkflowStepRun skippedRun = new WorkflowStepRun();
                    skippedRun.setStepId(child.getId());
                    skippedRun.setStepName(child.getName());
                    skippedRun.setActionDefinitionId(child.getActionDefinitionId());
                    skippedRun.setStartedAt(Instant.now());
                    skippedRun.markSkipped();
                    run.addStepRun(skippedRun);
                    runRepo.save(run); // persist skipped status
                }
            }

        } else {
            // FAILED — onError was STOP, already marked in executeWithRetry
            // do not process children — branch terminates here
            log.info("Step '{}' failed — branch terminated", step.getName());
        }
    }

    /**
     * Executes the action with retry support.
     *
     * Returns output map on success.
     * Returns null if step failed with STOP strategy (branch should terminate).
     * Throws nothing — all errors are handled and recorded on stepRun.
     */
    private Map<String, Object> executeWithRetry(WorkflowStep step,
                                                 WorkflowStepRun stepRun,
                                                 Map<String, Object> resolvedInputs,ExecutionContext context) {

        ActionHandler actionHandler = actionHandlerRegistry.findOrThrow(step.getActionDefinitionId());
        Integration integration = resolveIntegration(step);

        int maxAttempts = 1;
        long delayMs    = 2000L;
        boolean exponential = false;

        if (step.getOnError() == OnErrorStrategy.RETRY && step.getRetryConfig() != null) {
            maxAttempts  = step.getRetryConfig().getMaxAttempts();
            delayMs      = step.getRetryConfig().getDelayMs();
            exponential  = step.getRetryConfig().getBackoff()
                    == RetryConfig.BackoffStrategy.EXPONENTIAL;
        }

        Exception lastException = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            stepRun.setAttemptCount(attempt);

            try {
                Map<String, Object> output = actionHandler.execute(resolvedInputs, context, integration);
                stepRun.markSuccess(output);
                return output; // success
            }
            catch (ActionExecutionException e) {
                lastException = e;
                log.warn("Step '{}' attempt {}/{} failed: {}",
                        step.getName(), attempt, maxAttempts, e.getMessage());

                // non-retryable error — stop immediately regardless of strategy
                if (!e.isRetryable()) {
                    break;
                }

                // more attempts remaining — sleep before next attempt
                if (attempt < maxAttempts) {
                    long sleep = exponential
                            ? delayMs * (long) Math.pow(2, attempt - 1)
                            : delayMs;
                    log.debug("Retrying step '{}' in {}ms", step.getName(), sleep);
                    try {
                        sleep(sleep);
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                }

            } catch (Exception e) {
                lastException = e;
                log.warn("Step '{}' attempt {}/{} unexpected error: {}",
                        step.getName(), attempt, maxAttempts, e.getMessage());
                break; // unexpected errors don't retry
            }
        }

        // all attempts exhausted — handle based on onError strategy
        String errorMessage = lastException != null
                ? lastException.getMessage() : "Unknown error";

        return switch (step.getOnError()) {
            case STOP -> {
                stepRun.markFailed(errorMessage);
                yield null; // signals branch termination to caller
            }
            case CONTINUE -> {
                stepRun.markFailed(errorMessage);
                log.info("Step '{}' failed but onError=CONTINUE — proceeding", step.getName());
                yield Map.of(); // empty output — downstream refs resolve to empty string
            }
            case RETRY -> {
                // exhausted all retries — treat as STOP
                stepRun.markFailed("Exhausted " + maxAttempts + " retries. Last error: " + errorMessage);
                yield null;
            }
        };

    }

    private Map<UUID, List<WorkflowStep>> buildChildrenIndex(List<WorkflowStep> steps) {
        Map<UUID, List<WorkflowStep>> index = new HashMap<>();
        for (WorkflowStep step : steps) {
            if (step.getParentStepId() != null) {
                index.computeIfAbsent(step.getParentStepId(), k -> new ArrayList<>())
                        .add(step);
            }
        }
        return index;
    }

    private Integration resolveIntegration(WorkflowStep step) {
        if (step.getIntegrationId() == null) return null;
        return integrationService.fetchIntegration(step.getIntegrationId());
    }

    private WorkflowRun createRun(WorkflowDefinition wf,
                                  Map<String, Object> triggerPayload,
                                  TriggerSource source,
                                  UUID parentRunId,
                                  UUID rerunFromStepId) {
        WorkflowRun run = new WorkflowRun();
        run.setWorkflowDefinition(wf);
        run.setTriggerPayload(triggerPayload);
        run.setTriggeredBy(source);
        run.setParentRunId(parentRunId);
        run.setRerunFromStepId(rerunFromStepId);
        run.setStatus(WorkflowRunStatus.RUNNING);
        // save immediately — run exists in DB even if execution crashes mid-way
        return runRepo.save(run);
    }

    private WorkflowRun finalizeRun(WorkflowRun run) {
        if (run.hasFailedSteps()) {
            boolean anySuccess = run.getStepRuns().stream()
                    .anyMatch(s -> s.getStatus() == WorkflowStepRunStatus.SUCCESS);
            if (anySuccess) {
                run.markPartial();
            } else {
                run.markFailed();
            }
        } else {
            run.markSuccess();
        }
        WorkflowRun saved = runRepo.save(run);
        log.info("Run {} finalized with status={}", saved.getId(), saved.getStatus());
        return saved;
    }



    private Map<String, Object> parseToMap(TriggerPayload triggerPayload) {
        try {
            return objectMapper.convertValue(triggerPayload, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse trigger payload", e);
        }
    }

}
