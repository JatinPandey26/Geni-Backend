package com.geni.backend.trigger.impl.github.triggers;


import com.geni.backend.Connector.impl.github.payload.GithubWebhookEvent;
import com.geni.backend.Connector.impl.github.payload.GithubWebhookPayload;
import com.geni.backend.trigger.core.TriggerType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Resolves a parsed GithubWebhookPayload into a TriggerType.
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
    public Optional<TriggerType> resolve(GithubWebhookEvent event,
                                         GithubWebhookPayload payload,
                                         String deliveryId) {
        return switch (event) {
            case ISSUE_OPENED -> Optional.of(TriggerType.GITHUB_ISSUE_OPENED);
            case ISSUE_REOPENED -> Optional.of(TriggerType.GITHUB_ISSUE_REOPENED);
            case ISSUE_ASSIGNED -> Optional.of(TriggerType.GITHUB_ISSUE_ASSIGNED);
            case PULL_REQUEST_OPENED -> Optional.of(TriggerType.GITHUB_PR_OPENED);
            case PULL_REQUEST_MERGED -> Optional.of(TriggerType.GITHUB_PR_MERGED);
            case PULL_REQUEST_REVIEW_REQUESTED -> Optional.of(TriggerType.GITHUB_REVIEW_REQUESTED);
            case PULL_REQUEST_REVIEW_SUBMITTED -> Optional.of(TriggerType.GITHUB_REVIEW_SUBMITTED);
            case PULL_REQUEST_REVIEW_COMMENT -> Optional.of(TriggerType.GITHUB_PR_COMMENT_ADDED);
            case ISSUE_COMMENT -> Optional.of(TriggerType.GITHUB_PR_COMMENT_ADDED); // Assuming issue_comment on PR is handled here
            case PUSH -> Optional.of(TriggerType.GITHUB_PUSH);
            default -> {
                log.debug("No TriggerDefinition mapped for GitHub event: {}", event);
                yield Optional.empty();
            }
        };
    }
}
