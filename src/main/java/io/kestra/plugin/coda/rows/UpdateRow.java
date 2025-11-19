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
import io.kestra.plugin.coda.models.UpdateRowRequest;
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

/**
 * Update an existing row in a Coda table.
 *
 * This task updates the specified cells in an existing row. Only the cells you specify
 * will be updated; other cells in the row will remain unchanged.
 */
@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@Schema(
    title = "Update Row",
    description = "Update an existing row in a Coda table. " +
        "Only the specified cells will be updated; other cells remain unchanged."
)
@Plugin(
    examples = {
        @Example(
            title = "Update a single cell in a row",
            code = {
                "apiToken: \"{{ secret('CODA_API_TOKEN') }}\"",
                "docId: \"abc123\"",
                "tableId: \"grid-pqRst-U\"",
                "rowId: \"i-xYz789\"",
                "cells:",
                "  - column: \"Status\"",
                "    value: \"Completed\""
            }
        ),
        @Example(
            title = "Update multiple cells in a row",
            code = {
                "apiToken: \"{{ secret('CODA_API_TOKEN') }}\"",
                "docId: \"abc123\"",
                "tableId: \"grid-pqRst-U\"",
                "rowId: \"i-xYz789\"",
                "cells:",
                "  - column: \"c-abc123\"",
                "    value: \"Updated Task Name\"",
                "  - column: \"c-def456\"",
                "    value: \"Completed\"",
                "  - column: \"c-ghi789\"",
                "    value: \"2024-01-15\""
            }
        ),
        @Example(
            title = "Update with various data types",
            code = {
                "apiToken: \"{{ secret('CODA_API_TOKEN') }}\"",
                "docId: \"abc123\"",
                "tableId: \"grid-pqRst-U\"",
                "rowId: \"i-xYz789\"",
                "cells:",
                "  - column: \"Name\"",
                "    value: \"Jane Doe\"",
                "  - column: \"Count\"",
                "    value: 42",
                "  - column: \"Active\"",
                "    value: false"
            }
        )
    }
)
public class UpdateRow extends CodaTask implements RunnableTask<UpdateRow.Output> {

    @Schema(
        title = "Table ID",
        description = "The ID of the table containing the row to update. " +
            "Table IDs typically start with 'grid-' or 'table-'."
    )
    @NotNull
    private Property<String> tableId;

    @Schema(
        title = "Row ID",
        description = "The ID of the row to update. " +
            "Row IDs typically start with 'i-' and can be obtained from ListRows or InsertRows tasks."
    )
    @NotNull
    private Property<String> rowId;

    @Schema(
        title = "Cells",
        description = "List of cells to update. Each cell contains a column identifier and a value. " +
            "Column can be specified by column ID (e.g., 'c-abc123') or column name (e.g., 'Status'). " +
            "Only the specified cells will be updated; other cells in the row will remain unchanged."
    )
    @NotNull
    @PluginProperty
    private List<CellInput> cells;

    @Override
    public Output run(RunContext runContext) throws Exception {
        Logger logger = runContext.logger();
        String docId = getDocId(runContext);
        String tableIdValue = runContext.render(tableId).as(String.class).orElseThrow();
        String rowIdValue = runContext.render(rowId).as(String.class).orElseThrow();
        CodaConnection connection = createConnection(runContext);

        logger.info("Updating row {} in table {} in document {}", rowIdValue, tableIdValue, docId);

        // Build the request
        List<CodaRowCell> cellList = new ArrayList<>();
        for (CellInput cellInput : cells) {
            String columnValue = runContext.render(cellInput.getColumn()).as(String.class).orElseThrow();
            Object value = renderValue(runContext, cellInput.getValue());

            cellList.add(CodaRowCell.builder()
                .column(columnValue)
                .value(value)
                .build());
        }

        UpdateRowRequest request = UpdateRowRequest.builder()
            .row(UpdateRowRequest.RowData.builder()
                .cells(cellList)
                .build())
            .build();

        String endpoint = String.format("/docs/%s/tables/%s/rows/%s", docId, tableIdValue, rowIdValue);

        logger.debug("Sending update request to: {}", endpoint);

        UpdateRowResponse response = connection.put(endpoint, request, UpdateRowResponse.class);

        logger.info("Successfully updated row {}. Request ID: {}", rowIdValue, response.getRequestId());

        return Output.builder()
            .rowId(response.getId())
            .requestId(response.getRequestId())
            .build();
    }

    /**
     * Renders a value from the input, handling both simple types and Property types.
     */
    private Object renderValue(RunContext runContext, Object value) throws Exception {
        if (value instanceof Property) {
            Property<?> prop = (Property<?>) value;
            return runContext.render(prop).as(Object.class).orElse(null);
        }
        return value;
    }

    /**
     * Input model for a cell value.
     */
    @Builder
    @Getter
    public static class CellInput {
        @Schema(
            title = "Column",
            description = "Column ID (e.g., 'c-abc123') or column name (e.g., 'Status')"
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
     * Response from the Coda API when updating a row.
     */
    @Builder
    @Getter
    private static class UpdateRowResponse {
        private String requestId;
        private String id;
    }

    @Builder
    @Getter
    public static class Output implements io.kestra.core.models.tasks.Output {
        @Schema(
            title = "Row ID",
            description = "The ID of the updated row"
        )
        private String rowId;

        @Schema(
            title = "Request ID",
            description = "Unique identifier for this API request"
        )
        private String requestId;
    }
}
