package io.kestra.plugin.coda;

import io.kestra.core.models.property.Property;
import io.kestra.core.runners.RunContext;
import io.kestra.core.runners.RunContextFactory;
import io.kestra.core.junit.annotations.KestraTest;
import org.junit.jupiter.api.BeforeEach;

import jakarta.inject.Inject;

/**
 * Base class for Coda plugin tests.
 * Provides common setup and utilities for testing Coda tasks.
 */
@KestraTest
public abstract class CodaTestBase {
    @Inject
    protected RunContextFactory runContextFactory;

    protected RunContext runContext;

    /**
     * Test configuration - override these in your test class or via environment variables
     */
    protected String testApiToken = System.getenv("CODA_API_TOKEN");
    protected String testDocId = System.getenv("CODA_DOC_ID");
    protected String testTableId = System.getenv("CODA_TABLE_ID");
    protected String testColumnId = System.getenv("CODA_COLUMN_ID");
    protected String testRowId = System.getenv("CODA_ROW_ID");

    @BeforeEach
    public void setUp() {
        runContext = runContextFactory.of();
    }

    /**
     * Check if integration tests should run.
     * Integration tests require CODA_API_TOKEN and CODA_DOC_ID environment variables.
     */
    protected boolean shouldRunIntegrationTests() {
        return testApiToken != null && testDocId != null;
    }

    /**
     * Skip test if integration test requirements are not met.
     */
    protected void requireIntegrationTest() {
        org.junit.jupiter.api.Assumptions.assumeTrue(
            shouldRunIntegrationTests(),
            "Skipping integration test - CODA_API_TOKEN and CODA_DOC_ID environment variables not set"
        );
    }

    /**
     * Create Property from string value.
     */
    protected <T> Property<T> property(T value) {
        return Property.of(value);
    }

    /**
     * Get test API token as Property.
     */
    protected Property<String> getApiToken() {
        return Property.of(testApiToken);
    }

    /**
     * Get test doc ID as Property.
     */
    protected Property<String> getDocId() {
        return Property.of(testDocId);
    }

    /**
     * Get test table ID as Property.
     */
    protected Property<String> getTableId() {
        return Property.of(testTableId);
    }

    /**
     * Get test column ID as Property.
     */
    protected Property<String> getColumnId() {
        return Property.of(testColumnId);
    }

    /**
     * Get test row ID as Property.
     */
    protected Property<String> getRowId() {
        return Property.of(testRowId);
    }
}
