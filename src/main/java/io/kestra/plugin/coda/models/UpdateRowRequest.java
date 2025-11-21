package io.kestra.plugin.coda.models;

import com.google.gson.annotations.SerializedName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

/**
 * Request body for updating a row in a Coda table.
 */
@Builder
@Getter
@Jacksonized
public class UpdateRowRequest {
    @Schema(
        title = "Row",
        description = "The row data to update"
    )
    @SerializedName("row")
    private RowData row;

    /**
     * Represents the row data to update.
     */
    @Builder
    @Getter
    @Jacksonized
    public static class RowData {
        @Schema(
            title = "Cells",
            description = "The cell values to update"
        )
        @SerializedName("cells")
        @Singular
        private List<CodaRowCell> cells;
    }
}
