package com.geni.backend.workflow.validation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Holds available fields during workflow validation.
 * Used to check if expressions reference valid trigger fields or step outputs.
 */
public class AvailableContext {

    /** Fields available from the trigger */
    Set<String> triggerFields = new HashSet<>();

    /** Step outputs: stepId -> output fields */
    Map<String, Set<String>> stepOutputs = new HashMap<>();
}