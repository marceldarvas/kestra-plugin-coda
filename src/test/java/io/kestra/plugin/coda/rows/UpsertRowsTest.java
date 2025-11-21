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
class UpsertRowsTest {
    @Inject
    private RunContextFactory runContextFactory;

    @Test
    @Disabled("Integration test - requires valid Coda API token, document ID, and table ID")
    void testUpsertSingleRowWithSingleKeyColumn() throws Exception {
        RunContext runContext = runContextFactory.of();

        UpsertRows task = UpsertRows.builder()
            .apiToken(Property.of("YOUR_API_TOKEN"))
            .docId(Property.of("YOUR_DOC_ID"))
            .tableId(Property.of("YOUR_TABLE_ID"))
            .keyColumns(List.of(Property.of("Task Name")))
            .rows(List.of(
                UpsertRows.RowInput.builder()
                    .cells(List.of(
                        UpsertRows.CellInput.builder()
                            .column(Property.of("Task Name"))
                            .value("Unique Task")
                            .build(),
                        UpsertRows.CellInput.builder()
                            .column(Property.of("Status"))
                            .value("In Progress")
                            .build(),
                        UpsertRows.CellInput.builder()
                            .column(Property.of("Priority"))
                            .value("High")
                            .build()
                    ))
                    .build()
            ))
            .build();

        UpsertRows.Output output = task.run(runContext);

        assertThat(output.getRowIds(), is(notNullValue()));
        assertThat(output.getRowIds(), hasSize(1));
        assertThat(output.getRowCount(), is(1));
        assertThat(output.getKeyColumns(), contains("Task Name"));
        assertThat(output.getRequestId(), is(notNullValue()));
    }

    @Test
    @Disabled("Integration test - requires valid Coda API token, document ID, and table ID")
    void testUpsertMultipleRowsWithCompositeKey() throws Exception {
        RunContext runContext = runContextFactory.of();

        UpsertRows task = UpsertRows.builder()
            .apiToken(Property.of("YOUR_API_TOKEN"))
            .docId(Property.of("YOUR_DOC_ID"))
            .tableId(Property.of("YOUR_TABLE_ID"))
            .keyColumns(List.of(
                Property.of("Project"),
                Property.of("Year")
            ))
            .rows(List.of(
                UpsertRows.RowInput.builder()
                    .cells(List.of(
                        UpsertRows.CellInput.builder()
                            .column(Property.of("Project"))
                            .value("ProjectA")
                            .build(),
                        UpsertRows.CellInput.builder()
                            .column(Property.of("Year"))
                            .value(2024)
                            .build(),
                        UpsertRows.CellInput.builder()
                            .column(Property.of("Data"))
                            .value("Some Data")
                            .build()
                    ))
                    .build(),
                UpsertRows.RowInput.builder()
                    .cells(List.of(
                        UpsertRows.CellInput.builder()
                            .column(Property.of("Project"))
                            .value("ProjectB")
                            .build(),
                        UpsertRows.CellInput.builder()
                            .column(Property.of("Year"))
                            .value(2024)
                            .build(),
                        UpsertRows.CellInput.builder()
                            .column(Property.of("Data"))
                            .value("More Data")
                            .build()
                    ))
                    .build()
            ))
            .build();

        UpsertRows.Output output = task.run(runContext);

        assertThat(output.getRowIds(), is(notNullValue()));
        assertThat(output.getRowIds(), hasSize(2));
        assertThat(output.getRowCount(), is(2));
        assertThat(output.getKeyColumns(), contains("Project", "Year"));
        assertThat(output.getRequestId(), is(notNullValue()));
    }

    @Test
    @Disabled("Integration test - requires valid Coda API token, document ID, and table ID")
    void testUpsertWithEmailAsUniqueKey() throws Exception {
        RunContext runContext = runContextFactory.of();

        UpsertRows task = UpsertRows.builder()
            .apiToken(Property.of("YOUR_API_TOKEN"))
            .docId(Property.of("YOUR_DOC_ID"))
            .tableId(Property.of("YOUR_TABLE_ID"))
            .keyColumns(List.of(Property.of("Email")))
            .rows(List.of(
                UpsertRows.RowInput.builder()
                    .cells(List.of(
                        UpsertRows.CellInput.builder()
                            .column(Property.of("Email"))
                            .value("john@example.com")
                            .build(),
                        UpsertRows.CellInput.builder()
                            .column(Property.of("Name"))
                            .value("John Doe")
                            .build(),
                        UpsertRows.CellInput.builder()
                            .column(Property.of("Status"))
                            .value("Active")
                            .build()
                    ))
                    .build()
            ))
            .build();

        UpsertRows.Output output = task.run(runContext);

        assertThat(output.getRowIds(), is(notNullValue()));
        assertThat(output.getRowIds(), hasSize(1));
        assertThat(output.getKeyColumns(), contains("Email"));
        assertThat(output.getRequestId(), is(notNullValue()));
    }
}
