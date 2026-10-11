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


# Kestra Plugin for Superhuman Docs

> A Kestra plugin for integrating with Superhuman Docs (formerly Coda) documents and tables

This plugin enables Kestra workflows to interact with [Superhuman Docs](https://docs.superhuman.com) documents through the Superhuman Docs API. Coda was renamed to Superhuman Docs on 2026-07-08; existing coda.io doc links redirect to docs.superhuman.com, and the API is still served at `https://coda.io/apis/v1`.

> [!NOTE]
> **Status:** the API client, models and the `SuperhumanDocsTask` base class are in place; the concrete tasks listed below (such as `ListTables`) are planned. See the [Implementation Roadmap](./docs/IMPLEMENTATION_ROADMAP.md).

![Kestra orchestrator](https://kestra.io/video.gif)

## Features

- **Table Operations**: List and discover tables in Superhuman Docs documents
- **Column Management**: Retrieve column configurations and metadata
- **Row Operations**: Insert, update, upsert, and delete rows in Superhuman Docs tables
- **Automations**: Trigger Superhuman Docs automations via webhooks
- **Data Retrieval**: Query and fetch data from Superhuman Docs tables with pagination support

## Documentation

📚 **For Developers**: Comprehensive documentation is available in the [`docs/`](./docs/) directory:

- **[Getting Started](./docs/GETTING_STARTED.md)** - 🚀 Start here! Step-by-step guide including:
  - Development environment setup
  - Creating your first task
  - Testing strategies
  - Local development with Kestra

- **[Superhuman Docs API Guide](./docs/SUPERHUMAN_DOCS_API_GUIDE.md)** - Complete reference for the Superhuman Docs API including:
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

To use this plugin, you need a Superhuman Docs API token:

1. Sign in to Superhuman Docs at https://docs.superhuman.com
2. Open your Superhuman Docs account settings and find the API settings
3. Generate a new API token
4. Store it securely in Kestra's secret management

### Example Usage

```yaml
id: superhuman-docs-example
namespace: company.team

tasks:
  - id: list_tables
    type: io.kestra.plugin.superhuman.docs.ListTables # planned task
    apiToken: "{{ secret('SUPERHUMAN_DOCS_API_TOKEN') }}"
    docId: "abc123xyz"
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

⚠️ Please note the following Superhuman Docs API limitations:

- **Cannot create tables** programmatically (only list existing tables)
- **Comments are not accessible** via the API
- **Views are read-only** (use base tables for modifications)
- Rate limits apply (not publicly documented)

## Running the project locally

### Compatibility
- Built against Kestra **2.0** (`kestraVersion=2.0.5` in `gradle.properties`); targets Kestra 2.0.x servers.
- The local dev image (`Dockerfile`) is pinned to `kestra/kestra:v2.0`.

### Prerequisites
- Java 25 (required by Kestra 2.0)
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

### Superhuman Docs Resources
* **Superhuman Docs API reference**: [https://docs.superhuman.com/developers/apis/v1](https://docs.superhuman.com/developers/apis/v1)
* **Superhuman Docs**: [https://docs.superhuman.com](https://docs.superhuman.com)

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
