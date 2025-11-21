package io.kestra.plugin.coda.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;

/**
 * Represents a cell value when inserting or updating rows.
 */
@Builder
@Getter
@ToString
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class CodaRowCell {
    @Schema(
        title = "Column ID or Name",
        description = "The column identifier (can be column ID or name)"
    )
    @JsonProperty("column")
    private String column;

    @Schema(
        title = "Value",
        description = "The value to set for this cell"
    )
    @JsonProperty("value")
    private Object value;
}
