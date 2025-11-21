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
import java.util.Map;

/**
 * Insert one or more rows into a Coda table.
 *
 * This task inserts new rows into the specified table. You can insert a single row
 * or multiple rows in a batch operation.
 */
@SuperBuilder
@ToString
@EqualsAndHashCode(callSuper = true)
@Getter
@NoArgsConstructor
@Schema(
    title = "Insert Rows",
    description = "Insert one or more rows into a Coda table. " +
        "Supports batch insertion of multiple rows in a single API call."
)
@Plugin(
    examples = {
        @Example(
            title = "Insert a single row",
            code = {
                "apiToken: \"{{ secret('CODA_API_TOKEN') }}\"",
                "docId: \"abc123\"",
                "tableId: \"grid-pqRst-U\"",
                "rows:",
                "  - cells:",
                "      - column: \"c-abc123\"",
                "        value: \"Task Name\"",
                "      - column: \"c-def456\"",
                "        value: \"In Progress\""
            }
        ),
        @Example(
            title = "Insert multiple rows at once",
            code = {
                "apiToken: \"{{ secret('CODA_API_TOKEN') }}\"",
                "docId: \"abc123\"",
                "tableId: \"grid-pqRst-U\"",
                "rows:",
                "  - cells:",
                "      - column: \"Task Name\"",
                "        value: \"First Task\"",
                "      - column: \"Status\"",
                "        value: \"To Do\"",
                "  - cells:",
                "      - column: \"Task Name\"",
                "        value: \"Second Task\"",
                "      - column: \"Status\"",
                "        value: \"In Progress\""
            }
        ),
        @Example(
            title = "Insert row with various data types",
            code = {
                "apiToken: \"{{ secret('CODA_API_TOKEN') }}\"",
                "docId: \"abc123\"",
                "tableId: \"grid-pqRst-U\"",
                "rows:",
                "  - cells:",
                "      - column: \"Name\"",
                "        value: \"John Doe\"",
                "      - column: \"Age\"",
                "        value: 30",
                "      - column: \"Active\"",
                "        value: true",
                "      - column: \"Created\"",
                "        value: \"2024-01-15\""
            }
        )
    }
)
public class InsertRows extends CodaTask implements RunnableTask<InsertRows.Output> {

    @Schema(
        title = "Table ID",
        description = "The ID of the table to insert rows into. " +
            "Table IDs typically start with 'grid-' or 'table-'."
    )
    @NotNull
    private Property<String> tableId;

    @Schema(
        title = "Rows",
        description = "List of rows to insert. Each row contains a list of cells with column and value pairs. " +
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

        logger.info("Inserting {} row(s) into table {} in document {}", rows.size(), tableIdValue, docId);

        // Build the request
        InsertRowsRequest.InsertRowsRequestBuilder requestBuilder = InsertRowsRequest.builder();

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

        logger.debug("Sending insert request to: {}", endpoint);

        InsertRowsResponse response = connection.post(endpoint, request, InsertRowsResponse.class);

        logger.info("Successfully inserted {} row(s). Request ID: {}",
            response.getAddedRowIds().size(),
            response.getRequestId());

        return Output.builder()
            .addedRowIds(response.getAddedRowIds())
            .requestId(response.getRequestId())
            .rowCount(response.getAddedRowIds().size())
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
     * Input model for a row to insert.
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
     * Response from the Coda API when inserting rows.
     */
    @Builder
    @Getter
    private static class InsertRowsResponse {
        private String requestId;
        private List<String> addedRowIds;
    }

    @Builder
    @Getter
    public static class Output implements io.kestra.core.models.tasks.Output {
        @Schema(
            title = "Added Row IDs",
            description = "List of IDs for the newly created rows"
        )
        private List<String> addedRowIds;

        @Schema(
            title = "Request ID",
            description = "Unique identifier for this API request"
        )
        private String requestId;

        @Schema(
            title = "Row Count",
            description = "Number of rows successfully inserted"
        )
        private Integer rowCount;
    }
}
