<p align="center">
  <a href="https://www.kestra.io">
    <img src="https://kestra.io/banner.png"  alt="Kestra workflow orchestrator" />
  </a>
</p>

<h1 align="center" style="border-bottom: none">
    Event-Driven Declarative Orchestrator
</h1>

<div align="center">
 <a href="https://github.com/kestra-io/kestra/releases"><img src="https://img.shields.io/github/tag-pre/kestra-io/kestra.svg?color=blueviolet" alt="Last Version" /></a>
  <a href="https://github.com/kestra-io/kestra/blob/develop/LICENSE"><img src="https://img.shields.io/github/license/kestra-io/kestra?color=blueviolet" alt="License" /></a>
  <a href="https://github.com/kestra-io/kestra/stargazers"><img src="https://img.shields.io/github/stars/kestra-io/kestra?color=blueviolet&logo=github" alt="Github star" /></a> <br>
<a href="https://kestra.io"><img src="https://img.shields.io/badge/Website-kestra.io-192A4E?color=blueviolet" alt="Kestra infinitely scalable orchestration and scheduling platform"></a>
<a href="https://kestra.io/slack"><img src="https://img.shields.io/badge/Slack-Join%20Community-blueviolet?logo=slack" alt="Slack"></a>
</div>

<br />

<p align="center">
  <a href="https://twitter.com/kestra_io" style="margin: 0 10px;">
        <img src="https://kestra.io/twitter.svg" alt="twitter" width="35" height="25" /></a>
  <a href="https://www.linkedin.com/company/kestra/" style="margin: 0 10px;">
        <img src="https://kestra.io/linkedin.svg" alt="linkedin" width="35" height="25" /></a>
  <a href="https://www.youtube.com/@kestra-io" style="margin: 0 10px;">
        <img src="https://kestra.io/youtube.svg" alt="youtube" width="35" height="25" /></a>
</p>

<br />
<p align="center">
    <a href="https://go.kestra.io/video/product-overview" target="_blank">
        <img src="https://kestra.io/startvideo.png" alt="Get started in 3 minutes with Kestra" width="640px" />
    </a>
</p>
<p align="center" style="color:grey;"><i>Get started with Kestra in 3 minutes.</i></p>


# Kestra Plugin for Coda

> A Kestra plugin for integrating with Coda documents and tables

This plugin enables Kestra workflows to interact with [Coda](https://coda.io) documents through the Coda API. It provides tasks for managing tables, columns, rows, and triggering automations.

![Kestra orchestrator](https://kestra.io/video.gif)

## Features

- **Table Operations**: List and discover tables in Coda documents
- **Column Management**: Retrieve column configurations and metadata
- **Row Operations**: Insert, update, upsert, and delete rows in Coda tables
- **Automations**: Trigger Coda automations via webhooks
- **Data Retrieval**: Query and fetch data from Coda tables with pagination support

## Documentation

📚 **For Developers**: Comprehensive documentation is available in the [`docs/`](./docs/) directory:

- **[Getting Started](./docs/GETTING_STARTED.md)** - 🚀 Start here! Step-by-step guide including:
  - Development environment setup
  - Creating your first task
  - Testing strategies
  - Local development with Kestra

- **[Coda API Guide](./docs/CODA_API_GUIDE.md)** - Complete reference for Coda API including:
  - Authentication and setup
  - Tables, columns, and rows operations
  - Comments limitations and workarounds
  - Automations and webhooks
  - Code examples and best practices
  
- **[Quick Reference](./docs/QUICK_REFERENCE.md)** - Handy cheat sheet with:
  - Common API endpoints
  - Request/response examples
  - Column types and query parameters
  
- **[Implementation Roadmap](./docs/IMPLEMENTATION_ROADMAP.md)** - Development guide including:
  - Phase-by-phase implementation plan
  - Task specifications and examples
  - Testing strategy
  - Timeline estimates

## Quick Start

### Authentication

To use this plugin, you need a Coda API token:

1. Sign in to your Coda account
2. Go to Account Settings → Developer/API Settings
3. Generate a new API token
4. Store it securely in Kestra's secret management

### Example Usage

#### List Tables
```yaml
id: coda-list-tables
namespace: io.kestra.plugin.coda

tasks:
  - id: list_tables
    type: io.kestra.plugin.coda.tables.ListTables
    apiToken: "{{ secret('CODA_API_TOKEN') }}"
    docId: "abc123xyz"
```

#### Insert Rows
```yaml
id: coda-insert-rows
namespace: io.kestra.plugin.coda

tasks:
  - id: insert_rows
    type: io.kestra.plugin.coda.rows.InsertRows
    apiToken: "{{ secret('CODA_API_TOKEN') }}"
    docId: "abc123xyz"
    tableId: "grid-pqRst-U"
    rows:
      - cells:
          - column: "Task Name"
            value: "My Task"
          - column: "Status"
            value: "In Progress"
```

#### Trigger Webhook Automation
```yaml
id: coda-trigger-webhook
namespace: io.kestra.plugin.coda

tasks:
  - id: trigger_automation
    type: io.kestra.plugin.coda.automations.TriggerWebhook
    apiToken: "{{ secret('CODA_API_TOKEN') }}"
    webhookUrl: "https://coda.io/apis/v1/webhooks/abc-123-def"
    payload:
      taskName: "Complete Documentation"
      priority: "High"
      daysRemaining: 5
      isUrgent: true
```

## Supported Operations

### Read Operations
- List tables in a document
- List columns in a table
- Retrieve rows with filtering and pagination

### Write Operations
- Insert new rows
- Update existing rows
- Upsert rows (insert or update based on key)
- Delete rows

### Automation
- Trigger webhook-based automations

## API Limitations

⚠️ Please note the following Coda API limitations:

- **Cannot create tables** programmatically (only list existing tables)
- **Comments are not accessible** via the API
- **Views are read-only** (use base tables for modifications)
- Rate limits apply (not publicly documented by Coda)

## Running the project locally

### Prerequisites
- Java 21
- Docker

### Running tests
```bash
./gradlew check --parallel
```

### Development

**VSCode:**

Follow the README.md within the `.devcontainer` folder for a quick and easy way to get up and running with developing plugins if you are using VSCode.

**Other IDEs:**

```bash
./gradlew shadowJar && docker build -t kestra-custom . && docker run --rm -p 8080:8080 kestra-custom server local
```
> [!NOTE]
> You need to relaunch this whole command every time you make a change to your plugin

Go to http://localhost:8080, your plugin will be available to use.

## Additional Resources

### Coda Resources
* **Coda API Documentation**: [https://coda.io/developers](https://coda.io/developers)
* **Getting Started Guide**: [https://coda.io/@oleg/getting-started-guide-coda-api](https://coda.io/@oleg/getting-started-guide-coda-api)
* **Coda Community**: [https://community.coda.io/](https://community.coda.io/)

### Kestra Resources
* **Full documentation**: [kestra.io/docs](https://kestra.io/docs)
* **Plugin Developer Guide**: [https://kestra.io/docs/plugin-developer-guide/](https://kestra.io/docs/plugin-developer-guide/)

## Contributing

We welcome contributions! Please see the [Implementation Roadmap](./docs/IMPLEMENTATION_ROADMAP.md) for development guidelines and planned features.


## License
Apache 2.0 © [Kestra Technologies](https://kestra.io)


## Stay up to date

We release new versions every month. Give the [main repository](https://github.com/kestra-io/kestra) a star to stay up to date with the latest releases and get notified about future updates.

![Star the repo](https://kestra.io/star.gif)
