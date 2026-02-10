package io.kestra.plugin.coda.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.kestra.plugin.coda.models.*;

import java.util.List;

/**
 * Mock Coda API server for testing.
 * Provides utilities for creating mock responses and test data.
 *
 * Note: This class provides mock data generation. For integration tests
 * that require an actual mock HTTP server, use Micronaut's @MockBean
 * or embedded test servers.
 */
public class MockCodaServer {
    private static final ObjectMapper MAPPER = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .registerModule(new JavaTimeModule());

    private MockCodaServer() {
        // Utility class
    }

    /**
     * Convert an object to JSON string.
     */
    public static String toJson(Object obj) throws Exception {
        return MAPPER.writeValueAsString(obj);
    }

    /**
     * Parse JSON string to object.
     */
    public static <T> T fromJson(String json, Class<T> clazz) throws Exception {
        return MAPPER.readValue(json, clazz);
    }

    /**
     * Create a mock PagedResponse for tables.
     */
    public static PagedResponse<CodaTable> createTablesResponse(List<CodaTable> tables, String nextPageToken) {
        return PagedResponse.<CodaTable>builder()
            .items(tables)
            .nextPageToken(nextPageToken)
            .nextPageLink(nextPageToken != null ? "https://coda.io/apis/v1/next?pageToken=" + nextPageToken : null)
            .build();
    }

    /**
     * Create a mock PagedResponse for columns.
     */
    public static PagedResponse<CodaColumn> createColumnsResponse(List<CodaColumn> columns, String nextPageToken) {
        return PagedResponse.<CodaColumn>builder()
            .items(columns)
            .nextPageToken(nextPageToken)
            .nextPageLink(nextPageToken != null ? "https://coda.io/apis/v1/next?pageToken=" + nextPageToken : null)
            .build();
    }

    /**
     * Create a mock PagedResponse for rows.
     */
    public static PagedResponse<CodaRow> createRowsResponse(List<CodaRow> rows, String nextPageToken) {
        return PagedResponse.<CodaRow>builder()
            .items(rows)
            .nextPageToken(nextPageToken)
            .nextPageLink(nextPageToken != null ? "https://coda.io/apis/v1/next?pageToken=" + nextPageToken : null)
            .build();
    }

    /**
     * Create an error response body.
     */
    public static String createErrorResponse(String message) {
        return "{\"message\": \"" + message + "\"}";
    }
}
