package io.kestra.plugin.coda.columns;

import com.google.gson.reflect.TypeToken;
import io.kestra.core.models.annotations.Example;
import io.kestra.core.models.annotations.Plugin;
import io.kestra.core.models.annotations.PluginProperty;
import io.kestra.core.models.property.Property;
import io.kestra.core.models.tasks.RunnableTask;
import io.kestra.core.runners.RunContext;
import io.kestra.plugin.coda.CodaTask;
import io.kestra.plugin.coda.client.CodaConnection;
import io.kestra.plugin.coda.models.CodaColumn;
import io.kestra.plugin.coda.models.PagedResponse;
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
 * List all columns in a Coda table.
 *
 * This task retrieves all columns from the specified table, including their types,
 * names, and whether they are calculated (read-only) columns.
 */
@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@Schema(
    title = "List Columns",
    description = "List all columns in a Coda table. Returns metadata about each column including ID, name, type, and whether it's calculated."
)
@Plugin(
    examples = {
        @Example(
            title = "List all columns in a table",
            code = {
                "apiToken: \"{{ secret('CODA_API_TOKEN') }}\"",
                "docId: \"abc123\"",
                "tableId: \"grid-pqRst-U\""
            }
        ),
        @Example(
            title = "List all columns with pagination",
            code = {
                "apiToken: \"{{ secret('CODA_API_TOKEN') }}\"",
                "docId: \"abc123\"",
                "tableId: \"grid-pqRst-U\"",
                "fetchAllPages: true"
            }
        )
    }
)
public class ListColumns extends CodaTask implements RunnableTask<ListColumns.Output> {

    @Schema(
        title = "Table ID",
        description = "The ID of the table to list columns from. " +
            "Table IDs typically start with 'grid-' or 'table-'."
    )
    @NotNull
    private Property<String> tableId;

    @Schema(
        title = "Fetch All Pages",
        description = "Whether to automatically fetch all pages of results. " +
            "If false, only the first page will be returned. Default is false."
    )
    @PluginProperty
    @Builder.Default
    private Boolean fetchAllPages = false;

    @Schema(
        title = "Page Limit",
        description = "Maximum number of columns to return per page. Default is 25."
    )
    @PluginProperty
    @Builder.Default
    private Integer limit = 25;

    @Override
    public Output run(RunContext runContext) throws Exception {
        Logger logger = runContext.logger();
        String docId = getDocId(runContext);
        String tableIdValue = runContext.render(tableId).as(String.class).orElseThrow();
        CodaConnection connection = createConnection(runContext);

        logger.info("Listing columns for table {} in document {}", tableIdValue, docId);

        List<CodaColumn> allColumns = new ArrayList<>();
        String endpoint = String.format("/docs/%s/tables/%s/columns?limit=%d", docId, tableIdValue, limit);
        String nextPageLink = null;
        int pageCount = 0;

        do {
            pageCount++;
            String currentEndpoint = nextPageLink != null ? nextPageLink : endpoint;

            logger.debug("Fetching page {} from: {}", pageCount, currentEndpoint);

            PagedResponse<CodaColumn> response = connection.get(
                currentEndpoint,
                new TypeToken<PagedResponse<CodaColumn>>() {}
            );

            if (response.getItems() != null) {
                allColumns.addAll(response.getItems());
                logger.debug("Retrieved {} columns from page {}", response.getItems().size(), pageCount);
            }

            nextPageLink = response.getNextPageLink();

            // Only continue if fetchAllPages is true and there are more pages
            if (!fetchAllPages) {
                break;
            }

        } while (nextPageLink != null);

        logger.info("Successfully retrieved {} column(s) across {} page(s)", allColumns.size(), pageCount);

        return Output.builder()
            .columns(allColumns)
            .totalCount(allColumns.size())
            .pageCount(pageCount)
            .build();
    }

    @Builder
    @Getter
    public static class Output implements io.kestra.core.models.tasks.Output {
        @Schema(
            title = "Columns",
            description = "List of columns in the table"
        )
        private List<CodaColumn> columns;

        @Schema(
            title = "Total Count",
            description = "Total number of columns retrieved"
        )
        private Integer totalCount;

        @Schema(
            title = "Page Count",
            description = "Number of pages fetched"
        )
        private Integer pageCount;
    }
}
