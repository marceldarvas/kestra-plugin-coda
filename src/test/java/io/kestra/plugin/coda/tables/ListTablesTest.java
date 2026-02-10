package io.kestra.plugin.coda.tables;

import io.kestra.plugin.coda.CodaTestBase;
import io.kestra.plugin.coda.models.CodaTable;
import io.kestra.plugin.coda.utils.TestDataFactory;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class ListTablesTest extends CodaTestBase {

    @Test
    void testListTables_Integration() throws Exception {
        requireIntegrationTest();

        ListTables task = ListTables.builder()
            .apiToken(getApiToken())
            .docId(getDocId())
            .build();

        ListTables.Output output = task.run(runContext);

        assertThat(output.getTables(), is(notNullValue()));
        assertThat(output.getTotalCount(), is(greaterThanOrEqualTo(0)));
        assertThat(output.getPageCount(), is(greaterThan(0)));
    }

    @Test
    void testListTables_WithPagination_Integration() throws Exception {
        requireIntegrationTest();

        ListTables task = ListTables.builder()
            .apiToken(getApiToken())
            .docId(getDocId())
            .fetchAllPages(property(true))
            .limit(property(10))
            .build();

        ListTables.Output output = task.run(runContext);

        assertThat(output.getTables(), is(notNullValue()));
        assertThat(output.getTotalCount(), is(greaterThanOrEqualTo(0)));
    }

    @Test
    void testListTables_OutputStructure() {
        // Test output structure without API calls
        List<CodaTable> mockTables = TestDataFactory.createTables(5);

        ListTables.Output output = ListTables.Output.builder()
            .tables(mockTables)
            .totalCount(5)
            .pageCount(1)
            .build();

        assertThat(output.getTables(), hasSize(5));
        assertThat(output.getTotalCount(), is(5));
        assertThat(output.getPageCount(), is(1));

        // Verify first table structure
        CodaTable firstTable = output.getTables().get(0);
        assertThat(firstTable.getId(), is(notNullValue()));
        assertThat(firstTable.getName(), is(notNullValue()));
        assertThat(firstTable.getType(), is(notNullValue()));
    }

    @Test
    void testListTables_EmptyResults() {
        // Test with empty results
        List<CodaTable> emptyTables = List.of();

        ListTables.Output output = ListTables.Output.builder()
            .tables(emptyTables)
            .totalCount(0)
            .pageCount(1)
            .build();

        assertThat(output.getTables(), is(empty()));
        assertThat(output.getTotalCount(), is(0));
    }
}
