package io.kestra.plugin.coda.rows;

import io.kestra.core.models.property.Property;
import io.kestra.core.runners.RunContext;
import io.kestra.core.runners.RunContextFactory;
import io.kestra.core.junit.annotations.KestraTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;

import jakarta.inject.Inject;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@KestraTest
class UpdateRowTest {
    @Inject
    private RunContextFactory runContextFactory;

    @Test
    @Disabled("Integration test - requires valid Coda API token, document ID, table ID, and row ID")
    void testUpdateSingleCell() throws Exception {
        RunContext runContext = runContextFactory.of();

        UpdateRow task = UpdateRow.builder()
            .apiToken(Property.of("YOUR_API_TOKEN"))
            .docId(Property.of("YOUR_DOC_ID"))
            .tableId(Property.of("YOUR_TABLE_ID"))
            .rowId(Property.of("YOUR_ROW_ID"))
            .cells(List.of(
                UpdateRow.CellInput.builder()
                    .column(Property.of("Status"))
                    .value("Completed")
                    .build()
            ))
            .build();

        UpdateRow.Output output = task.run(runContext);

        assertThat(output.getRowId(), is(notNullValue()));
        assertThat(output.getRequestId(), is(notNullValue()));
    }

    @Test
    @Disabled("Integration test - requires valid Coda API token, document ID, table ID, and row ID")
    void testUpdateMultipleCells() throws Exception {
        RunContext runContext = runContextFactory.of();

        UpdateRow task = UpdateRow.builder()
            .apiToken(Property.of("YOUR_API_TOKEN"))
            .docId(Property.of("YOUR_DOC_ID"))
            .tableId(Property.of("YOUR_TABLE_ID"))
            .rowId(Property.of("YOUR_ROW_ID"))
            .cells(List.of(
                UpdateRow.CellInput.builder()
                    .column(Property.of("Task Name"))
                    .value("Updated Task Name")
                    .build(),
                UpdateRow.CellInput.builder()
                    .column(Property.of("Status"))
                    .value("Completed")
                    .build(),
                UpdateRow.CellInput.builder()
                    .column(Property.of("Priority"))
                    .value("High")
                    .build()
            ))
            .build();

        UpdateRow.Output output = task.run(runContext);

        assertThat(output.getRowId(), is(notNullValue()));
        assertThat(output.getRequestId(), is(notNullValue()));
    }

    @Test
    @Disabled("Integration test - requires valid Coda API token, document ID, table ID, and row ID")
    void testUpdateWithVariousDataTypes() throws Exception {
        RunContext runContext = runContextFactory.of();

        UpdateRow task = UpdateRow.builder()
            .apiToken(Property.of("YOUR_API_TOKEN"))
            .docId(Property.of("YOUR_DOC_ID"))
            .tableId(Property.of("YOUR_TABLE_ID"))
            .rowId(Property.of("YOUR_ROW_ID"))
            .cells(List.of(
                UpdateRow.CellInput.builder()
                    .column(Property.of("Name"))
                    .value("Updated Name")
                    .build(),
                UpdateRow.CellInput.builder()
                    .column(Property.of("Count"))
                    .value(42)
                    .build(),
                UpdateRow.CellInput.builder()
                    .column(Property.of("Active"))
                    .value(false)
                    .build()
            ))
            .build();

        UpdateRow.Output output = task.run(runContext);

        assertThat(output.getRowId(), is(notNullValue()));
        assertThat(output.getRequestId(), is(notNullValue()));
    }
}
