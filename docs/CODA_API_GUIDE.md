# Coda Plugin Developer Guide

## Table of Contents
1. [Overview](#overview)
2. [Authentication](#authentication)
3. [Core Concepts](#core-concepts)
4. [Tables Operations](#tables-operations)
5. [Columns Operations](#columns-operations)
6. [Rows Operations](#rows-operations)
7. [Comments (Limitations)](#comments-limitations)
8. [Automations & Webhooks](#automations--webhooks)
9. [Code Examples](#code-examples)
10. [Best Practices](#best-practices)
11. [Resources](#resources)

---

## Overview

The Coda API is a RESTful API that allows programmatic interaction with Coda documents. This guide covers the essential features for developing a Kestra plugin for Coda, including:
- **Tables & Columns**: Discovering and managing table schemas
- **Row Operations**: Reading, inserting, updating, upserting, and deleting rows
- **Automations**: Triggering automations via webhooks
- **Comments**: API limitations and workarounds

### Base API Endpoint
```
https://coda.io/apis/v1/
```

---

## Authentication

### Getting an API Key
1. Sign in to your Coda account
2. Navigate to Account Settings → Developer/API Settings
3. Generate a new API token
4. Store the token securely (treat it like a password)

### Using the API Token
All API requests require Bearer token authentication in the HTTP header:

```http
Authorization: Bearer YOUR_API_KEY
```

**Example with curl:**
```bash
curl -X GET "https://coda.io/apis/v1/docs" \
  -H "Authorization: Bearer YOUR_API_KEY"
```

**Example with Java (OkHttp):**
```java
Request request = new Request.Builder()
    .url("https://coda.io/apis/v1/docs")
    .addHeader("Authorization", "Bearer " + apiToken)
    .build();
```

---

## Core Concepts

### Resource Identifiers
- **Doc ID**: Unique identifier for a Coda document
- **Table ID**: Unique identifier for a table (use IDs instead of names for reliability)
- **Column ID**: Unique identifier for a column (immutable, unlike column names)
- **Row ID**: Unique identifier for a row

### Pagination
Large result sets are paginated using:
- `limit`: Number of results per page (default varies by endpoint)
- `pageToken`: Token for retrieving the next page (provided in response)

---

## Tables Operations

### List All Tables in a Document
**Endpoint:** `GET /docs/{docId}/tables`

**Description:** Returns all tables in a document

**Response includes:**
- Table ID (use this for subsequent operations)
- Table name
- Table type (table or view)
- Row count
- Parent table ID (for views)

**Example Request:**
```bash
curl -X GET "https://coda.io/apis/v1/docs/{docId}/tables" \
  -H "Authorization: Bearer YOUR_API_KEY"
```

**Example Response:**
```json
{
  "items": [
    {
      "id": "grid-abc123",
      "type": "table",
      "name": "Tasks",
      "rowCount": 42,
      "createdAt": "2024-01-15T10:30:00.000Z"
    }
  ]
}
```

**Important Notes:**
- You cannot create new tables via the API (only list existing ones)
- Use base tables (not views) for insert/upsert operations
- Table IDs are more reliable than names for API operations

---

## Columns Operations

### List Columns in a Table
**Endpoint:** `GET /docs/{docId}/tables/{tableId}/columns`

**Description:** Returns all columns in a table with their metadata

**Response includes:**
- Column ID (immutable identifier)
- Column name
- Column type (text, number, date, select, etc.)
- Display format
- Calculation formula (if applicable)

**Example Request:**
```bash
curl -X GET "https://coda.io/apis/v1/docs/{docId}/tables/{tableId}/columns" \
  -H "Authorization: Bearer YOUR_API_KEY"
```

**Example Response:**
```json
{
  "items": [
    {
      "id": "c-abc123",
      "name": "Task Name",
      "type": "text",
      "calculated": false
    },
    {
      "id": "c-def456",
      "name": "Status",
      "type": "select",
      "calculated": false
    }
  ]
}
```

**Column Types:**
- `text`: Text values
- `number`: Numeric values
- `date`, `time`, `dateTime`: Date/time values
- `select`: Select list (single choice)
- `multiSelect`: Multiple choice
- `people`: Coda user references
- `lookup`: References to other tables
- `button`: Action buttons
- `image`, `file`: File attachments
- `link`: URL links
- `canvas`: Rich text/embedded content

**Important Notes:**
- Always use Column IDs (not names) in API operations
- Some column details (like select options) may not be fully exposed in API responses
- Calculated columns are read-only

---

## Rows Operations

### List Rows
**Endpoint:** `GET /docs/{docId}/tables/{tableId}/rows`

**Description:** Retrieves rows from a table

**Query Parameters:**
- `limit`: Maximum number of rows to return (default: 25, max: 500)
- `pageToken`: Token for pagination
- `query`: Filter expression (limited support)
- `sortBy`: Column to sort by
- `visibleOnly`: Return only visible rows (default: false)
- `useColumnNames`: Use column names instead of IDs in response (default: false)

**Example Request:**
```bash
curl -X GET "https://coda.io/apis/v1/docs/{docId}/tables/{tableId}/rows?limit=100" \
  -H "Authorization: Bearer YOUR_API_KEY"
```

**Example Response:**
```json
{
  "items": [
    {
      "id": "i-abc123",
      "type": "row",
      "href": "https://coda.io/apis/v1/docs/{docId}/tables/{tableId}/rows/i-abc123",
      "name": "Row 1",
      "index": 0,
      "createdAt": "2024-01-15T10:30:00.000Z",
      "updatedAt": "2024-01-15T11:45:00.000Z",
      "values": {
        "c-abc123": "Task Name",
        "c-def456": "In Progress"
      }
    }
  ],
  "nextPageToken": "eyJsaW1pdCI..."
}
```

---

### Insert Rows
**Endpoint:** `POST /docs/{docId}/tables/{tableId}/rows`

**Description:** Inserts new rows into a table

**Request Body:**
```json
{
  "rows": [
    {
      "cells": [
        {
          "column": "c-abc123",
          "value": "New Task"
        },
        {
          "column": "c-def456",
          "value": "Not Started"
        }
      ]
    }
  ],
  "keyColumns": []
}
```

**Example Request:**
```bash
curl -X POST "https://coda.io/apis/v1/docs/{docId}/tables/{tableId}/rows" \
  -H "Authorization: Bearer YOUR_API_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "rows": [
      {
        "cells": [
          {"column": "c-abc123", "value": "New Task"},
          {"column": "c-def456", "value": "Not Started"}
        ]
      }
    ]
  }'
```

**Important Notes:**
- Must use a base table (not a view) for insertion
- Use column IDs (not names) in the cells array
- Multiple rows can be inserted in a single request

---

### Update Row
**Endpoint:** `PUT /docs/{docId}/tables/{tableId}/rows/{rowId}`

**Description:** Updates an existing row

**Request Body:**
```json
{
  "row": {
    "cells": [
      {
        "column": "c-def456",
        "value": "Completed"
      }
    ]
  }
}
```

**Example Request:**
```bash
curl -X PUT "https://coda.io/apis/v1/docs/{docId}/tables/{tableId}/rows/{rowId}" \
  -H "Authorization: Bearer YOUR_API_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "row": {
      "cells": [
        {"column": "c-def456", "value": "Completed"}
      ]
    }
  }'
```

---

### Upsert Rows
**Endpoint:** `POST /docs/{docId}/tables/{tableId}/rows/upsert`

**Description:** Inserts or updates rows based on a key column

**Request Body:**
```json
{
  "rows": [
    {
      "cells": [
        {
          "column": "c-abc123",
          "value": "Task Name"
        },
        {
          "column": "c-def456",
          "value": "In Progress"
        }
      ]
    }
  ],
  "keyColumns": ["c-abc123"]
}
```

**How it works:**
- If a row with the matching key column value exists, it will be updated
- If no matching row exists, a new row will be inserted
- The `keyColumns` array specifies which column(s) to use for matching

**Example Request:**
```bash
curl -X POST "https://coda.io/apis/v1/docs/{docId}/tables/{tableId}/rows/upsert" \
  -H "Authorization: Bearer YOUR_API_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "rows": [
      {
        "cells": [
          {"column": "c-abc123", "value": "Task Name"},
          {"column": "c-def456", "value": "In Progress"}
        ]
      }
    ],
    "keyColumns": ["c-abc123"]
  }'
```

---

### Delete Row
**Endpoint:** `DELETE /docs/{docId}/tables/{tableId}/rows/{rowId}`

**Description:** Deletes a specific row

**Example Request:**
```bash
curl -X DELETE "https://coda.io/apis/v1/docs/{docId}/tables/{tableId}/rows/{rowId}" \
  -H "Authorization: Bearer YOUR_API_KEY"
```

---

## Comments (Limitations)

### Current API Limitations

**Important:** As of the current API version, **comments are NOT natively exposed via the Coda API**. This means:

- ❌ Cannot retrieve comments via API
- ❌ Cannot search or filter by comments
- ❌ Cannot create comments programmatically
- ❌ Cannot interact with row-level comments or threaded discussions

### Workarounds

1. **Use a Custom Text Column**: Create a dedicated column for notes/comments that can be read/written via API
2. **Coda Packs**: Build a custom Pack with internal APIs (advanced)
3. **Integration Tools**: Use third-party tools like Zapier or Make.com with limited comment capabilities
4. **Activity Logs**: Track changes in a separate table as a comment alternative

### Community Requests
The Coda community has requested native comment API support. Monitor the [Coda Feature Requests](https://community.coda.io/) for updates.

---

## Automations & Webhooks

### Overview
Coda automations can be triggered via webhooks, allowing external applications to invoke actions in your Coda doc.

### Setting Up Webhook-Triggered Automations

**In Coda:**
1. Open your Coda document
2. Click the three-dot menu (⋮) → "Doc settings"
3. Navigate to "Automations" → "+ Add rule"
4. For the **When** condition, select "Webhook invoked"
5. Coda generates a unique webhook URL
6. Define the **Then** actions (e.g., add row, update table, send notification)

**Authentication:**
- Webhook endpoints require Bearer token authentication
- Use your Coda API token in the Authorization header

### Triggering Automations Programmatically

**Example with curl:**
```bash
curl -X POST "https://coda.io/apis/v1/webhooks/{webhookId}" \
  -H "Authorization: Bearer YOUR_API_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "status": "Active",
    "timestamp": "2024-01-15T10:30:00Z"
  }'
```

**Example with Java (using OkHttp):**
```java
MediaType JSON = MediaType.get("application/json; charset=utf-8");
RequestBody body = RequestBody.create(
    "{\"name\":\"John Doe\",\"status\":\"Active\"}", 
    JSON
);

Request request = new Request.Builder()
    .url(webhookUrl)
    .post(body)
    .addHeader("Authorization", "Bearer " + apiToken)
    .addHeader("Content-Type", "application/json")
    .build();

Response response = client.newCall(request).execute();
```

### Accessing Webhook Data in Automations

In your Coda automation:
- The webhook payload is available as **"Step 1 Result"**
- Use `ParseJSON([Step 1 Result], "key")` to extract values
- Example: `ParseJSON([Step 1 Result], "name")` returns "John Doe"

**Handling Arrays:**
```json
{
  "items": [
    {"name": "Alice", "score": 95},
    {"name": "Bob", "score": 87}
  ]
}
```

In Coda automation, iterate over `ParseJSON([Step 1 Result], "items")` using ForEach.

### Automation Triggers

Supported triggers:
- **Webhook invoked**: External HTTP POST triggers the automation
- **Row changes**: When a row is added, modified, or deleted
- **Time-based**: Scheduled automations (hourly, daily, weekly)
- **Button clicked**: Manual triggers within the doc
- **Form submission**: When a form is submitted

**Note:** Formula changes and comments do NOT trigger automations

### Best Practices for Webhooks

1. **Authentication**: Always include Bearer token in webhook requests
2. **Error Handling**: Implement retry logic for failed webhook calls
3. **Payload Structure**: Use JSON with named keys (avoid top-level arrays)
4. **Rate Limiting**: Be mindful of API rate limits
5. **Testing**: Use tools like Postman to test webhooks before integration
6. **Monitoring**: Log webhook responses and automation results

---

## Code Examples

### Java Example: List Tables and Columns

```java
import okhttp3.*;
import com.google.gson.Gson;
import java.io.IOException;

public class CodaClient {
    private final String apiToken;
    private final OkHttpClient client;
    private final Gson gson;
    
    public CodaClient(String apiToken) {
        this.apiToken = apiToken;
        this.client = new OkHttpClient();
        this.gson = new Gson();
    }
    
    public TablesResponse listTables(String docId) throws IOException {
        String url = "https://coda.io/apis/v1/docs/" + docId + "/tables";
        
        Request request = new Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer " + apiToken)
            .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response: " + response);
            }
            
            String responseBody = response.body().string();
            return gson.fromJson(responseBody, TablesResponse.class);
        }
    }
    
    public ColumnsResponse listColumns(String docId, String tableId) throws IOException {
        String url = String.format(
            "https://coda.io/apis/v1/docs/%s/tables/%s/columns",
            docId, tableId
        );
        
        Request request = new Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer " + apiToken)
            .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response: " + response);
            }
            
            String responseBody = response.body().string();
            return gson.fromJson(responseBody, ColumnsResponse.class);
        }
    }
}
```

### Java Example: Insert Rows

```java
public class CodaRowOperations {
    private final String apiToken;
    private final OkHttpClient client;
    private final Gson gson;
    
    public CodaRowOperations(String apiToken) {
        this.apiToken = apiToken;
        this.client = new OkHttpClient();
        this.gson = new Gson();
    }
    
    public void insertRow(String docId, String tableId, Map<String, Object> cellValues) 
            throws IOException {
        String url = String.format(
            "https://coda.io/apis/v1/docs/%s/tables/%s/rows",
            docId, tableId
        );
        
        // Build the request body
        List<Cell> cells = new ArrayList<>();
        for (Map.Entry<String, Object> entry : cellValues.entrySet()) {
            cells.add(new Cell(entry.getKey(), entry.getValue()));
        }
        
        InsertRowRequest requestData = new InsertRowRequest(
            List.of(new Row(cells))
        );
        
        String jsonBody = gson.toJson(requestData);
        
        RequestBody body = RequestBody.create(
            jsonBody,
            MediaType.get("application/json; charset=utf-8")
        );
        
        Request request = new Request.Builder()
            .url(url)
            .post(body)
            .addHeader("Authorization", "Bearer " + apiToken)
            .addHeader("Content-Type", "application/json")
            .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to insert row: " + response);
            }
        }
    }
    
    // Helper classes for JSON serialization
    static class InsertRowRequest {
        List<Row> rows;
        
        InsertRowRequest(List<Row> rows) {
            this.rows = rows;
        }
    }
    
    static class Row {
        List<Cell> cells;
        
        Row(List<Cell> cells) {
            this.cells = cells;
        }
    }
    
    static class Cell {
        String column;
        Object value;
        
        Cell(String column, Object value) {
            this.column = column;
            this.value = value;
        }
    }
}
```

### Java Example: Upsert Rows

```java
public void upsertRow(String docId, String tableId, 
                     Map<String, Object> cellValues, 
                     List<String> keyColumns) throws IOException {
    String url = String.format(
        "https://coda.io/apis/v1/docs/%s/tables/%s/rows/upsert",
        docId, tableId
    );
    
    List<Cell> cells = new ArrayList<>();
    for (Map.Entry<String, Object> entry : cellValues.entrySet()) {
        cells.add(new Cell(entry.getKey(), entry.getValue()));
    }
    
    UpsertRowRequest requestData = new UpsertRowRequest(
        List.of(new Row(cells)),
        keyColumns
    );
    
    String jsonBody = gson.toJson(requestData);
    
    RequestBody body = RequestBody.create(
        jsonBody,
        MediaType.get("application/json; charset=utf-8")
    );
    
    Request request = new Request.Builder()
        .url(url)
        .post(body)
        .addHeader("Authorization", "Bearer " + apiToken)
        .addHeader("Content-Type", "application/json")
        .build();
    
    try (Response response = client.newCall(request).execute()) {
        if (!response.isSuccessful()) {
            throw new IOException("Failed to upsert row: " + response);
        }
    }
}

static class UpsertRowRequest {
    List<Row> rows;
    List<String> keyColumns;
    
    UpsertRowRequest(List<Row> rows, List<String> keyColumns) {
        this.rows = rows;
        this.keyColumns = keyColumns;
    }
}
```

---

## Best Practices

### 1. Use Resource IDs, Not Names
- Always use IDs (docId, tableId, columnId, rowId) instead of names
- Names can change, but IDs remain stable
- IDs are required for reliable API operations

### 2. Implement Proper Error Handling
```java
try (Response response = client.newCall(request).execute()) {
    if (!response.isSuccessful()) {
        String errorBody = response.body().string();
        logger.error("API Error: {} - {}", response.code(), errorBody);
        
        if (response.code() == 429) {
            // Rate limit exceeded - implement backoff
            Thread.sleep(60000); // Wait 1 minute
            // Retry the request
        } else if (response.code() == 401) {
            // Authentication failed - check API token
            throw new AuthenticationException("Invalid API token");
        }
    }
}
```

### 3. Handle Pagination
```java
public List<Row> getAllRows(String docId, String tableId) throws IOException {
    List<Row> allRows = new ArrayList<>();
    String pageToken = null;
    
    do {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(
            String.format("https://coda.io/apis/v1/docs/%s/tables/%s/rows", 
                         docId, tableId)
        ).newBuilder();
        
        if (pageToken != null) {
            urlBuilder.addQueryParameter("pageToken", pageToken);
        }
        
        Request request = new Request.Builder()
            .url(urlBuilder.build())
            .addHeader("Authorization", "Bearer " + apiToken)
            .build();
        
        try (Response response = client.newCall(request).execute()) {
            RowsResponse rowsResponse = gson.fromJson(
                response.body().string(), 
                RowsResponse.class
            );
            
            allRows.addAll(rowsResponse.items);
            pageToken = rowsResponse.nextPageToken;
        }
    } while (pageToken != null);
    
    return allRows;
}
```

### 4. Respect Rate Limits
- Implement exponential backoff for retries
- Cache frequently accessed data (e.g., table/column metadata)
- Batch operations when possible
- Monitor API response headers for rate limit info

### 5. Validate Input Data
```java
public void validateCellValue(Column column, Object value) {
    switch (column.type) {
        case "number":
            if (!(value instanceof Number)) {
                throw new IllegalArgumentException(
                    "Column " + column.name + " requires a number"
                );
            }
            break;
        case "date":
            // Validate date format (ISO 8601)
            if (!(value instanceof String)) {
                throw new IllegalArgumentException(
                    "Date must be in ISO 8601 format"
                );
            }
            break;
        // Add more validations as needed
    }
}
```

### 6. Use Base Tables for Modifications
- Only base tables support insert/upsert/delete operations
- Views are read-only via the API
- Check table type before attempting modifications

### 7. Secure API Tokens
- Never hardcode API tokens in source code
- Use environment variables or secure secret management
- Implement token rotation policies
- Use the principle of least privilege

### 8. Log and Monitor
```java
logger.info("Inserting {} rows into table {}", rows.size(), tableId);
long startTime = System.currentTimeMillis();

// Perform operation
insertRows(docId, tableId, rows);

long duration = System.currentTimeMillis() - startTime;
logger.info("Successfully inserted {} rows in {}ms", rows.size(), duration);
```

---

## Resources

### Official Documentation
- **Coda API Reference**: [https://coda.io/developers](https://coda.io/developers)
- **Getting Started Guide**: [https://coda.io/@oleg/getting-started-guide-coda-api](https://coda.io/@oleg/getting-started-guide-coda-api)
- **Automations Help**: [https://help.coda.io/hc/en-us/articles/39555778179853-Automations-in-Coda](https://help.coda.io/hc/en-us/articles/39555778179853-Automations-in-Coda)
- **Webhook Automations**: [https://help.coda.io/hc/en-us/articles/39555972006541-Create-webhook-triggered-automations](https://help.coda.io/hc/en-us/articles/39555972006541-Create-webhook-triggered-automations)
- **Tables and Views**: [https://help.coda.io/hc/en-us/categories/37412217582221-Tables-and-views](https://help.coda.io/hc/en-us/categories/37412217582221-Tables-and-views)
- **Formula Reference**: [https://coda.io/formulas](https://coda.io/formulas)

### Testing Tools
- **Postman Collection**: [Coda API Postman Collection](https://www.postman.com/codaio/coda-workspace/collection/0vy7uxn/coda-api)
- Use Postman for rapid API testing and exploration

### Community Resources
- **Coda Maker Community**: [https://community.coda.io/](https://community.coda.io/)
- Search for API discussions, feature requests, and community solutions
- Report issues and request features

### Additional Guides
- **API Essentials**: [https://rollout.com/integration-guides/coda/api-essentials](https://rollout.com/integration-guides/coda/api-essentials)
- **Webhook Implementation**: [https://rollout.com/integration-guides/coda/quick-guide-to-implementing-webhooks-in-coda](https://rollout.com/integration-guides/coda/quick-guide-to-implementing-webhooks-in-coda)

### Kestra Plugin Development
- **Kestra Plugin Developer Guide**: [https://kestra.io/docs/plugin-developer-guide/](https://kestra.io/docs/plugin-developer-guide/)
- **Example Plugins**: Browse existing Kestra plugins on GitHub for reference patterns

---

## API Limitations to Be Aware Of

1. **No Table Creation**: Cannot create new tables via API (only read existing ones)
2. **No Comment Access**: Comments are not exposed via the API
3. **Views Are Read-Only**: Cannot insert/update/delete rows in views (use base tables)
4. **Limited Column Metadata**: Some column details (like select options) may not be fully available
5. **Automation Triggers**: Formula changes and comments don't trigger automations
6. **Rate Limits**: API has rate limiting (exact limits not publicly documented - monitor response headers)
7. **Webhook Authentication Required**: All webhook endpoints require Bearer token authentication

---

## Next Steps for Plugin Development

1. **Set Up Authentication**: Implement secure API token management in Kestra
2. **Create Base Tasks**: 
   - ListTables
   - ListColumns
   - ListRows
   - InsertRow
   - UpdateRow
   - UpsertRow
   - DeleteRow
   - TriggerAutomation (webhook)
3. **Add Error Handling**: Implement robust retry logic and error messages
4. **Create Tests**: Write unit and integration tests for all tasks
5. **Documentation**: Add examples and documentation for each task
6. **Publish**: Follow Kestra plugin publishing guidelines

---

**Last Updated**: 2024-11-18

For questions or issues, please refer to the official Coda API documentation or the Coda Maker Community.
