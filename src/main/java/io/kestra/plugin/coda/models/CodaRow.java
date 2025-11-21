package io.kestra.plugin.coda.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;

import java.util.List;
import java.util.Map;

/**
 * Represents a row in a Coda table.
 */
@Builder
@Getter
@ToString
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class CodaRow {
    @Schema(
        title = "Row ID",
        description = "The unique identifier for this row"
    )
    @JsonProperty("id")
    private String id;

    @Schema(
        title = "Row Type",
        description = "The type of this row (typically 'row')"
    )
    @JsonProperty("type")
    private String type;

    @Schema(
        title = "Row Name",
        description = "The display name of the row"
    )
    @JsonProperty("name")
    private String name;

    @Schema(
        title = "Row Index",
        description = "The index of this row in the table"
    )
    @JsonProperty("index")
    private Integer index;

    @Schema(
        title = "Browser Link",
        description = "The browser URL for this row"
    )
    @JsonProperty("browserLink")
    private String browserLink;

    @Schema(
        title = "Created At",
        description = "Timestamp when the row was created"
    )
    @JsonProperty("createdAt")
    private String createdAt;

    @Schema(
        title = "Updated At",
        description = "Timestamp when the row was last updated"
    )
    @JsonProperty("updatedAt")
    private String updatedAt;

    @Schema(
        title = "Values",
        description = "The cell values in this row, mapped by column ID or name"
    )
    @JsonProperty("values")
    private Map<String, Object> values;

    @Schema(
        title = "Parent",
        description = "Information about the parent row (for hierarchical tables)"
    )
    @JsonProperty("parent")
    private CodaRowReference parent;
}
