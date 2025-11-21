package io.kestra.plugin.coda.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

/**
 * Request body for updating a row in a Coda table.
 */
@Builder
@Getter
@ToString
@Jacksonized
public class UpdateRowRequest {
    @Schema(
        title = "Row",
        description = "The row data to update"
    )
    @JsonProperty("row")
    private RowData row;

    /**
     * Represents the row data to update.
     */
    @Builder
    @Getter
    @ToString
    @Jacksonized
    public static class RowData {
        @Schema(
            title = "Cells",
            description = "The cell values to update"
        )
        @JsonProperty("cells")
        @Singular
        private List<CodaRowCell> cells;
    }
}
