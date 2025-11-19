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
class ListTablesTest {
    @Inject
    private RunContextFactory runContextFactory;

    @Test
    @Disabled("Integration test - requires valid Coda API token and document ID")
    void testListTables() throws Exception {
        RunContext runContext = runContextFactory.of();

        ListTables task = ListTables.builder()
            .apiToken(Property.of("YOUR_API_TOKEN"))
            .docId(Property.of("YOUR_DOC_ID"))
            .build();

        ListTables.Output output = task.run(runContext);

        assertThat(output.getTables(), is(notNullValue()));
        assertThat(output.getTotalCount(), is(greaterThanOrEqualTo(0)));
        assertThat(output.getPageCount(), is(greaterThan(0)));
    }

    @Test
    @Disabled("Integration test - requires valid Coda API token and document ID")
    void testListTablesWithPagination() throws Exception {
        RunContext runContext = runContextFactory.of();

        ListTables task = ListTables.builder()
            .apiToken(Property.of("YOUR_API_TOKEN"))
            .docId(Property.of("YOUR_DOC_ID"))
            .fetchAllPages(true)
            .limit(10)
            .build();

        ListTables.Output output = task.run(runContext);

        assertThat(output.getTables(), is(notNullValue()));
        assertThat(output.getTotalCount(), is(greaterThanOrEqualTo(0)));
    }
}
