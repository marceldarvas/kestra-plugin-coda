package io.kestra.plugin.coda.models;

import com.google.gson.annotations.SerializedName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

/**
 * Represents a table in a Coda document.
 */
@Builder
@Getter
@Jacksonized
public class CodaTable {
    @Schema(
        title = "Table ID",
        description = "The unique identifier for this table"
    )
    @SerializedName("id")
    private String id;

    @Schema(
        title = "Table Type",
        description = "The type of this table (table or view)"
    )
    @SerializedName("type")
    private String type;

    @Schema(
        title = "Table Name",
        description = "The name of the table"
    )
    @SerializedName("name")
    private String name;

    @Schema(
        title = "Parent Table ID",
        description = "The ID of the parent table (for views)"
    )
    @SerializedName("parent")
    private String parent;

    @Schema(
        title = "Parent Table Name",
        description = "The name of the parent table (for views)"
    )
    @SerializedName("parentName")
    private String parentName;

    @Schema(
        title = "Table URL",
        description = "The browser URL for this table"
    )
    @SerializedName("browserLink")
    private String browserLink;

    @Schema(
        title = "Created At",
        description = "Timestamp when the table was created"
    )
    @SerializedName("createdAt")
    private String createdAt;

    @Schema(
        title = "Updated At",
        description = "Timestamp when the table was last updated"
    )
    @SerializedName("updatedAt")
    private String updatedAt;
}
