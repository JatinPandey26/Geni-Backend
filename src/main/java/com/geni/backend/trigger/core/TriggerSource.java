package com.geni.backend.trigger.core;

public enum TriggerSource {
    WEBHOOK,  // fired by an incoming webhook (GitHub, Gmail Pub/Sub etc.)
    MANUAL,   // user clicked "run now" in the UI
    RERUN     // user triggered a rerun of a previous WorkflowRun
}
