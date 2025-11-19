package io.kestra.plugin.coda.tables;

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
class GetTableTest {
    @Inject
    private RunContextFactory runContextFactory;

    @Test
    @Disabled("Integration test - requires valid Coda API token, document ID, and table ID")
    void testGetTable() throws Exception {
        RunContext runContext = runContextFactory.of();

        GetTable task = GetTable.builder()
            .apiToken(Property.of("YOUR_API_TOKEN"))
            .docId(Property.of("YOUR_DOC_ID"))
            .tableId(Property.of("YOUR_TABLE_ID"))
            .build();

        GetTable.Output output = task.run(runContext);

        assertThat(output.getTable(), is(notNullValue()));
        assertThat(output.getTable().getId(), is(notNullValue()));
        assertThat(output.getTable().getName(), is(notNullValue()));
    }
}
