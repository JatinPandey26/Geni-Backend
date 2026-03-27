package com.geni.backend.trigger.core;

import com.geni.backend.common.Event;
import lombok.Data;

@Data
public class TriggerEvent<T> implements Event {
    T payload;
}
