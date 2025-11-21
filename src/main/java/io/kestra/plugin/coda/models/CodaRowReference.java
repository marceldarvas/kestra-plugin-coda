package io.kestra.plugin.coda.models;

import com.google.gson.annotations.SerializedName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

/**
 * Reference to a parent row in a hierarchical table.
 */
@Builder
@Getter
@Jacksonized
public class CodaRowReference {
    @Schema(
        title = "Row ID",
        description = "The ID of the referenced row"
    )
    @SerializedName("id")
    private String id;

    @Schema(
        title = "Row Name",
        description = "The display name of the referenced row"
    )
    @SerializedName("name")
    private String name;

    @Schema(
        title = "Browser Link",
        description = "The browser URL for the referenced row"
    )
    @SerializedName("browserLink")
    private String browserLink;
}
