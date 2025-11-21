package io.kestra.plugin.coda.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;

/**
 * Represents a table in a Coda document.
 */
@Builder
@Getter
@ToString
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class CodaTable {
    @Schema(
        title = "Table ID",
        description = "The unique identifier for this table"
    )
    @JsonProperty("id")
    private String id;

    @Schema(
        title = "Table Type",
        description = "The type of this table (table or view)"
    )
    @JsonProperty("type")
    private String type;

    @Schema(
        title = "Table Name",
        description = "The name of the table"
    )
    @JsonProperty("name")
    private String name;

    @Schema(
        title = "Parent Table ID",
        description = "The ID of the parent table (for views)"
    )
    @JsonProperty("parent")
    private String parent;

    @Schema(
        title = "Parent Table Name",
        description = "The name of the parent table (for views)"
    )
    @JsonProperty("parentName")
    private String parentName;

    @Schema(
        title = "Table URL",
        description = "The browser URL for this table"
    )
    @JsonProperty("browserLink")
    private String browserLink;

    @Schema(
        title = "Created At",
        description = "Timestamp when the table was created"
    )
    @JsonProperty("createdAt")
    private String createdAt;

    @Schema(
        title = "Updated At",
        description = "Timestamp when the table was last updated"
    )
    @JsonProperty("updatedAt")
    private String updatedAt;
}
