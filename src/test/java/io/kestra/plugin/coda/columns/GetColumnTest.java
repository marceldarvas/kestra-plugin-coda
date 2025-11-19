package io.kestra.plugin.coda.columns;

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
class GetColumnTest {
    @Inject
    private RunContextFactory runContextFactory;

    @Test
    @Disabled("Integration test - requires valid Coda API token, document ID, table ID, and column ID")
    void testGetColumn() throws Exception {
        RunContext runContext = runContextFactory.of();

        GetColumn task = GetColumn.builder()
            .apiToken(Property.of("YOUR_API_TOKEN"))
            .docId(Property.of("YOUR_DOC_ID"))
            .tableId(Property.of("YOUR_TABLE_ID"))
            .columnId(Property.of("YOUR_COLUMN_ID"))
            .build();

        GetColumn.Output output = task.run(runContext);

        assertThat(output.getColumn(), is(notNullValue()));
        assertThat(output.getColumn().getId(), is(notNullValue()));
        assertThat(output.getColumn().getName(), is(notNullValue()));
    }
}
