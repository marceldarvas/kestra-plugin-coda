package io.kestra.plugin.coda.tables;

import com.google.gson.reflect.TypeToken;
import io.kestra.core.models.annotations.Example;
import io.kestra.core.models.annotations.Plugin;
import io.kestra.core.models.annotations.PluginProperty;
import io.kestra.core.models.tasks.RunnableTask;
import io.kestra.core.runners.RunContext;
import io.kestra.plugin.coda.CodaTask;
import io.kestra.plugin.coda.client.CodaConnection;
import io.kestra.plugin.coda.exceptions.CodaException;
import io.kestra.plugin.coda.models.CodaTable;
import io.kestra.plugin.coda.models.PagedResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * List all tables in a Coda document.
 *
 * This task retrieves all tables (including views) from the specified Coda document.
 * The task supports pagination and can optionally fetch all pages automatically.
 */
@SuperBuilder
@ToString
@EqualsAndHashCode(callSuper = true)
@Getter
@NoArgsConstructor
@Schema(
    title = "List Tables",
    description = "List all tables in a Coda document. Returns metadata about each table including ID, name, type, and browser link."
)
@Plugin(
    examples = {
        @Example(
            title = "List all tables in a document",
            code = {
                "apiToken: \"{{ secret('CODA_API_TOKEN') }}\"",
                "docId: \"abc123\""
            }
        ),
        @Example(
            title = "List all tables and fetch all pages",
            code = {
                "apiToken: \"{{ secret('CODA_API_TOKEN') }}\"",
                "docId: \"abc123\"",
                "fetchAllPages: true"
            }
        )
    }
)
public class ListTables extends CodaTask implements RunnableTask<ListTables.Output> {

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
        description = "Maximum number of tables to return per page. Default is 25."
    )
    @PluginProperty
    @Builder.Default
    private Integer limit = 25;

    @Override
    public Output run(RunContext runContext) throws Exception {
        Logger logger = runContext.logger();
        String docId = getDocId(runContext);
        CodaConnection connection = createConnection(runContext);

        logger.info("Listing tables for document: {}", docId);

        List<CodaTable> allTables = new ArrayList<>();
        String endpoint = String.format("/docs/%s/tables?limit=%d", docId, limit);
        String nextPageLink = null;
        int pageCount = 0;

        do {
            pageCount++;
            String currentEndpoint = nextPageLink != null ? nextPageLink : endpoint;

            logger.debug("Fetching page {} from: {}", pageCount, currentEndpoint);

            PagedResponse<CodaTable> response = connection.get(
                currentEndpoint,
                new TypeToken<PagedResponse<CodaTable>>() {}
            );

            if (response.getItems() != null) {
                allTables.addAll(response.getItems());
                logger.debug("Retrieved {} tables from page {}", response.getItems().size(), pageCount);
            }

            nextPageLink = response.getNextPageLink();

            // Only continue if fetchAllPages is true and there are more pages
            if (!fetchAllPages) {
                break;
            }

        } while (nextPageLink != null);

        logger.info("Successfully retrieved {} table(s) across {} page(s)", allTables.size(), pageCount);

        return Output.builder()
            .tables(allTables)
            .totalCount(allTables.size())
            .pageCount(pageCount)
            .build();
    }

    @Builder
    @Getter
    public static class Output implements io.kestra.core.models.tasks.Output {
        @Schema(
            title = "Tables",
            description = "List of tables in the document"
        )
        private List<CodaTable> tables;

        @Schema(
            title = "Total Count",
            description = "Total number of tables retrieved"
        )
        private Integer totalCount;

        @Schema(
            title = "Page Count",
            description = "Number of pages fetched"
        )
        private Integer pageCount;
    }
}
