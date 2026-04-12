package com.geni.backend;

import com.geni.backend.workflow.core.ExecutionContext;
import com.geni.backend.workflow.core.FieldMappingResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class FieldMappingResolverTest {

    private FieldMappingResolver resolver;
    private ExecutionContext context;

    private static final UUID STEP_A = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID STEP_B = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

    @BeforeEach
    void setUp() {
        resolver = new FieldMappingResolver();

        // flat-key trigger payload (matching GitHub trigger payload shape)
        Map<String, Object> triggerPayload = Map.of(
                "issue.title",   "Bug: crash on login",
                "issue.number",  "42",
                "sender.login",  "jatin",
                "repo.fullName", "JatinPersonal26/feb"
        );

        context = new ExecutionContext(triggerPayload);

        // step A output
        context.addStepOutput(STEP_A.toString(), Map.of(
                "summary", "Short summary of the issue",
                "tags",    "critical,urgent"
        ));
    }

    // ── Literal passthrough ───────────────────────────────────────────────────

    @Test
    void literal_passthrough() {
        assertThat(resolver.resolveExpression("#inbox-summaries", context))
                .isEqualTo("#inbox-summaries");
    }

    @Test
    void empty_string_passthrough() {
        assertThat(resolver.resolveExpression("", context)).isEmpty();
    }

    @Test
    void null_returns_empty() {
        assertThat(resolver.resolveExpression(null, context)).isEmpty();
    }

    // ── Trigger references ────────────────────────────────────────────────────

    @Test
    void simple_trigger_ref() {
        assertThat(resolver.resolveExpression("{{trigger.issue.title}}", context))
                .isEqualTo("Bug: crash on login");
    }

    @Test
    void trigger_ref_in_mixed_string() {
        String result = resolver.resolveExpression(
                "Issue #{{trigger.issue.number}} by {{trigger.sender.login}}", context);
        assertThat(result).isEqualTo("Issue #42 by jatin");
    }

    @Test
    void missing_trigger_field_returns_empty() {
        assertThat(resolver.resolveExpression("{{trigger.nonexistent}}", context))
                .isEmpty();
    }

    // ── Step output references ────────────────────────────────────────────────

    @Test
    void step_output_ref() {
        String template = "{{steps." + STEP_A + ".output.summary}}";
        assertThat(resolver.resolveExpression(template, context))
                .isEqualTo("Short summary of the issue");
    }

    @Test
    void step_output_in_mixed_string() {
        String template = "From {{trigger.sender.login}}: {{steps." + STEP_A + ".output.summary}}";
        assertThat(resolver.resolveExpression(template, context))
                .isEqualTo("From jatin: Short summary of the issue");
    }

    @Test
    void step_with_no_output_returns_empty() {
        String template = "{{steps." + STEP_B + ".output.anything}}";
        assertThat(resolver.resolveExpression(template, context)).isEmpty();
    }

    @Test
    void missing_step_output_field_returns_empty() {
        String template = "{{steps." + STEP_A + ".output.nonexistent}}";
        assertThat(resolver.resolveExpression(template, context)).isEmpty();
    }

    // ── Full resolve() map ────────────────────────────────────────────────────

    @Test
    void resolve_full_mapping() {
        Map<String, Object> fieldMappings = Map.of(
                "to",      "jatin@gmail.com",
                "subject", "Bug: {{trigger.issue.title}}",
                "body",    "Reported by {{trigger.sender.login}}\n{{steps." + STEP_A + ".output.summary}}"
        );

        Map<String, Object> result = resolver.resolve(fieldMappings, context);

        assertThat(result.get("to")).isEqualTo("jatin@gmail.com");
        assertThat(result.get("subject")).isEqualTo("Bug: Bug: crash on login");
        assertThat(result.get("body")).isEqualTo(
                "Reported by jatin\nShort summary of the issue");
    }

    @Test
    void resolve_null_mappings_returns_empty_map() {
        assertThat(resolver.resolve(null, context)).isEmpty();
    }

    // ── Edge cases ────────────────────────────────────────────────────────────

    @Test
    void malformed_uuid_in_step_ref_returns_empty() {
        assertThat(resolver.resolveExpression("{{steps.not-a-uuid.output.field}}", context))
                .isEmpty();
    }

    @Test
    void multiple_expressions_same_template() {
        String template = "{{trigger.issue.title}} — {{trigger.issue.number}} — {{trigger.sender.login}}";
        assertThat(resolver.resolveExpression(template, context))
                .isEqualTo("Bug: crash on login — 42 — jatin");
    }
}