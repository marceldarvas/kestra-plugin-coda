package io.kestra.plugin.coda.models;

import com.google.gson.annotations.SerializedName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

/**
 * Represents a cell value when inserting or updating rows.
 */
@Builder
@Getter
@Jacksonized
public class CodaRowCell {
    @Schema(
        title = "Column ID or Name",
        description = "The column identifier (can be column ID or name)"
    )
    @SerializedName("column")
    private String column;

    @Schema(
        title = "Value",
        description = "The value to set for this cell"
    )
    @SerializedName("value")
    private Object value;
}
