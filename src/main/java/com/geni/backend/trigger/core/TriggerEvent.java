package com.geni.backend.trigger.core;

import com.geni.backend.common.Event;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TriggerEvent<T> implements Event {
    TriggerType triggerType;
    T payload;
}
