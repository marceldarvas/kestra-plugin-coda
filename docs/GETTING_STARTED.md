# Getting Started with Superhuman Docs Plugin Development

This guide will help you get started with developing the Kestra Superhuman Docs (formerly Coda) plugin quickly.

## Prerequisites

- Java 25 or higher (required by Kestra 2.0)
- Gradle (included via wrapper)
- Docker (for local Kestra development)
- A Superhuman Docs account with API access
- An IDE (IntelliJ IDEA, VS Code, or Eclipse)

## Step 1: Set Up Your Development Environment

### Clone the Repository

```bash
git clone https://github.com/marceldarvas/kestra-plugin-coda.git
cd kestra-plugin-coda
```

### Verify Java Version

```bash
java -version
# Should show Java 25 or higher
```

### Run Tests

```bash
./gradlew check --parallel
```

This will compile the code and run existing tests to ensure everything is working.

## Step 2: Get Your Superhuman Docs API Token

1. Sign in to [Superhuman Docs](https://docs.superhuman.com)
2. Open your Superhuman Docs account settings and find the API settings
3. Generate a new API token
4. Copy the token (you'll need it for testing)

⚠️ **Important**: Keep your API token secure! Never commit it to version control.

## Step 3: Create a Test Superhuman Docs Document

For development and testing, create a simple test document:

1. Create a new Superhuman Docs doc
2. Add a table named "Test Tasks" with these columns:
   - Task Name (Text)
   - Status (Select: Not Started, In Progress, Completed)
   - Priority (Number)
   - Due Date (Date)
3. Add a few sample rows
4. Note the document ID from the URL: `https://docs.superhuman.com/d/_dABC123XYZ` → `ABC123XYZ`

## Step 4: Test the Superhuman Docs API Manually

Before coding, verify API access with curl:

```bash
# List your documents
curl -X GET "https://coda.io/apis/v1/docs" \
  -H "Authorization: Bearer YOUR_API_TOKEN"

# List tables in your test document
curl -X GET "https://coda.io/apis/v1/docs/YOUR_DOC_ID/tables" \
  -H "Authorization: Bearer YOUR_API_TOKEN"

# List columns in a table
curl -X GET "https://coda.io/apis/v1/docs/YOUR_DOC_ID/tables/YOUR_TABLE_ID/columns" \
  -H "Authorization: Bearer YOUR_API_TOKEN"
```

If these work, you're ready to start coding!

## Step 5: Project Structure Overview

```
kestra-plugin-coda/
├── src/
│   ├── main/
│   │   └── java/
│   │       └── io/kestra/plugin/
│   │           └── superhuman/docs/    # Your plugin code goes here
│   │               ├── ListTables.java
│   │               ├── ListColumns.java
│   │               ├── InsertRow.java
│   │               └── ...
│   └── test/
│       └── java/
│           └── io/kestra/plugin/
│               └── superhuman/docs/    # Your tests go here
├── docs/                              # Documentation (already created)
├── build.gradle                       # Build configuration
└── README.md
```

## Step 6: Create Your First Task - ListTables

Create a new file: `src/main/java/io/kestra/plugin/superhuman/docs/ListTables.java`

```java
package io.kestra.plugin.superhuman.docs;

import io.kestra.core.models.annotations.Plugin;
import io.kestra.core.models.property.Property;
import io.kestra.core.models.tasks.RunnableTask;
import io.kestra.core.models.tasks.Task;
import io.kestra.core.runners.RunContext;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;

import java.util.List;

@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@Schema(
    title = "List all tables in a Superhuman Docs document",
    description = "Retrieves a list of all tables in the specified Superhuman Docs document"
)
@Plugin(
    examples = {
        @io.kestra.core.models.annotations.Example(
            title = "List all tables in a document",
            code = {
                "apiToken: \"{{ secret('SUPERHUMAN_DOCS_API_TOKEN') }}\"",
                "docId: \"abc123xyz\""
            }
        )
    }
)
public class ListTables extends Task implements RunnableTask<ListTables.Output> {
    
    @Schema(
        title = "Superhuman Docs API Token",
        description = "Your Superhuman Docs API token for authentication"
    )
    private Property<String> apiToken;
    
    @Schema(
        title = "Document ID",
        description = "The Superhuman Docs document ID"
    )
    private Property<String> docId;
    
    @Override
    public Output run(RunContext runContext) throws Exception {
        Logger logger = runContext.logger();
        
        // Render properties
        String token = runContext.render(apiToken).as(String.class).orElseThrow();
        String doc = runContext.render(docId).as(String.class).orElseThrow();
        
        logger.debug("Listing tables for document: {}", doc);
        
        // Create HTTP client
        OkHttpClient client = new OkHttpClient();
        
        // Build request
        String url = "https://coda.io/apis/v1/docs/" + doc + "/tables";
        Request request = new Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer " + token)
            .build();
        
        // Execute request
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new Exception("Failed to list tables: " + response.code() + " - " + response.message());
            }
            
            String responseBody = response.body().string();
            logger.info("Successfully retrieved tables");
            
            return Output.builder()
                .tablesJson(responseBody)
                .build();
        }
    }
    
    @Builder
    @Getter
    public static class Output implements io.kestra.core.models.tasks.Output {
        @Schema(
            title = "Tables JSON",
            description = "JSON response containing the list of tables"
        )
        private final String tablesJson;
    }
}
```

### Update Dependencies

Add OkHttp to your `build.gradle`:

```gradle
dependencies {
    // Existing dependencies...
    
    // HTTP Client for Superhuman Docs API
    implementation 'com.squareup.okhttp3:okhttp:4.12.0'
    
    // JSON Processing
    implementation 'com.google.code.gson:gson:2.10.1'
}
```

## Step 7: Create a Test

Create: `src/test/java/io/kestra/plugin/superhuman/docs/ListTablesTest.java`

```java
package io.kestra.plugin.superhuman.docs;

import io.kestra.core.models.property.Property;
import io.kestra.core.runners.RunContext;
import io.kestra.core.runners.RunContextFactory;
import io.kestra.core.junit.annotations.KestraTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

@KestraTest
class ListTablesTest {
    
    @Inject
    private RunContextFactory runContextFactory;
    
    @Test
    void run() throws Exception {
        // Skip if no API token provided (for CI/CD)
        String apiToken = System.getenv("SUPERHUMAN_DOCS_API_TOKEN");
        String docId = System.getenv("SUPERHUMAN_DOCS_DOC_ID");
        
        if (apiToken == null || docId == null) {
            System.out.println("Skipping test - SUPERHUMAN_DOCS_API_TOKEN or SUPERHUMAN_DOCS_DOC_ID not set");
            return;
        }
        
        RunContext runContext = runContextFactory.of();
        
        ListTables task = ListTables.builder()
            .apiToken(Property.ofValue(apiToken))
            .docId(Property.ofValue(docId))
            .build();
        
        ListTables.Output output = task.run(runContext);
        
        assertThat(output.getTablesJson(), notNullValue());
        System.out.println("Tables: " + output.getTablesJson());
    }
}
```

## Step 8: Build and Test

```bash
# Build the project
./gradlew build

# Run tests
export SUPERHUMAN_DOCS_API_TOKEN="your-api-token"
export SUPERHUMAN_DOCS_DOC_ID="your-doc-id"
./gradlew test
```

## Step 9: Test Locally with Kestra

### Build the plugin JAR

```bash
./gradlew shadowJar
```

### Run Kestra with your plugin

```bash
docker build -t kestra-superhuman-docs .
docker run --rm -p 8080:8080 kestra-superhuman-docs server local
```

### Create a test flow

Open http://localhost:8080 and create a new flow:

```yaml
id: test-superhuman-docs-plugin
namespace: io.kestra.tests

tasks:
  - id: list_tables
    type: io.kestra.plugin.superhuman.docs.ListTables
    apiToken: "{{ secret('SUPERHUMAN_DOCS_API_TOKEN') }}"
    docId: "your-doc-id"
```

## Step 10: Follow the Implementation Roadmap

Now that you have a working task, follow the [Implementation Roadmap](./IMPLEMENTATION_ROADMAP.md) to build out the rest of the plugin:

1. **Phase 1**: Create base classes and authentication
2. **Phase 2**: Implement all read operations (ListColumns, ListRows, etc.)
3. **Phase 3**: Implement write operations (InsertRow, UpdateRow, etc.)
4. **Phase 4**: Add automation support
5. **Phase 5**: Polish and optimize

## Development Tips

### Use Logging Effectively

```java
Logger logger = runContext.logger();
logger.debug("Debug message with context: {}", variable);
logger.info("Important information");
logger.warn("Warning message");
logger.error("Error occurred", exception);
```

### Handle Errors Gracefully

```java
try (Response response = client.newCall(request).execute()) {
    if (!response.isSuccessful()) {
        String errorBody = response.body().string();
        throw new Exception(String.format(
            "Superhuman Docs API error: HTTP %d - %s. Response: %s",
            response.code(),
            response.message(),
            errorBody
        ));
    }
    // Process success
}
```

### Use Property<T> for Dynamic Values

```java
@Schema(title = "API Token")
private Property<String> apiToken;

// In run() method:
String token = runContext.render(apiToken).as(String.class).orElseThrow();
```

### Create Reusable Components

Consider creating base classes or utilities:

```java
public abstract class SuperhumanDocsTask extends Task {
    protected Property<String> apiToken;
    protected Property<String> docId;
    
    protected OkHttpClient createClient() {
        return new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build();
    }
    
    protected Request.Builder authenticatedRequest(RunContext runContext) throws Exception {
        String token = runContext.render(apiToken).as(String.class).orElseThrow();
        return new Request.Builder()
            .addHeader("Authorization", "Bearer " + token);
    }
}
```

## Testing Strategy

### Unit Tests
- Mock HTTP responses
- Test input validation
- Test error handling

### Integration Tests
- Use actual Superhuman Docs API
- Create/cleanup test data
- Test pagination and edge cases

### Local Testing with Kestra
- Build and run locally
- Create test flows
- Verify outputs

## Resources

- **[Superhuman Docs API Guide](./SUPERHUMAN_DOCS_API_GUIDE.md)** - Complete API reference
- **[Quick Reference](./QUICK_REFERENCE.md)** - Handy cheat sheet
- **[Implementation Roadmap](./IMPLEMENTATION_ROADMAP.md)** - Development plan
- **[Kestra Plugin Guide](https://kestra.io/docs/plugin-developer-guide/)** - Official Kestra docs

## Need Help?

- **Superhuman Docs API Issues**: Check the [Coda community forum](https://community.coda.io/) (legacy Coda link)
- **Kestra Plugin Issues**: Check [Kestra Docs](https://kestra.io/docs)
- **General Questions**: Create an issue in this repository

## Next Steps

1. ✅ Complete this getting started guide
2. 📖 Review the [Superhuman Docs API Guide](./SUPERHUMAN_DOCS_API_GUIDE.md)
3. 🗺️ Follow the [Implementation Roadmap](./IMPLEMENTATION_ROADMAP.md)
4. 💻 Start coding!
5. 🧪 Write tests
6. 📝 Document your work
7. 🚀 Ship it!

Good luck with your Superhuman Docs plugin development! 🎉
