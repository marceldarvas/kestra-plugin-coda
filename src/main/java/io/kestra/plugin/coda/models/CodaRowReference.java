package io.kestra.plugin.coda.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;

/**
 * Reference to a parent row in a hierarchical table.
 */
@Builder
@Getter
@ToString
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class CodaRowReference {
    @Schema(
        title = "Row ID",
        description = "The ID of the referenced row"
    )
    @JsonProperty("id")
    private String id;

    @Schema(
        title = "Row Name",
        description = "The display name of the referenced row"
    )
    @JsonProperty("name")
    private String name;

    @Schema(
        title = "Browser Link",
        description = "The browser URL for the referenced row"
    )
    @JsonProperty("browserLink")
    private String browserLink;
}
