package io.kestra.plugin.superhuman.docs;

import io.kestra.core.models.property.Property;
import io.kestra.core.models.tasks.Task;
import io.kestra.core.runners.RunContext;
import io.kestra.plugin.superhuman.docs.client.SuperhumanDocsConnection;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.slf4j.Logger;

import jakarta.validation.constraints.NotNull;

/**
 * Base class for all Superhuman Docs tasks.
 * Provides common functionality for authentication and connection management.
 */
@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
public abstract class SuperhumanDocsTask extends Task {
    @Schema(
        title = "Superhuman Docs API Token",
        description = "Your Superhuman Docs (formerly Coda) API token. Generate one in your Superhuman Docs account settings. " +
            "Use {{ secret('SUPERHUMAN_DOCS_API_TOKEN') }} to reference a secret."
    )
    @NotNull
    protected Property<String> apiToken;

    @Schema(
        title = "Document ID",
        description = "The ID of the Superhuman Docs document. " +
            "You can find it in the document URL on https://docs.superhuman.com: it is the part after `_d` " +
            "(legacy coda.io document links redirect there)."
    )
    @NotNull
    protected Property<String> docId;

    /**
     * Creates a new Superhuman Docs API connection using the provided credentials.
     *
     * @param runContext The Kestra run context
     * @return A configured SuperhumanDocsConnection instance
     * @throws Exception if the connection cannot be created
     */
    protected SuperhumanDocsConnection createConnection(RunContext runContext) throws Exception {
        Logger logger = runContext.logger();
        String renderedApiToken = runContext.render(apiToken).as(String.class).orElseThrow();

        logger.debug("Creating Superhuman Docs API connection");
        return new SuperhumanDocsConnection(renderedApiToken, logger);
    }

    /**
     * Renders the document ID from the property.
     *
     * @param runContext The Kestra run context
     * @return The rendered document ID
     * @throws Exception if the document ID cannot be rendered
     */
    protected String getDocId(RunContext runContext) throws Exception {
        return runContext.render(docId).as(String.class).orElseThrow();
    }
}
