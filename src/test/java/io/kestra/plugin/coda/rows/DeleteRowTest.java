package io.kestra.plugin.coda.rows;

import io.kestra.core.models.property.Property;
import io.kestra.core.runners.RunContext;
import io.kestra.core.runners.RunContextFactory;
import io.kestra.core.junit.annotations.KestraTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;

import jakarta.inject.Inject;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@KestraTest
class DeleteRowTest {
    @Inject
    private RunContextFactory runContextFactory;

    @Test
    @Disabled("Integration test - requires valid Coda API token, document ID, table ID, and row ID")
    void testDeleteRow() throws Exception {
        RunContext runContext = runContextFactory.of();

        DeleteRow task = DeleteRow.builder()
            .apiToken(Property.of("YOUR_API_TOKEN"))
            .docId(Property.of("YOUR_DOC_ID"))
            .tableId(Property.of("YOUR_TABLE_ID"))
            .rowId(Property.of("YOUR_ROW_ID"))
            .build();

        DeleteRow.Output output = task.run(runContext);

        assertThat(output.getRowId(), is(notNullValue()));
        assertThat(output.getDeleted(), is(true));
        assertThat(output.getRequestId(), is(notNullValue()));
    }

    @Test
    @Disabled("Integration test - requires valid Coda API token, document ID, table ID, and row ID from previous task")
    void testDeleteRowFromPreviousTask() throws Exception {
        RunContext runContext = runContextFactory.of();

        // Note: In a real workflow, this would reference an output from a previous InsertRows task
        DeleteRow task = DeleteRow.builder()
            .apiToken(Property.of("YOUR_API_TOKEN"))
            .docId(Property.of("YOUR_DOC_ID"))
            .tableId(Property.of("YOUR_TABLE_ID"))
            .rowId(Property.of("{{ outputs.insertTask.addedRowIds[0] }}"))
            .build();

        DeleteRow.Output output = task.run(runContext);

        assertThat(output.getRowId(), is(notNullValue()));
        assertThat(output.getDeleted(), is(true));
        assertThat(output.getRequestId(), is(notNullValue()));
    }
}
