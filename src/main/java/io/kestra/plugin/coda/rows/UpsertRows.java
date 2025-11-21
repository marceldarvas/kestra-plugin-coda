package io.kestra.plugin.coda.rows;

import io.kestra.core.models.annotations.Example;
import io.kestra.core.models.annotations.Plugin;
import io.kestra.core.models.annotations.PluginProperty;
import io.kestra.core.models.property.Property;
import io.kestra.core.models.tasks.RunnableTask;
import io.kestra.core.runners.RunContext;
import io.kestra.plugin.coda.CodaTask;
import io.kestra.plugin.coda.client.CodaConnection;
import io.kestra.plugin.coda.models.CodaRowCell;
import io.kestra.plugin.coda.models.InsertRowsRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.slf4j.Logger;

import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Insert or update rows in a Coda table based on key columns.
 *
 * This task performs an upsert operation: if a row with matching key column values exists,
 * it will be updated; otherwise, a new row will be inserted.
 */
@SuperBuilder
@ToString
@EqualsAndHashCode(callSuper = true)
@Getter
@NoArgsConstructor
@Schema(
    title = "Upsert Rows",
    description = "Insert or update rows in a Coda table based on key columns. " +
        "If a row with matching key column values exists, it will be updated; otherwise, a new row will be inserted."
)
@Plugin(
    examples = {
        @Example(
            title = "Upsert a single row using a key column",
            code = {
                "apiToken: \"{{ secret('CODA_API_TOKEN') }}\"",
                "docId: \"abc123\"",
                "tableId: \"grid-pqRst-U\"",
                "keyColumns:",
                "  - \"Task Name\"",
                "rows:",
                "  - cells:",
                "      - column: \"Task Name\"",
                "        value: \"Unique Task\"",
                "      - column: \"Status\"",
                "        value: \"In Progress\"",
                "      - column: \"Priority\"",
                "        value: \"High\""
            }
        ),
        @Example(
            title = "Upsert multiple rows with composite key",
            code = {
                "apiToken: \"{{ secret('CODA_API_TOKEN') }}\"",
                "docId: \"abc123\"",
                "tableId: \"grid-pqRst-U\"",
                "keyColumns:",
                "  - \"c-abc123\"",
                "  - \"c-def456\"",
                "rows:",
                "  - cells:",
                "      - column: \"c-abc123\"",
                "        value: \"ProjectA\"",
                "      - column: \"c-def456\"",
                "        value: \"2024\"",
                "      - column: \"c-ghi789\"",
                "        value: \"Data\"",
                "  - cells:",
                "      - column: \"c-abc123\"",
                "        value: \"ProjectB\"",
                "      - column: \"c-def456\"",
                "        value: \"2024\"",
                "      - column: \"c-ghi789\"",
                "        value: \"More Data\""
            }
        ),
        @Example(
            title = "Upsert with email as unique key",
            code = {
                "apiToken: \"{{ secret('CODA_API_TOKEN') }}\"",
                "docId: \"abc123\"",
                "tableId: \"grid-pqRst-U\"",
                "keyColumns:",
                "  - \"Email\"",
                "rows:",
                "  - cells:",
                "      - column: \"Email\"",
                "        value: \"john@example.com\"",
                "      - column: \"Name\"",
                "        value: \"John Doe\"",
                "      - column: \"Status\"",
                "        value: \"Active\""
            }
        )
    }
)
public class UpsertRows extends CodaTask implements RunnableTask<UpsertRows.Output> {

    @Schema(
        title = "Table ID",
        description = "The ID of the table to upsert rows into. " +
            "Table IDs typically start with 'grid-' or 'table-'."
    )
    @NotNull
    private Property<String> tableId;

    @Schema(
        title = "Key Columns",
        description = "List of column IDs or names to use as merge keys. " +
            "These columns uniquely identify a row. If a row with matching values in these columns exists, " +
            "it will be updated; otherwise, a new row will be inserted."
    )
    @NotNull
    private List<Property<String>> keyColumns;

    @Schema(
        title = "Rows",
        description = "List of rows to upsert. Each row contains a list of cells with column and value pairs. " +
            "Column can be specified by column ID (e.g., 'c-abc123') or column name (e.g., 'Task Name')."
    )
    @NotNull
    private List<RowInput> rows;

    @Override
    public Output run(RunContext runContext) throws Exception {
        Logger logger = runContext.logger();
        String docId = getDocId(runContext);
        String tableIdValue = runContext.render(tableId).as(String.class).orElseThrow();
        CodaConnection connection = createConnection(runContext);

        logger.info("Upserting {} row(s) into table {} in document {}", rows.size(), tableIdValue, docId);

        // Render key columns
        List<String> renderedKeyColumns = new ArrayList<>();
        for (Property<String> keyColumn : keyColumns) {
            String keyColumnValue = runContext.render(keyColumn).as(String.class).orElseThrow();
            renderedKeyColumns.add(keyColumnValue);
        }

        logger.debug("Using key columns: {}", renderedKeyColumns);

        // Build the request
        InsertRowsRequest.InsertRowsRequestBuilder requestBuilder = InsertRowsRequest.builder();

        // Add key columns
        for (String keyColumn : renderedKeyColumns) {
            requestBuilder.keyColumn(keyColumn);
        }

        // Add rows
        for (RowInput rowInput : rows) {
            List<CodaRowCell> cells = new ArrayList<>();
            for (CellInput cellInput : rowInput.getCells()) {
                String columnValue = runContext.render(cellInput.getColumn()).as(String.class).orElseThrow();
                Object value = renderValue(runContext, cellInput.getValue());

                cells.add(CodaRowCell.builder()
                    .column(columnValue)
                    .value(value)
                    .build());
            }

            requestBuilder.row(InsertRowsRequest.RowData.builder()
                .cells(cells)
                .build());
        }

        InsertRowsRequest request = requestBuilder.build();

        String endpoint = String.format("/docs/%s/tables/%s/rows", docId, tableIdValue);

        logger.debug("Sending upsert request to: {}", endpoint);

        UpsertRowsResponse response = connection.post(endpoint, request, UpsertRowsResponse.class);

        logger.info("Successfully upserted {} row(s). Request ID: {}",
            response.getAddedRowIds().size(),
            response.getRequestId());

        return Output.builder()
            .rowIds(response.getAddedRowIds())
            .requestId(response.getRequestId())
            .rowCount(response.getAddedRowIds().size())
            .keyColumns(renderedKeyColumns)
            .build();
    }

    /**
     * Renders a value from the input, handling both simple types and Property types.
     */
    private Object renderValue(RunContext runContext, Object value) throws Exception {
        if (value instanceof Property) {
            // Render as String to avoid generic type issues
            // Gson will handle type conversion during JSON serialization
            return runContext.render((Property<?>) value).as(String.class).orElse(null);
        }
        return value;
    }

    /**
     * Input model for a row to upsert.
     */
    @Builder
    @Getter
    public static class RowInput {
        @Schema(
            title = "Cells",
            description = "List of cells for this row"
        )
        @NotNull
        private List<CellInput> cells;
    }

    /**
     * Input model for a cell value.
     */
    @Builder
    @Getter
    public static class CellInput {
        @Schema(
            title = "Column",
            description = "Column ID (e.g., 'c-abc123') or column name (e.g., 'Task Name')"
        )
        @NotNull
        private Property<String> column;

        @Schema(
            title = "Value",
            description = "The value to set for this cell. Can be a string, number, boolean, or date."
        )
        @NotNull
        private Object value;
    }

    /**
     * Response from the Coda API when upserting rows.
     */
    @Builder
    @Getter
    private static class UpsertRowsResponse {
        private String requestId;
        private List<String> addedRowIds;
    }

    @Builder
    @Getter
    public static class Output implements io.kestra.core.models.tasks.Output {
        @Schema(
            title = "Row IDs",
            description = "List of IDs for the upserted rows (both inserted and updated)"
        )
        private List<String> rowIds;

        @Schema(
            title = "Request ID",
            description = "Unique identifier for this API request"
        )
        private String requestId;

        @Schema(
            title = "Row Count",
            description = "Number of rows successfully upserted"
        )
        private Integer rowCount;

        @Schema(
            title = "Key Columns",
            description = "The key columns used for matching during the upsert operation"
        )
        private List<String> keyColumns;
    }
}
