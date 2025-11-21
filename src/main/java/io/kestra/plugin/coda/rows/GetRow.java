package io.kestra.plugin.coda.rows;

import io.kestra.core.models.annotations.Example;
import io.kestra.core.models.annotations.Plugin;
import io.kestra.core.models.annotations.PluginProperty;
import io.kestra.core.models.property.Property;
import io.kestra.core.models.tasks.RunnableTask;
import io.kestra.core.runners.RunContext;
import io.kestra.plugin.coda.CodaTask;
import io.kestra.plugin.coda.client.CodaConnection;
import io.kestra.plugin.coda.models.CodaRow;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.slf4j.Logger;

import jakarta.validation.constraints.NotNull;

/**
 * Get a specific row from a Coda table.
 *
 * This task retrieves detailed information about a single row, including all cell values,
 * metadata, and timestamps.
 */
@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@Schema(
    title = "Get Row",
    description = "Get detailed information about a specific row in a Coda table. " +
        "Returns row data including ID, values, and metadata."
)
@Plugin(
    examples = {
        @Example(
            title = "Get a specific row by ID",
            code = {
                "apiToken: \"{{ secret('CODA_API_TOKEN') }}\"",
                "docId: \"abc123\"",
                "tableId: \"grid-pqRst-U\"",
                "rowId: \"i-tuVwxYz\""
            }
        ),
        @Example(
            title = "Get row with column names",
            code = {
                "apiToken: \"{{ secret('CODA_API_TOKEN') }}\"",
                "docId: \"abc123\"",
                "tableId: \"grid-pqRst-U\"",
                "rowId: \"i-tuVwxYz\"",
                "useColumnNames: true"
            }
        ),
        @Example(
            title = "Get row with rich value format",
            code = {
                "apiToken: \"{{ secret('CODA_API_TOKEN') }}\"",
                "docId: \"{{ inputs.docId }}\"",
                "tableId: \"{{ inputs.tableId }}\"",
                "rowId: \"{{ inputs.rowId }}\"",
                "valueFormat: \"rich\""
            }
        )
    }
)
public class GetRow extends CodaTask implements RunnableTask<GetRow.Output> {

    @Schema(
        title = "Table ID",
        description = "The ID of the table containing the row. " +
            "Table IDs typically start with 'grid-' or 'table-'."
    )
    @NotNull
    private Property<String> tableId;

    @Schema(
        title = "Row ID",
        description = "The ID of the row to retrieve. " +
            "Row IDs typically start with 'i-'."
    )
    @NotNull
    private Property<String> rowId;

    @Schema(
        title = "Use Column Names",
        description = "If true, return column values keyed by column name instead of column ID. Default is false."
    )
    @PluginProperty
    @Builder.Default
    private Boolean useColumnNames = false;

    @Schema(
        title = "Value Format",
        description = "The format for cell values. Options: 'simple' (default), 'simpleWithArrays', or 'rich'. " +
            "Use 'rich' to include formatted values and metadata."
    )
    @PluginProperty
    @Builder.Default
    private Property<String> valueFormat = Property.of("simple");

    @Override
    public Output run(RunContext runContext) throws Exception {
        Logger logger = runContext.logger();
        String docId = getDocId(runContext);
        String tableIdValue = runContext.render(tableId).as(String.class).orElseThrow();
        String rowIdValue = runContext.render(rowId).as(String.class).orElseThrow();
        CodaConnection connection = createConnection(runContext);

        logger.info("Getting row {} from table {} in document {}",
            rowIdValue, tableIdValue, docId);

        // Build the endpoint with query parameters
        StringBuilder endpointBuilder = new StringBuilder(
            String.format("/docs/%s/tables/%s/rows/%s", docId, tableIdValue, rowIdValue)
        );

        // Add query parameters
        boolean hasQueryParams = false;

        if (useColumnNames) {
            endpointBuilder.append("?useColumnNames=true");
            hasQueryParams = true;
        }

        String valueFormatValue = runContext.render(valueFormat).as(String.class).orElse("simple");
        if (hasQueryParams) {
            endpointBuilder.append("&valueFormat=").append(valueFormatValue);
        } else {
            endpointBuilder.append("?valueFormat=").append(valueFormatValue);
        }

        String endpoint = endpointBuilder.toString();

        CodaRow row = connection.get(endpoint, CodaRow.class);

        logger.info("Successfully retrieved row: {} (index: {})",
            row.getName(), row.getIndex());

        return Output.builder()
            .row(row)
            .build();
    }

    @Builder
    @Getter
    public static class Output implements io.kestra.core.models.tasks.Output {
        @Schema(
            title = "Row",
            description = "The retrieved row details"
        )
        private CodaRow row;
    }
}
