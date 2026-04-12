package com.geni.backend.workflow.core;

import com.geni.backend.integration.Integration;
import lombok.Data;

import java.util.Map;

@Data
public abstract class ActionHandler {

    private volatile ActionDefinition definition;

    public abstract ActionDefinition buildDefinition();

    protected abstract ActionType type();

    public ActionDefinition definition(){
        synchronized (this) {
            if (definition == null) {
                definition = buildDefinition();
            }
        }
        return definition;
    }

    public abstract Map<String,Object> execute(Map<String,Object> inputs, ExecutionContext context , Integration integration);

    protected abstract void validateInputs(Map<String, Object> inputs);

}
