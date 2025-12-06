package io.kestra.plugin.coda.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;

/**
 * Represents a column in a Coda table.
 */
@Builder
@Getter
@ToString
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class CodaColumn {
    @Schema(
        title = "Column ID",
        description = "The unique identifier for this column"
    )
    @JsonProperty("id")
    private String id;

    @Schema(
        title = "Column Type",
        description = "The type of this column (text, number, date, etc.)"
    )
    @JsonProperty("type")
    private String type;

    @Schema(
        title = "Column Name",
        description = "The name of the column"
    )
    @JsonProperty("name")
    private String name;

    @Schema(
        title = "Display",
        description = "Display format for the column"
    )
    @JsonProperty("display")
    private String display;

    @Schema(
        title = "Calculated",
        description = "Whether this column is calculated (read-only)"
    )
    @JsonProperty("calculated")
    private Boolean calculated;

    @Schema(
        title = "Formula",
        description = "The formula for calculated columns"
    )
    @JsonProperty("formula")
    private String formula;

    @Schema(
        title = "Default Value",
        description = "The default value for this column"
    )
    @JsonProperty("defaultValue")
    private Object defaultValue;
}
