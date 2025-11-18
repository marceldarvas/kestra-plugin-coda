# Coda Plugin Implementation Roadmap

This document outlines the recommended approach for implementing Coda plugin features in Kestra.

## Phase 1: Foundation (Core Infrastructure)

### 1.1 Authentication & Configuration
- [ ] Create `CodaConnection` class for managing API credentials
- [ ] Implement secure API token storage using Kestra's secret management
- [ ] Add connection validation task
- [ ] Create base HTTP client with proper headers and authentication

**Dependencies:**
- OkHttp or similar HTTP client library
- Gson or Jackson for JSON serialization

**Example Structure:**
```java
@SuperBuilder
@NoArgsConstructor
public abstract class CodaTask extends Task {
    @Schema(title = "Coda API Token", description = "Your Coda API token")
    @PluginProperty(dynamic = true)
    @NotNull
    protected String apiToken;
    
    @Schema(title = "Document ID", description = "The Coda document ID")
    @PluginProperty(dynamic = true)
    @NotNull
    protected String docId;
    
    protected OkHttpClient buildClient() {
        return new OkHttpClient.Builder()
            .addInterceptor(new AuthInterceptor(apiToken))
            .build();
    }
}
```

### 1.2 Common Models & DTOs
- [ ] Create response models (Table, Column, Row, etc.)
- [ ] Create request models (InsertRowRequest, UpsertRowRequest, etc.)
- [ ] Implement pagination handling
- [ ] Add error handling and exception classes

**Key Models:**
- `CodaTable`
- `CodaColumn`
- `CodaRow`
- `CodaCell`
- `PagedResponse<T>`

---

## Phase 2: Read Operations (Discovery & Retrieval)

### 2.1 Table Operations
- [ ] `ListTables` - List all tables in a document
  - Input: docId
  - Output: List of tables with metadata
- [ ] `GetTable` - Get details of a specific table
  - Input: docId, tableId
  - Output: Table metadata

**Task Example:**
```java
@Schema(title = "List Tables", description = "List all tables in a Coda document")
@Plugin(examples = {
    @Example(
        title = "List all tables in a document",
        code = {
            "apiToken: \"{{ secret('CODA_API_TOKEN') }}\"",
            "docId: \"abc123\""
        }
    )
})
public class ListTables extends CodaTask implements RunnableTask<ListTables.Output> {
    @Override
    public Output run(RunContext runContext) throws Exception {
        // Implementation
    }
    
    @Builder
    @Getter
    public static class Output implements io.kestra.core.models.tasks.Output {
        private List<Table> tables;
    }
}
```

### 2.2 Column Operations
- [ ] `ListColumns` - List all columns in a table
  - Input: docId, tableId
  - Output: List of columns with types and metadata
- [ ] `GetColumn` - Get details of a specific column
  - Input: docId, tableId, columnId
  - Output: Column metadata

### 2.3 Row Operations
- [ ] `ListRows` - List rows in a table with pagination
  - Input: docId, tableId, optional filters
  - Output: List of rows with values
- [ ] `GetRow` - Get a specific row
  - Input: docId, tableId, rowId
  - Output: Row data

**Advanced Options:**
- Support for `limit`, `pageToken` parameters
- Filter by column values (if API supports)
- Sort by column
- Return only visible rows option

---

## Phase 3: Write Operations (Data Manipulation)

### 3.1 Insert Operations
- [ ] `InsertRow` - Insert a single row
  - Input: docId, tableId, cell values (Map<columnId, value>)
  - Output: Created row ID and metadata
- [ ] `InsertRows` - Batch insert multiple rows
  - Input: docId, tableId, list of rows
  - Output: List of created row IDs

**Task Example:**
```java
@Schema(title = "Insert Row", description = "Insert a new row into a Coda table")
public class InsertRow extends CodaTask implements RunnableTask<InsertRow.Output> {
    @Schema(title = "Table ID")
    @NotNull
    private Property<String> tableId;
    
    @Schema(title = "Cell Values", description = "Map of column IDs to values")
    @NotNull
    private Property<Map<String, Object>> cells;
    
    @Override
    public Output run(RunContext runContext) throws Exception {
        // Implementation
    }
}
```

### 3.2 Update Operations
- [ ] `UpdateRow` - Update an existing row
  - Input: docId, tableId, rowId, cell values
  - Output: Updated row metadata

### 3.3 Upsert Operations
- [ ] `UpsertRow` - Insert or update based on key column
  - Input: docId, tableId, cell values, key columns
  - Output: Upserted row ID and operation type (inserted/updated)
- [ ] `UpsertRows` - Batch upsert multiple rows
  - Input: docId, tableId, list of rows, key columns
  - Output: List of upserted row IDs

**Important Features:**
- Validate column types before sending
- Support for different data types (text, number, date, select, etc.)
- Handle calculated columns (read-only)

### 3.4 Delete Operations
- [ ] `DeleteRow` - Delete a row
  - Input: docId, tableId, rowId
  - Output: Success confirmation
- [ ] `DeleteRows` - Batch delete multiple rows
  - Input: docId, tableId, list of rowIds
  - Output: Count of deleted rows

---

## Phase 4: Automation & Webhooks

### 4.1 Webhook Trigger
- [ ] `TriggerWebhook` - Trigger a Coda automation via webhook
  - Input: webhookUrl, payload (JSON object)
  - Output: Success confirmation and response

**Task Example:**
```java
@Schema(title = "Trigger Webhook", description = "Trigger a Coda automation via webhook")
public class TriggerWebhook extends Task implements RunnableTask<TriggerWebhook.Output> {
    @Schema(title = "Webhook URL")
    @NotNull
    private Property<String> webhookUrl;
    
    @Schema(title = "API Token")
    @NotNull
    private Property<String> apiToken;
    
    @Schema(title = "Payload")
    @NotNull
    private Property<Map<String, Object>> payload;
    
    @Override
    public Output run(RunContext runContext) throws Exception {
        // POST to webhook URL with payload
    }
}
```

### 4.2 Webhook Receiver (Optional)
- [ ] Create Kestra trigger that listens for Coda webhooks
- [ ] Parse incoming webhook payloads
- [ ] Validate webhook authentication

---

## Phase 5: Advanced Features (Optional)

### 5.1 Formulas & Calculations
- [ ] Support for reading calculated column values
- [ ] Note: Cannot write to calculated columns (API limitation)

### 5.2 Bulk Operations
- [ ] Optimize batch operations with concurrent requests
- [ ] Implement bulk row operations with progress tracking
- [ ] Add retry logic for failed rows in batch operations

### 5.3 Schema Discovery
- [ ] `InferSchema` - Analyze table structure and suggest column mappings
- [ ] Generate column type mappings for data validation

### 5.4 Data Sync
- [ ] `SyncFromCoda` - Pull data from Coda and store in Kestra internal storage
- [ ] `SyncToCoda` - Push data from Kestra to Coda tables
- [ ] Support for incremental syncs based on timestamps

---

## Testing Strategy

### Unit Tests
- [ ] Test each task independently with mocked HTTP responses
- [ ] Validate input parameter validation
- [ ] Test error handling and edge cases

**Example:**
```java
@Test
void testListTables() throws Exception {
    // Mock HTTP client response
    // Create task with test inputs
    // Verify output structure
}
```

### Integration Tests
- [ ] Test against actual Coda API (requires test account)
- [ ] Create test document with known structure
- [ ] Verify CRUD operations end-to-end
- [ ] Test pagination with large datasets

### Test Fixtures
- [ ] Create sample Coda document for testing
- [ ] Generate test data for various column types
- [ ] Document test setup in README

---

## Documentation Requirements

### Task Documentation
Each task should include:
- [ ] Clear description and purpose
- [ ] Input parameter descriptions with examples
- [ ] Output structure documentation
- [ ] At least 2-3 usage examples
- [ ] Common error scenarios and solutions

### Plugin Documentation
- [ ] README with plugin overview
- [ ] Getting started guide
- [ ] Authentication setup instructions
- [ ] Complete API coverage matrix
- [ ] Troubleshooting guide
- [ ] Changelog

---

## Code Quality & Best Practices

### Code Standards
- [ ] Follow Kestra plugin conventions
- [ ] Use Lombok for boilerplate reduction
- [ ] Implement proper logging (SLF4J)
- [ ] Add comprehensive JavaDoc comments
- [ ] Follow Java naming conventions

### Error Handling
- [ ] Wrap API exceptions in meaningful error messages
- [ ] Implement retry logic with exponential backoff
- [ ] Handle rate limiting (HTTP 429)
- [ ] Validate inputs before API calls
- [ ] Return detailed error information in task outputs

### Performance
- [ ] Implement connection pooling for HTTP client
- [ ] Cache table/column metadata when appropriate
- [ ] Support pagination for large datasets
- [ ] Minimize API calls with batch operations
- [ ] Add configurable timeouts

### Security
- [ ] Never log API tokens
- [ ] Use Kestra's secret management
- [ ] Validate webhook signatures (if available)
- [ ] Sanitize user inputs
- [ ] Follow security best practices for HTTP clients

---

## Dependencies

### Required Libraries
```gradle
dependencies {
    // HTTP Client
    implementation 'com.squareup.okhttp3:okhttp:4.12.0'
    
    // JSON Serialization
    implementation 'com.google.code.gson:gson:2.10.1'
    // OR
    implementation 'com.fasterxml.jackson.core:jackson-databind:2.16.0'
    
    // Validation
    implementation 'jakarta.validation:jakarta.validation-api:3.0.2'
    
    // Logging (already in Kestra)
    // SLF4J is provided by Kestra
    
    // Testing
    testImplementation 'org.mockito:mockito-core:5.8.0'
    testImplementation 'com.squareup.okhttp3:mockwebserver:4.12.0'
}
```

---

## Implementation Checklist

### Phase 1: Foundation
- [ ] Set up project structure
- [ ] Add dependencies
- [ ] Create base classes and interfaces
- [ ] Implement authentication
- [ ] Create common models
- [ ] Set up logging

### Phase 2: Read Operations
- [ ] Implement ListTables
- [ ] Implement ListColumns
- [ ] Implement ListRows
- [ ] Add pagination support
- [ ] Create unit tests
- [ ] Write documentation

### Phase 3: Write Operations
- [ ] Implement InsertRow/InsertRows
- [ ] Implement UpdateRow
- [ ] Implement UpsertRow/UpsertRows
- [ ] Implement DeleteRow
- [ ] Add data validation
- [ ] Create integration tests

### Phase 4: Automation
- [ ] Implement TriggerWebhook
- [ ] Document webhook setup
- [ ] Test webhook integration

### Phase 5: Polish
- [ ] Add comprehensive examples
- [ ] Create troubleshooting guide
- [ ] Optimize performance
- [ ] Add metrics/monitoring
- [ ] Code review and refactoring

### Phase 6: Release
- [ ] Final testing
- [ ] Update README and documentation
- [ ] Create release notes
- [ ] Publish plugin
- [ ] Announce to community

---

## Timeline Estimate

| Phase | Estimated Duration |
|-------|-------------------|
| Phase 1: Foundation | 2-3 days |
| Phase 2: Read Operations | 3-4 days |
| Phase 3: Write Operations | 4-5 days |
| Phase 4: Automation | 2-3 days |
| Phase 5: Advanced (Optional) | 3-5 days |
| Testing & Documentation | 3-4 days |
| **Total** | **17-24 days** |

*Note: Timeline assumes one developer working full-time*

---

## Success Criteria

The plugin is considered complete when:
- ✅ All core CRUD operations are implemented and tested
- ✅ Authentication and error handling are robust
- ✅ Documentation is comprehensive with examples
- ✅ Integration tests pass against live Coda API
- ✅ Code follows Kestra plugin conventions
- ✅ All tasks have proper logging and error messages
- ✅ Webhook automation triggering works reliably

---

## Support & Maintenance

### Known Limitations (from Coda API)
- Cannot create tables via API
- Comments are not accessible via API
- Views are read-only
- Rate limits are not publicly documented

### Future Enhancements
- Add support for Coda Packs integration (if API becomes available)
- Implement comment operations (when API supports it)
- Add support for creating tables (if API adds this feature)
- Enhanced formula support
- Real-time data streaming (if webhooks expand)

---

For questions or contributions, refer to:
- [Coda API Guide](./CODA_API_GUIDE.md)
- [Quick Reference](./QUICK_REFERENCE.md)
- [Kestra Plugin Developer Guide](https://kestra.io/docs/plugin-developer-guide/)
