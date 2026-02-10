package io.kestra.plugin.coda.utils;

import io.kestra.plugin.coda.models.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Factory for creating test data fixtures.
 */
public class TestDataFactory {

    /**
     * Create a sample CodaTable.
     */
    public static CodaTable createTable(String id, String name, String type) {
        return CodaTable.builder()
            .id(id)
            .name(name)
            .type(type)
            .browserLink("https://coda.io/d/_dABC123/" + id)
            .createdAt("2024-01-01T00:00:00.000Z")
            .updatedAt("2024-01-02T00:00:00.000Z")
            .build();
    }

    /**
     * Create a sample CodaColumn.
     */
    public static CodaColumn createColumn(String id, String name, String type, boolean calculated) {
        return CodaColumn.builder()
            .id(id)
            .name(name)
            .type(type)
            .calculated(calculated)
            .display(type)
            .build();
    }

    /**
     * Create a sample CodaRow.
     */
    public static CodaRow createRow(String id, String name, int index, Map<String, Object> values) {
        return CodaRow.builder()
            .id(id)
            .name(name)
            .type("row")
            .index(index)
            .values(values)
            .browserLink("https://coda.io/d/_dABC123/table#" + id)
            .createdAt("2024-01-01T00:00:00.000Z")
            .updatedAt("2024-01-02T00:00:00.000Z")
            .build();
    }

    /**
     * Create a list of sample tables.
     */
    public static List<CodaTable> createTables(int count) {
        List<CodaTable> tables = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            tables.add(createTable(
                "grid-" + i,
                "Table " + i,
                "table"
            ));
        }
        return tables;
    }

    /**
     * Create a list of sample columns.
     */
    public static List<CodaColumn> createColumns(int count) {
        List<CodaColumn> columns = new ArrayList<>();
        String[] types = {"text", "number", "date", "select", "person"};

        for (int i = 1; i <= count; i++) {
            columns.add(createColumn(
                "c-" + i,
                "Column " + i,
                types[(i - 1) % types.length],
                i % 3 == 0 // Every third column is calculated
            ));
        }
        return columns;
    }

    /**
     * Create a list of sample rows.
     */
    public static List<CodaRow> createRows(int count) {
        List<CodaRow> rows = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            Map<String, Object> values = new HashMap<>();
            values.put("c-1", "Value " + i);
            values.put("c-2", i * 100);
            values.put("c-3", "2024-01-0" + (i % 9 + 1));

            rows.add(createRow(
                "i-" + i,
                "Row " + i,
                i,
                values
            ));
        }
        return rows;
    }

    /**
     * Create a PagedResponse with items.
     */
    public static <T> PagedResponse<T> createPagedResponse(List<T> items, String nextPageToken) {
        return PagedResponse.<T>builder()
            .items(items)
            .nextPageToken(nextPageToken)
            .nextPageLink(nextPageToken != null ? "https://coda.io/apis/v1/next?pageToken=" + nextPageToken : null)
            .build();
    }

    /**
     * Create an InsertRowsRequest.
     */
    public static InsertRowsRequest createInsertRowsRequest(List<Map<String, Object>> rows) {
        List<CodaRowCell> rowCells = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            List<CodaRowCell.Cell> cells = new ArrayList<>();
            for (Map.Entry<String, Object> entry : row.entrySet()) {
                cells.add(CodaRowCell.Cell.builder()
                    .column(entry.getKey())
                    .value(entry.getValue())
                    .build());
            }
            rowCells.add(CodaRowCell.builder()
                .cells(cells)
                .build());
        }

        return InsertRowsRequest.builder()
            .rows(rowCells)
            .build();
    }

    /**
     * Create an UpdateRowRequest.
     */
    public static UpdateRowRequest createUpdateRowRequest(Map<String, Object> values) {
        List<CodaRowCell.Cell> cells = new ArrayList<>();
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            cells.add(CodaRowCell.Cell.builder()
                .column(entry.getKey())
                .value(entry.getValue())
                .build());
        }

        return UpdateRowRequest.builder()
            .row(CodaRowCell.builder()
                .cells(cells)
                .build())
            .build();
    }
}
