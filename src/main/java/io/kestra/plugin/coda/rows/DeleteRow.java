package io.kestra.plugin.coda.rows;

import io.kestra.core.models.annotations.Example;
import io.kestra.core.models.annotations.Plugin;
import io.kestra.core.models.property.Property;
import io.kestra.core.models.tasks.RunnableTask;
import io.kestra.core.runners.RunContext;
import io.kestra.plugin.coda.CodaTask;
import io.kestra.plugin.coda.client.CodaConnection;
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
 * Delete a row from a Coda table.
 *
 * This task permanently deletes the specified row from a table. This operation cannot be undone.
 */
@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@Schema(
    title = "Delete Row",
    description = "Delete a row from a Coda table. This operation permanently removes the row and cannot be undone."
)
@Plugin(
    examples = {
        @Example(
            title = "Delete a row by ID",
            code = {
                "apiToken: \"{{ secret('CODA_API_TOKEN') }}\"",
                "docId: \"abc123\"",
                "tableId: \"grid-pqRst-U\"",
                "rowId: \"i-xYz789\""
            }
        ),
        @Example(
            title = "Delete a row using output from previous task",
            code = {
                "apiToken: \"{{ secret('CODA_API_TOKEN') }}\"",
                "docId: \"abc123\"",
                "tableId: \"grid-pqRst-U\"",
                "rowId: \"{{ outputs.insertTask.addedRowIds[0] }}\""
            }
        )
    }
)
public class DeleteRow extends CodaTask implements RunnableTask<DeleteRow.Output> {

    @Schema(
        title = "Table ID",
        description = "The ID of the table containing the row to delete. " +
            "Table IDs typically start with 'grid-' or 'table-'."
    )
    @NotNull
    private Property<String> tableId;

    @Schema(
        title = "Row ID",
        description = "The ID of the row to delete. " +
            "Row IDs typically start with 'i-' and can be obtained from ListRows, InsertRows, or other row operations."
    )
    @NotNull
    private Property<String> rowId;

    @Override
    public Output run(RunContext runContext) throws Exception {
        Logger logger = runContext.logger();
        String docId = getDocId(runContext);
        String tableIdValue = runContext.render(tableId).as(String.class).orElseThrow();
        String rowIdValue = runContext.render(rowId).as(String.class).orElseThrow();
        CodaConnection connection = createConnection(runContext);

        logger.info("Deleting row {} from table {} in document {}", rowIdValue, tableIdValue, docId);

        String endpoint = String.format("/docs/%s/tables/%s/rows/%s", docId, tableIdValue, rowIdValue);

        logger.debug("Sending delete request to: {}", endpoint);

        DeleteRowResponse response = connection.delete(endpoint, DeleteRowResponse.class);

        logger.info("Successfully deleted row {}. Request ID: {}", rowIdValue, response.getRequestId());

        return Output.builder()
            .rowId(response.getId())
            .requestId(response.getRequestId())
            .deleted(true)
            .build();
    }

    /**
     * Response from the Coda API when deleting a row.
     */
    @Builder
    @Getter
    private static class DeleteRowResponse {
        private String requestId;
        private String id;
    }

    @Builder
    @Getter
    public static class Output implements io.kestra.core.models.tasks.Output {
        @Schema(
            title = "Row ID",
            description = "The ID of the deleted row"
        )
        private String rowId;

        @Schema(
            title = "Request ID",
            description = "Unique identifier for this API request"
        )
        private String requestId;

        @Schema(
            title = "Deleted",
            description = "Confirmation that the row was deleted"
        )
        private Boolean deleted;
    }
}
