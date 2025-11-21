package io.kestra.plugin.coda.columns;

import io.kestra.core.models.annotations.Example;
import io.kestra.core.models.annotations.Plugin;
import io.kestra.core.models.property.Property;
import io.kestra.core.models.tasks.RunnableTask;
import io.kestra.core.runners.RunContext;
import io.kestra.plugin.coda.CodaTask;
import io.kestra.plugin.coda.client.CodaConnection;
import io.kestra.plugin.coda.models.CodaColumn;
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
 * Get detailed information about a specific column in a Coda table.
 *
 * This task retrieves metadata for a single column, including its type, name,
 * whether it's calculated (read-only), and any formula if applicable.
 */
@SuperBuilder
@ToString
@EqualsAndHashCode(callSuper = true)
@Getter
@NoArgsConstructor
@Schema(
    title = "Get Column",
    description = "Get detailed information about a specific column in a Coda table. " +
        "Returns metadata including column ID, name, type, and calculated status."
)
@Plugin(
    examples = {
        @Example(
            title = "Get a specific column by ID",
            code = {
                "apiToken: \"{{ secret('CODA_API_TOKEN') }}\"",
                "docId: \"abc123\"",
                "tableId: \"grid-pqRst-U\"",
                "columnId: \"c-tuVwxYz\""
            }
        ),
        @Example(
            title = "Get column and check if it's calculated",
            code = {
                "apiToken: \"{{ secret('CODA_API_TOKEN') }}\"",
                "docId: \"{{ inputs.docId }}\"",
                "tableId: \"{{ inputs.tableId }}\"",
                "columnId: \"{{ inputs.columnId }}\""
            }
        )
    }
)
public class GetColumn extends CodaTask implements RunnableTask<GetColumn.Output> {

    @Schema(
        title = "Table ID",
        description = "The ID of the table containing the column. " +
            "Table IDs typically start with 'grid-' or 'table-'."
    )
    @NotNull
    private Property<String> tableId;

    @Schema(
        title = "Column ID",
        description = "The ID of the column to retrieve. " +
            "Column IDs typically start with 'c-'."
    )
    @NotNull
    private Property<String> columnId;

    @Override
    public Output run(RunContext runContext) throws Exception {
        Logger logger = runContext.logger();
        String docId = getDocId(runContext);
        String tableIdValue = runContext.render(tableId).as(String.class).orElseThrow();
        String columnIdValue = runContext.render(columnId).as(String.class).orElseThrow();
        CodaConnection connection = createConnection(runContext);

        logger.info("Getting column {} from table {} in document {}",
            columnIdValue, tableIdValue, docId);

        String endpoint = String.format("/docs/%s/tables/%s/columns/%s",
            docId, tableIdValue, columnIdValue);

        CodaColumn column = connection.get(endpoint, CodaColumn.class);

        logger.info("Successfully retrieved column: {} (type: {})",
            column.getName(), column.getType());

        return Output.builder()
            .column(column)
            .build();
    }

    @Builder
    @Getter
    public static class Output implements io.kestra.core.models.tasks.Output {
        @Schema(
            title = "Column",
            description = "The retrieved column details"
        )
        private CodaColumn column;
    }
}
