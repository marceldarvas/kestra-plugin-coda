package io.kestra.plugin.coda.models;

import com.google.gson.annotations.SerializedName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

/**
 * Request body for inserting rows into a Coda table.
 */
@Builder
@Getter
@Jacksonized
public class InsertRowsRequest {
    @Schema(
        title = "Rows",
        description = "The rows to insert"
    )
    @SerializedName("rows")
    @Singular
    private List<RowData> rows;

    @Schema(
        title = "Key Columns",
        description = "Optional list of column IDs to use as merge keys for upsert operations"
    )
    @SerializedName("keyColumns")
    @Singular
    private List<String> keyColumns;

    /**
     * Represents a single row to insert.
     */
    @Builder
    @Getter
    @Jacksonized
    public static class RowData {
        @Schema(
            title = "Cells",
            description = "The cell values for this row"
        )
        @SerializedName("cells")
        @Singular
        private List<CodaRowCell> cells;
    }
}
