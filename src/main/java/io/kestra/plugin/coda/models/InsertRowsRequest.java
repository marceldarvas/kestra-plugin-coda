package io.kestra.plugin.coda.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

/**
 * Request body for inserting rows into a Coda table.
 */
@Builder
@Getter
@ToString
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class InsertRowsRequest {
    @Schema(
        title = "Rows",
        description = "The rows to insert"
    )
    @JsonProperty("rows")
    @Singular
    private List<RowData> rows;

    @Schema(
        title = "Key Columns",
        description = "Optional list of column IDs to use as merge keys for upsert operations"
    )
    @JsonProperty("keyColumns")
    @Singular
    private List<String> keyColumns;

    /**
     * Represents a single row to insert.
     */
    @Builder
    @Getter
    @ToString
    @Jacksonized
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RowData {
        @Schema(
            title = "Cells",
            description = "The cell values for this row"
        )
        @JsonProperty("cells")
        @Singular
        private List<CodaRowCell> cells;
    }
}
