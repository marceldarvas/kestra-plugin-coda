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
class InsertRowsTest {
    @Inject
    private RunContextFactory runContextFactory;

    @Test
    @Disabled("Integration test - requires valid Coda API token, document ID, and table ID")
    void testInsertSingleRow() throws Exception {
        RunContext runContext = runContextFactory.of();

        InsertRows task = InsertRows.builder()
            .apiToken(Property.of("YOUR_API_TOKEN"))
            .docId(Property.of("YOUR_DOC_ID"))
            .tableId(Property.of("YOUR_TABLE_ID"))
            .rows(List.of(
                InsertRows.RowInput.builder()
                    .cells(List.of(
                        InsertRows.CellInput.builder()
                            .column(Property.of("Task Name"))
                            .value("Test Task")
                            .build(),
                        InsertRows.CellInput.builder()
                            .column(Property.of("Status"))
                            .value("To Do")
                            .build()
                    ))
                    .build()
            ))
            .build();

        InsertRows.Output output = task.run(runContext);

        assertThat(output.getAddedRowIds(), is(notNullValue()));
        assertThat(output.getAddedRowIds(), hasSize(1));
        assertThat(output.getRowCount(), is(1));
        assertThat(output.getRequestId(), is(notNullValue()));
    }

    @Test
    @Disabled("Integration test - requires valid Coda API token, document ID, and table ID")
    void testInsertMultipleRows() throws Exception {
        RunContext runContext = runContextFactory.of();

        InsertRows task = InsertRows.builder()
            .apiToken(Property.of("YOUR_API_TOKEN"))
            .docId(Property.of("YOUR_DOC_ID"))
            .tableId(Property.of("YOUR_TABLE_ID"))
            .rows(List.of(
                InsertRows.RowInput.builder()
                    .cells(List.of(
                        InsertRows.CellInput.builder()
                            .column(Property.of("Task Name"))
                            .value("First Task")
                            .build(),
                        InsertRows.CellInput.builder()
                            .column(Property.of("Status"))
                            .value("To Do")
                            .build()
                    ))
                    .build(),
                InsertRows.RowInput.builder()
                    .cells(List.of(
                        InsertRows.CellInput.builder()
                            .column(Property.of("Task Name"))
                            .value("Second Task")
                            .build(),
                        InsertRows.CellInput.builder()
                            .column(Property.of("Status"))
                            .value("In Progress")
                            .build()
                    ))
                    .build()
            ))
            .build();

        InsertRows.Output output = task.run(runContext);

        assertThat(output.getAddedRowIds(), is(notNullValue()));
        assertThat(output.getAddedRowIds(), hasSize(2));
        assertThat(output.getRowCount(), is(2));
        assertThat(output.getRequestId(), is(notNullValue()));
    }

    @Test
    @Disabled("Integration test - requires valid Coda API token, document ID, and table ID")
    void testInsertRowWithVariousDataTypes() throws Exception {
        RunContext runContext = runContextFactory.of();

        InsertRows task = InsertRows.builder()
            .apiToken(Property.of("YOUR_API_TOKEN"))
            .docId(Property.of("YOUR_DOC_ID"))
            .tableId(Property.of("YOUR_TABLE_ID"))
            .rows(List.of(
                InsertRows.RowInput.builder()
                    .cells(List.of(
                        InsertRows.CellInput.builder()
                            .column(Property.of("Name"))
                            .value("Test User")
                            .build(),
                        InsertRows.CellInput.builder()
                            .column(Property.of("Age"))
                            .value(30)
                            .build(),
                        InsertRows.CellInput.builder()
                            .column(Property.of("Active"))
                            .value(true)
                            .build(),
                        InsertRows.CellInput.builder()
                            .column(Property.of("Created"))
                            .value("2024-01-15")
                            .build()
                    ))
                    .build()
            ))
            .build();

        InsertRows.Output output = task.run(runContext);

        assertThat(output.getAddedRowIds(), is(notNullValue()));
        assertThat(output.getAddedRowIds(), hasSize(1));
        assertThat(output.getRequestId(), is(notNullValue()));
    }
}
