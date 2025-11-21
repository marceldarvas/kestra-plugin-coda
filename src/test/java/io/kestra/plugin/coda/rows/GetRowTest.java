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
class GetRowTest {
    @Inject
    private RunContextFactory runContextFactory;

    @Test
    @Disabled("Integration test - requires valid Coda API token, document ID, table ID, and row ID")
    void testGetRow() throws Exception {
        RunContext runContext = runContextFactory.of();

        GetRow task = GetRow.builder()
            .apiToken(Property.of("YOUR_API_TOKEN"))
            .docId(Property.of("YOUR_DOC_ID"))
            .tableId(Property.of("YOUR_TABLE_ID"))
            .rowId(Property.of("YOUR_ROW_ID"))
            .build();

        GetRow.Output output = task.run(runContext);

        assertThat(output.getRow(), is(notNullValue()));
        assertThat(output.getRow().getId(), is(notNullValue()));
    }

    @Test
    @Disabled("Integration test - requires valid Coda API token, document ID, table ID, and row ID")
    void testGetRowWithColumnNames() throws Exception {
        RunContext runContext = runContextFactory.of();

        GetRow task = GetRow.builder()
            .apiToken(Property.of("YOUR_API_TOKEN"))
            .docId(Property.of("YOUR_DOC_ID"))
            .tableId(Property.of("YOUR_TABLE_ID"))
            .rowId(Property.of("YOUR_ROW_ID"))
            .useColumnNames(true)
            .valueFormat(Property.of("rich"))
            .build();

        GetRow.Output output = task.run(runContext);

        assertThat(output.getRow(), is(notNullValue()));
        assertThat(output.getRow().getId(), is(notNullValue()));
    }
}
