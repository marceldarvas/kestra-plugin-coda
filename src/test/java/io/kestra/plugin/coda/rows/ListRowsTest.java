package io.kestra.plugin.coda.rows;

import io.kestra.plugin.coda.CodaTestBase;
import io.kestra.plugin.coda.models.CodaRow;
import io.kestra.plugin.coda.utils.TestDataFactory;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class ListRowsTest extends CodaTestBase {

    @Test
    void testListRows_Integration() throws Exception {
        requireIntegrationTest();

        ListRows task = ListRows.builder()
            .apiToken(getApiToken())
            .docId(getDocId())
            .tableId(getTableId())
            .build();

        ListRows.Output output = task.run(runContext);

        assertThat(output.getRows(), is(notNullValue()));
        assertThat(output.getTotalCount(), is(greaterThanOrEqualTo(0)));
        assertThat(output.getPageCount(), is(greaterThan(0)));
    }

    @Test
    void testListRows_WithColumnNames_Integration() throws Exception {
        requireIntegrationTest();

        ListRows task = ListRows.builder()
            .apiToken(getApiToken())
            .docId(getDocId())
            .tableId(getTableId())
            .useColumnNames(property(true))
            .valueFormat(property("rich"))
            .build();

        ListRows.Output output = task.run(runContext);

        assertThat(output.getRows(), is(notNullValue()));
        assertThat(output.getTotalCount(), is(greaterThanOrEqualTo(0)));
    }

    @Test
    void testListRows_WithPagination_Integration() throws Exception {
        requireIntegrationTest();

        ListRows task = ListRows.builder()
            .apiToken(getApiToken())
            .docId(getDocId())
            .tableId(getTableId())
            .fetchAllPages(property(true))
            .limit(property(50))
            .build();

        ListRows.Output output = task.run(runContext);

        assertThat(output.getRows(), is(notNullValue()));
        assertThat(output.getTotalCount(), is(greaterThanOrEqualTo(0)));
    }

    @Test
    void testListRows_WithVisibleOnly_Integration() throws Exception {
        requireIntegrationTest();

        ListRows task = ListRows.builder()
            .apiToken(getApiToken())
            .docId(getDocId())
            .tableId(getTableId())
            .visibleOnly(property(true))
            .build();

        ListRows.Output output = task.run(runContext);

        assertThat(output.getRows(), is(notNullValue()));
        assertThat(output.getTotalCount(), is(greaterThanOrEqualTo(0)));
    }

    @Test
    void testListRows_OutputStructure() {
        // Test output structure without API calls
        List<CodaRow> mockRows = TestDataFactory.createRows(10);

        ListRows.Output output = ListRows.Output.builder()
            .rows(mockRows)
            .totalCount(10)
            .pageCount(1)
            .build();

        assertThat(output.getRows(), hasSize(10));
        assertThat(output.getTotalCount(), is(10));
        assertThat(output.getPageCount(), is(1));

        // Verify first row structure
        CodaRow firstRow = output.getRows().get(0);
        assertThat(firstRow.getId(), is(notNullValue()));
        assertThat(firstRow.getName(), is(notNullValue()));
        assertThat(firstRow.getValues(), is(notNullValue()));
    }

    @Test
    void testListRows_EmptyResults() {
        // Test with empty results
        List<CodaRow> emptyRows = List.of();

        ListRows.Output output = ListRows.Output.builder()
            .rows(emptyRows)
            .totalCount(0)
            .pageCount(1)
            .build();

        assertThat(output.getRows(), is(empty()));
        assertThat(output.getTotalCount(), is(0));
    }
}
