# Test Utilities

This package contains utilities for testing the Kestra Coda plugin.

## Classes

### MockCodaServer
Provides static utility methods for creating mock API responses and test data.

**Usage:**
```java
// Create mock paged response
PagedResponse<CodaTable> response = MockCodaServer.createTablesResponse(tables, null);

// Convert to/from JSON
String json = MockCodaServer.toJson(response);
CodaTable table = MockCodaServer.fromJson(json, CodaTable.class);
```

### TestDataFactory
Factory class for creating test data fixtures.

**Usage:**
```java
// Create mock tables
List<CodaTable> tables = TestDataFactory.createTables(5);

// Create mock columns
List<CodaColumn> columns = TestDataFactory.createColumns(10);

// Create mock rows
List<CodaRow> rows = TestDataFactory.createRows(20);

// Create custom data
Map<String, Object> values = Map.of("Name", "Test", "Count", 42);
CodaRow row = TestDataFactory.createRow("i-123", "Test Row", 1, values);
```

## See Also

- [Testing Guide](../../../../docs/TESTING.md) - Comprehensive testing documentation
- [CodaTestBase](../CodaTestBase.java) - Base class for all tests
