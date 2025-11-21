package io.kestra.plugin.coda.models;

import com.google.gson.annotations.SerializedName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import java.util.List;
import java.util.Map;

/**
 * Represents a row in a Coda table.
 */
@Builder
@Getter
@Jacksonized
public class CodaRow {
    @Schema(
        title = "Row ID",
        description = "The unique identifier for this row"
    )
    @SerializedName("id")
    private String id;

    @Schema(
        title = "Row Type",
        description = "The type of this row (typically 'row')"
    )
    @SerializedName("type")
    private String type;

    @Schema(
        title = "Row Name",
        description = "The display name of the row"
    )
    @SerializedName("name")
    private String name;

    @Schema(
        title = "Row Index",
        description = "The index of this row in the table"
    )
    @SerializedName("index")
    private Integer index;

    @Schema(
        title = "Browser Link",
        description = "The browser URL for this row"
    )
    @SerializedName("browserLink")
    private String browserLink;

    @Schema(
        title = "Created At",
        description = "Timestamp when the row was created"
    )
    @SerializedName("createdAt")
    private String createdAt;

    @Schema(
        title = "Updated At",
        description = "Timestamp when the row was last updated"
    )
    @SerializedName("updatedAt")
    private String updatedAt;

    @Schema(
        title = "Values",
        description = "The cell values in this row, mapped by column ID or name"
    )
    @SerializedName("values")
    private Map<String, Object> values;

    @Schema(
        title = "Parent",
        description = "Information about the parent row (for hierarchical tables)"
    )
    @SerializedName("parent")
    private CodaRowReference parent;
}
