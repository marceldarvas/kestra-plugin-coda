package io.kestra.plugin.coda.automations;

import io.kestra.core.models.property.Property;
import io.kestra.core.runners.RunContext;
import io.kestra.core.runners.RunContextFactory;
import io.kestra.core.junit.annotations.KestraTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;

import jakarta.inject.Inject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@KestraTest
class TriggerWebhookTest {
    @Inject
    private RunContextFactory runContextFactory;

    @Test
    @Disabled("Integration test - requires valid Coda API token and webhook URL")
    void testTriggerWebhookWithSimplePayload() throws Exception {
        RunContext runContext = runContextFactory.of();

        Map<String, Object> payload = new HashMap<>();
        payload.put("name", "John Doe");
        payload.put("status", "Active");
        payload.put("priority", "High");

        TriggerWebhook task = TriggerWebhook.builder()
            .apiToken(Property.of("YOUR_API_TOKEN"))
            .webhookUrl(Property.of("https://coda.io/apis/v1/webhooks/YOUR_WEBHOOK_ID"))
            .payload(Property.of(payload))
            .build();

        TriggerWebhook.TriggerWebhookOutput output = task.run(runContext);

        assertThat(output.getStatusCode(), is(202));
        assertThat(output.getResponseBody(), is(notNullValue()));
    }

    @Test
    @Disabled("Integration test - requires valid Coda API token and webhook URL")
    void testTriggerWebhookWithNumericAndBooleanValues() throws Exception {
        RunContext runContext = runContextFactory.of();

        Map<String, Object> payload = new HashMap<>();
        payload.put("taskName", "Complete Documentation");
        payload.put("daysRemaining", 5);
        payload.put("isUrgent", true);
        payload.put("progress", 75.5);

        TriggerWebhook task = TriggerWebhook.builder()
            .apiToken(Property.of("YOUR_API_TOKEN"))
            .webhookUrl(Property.of("https://coda.io/apis/v1/webhooks/YOUR_WEBHOOK_ID"))
            .payload(Property.of(payload))
            .build();

        TriggerWebhook.TriggerWebhookOutput output = task.run(runContext);

        assertThat(output.getStatusCode(), is(202));
        assertThat(output.getResponseBody(), is(notNullValue()));
    }

    @Test
    @Disabled("Integration test - requires valid Coda API token and webhook URL")
    void testTriggerWebhookWithArrayData() throws Exception {
        RunContext runContext = runContextFactory.of();

        Map<String, Object> item1 = new HashMap<>();
        item1.put("name", "Alice");
        item1.put("score", 95);

        Map<String, Object> item2 = new HashMap<>();
        item2.put("name", "Bob");
        item2.put("score", 87);

        Map<String, Object> payload = new HashMap<>();
        payload.put("eventType", "batch_update");
        payload.put("items", List.of(item1, item2));

        TriggerWebhook task = TriggerWebhook.builder()
            .apiToken(Property.of("YOUR_API_TOKEN"))
            .webhookUrl(Property.of("https://coda.io/apis/v1/webhooks/YOUR_WEBHOOK_ID"))
            .payload(Property.of(payload))
            .build();

        TriggerWebhook.TriggerWebhookOutput output = task.run(runContext);

        assertThat(output.getStatusCode(), is(202));
        assertThat(output.getResponseBody(), is(notNullValue()));
    }

    @Test
    @Disabled("Integration test - requires valid Coda API token and webhook URL")
    void testTriggerWebhookWithNestedData() throws Exception {
        RunContext runContext = runContextFactory.of();

        Map<String, Object> customer = new HashMap<>();
        customer.put("name", "John Doe");
        customer.put("email", "john@example.com");

        Map<String, Object> order = new HashMap<>();
        order.put("id", "ORD-12345");
        order.put("customer", customer);
        order.put("total", 99.99);
        order.put("status", "pending");

        Map<String, Object> payload = new HashMap<>();
        payload.put("order", order);

        TriggerWebhook task = TriggerWebhook.builder()
            .apiToken(Property.of("YOUR_API_TOKEN"))
            .webhookUrl(Property.of("https://coda.io/apis/v1/webhooks/YOUR_WEBHOOK_ID"))
            .payload(Property.of(payload))
            .build();

        TriggerWebhook.TriggerWebhookOutput output = task.run(runContext);

        assertThat(output.getStatusCode(), is(202));
        assertThat(output.getResponseBody(), is(notNullValue()));
    }

    @Test
    @Disabled("Integration test - requires valid Coda API token and webhook URL")
    void testTriggerWebhookWithCustomTimeout() throws Exception {
        RunContext runContext = runContextFactory.of();

        Map<String, Object> payload = new HashMap<>();
        payload.put("message", "Test with custom timeout");

        TriggerWebhook task = TriggerWebhook.builder()
            .apiToken(Property.of("YOUR_API_TOKEN"))
            .webhookUrl(Property.of("https://coda.io/apis/v1/webhooks/YOUR_WEBHOOK_ID"))
            .payload(Property.of(payload))
            .connectionTimeout(Property.of(60))
            .readTimeout(Property.of(60))
            .build();

        TriggerWebhook.TriggerWebhookOutput output = task.run(runContext);

        assertThat(output.getStatusCode(), is(202));
        assertThat(output.getResponseBody(), is(notNullValue()));
    }

    @Test
    @Disabled("Integration test - requires valid Coda API token and webhook URL")
    void testTriggerWebhookWithDynamicValues() throws Exception {
        RunContext runContext = runContextFactory.of();

        Map<String, Object> payload = new HashMap<>();
        payload.put("timestamp", "{{ now() }}");
        payload.put("executionId", "{{ execution.id }}");
        payload.put("taskId", "{{ task.id }}");

        TriggerWebhook task = TriggerWebhook.builder()
            .apiToken(Property.of("YOUR_API_TOKEN"))
            .webhookUrl(Property.of("https://coda.io/apis/v1/webhooks/YOUR_WEBHOOK_ID"))
            .payload(Property.of(payload))
            .build();

        TriggerWebhook.TriggerWebhookOutput output = task.run(runContext);

        assertThat(output.getStatusCode(), is(202));
        assertThat(output.getResponseBody(), is(notNullValue()));
    }
}
