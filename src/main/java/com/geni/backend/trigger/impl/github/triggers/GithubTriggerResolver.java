package com.geni.backend.trigger.impl.github.triggers;


import com.geni.backend.Connector.impl.github.GithubWebhookEvent;
import com.geni.backend.Connector.impl.github.GithubWebhookPayload;
import com.geni.backend.trigger.core.TriggerEventType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Resolves a parsed GithubWebhookPayload into a TriggerEventType.
 *
 * Returns Optional.empty() when the event+action combination has
 * no registered TriggerDefinition — caller silently ignores it.
 */
@Slf4j
@Component
public class GithubTriggerResolver {

    /**
     * Called from GithubService.handleEvent() after signature verification
     * and payload parsing are already done.
     */
    public Optional<TriggerEventType> resolve(GithubWebhookEvent event,
                                              GithubWebhookPayload payload,
                                              String deliveryId) {
        return switch (event) {
            case ISSUES -> resolveIssuesEvent(payload, deliveryId);
            default -> {
                log.debug("No TriggerDefinition mapped for GitHub event: {}", event);
                yield Optional.empty();
            }
        };
    }

    // ── Issues ────────────────────────────────────────────────────────────────

    private Optional<TriggerEventType> resolveIssuesEvent(GithubWebhookPayload payload,
                                                          String deliveryId) {
        return switch (payload.getAction()) {
            case "opened" -> Optional.of(TriggerEventType.GITHUB_ISSUE_OPENED);
            default -> {
                log.debug("No trigger mapped for issues action: {}", payload.getAction());
                yield Optional.empty();
            }
        };
    }
}
