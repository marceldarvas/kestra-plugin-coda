package io.kestra.plugin.coda.models;

import com.google.gson.annotations.SerializedName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

/**
 * Represents a column in a Coda table.
 */
@Builder
@Getter
@Jacksonized
public class CodaColumn {
    @Schema(
        title = "Column ID",
        description = "The unique identifier for this column"
    )
    @SerializedName("id")
    private String id;

    @Schema(
        title = "Column Type",
        description = "The type of this column (text, number, date, etc.)"
    )
    @SerializedName("type")
    private String type;

    @Schema(
        title = "Column Name",
        description = "The name of the column"
    )
    @SerializedName("name")
    private String name;

    @Schema(
        title = "Display",
        description = "Display format for the column"
    )
    @SerializedName("display")
    private String display;

    @Schema(
        title = "Calculated",
        description = "Whether this column is calculated (read-only)"
    )
    @SerializedName("calculated")
    private Boolean calculated;

    @Schema(
        title = "Formula",
        description = "The formula for calculated columns"
    )
    @SerializedName("formula")
    private String formula;

    @Schema(
        title = "Default Value",
        description = "The default value for this column"
    )
    @SerializedName("defaultValue")
    private Object defaultValue;
}
