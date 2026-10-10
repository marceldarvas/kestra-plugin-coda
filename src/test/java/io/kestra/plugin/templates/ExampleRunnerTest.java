package io.kestra.plugin.templates;

import io.kestra.core.exceptions.InternalException;
import io.kestra.core.junit.annotations.ExecuteFlow;
import io.kestra.core.junit.annotations.KestraTest;
import io.kestra.core.models.executions.Execution;
import io.kestra.core.services.TaskOutputService;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

/**
 * This test will start Kestra, load the flow located in `src/test/resources/flows/example.yaml`, and execute it.
 * The Kestra configuration file is in `src/test/resources/application.yml`, it configures the in-memory backend for tests.
 * Since Kestra 2.0, task outputs are no longer stored on the execution: read them through {@link TaskOutputService}.
 */
@KestraTest(startRunner = true) // This annotation starts an embedded Kestra for tests
class ExampleRunnerTest {
    @Inject
    private TaskOutputService taskOutputService;

    @Test
    @ExecuteFlow("flows/example.yaml")
    @SuppressWarnings("unchecked")
    void flow(Execution execution) throws InternalException {
        assertThat(execution.getTaskRunList(), hasSize(3));
        Map<String, Object> outputs = taskOutputService.getOutputs(execution.getTaskRunList().get(2));
        assertThat(((Map<String, Object>) outputs.get("child")).get("value"), is("task-id"));
    }
}
