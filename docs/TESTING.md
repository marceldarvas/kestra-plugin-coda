# Testing Guide for Kestra Coda Plugin

This document describes the testing framework and how to run tests for the Kestra Coda plugin.

## Table of Contents
- [Test Structure](#test-structure)
- [Running Tests](#running-tests)
- [Integration Tests](#integration-tests)
- [Test Utilities](#test-utilities)
- [Writing New Tests](#writing-new-tests)
- [Test Data](#test-data)
- [Continuous Integration](#continuous-integration)

---

## Test Structure

The plugin uses a multi-layered testing approach:

### 1. Unit Tests
- **Location**: `src/test/java/io/kestra/plugin/coda/*/`
- **Purpose**: Test individual tasks in isolation
- **Execution**: Run automatically with `./gradlew test`
- **Dependencies**: None (mocked)

### 2. Integration Tests
- **Location**: Same test files, guarded by `requireIntegrationTest()` (JUnit assumptions)
- **Purpose**: Test against real Coda API
- **Execution**: Requires API credentials via environment variables
- **Dependencies**: Valid Coda API token and test document
- **Behavior**: Automatically **skip** (not fail) when environment variables are not set

### Test Organization
```
src/test/java/io/kestra/plugin/coda/
├── CodaTestBase.java                 # Base class for all tests
├── tables/
│   ├── ListTablesTest.java           # Unit + integration tests
│   └── GetTableTest.java
├── columns/
│   ├── ListColumnsTest.java
│   └── GetColumnTest.java
├── rows/
│   ├── ListRowsTest.java
│   └── GetRowTest.java
└── utils/
    ├── MockCodaServer.java           # Mock response utilities
    └── TestDataFactory.java          # Test data generators
```

---

## Running Tests

### Run All Tests
```bash
./gradlew test
```

### Run Specific Test Class
```bash
./gradlew test --tests "io.kestra.plugin.coda.tables.ListTablesTest"
```

### Run Integration Tests Only
Integration tests are disabled by default and require environment variables:

```bash
export CODA_API_TOKEN="your-api-token-here"
export CODA_DOC_ID="your-test-doc-id"
export CODA_TABLE_ID="your-test-table-id"  # Optional
export CODA_COLUMN_ID="your-test-column-id"  # Optional
export CODA_ROW_ID="your-test-row-id"  # Optional

./gradlew test
```

### Run with Coverage
```bash
./gradlew test jacocoTestReport
```

View coverage report at: `build/reports/jacoco/test/html/index.html`

---

## Integration Tests

### Prerequisites

1. **Coda API Token**
   - Go to https://coda.io/account
   - Navigate to "API Settings"
   - Generate a new API token
   - Set `CODA_API_TOKEN` environment variable

2. **Test Coda Document**
   - Create a new Coda document for testing
   - Add at least one table with sample data
   - Get the document ID from the URL: `https://coda.io/d/_dYOUR_DOC_ID`
   - Set `CODA_DOC_ID` environment variable

3. **Test Table** (Optional)
   - Find a table ID from your test document
   - Set `CODA_TABLE_ID` environment variable

### Environment Variables

| Variable | Required | Description |
|----------|----------|-------------|
| `CODA_API_TOKEN` | Yes | Your Coda API token |
| `CODA_DOC_ID` | Yes | Test document ID |
| `CODA_TABLE_ID` | No | Specific table ID for table/column/row tests |
| `CODA_COLUMN_ID` | No | Specific column ID for GetColumn tests |
| `CODA_ROW_ID` | No | Specific row ID for GetRow tests |

### Running Integration Tests

```bash
# Set up environment
export CODA_API_TOKEN="your-token"
export CODA_DOC_ID="abc123"
export CODA_TABLE_ID="grid-xyz"

# Run tests
./gradlew test
```

Integration tests automatically skip if environment variables are not set.

---

## Test Utilities

### CodaTestBase

Base class for all tests providing:
- RunContext setup
- Environment variable handling
- Common test utilities
- Integration test guards

```java
@KestraTest
public class MyTest extends CodaTestBase {
    @Test
    void testSomething() {
        requireIntegrationTest(); // Skip if env vars not set

        MyTask task = MyTask.builder()
            .apiToken(getApiToken())
            .docId(getDocId())
            .build();

        MyTask.Output output = task.run(runContext);
        // assertions...
    }
}
```

### TestDataFactory

Creates mock data for testing:

```java
// Create test tables
List<CodaTable> tables = TestDataFactory.createTables(5);

// Create test columns
List<CodaColumn> columns = TestDataFactory.createColumns(10);

// Create test rows
List<CodaRow> rows = TestDataFactory.createRows(20);

// Create custom row with specific values
Map<String, Object> values = Map.of(
    "Name", "Test Item",
    "Count", 42,
    "Date", "2024-01-01"
);
CodaRow row = TestDataFactory.createRow("i-123", "Test", 1, values);
```

### MockCodaServer

Utilities for creating mock responses:

```java
// Create paged response
PagedResponse<CodaTable> response = MockCodaServer.createTablesResponse(
    tables,
    "nextPageToken123"
);

// Convert to/from JSON
String json = MockCodaServer.toJson(response);
CodaTable table = MockCodaServer.fromJson(json, CodaTable.class);
```

---

## Writing New Tests

### Example Unit Test

```java
package io.kestra.plugin.coda.tables;

import io.kestra.core.junit.annotations.KestraTest;
import io.kestra.plugin.coda.CodaTestBase;
import io.kestra.plugin.coda.utils.TestDataFactory;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@KestraTest
class MyTaskTest extends CodaTestBase {

    @Test
    void testTaskLogic() {
        // Test the task logic without API calls
        // Use TestDataFactory for mock data
    }
}
```

### Example Integration Test

```java
@Test
void testListTables_Integration() {
    requireIntegrationTest(); // Skip if not configured

    ListTables task = ListTables.builder()
        .apiToken(getApiToken())
        .docId(getDocId())
        .fetchAllPages(property(true))
        .build();

    ListTables.Output output = task.run(runContext);

    assertThat(output.getTables(), is(notNullValue()));
    assertThat(output.getTotalCount(), greaterThan(0));
}
```

---

## Test Data

### Sample Test Document Structure

For comprehensive integration testing, create a Coda document with:

1. **Tables Table**
   - Name: "Test Tables"
   - Contains sample data rows

2. **Columns** with various types:
   - Text column
   - Number column
   - Date column
   - Select column
   - Calculated column (formula)

3. **Rows** with sample data:
   - At least 10-20 rows for pagination testing
   - Various data types
   - Some rows with empty values

### Creating Test Data

```bash
# Use the Coda web UI to create:
1. New document
2. Add table with columns: Name (text), Count (number), Date (date)
3. Add 20+ sample rows
4. Get IDs from URL and API responses
```

---

## Continuous Integration

### GitHub Actions Example

```yaml
name: Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3

      - name: Set up JDK 21
        uses: actions/setup-java@v3
        with:
          java-version: '21'

      - name: Run unit tests
        run: ./gradlew test

      - name: Run integration tests
        if: github.ref == 'refs/heads/main'
        env:
          CODA_API_TOKEN: ${{ secrets.CODA_API_TOKEN }}
          CODA_DOC_ID: ${{ secrets.CODA_DOC_ID }}
        run: ./gradlew test

      - name: Upload coverage
        uses: codecov/codecov-action@v3
        with:
          files: ./build/reports/jacoco/test/jacocoTestReport.xml
```

### Required Secrets

For CI/CD, add these secrets to your GitHub repository:
- `CODA_API_TOKEN`: API token for integration tests
- `CODA_DOC_ID`: Test document ID

---

## Test Coverage Goals

Target coverage levels:
- **Unit Tests**: 80% code coverage
- **Integration Tests**: Critical paths (create, read, update, delete)
- **Edge Cases**: Error handling, pagination, empty results

Current Coverage: Run `./gradlew jacocoTestReport` to view

---

## Troubleshooting

### Tests Fail with "401 Unauthorized"
- Check `CODA_API_TOKEN` is set correctly
- Verify token is valid at https://coda.io/account
- Ensure token has required permissions

### Tests Fail with "404 Not Found"
- Verify `CODA_DOC_ID` exists and is accessible
- Check table/column/row IDs are correct
- Ensure test document wasn't deleted

### Integration Tests Always Skip
- Confirm environment variables are exported in current shell
- Run `echo $CODA_API_TOKEN` to verify
- Check test output for assumption failure messages

### Slow Test Execution
- Integration tests make real API calls (slower)
- Use unit tests for development
- Run integration tests only before commits

---

## Best Practices

1. **Use CodaTestBase** for all tests
2. **Mark integration tests** appropriately
3. **Don't commit credentials** (use env vars)
4. **Clean up test data** (if modifying data in future phases)
5. **Test error cases** (401, 404, 429 responses)
6. **Test pagination** with fetchAllPages option
7. **Verify output structure** (not just success)
8. **Use descriptive test names** (testListTables_WithPagination_ReturnsAllPages)

---

## Future Improvements

- [ ] Add MockWebServer for true unit testing without API calls
- [ ] Create test data setup/teardown utilities
- [ ] Add performance benchmarks
- [ ] Implement contract tests with Pact
- [ ] Add mutation testing with Pitest

---

**Last Updated**: 2026-02-10
**Applies To**: Phase 2 (Read Operations)
