package io.kestra.plugin.coda;

import io.kestra.core.models.property.Property;
import io.kestra.core.models.tasks.Task;
import io.kestra.core.runners.RunContext;
import io.kestra.plugin.coda.client.CodaConnection;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.slf4j.Logger;

import jakarta.validation.constraints.NotNull;

/**
 * Base class for all Coda tasks.
 * Provides common functionality for authentication and connection management.
 */
@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
public abstract class CodaTask extends Task {
    @Schema(
        title = "Coda API Token",
        description = "Your Coda API token. Get it from https://coda.io/account. " +
            "We recommend using Kestra secrets to store this value securely."
    )
    @NotNull
    protected Property<String> apiToken;

    @Schema(
        title = "Document ID",
        description = "The ID of the Coda document. " +
            "You can find this in the document URL: https://coda.io/d/_dYOUR_DOC_ID"
    )
    @NotNull
    protected Property<String> docId;

    /**
     * Creates a new Coda API connection using the provided credentials.
     *
     * @param runContext The Kestra run context
     * @return A configured CodaConnection instance
     * @throws Exception if the connection cannot be created
     */
    protected CodaConnection createConnection(RunContext runContext) throws Exception {
        Logger logger = runContext.logger();
        String renderedApiToken = runContext.render(apiToken).as(String.class).orElseThrow();

        logger.debug("Creating Coda API connection");
        return new CodaConnection(renderedApiToken, logger);
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
