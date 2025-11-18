# Getting Started with Coda Plugin Development

This guide will help you get started with developing the Kestra Coda plugin quickly.

## Prerequisites

- Java 21 or higher
- Gradle (included via wrapper)
- Docker (for local Kestra development)
- A Coda account with API access
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
# Should show Java 21 or higher
```

### Run Tests

```bash
./gradlew check --parallel
```

This will compile the code and run existing tests to ensure everything is working.

## Step 2: Get Your Coda API Token

1. Sign in to [Coda](https://coda.io)
2. Click on your profile picture → Settings
3. Navigate to the "Account" tab
4. Scroll down to "API Settings"
5. Click "Generate API Token"
6. Copy the token (you'll need it for testing)

⚠️ **Important**: Keep your API token secure! Never commit it to version control.

## Step 3: Create a Test Coda Document

For development and testing, create a simple test document:

1. Create a new Coda doc
2. Add a table named "Test Tasks" with these columns:
   - Task Name (Text)
   - Status (Select: Not Started, In Progress, Completed)
   - Priority (Number)
   - Due Date (Date)
3. Add a few sample rows
4. Note the document ID from the URL: `https://coda.io/d/_dABC123XYZ` → `ABC123XYZ`

## Step 4: Test the Coda API Manually

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
│   │           └── coda/              # Your plugin code goes here
│   │               ├── ListTables.java
│   │               ├── ListColumns.java
│   │               ├── InsertRow.java
│   │               └── ...
│   └── test/
│       └── java/
│           └── io/kestra/plugin/
│               └── coda/              # Your tests go here
├── docs/                              # Documentation (already created)
├── build.gradle                       # Build configuration
└── README.md
```

## Step 6: Create Your First Task - ListTables

Create a new file: `src/main/java/io/kestra/plugin/coda/ListTables.java`

```java
package io.kestra.plugin.coda;

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
    title = "List all tables in a Coda document",
    description = "Retrieves a list of all tables in the specified Coda document"
)
@Plugin(
    examples = {
        @io.kestra.core.models.annotations.Example(
            title = "List all tables in a document",
            code = {
                "apiToken: \"{{ secret('CODA_API_TOKEN') }}\"",
                "docId: \"abc123xyz\""
            }
        )
    }
)
public class ListTables extends Task implements RunnableTask<ListTables.Output> {
    
    @Schema(
        title = "Coda API Token",
        description = "Your Coda API token for authentication"
    )
    private Property<String> apiToken;
    
    @Schema(
        title = "Document ID",
        description = "The Coda document ID"
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
    
    // HTTP Client for Coda API
    implementation 'com.squareup.okhttp3:okhttp:4.12.0'
    
    // JSON Processing
    implementation 'com.google.code.gson:gson:2.10.1'
}
```

## Step 7: Create a Test

Create: `src/test/java/io/kestra/plugin/coda/ListTablesTest.java`

```java
package io.kestra.plugin.coda;

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
        String apiToken = System.getenv("CODA_API_TOKEN");
        String docId = System.getenv("CODA_DOC_ID");
        
        if (apiToken == null || docId == null) {
            System.out.println("Skipping test - CODA_API_TOKEN or CODA_DOC_ID not set");
            return;
        }
        
        RunContext runContext = runContextFactory.of();
        
        ListTables task = ListTables.builder()
            .apiToken(Property.of(apiToken))
            .docId(Property.of(docId))
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
export CODA_API_TOKEN="your-api-token"
export CODA_DOC_ID="your-doc-id"
./gradlew test
```

## Step 9: Test Locally with Kestra

### Build the plugin JAR

```bash
./gradlew shadowJar
```

### Run Kestra with your plugin

```bash
docker build -t kestra-coda .
docker run --rm -p 8080:8080 kestra-coda server local
```

### Create a test flow

Open http://localhost:8080 and create a new flow:

```yaml
id: test-coda-plugin
namespace: io.kestra.tests

tasks:
  - id: list_tables
    type: io.kestra.plugin.coda.ListTables
    apiToken: "{{ secret('CODA_API_TOKEN') }}"
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
            "Coda API error: HTTP %d - %s. Response: %s",
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
public abstract class CodaTask extends Task {
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
- Use actual Coda API
- Create/cleanup test data
- Test pagination and edge cases

### Local Testing with Kestra
- Build and run locally
- Create test flows
- Verify outputs

## Resources

- **[Coda API Guide](./CODA_API_GUIDE.md)** - Complete API reference
- **[Quick Reference](./QUICK_REFERENCE.md)** - Handy cheat sheet
- **[Implementation Roadmap](./IMPLEMENTATION_ROADMAP.md)** - Development plan
- **[Kestra Plugin Guide](https://kestra.io/docs/plugin-developer-guide/)** - Official Kestra docs

## Need Help?

- **Coda API Issues**: Check [Coda Community](https://community.coda.io/)
- **Kestra Plugin Issues**: Check [Kestra Docs](https://kestra.io/docs)
- **General Questions**: Create an issue in this repository

## Next Steps

1. ✅ Complete this getting started guide
2. 📖 Review the [Coda API Guide](./CODA_API_GUIDE.md)
3. 🗺️ Follow the [Implementation Roadmap](./IMPLEMENTATION_ROADMAP.md)
4. 💻 Start coding!
5. 🧪 Write tests
6. 📝 Document your work
7. 🚀 Ship it!

Good luck with your Coda plugin development! 🎉
