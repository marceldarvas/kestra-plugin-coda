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
class ListColumnsTest {
    @Inject
    private RunContextFactory runContextFactory;

    @Test
    @Disabled("Integration test - requires valid Coda API token, document ID, and table ID")
    void testListColumns() throws Exception {
        RunContext runContext = runContextFactory.of();

        ListColumns task = ListColumns.builder()
            .apiToken(Property.of("YOUR_API_TOKEN"))
            .docId(Property.of("YOUR_DOC_ID"))
            .tableId(Property.of("YOUR_TABLE_ID"))
            .build();

        ListColumns.Output output = task.run(runContext);

        assertThat(output.getColumns(), is(notNullValue()));
        assertThat(output.getTotalCount(), is(greaterThanOrEqualTo(0)));
        assertThat(output.getPageCount(), is(greaterThan(0)));
    }

    @Test
    @Disabled("Integration test - requires valid Coda API token, document ID, and table ID")
    void testListColumnsWithPagination() throws Exception {
        RunContext runContext = runContextFactory.of();

        ListColumns task = ListColumns.builder()
            .apiToken(Property.of("YOUR_API_TOKEN"))
            .docId(Property.of("YOUR_DOC_ID"))
            .tableId(Property.of("YOUR_TABLE_ID"))
            .fetchAllPages(true)
            .limit(10)
            .build();

        ListColumns.Output output = task.run(runContext);

        assertThat(output.getColumns(), is(notNullValue()));
        assertThat(output.getTotalCount(), is(greaterThanOrEqualTo(0)));
    }
}
