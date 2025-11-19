package io.kestra.plugin.coda.rows;

import com.google.gson.reflect.TypeToken;
import io.kestra.core.models.annotations.Example;
import io.kestra.core.models.annotations.Plugin;
import io.kestra.core.models.annotations.PluginProperty;
import io.kestra.core.models.property.Property;
import io.kestra.core.models.tasks.RunnableTask;
import io.kestra.core.runners.RunContext;
import io.kestra.plugin.coda.CodaTask;
import io.kestra.plugin.coda.client.CodaConnection;
import io.kestra.plugin.coda.models.CodaRow;
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
 * List rows in a Coda table.
 *
 * This task retrieves rows from the specified table with support for pagination,
 * filtering, and sorting. You can optionally fetch all pages automatically.
 */
@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@Schema(
    title = "List Rows",
    description = "List rows in a Coda table. Returns row data including IDs, values, and metadata. " +
        "Supports pagination, filtering, and sorting."
)
@Plugin(
    examples = {
        @Example(
            title = "List all rows in a table",
            code = {
                "apiToken: \"{{ secret('CODA_API_TOKEN') }}\"",
                "docId: \"abc123\"",
                "tableId: \"grid-pqRst-U\""
            }
        ),
        @Example(
            title = "List rows with pagination (fetch all pages)",
            code = {
                "apiToken: \"{{ secret('CODA_API_TOKEN') }}\"",
                "docId: \"abc123\"",
                "tableId: \"grid-pqRst-U\"",
                "fetchAllPages: true",
                "limit: 100"
            }
        ),
        @Example(
            title = "List rows with value format",
            code = {
                "apiToken: \"{{ secret('CODA_API_TOKEN') }}\"",
                "docId: \"abc123\"",
                "tableId: \"grid-pqRst-U\"",
                "useColumnNames: true",
                "valueFormat: \"rich\""
            }
        ),
        @Example(
            title = "List only visible rows",
            code = {
                "apiToken: \"{{ secret('CODA_API_TOKEN') }}\"",
                "docId: \"abc123\"",
                "tableId: \"grid-pqRst-U\"",
                "visibleOnly: true"
            }
        )
    }
)
public class ListRows extends CodaTask implements RunnableTask<ListRows.Output> {

    @Schema(
        title = "Table ID",
        description = "The ID of the table to list rows from. " +
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
        description = "Maximum number of rows to return per page. Default is 25."
    )
    @PluginProperty
    @Builder.Default
    private Integer limit = 25;

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

    @Schema(
        title = "Visible Only",
        description = "If true, return only visible rows (respecting table filters). Default is false."
    )
    @PluginProperty
    @Builder.Default
    private Boolean visibleOnly = false;

    @Schema(
        title = "Sort By",
        description = "Column ID or name to sort by. Can be prefixed with '-' for descending order."
    )
    @PluginProperty
    private Property<String> sortBy;

    @Override
    public Output run(RunContext runContext) throws Exception {
        Logger logger = runContext.logger();
        String docId = getDocId(runContext);
        String tableIdValue = runContext.render(tableId).as(String.class).orElseThrow();
        CodaConnection connection = createConnection(runContext);

        logger.info("Listing rows for table {} in document {}", tableIdValue, docId);

        // Build the endpoint with query parameters
        StringBuilder endpointBuilder = new StringBuilder(
            String.format("/docs/%s/tables/%s/rows?limit=%d", docId, tableIdValue, limit)
        );

        if (useColumnNames) {
            endpointBuilder.append("&useColumnNames=true");
        }

        String valueFormatValue = runContext.render(valueFormat).as(String.class).orElse("simple");
        endpointBuilder.append("&valueFormat=").append(valueFormatValue);

        if (visibleOnly) {
            endpointBuilder.append("&visibleOnly=true");
        }

        if (sortBy != null) {
            String sortByValue = runContext.render(sortBy).as(String.class).orElse(null);
            if (sortByValue != null && !sortByValue.isEmpty()) {
                endpointBuilder.append("&sortBy=").append(sortByValue);
            }
        }

        List<CodaRow> allRows = new ArrayList<>();
        String endpoint = endpointBuilder.toString();
        String nextPageLink = null;
        int pageCount = 0;

        do {
            pageCount++;
            String currentEndpoint = nextPageLink != null ? nextPageLink : endpoint;

            logger.debug("Fetching page {} from: {}", pageCount, currentEndpoint);

            PagedResponse<CodaRow> response = connection.get(
                currentEndpoint,
                new TypeToken<PagedResponse<CodaRow>>() {}
            );

            if (response.getItems() != null) {
                allRows.addAll(response.getItems());
                logger.debug("Retrieved {} rows from page {}", response.getItems().size(), pageCount);
            }

            nextPageLink = response.getNextPageLink();

            // Only continue if fetchAllPages is true and there are more pages
            if (!fetchAllPages) {
                break;
            }

        } while (nextPageLink != null);

        logger.info("Successfully retrieved {} row(s) across {} page(s)", allRows.size(), pageCount);

        return Output.builder()
            .rows(allRows)
            .totalCount(allRows.size())
            .pageCount(pageCount)
            .build();
    }

    @Builder
    @Getter
    public static class Output implements io.kestra.core.models.tasks.Output {
        @Schema(
            title = "Rows",
            description = "List of rows in the table"
        )
        private List<CodaRow> rows;

        @Schema(
            title = "Total Count",
            description = "Total number of rows retrieved"
        )
        private Integer totalCount;

        @Schema(
            title = "Page Count",
            description = "Number of pages fetched"
        )
        private Integer pageCount;
    }
}
