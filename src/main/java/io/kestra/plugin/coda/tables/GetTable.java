package io.kestra.plugin.coda.tables;

import io.kestra.core.models.annotations.Example;
import io.kestra.core.models.annotations.Plugin;
import io.kestra.core.models.property.Property;
import io.kestra.core.models.tasks.RunnableTask;
import io.kestra.core.runners.RunContext;
import io.kestra.plugin.coda.CodaTask;
import io.kestra.plugin.coda.client.CodaConnection;
import io.kestra.plugin.coda.models.CodaTable;
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
 * Get detailed information about a specific table in a Coda document.
 *
 * This task retrieves metadata for a single table, including its ID, name, type,
 * parent information (for views), and timestamps.
 */
@SuperBuilder
@ToString
@EqualsAndHashCode(callSuper = true)
@Getter
@NoArgsConstructor
@Schema(
    title = "Get Table",
    description = "Get detailed information about a specific table in a Coda document. " +
        "Returns metadata including table ID, name, type (table or view), and browser link."
)
@Plugin(
    examples = {
        @Example(
            title = "Get a specific table by ID",
            code = {
                "apiToken: \"{{ secret('CODA_API_TOKEN') }}\"",
                "docId: \"abc123\"",
                "tableId: \"grid-pqRst-U\""
            }
        ),
        @Example(
            title = "Get table and use output in next task",
            code = {
                "apiToken: \"{{ secret('CODA_API_TOKEN') }}\"",
                "docId: \"{{ inputs.docId }}\"",
                "tableId: \"{{ inputs.tableId }}\""
            }
        )
    }
)
public class GetTable extends CodaTask implements RunnableTask<GetTable.Output> {

    @Schema(
        title = "Table ID",
        description = "The ID of the table to retrieve. " +
            "Table IDs typically start with 'grid-' or 'table-'."
    )
    @NotNull
    private Property<String> tableId;

    @Override
    public Output run(RunContext runContext) throws Exception {
        Logger logger = runContext.logger();
        String docId = getDocId(runContext);
        String tableIdValue = runContext.render(tableId).as(String.class).orElseThrow();
        CodaConnection connection = createConnection(runContext);

        logger.info("Getting table {} from document {}", tableIdValue, docId);

        String endpoint = String.format("/docs/%s/tables/%s", docId, tableIdValue);

        CodaTable table = connection.get(endpoint, CodaTable.class);

        logger.info("Successfully retrieved table: {}", table.getName());

        return Output.builder()
            .table(table)
            .build();
    }

    @Builder
    @Getter
    public static class Output implements io.kestra.core.models.tasks.Output {
        @Schema(
            title = "Table",
            description = "The retrieved table details"
        )
        private CodaTable table;
    }
}
