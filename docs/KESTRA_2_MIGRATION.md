# Kestra 2.0 migration spec — Coda plugin

Status: implemented on branch `claude/kestra-2-plugin-readiness-r36f3i`.
Target: Kestra **2.0.5** (`io.kestra:platform:2.0.5`), Java **25**.

## Why

Kestra 2.0 (tagged 2026-09-07) is compiled to Java 25 bytecode (class file major 69) and removes APIs that were
deprecated in 1.x. The repository currently contains Coda client/model code plus the Kestra plugin-template
scaffolding (`io.kestra.plugin.templates.Example` / `Trigger`). The scaffolding used removed APIs.

## Upstream changes that affect this repo (verified)

| Change in 2.0 | How verified | Impact here |
|---|---|---|
| `Property.of(V)` and the `Property` constructors removed (use `Property.ofValue` / `Property.ofExpression`) | `javap` on `core-2.0.5.jar` | `ExampleTest` and a `docs/GETTING_STARTED.md` snippet updated |
| Task outputs no longer kept on the `Execution`; `TaskRun.getOutputs()` returns `Map` | kestra-io/plugin-template `fix/v2` | `ExampleRunnerTest` reads outputs via `TaskOutputService` |
| `PollingTriggerInterface.evaluate()` deprecated for removal → `eval(...)` / `TriggerService.generateEvaluationResult` | `javap` | Template `Trigger` ported |
| The worker talks to an embedded controller over gRPC (default port 50051) | Runner logs | Test `application.yml` uses `${random.port}` |
| Core classes are Java 25 bytecode | `javap` (major 69) | `VERSION_25`, CI on Java 25 |

The Coda-specific code (`CodaTask`, `client`, `models`, `exceptions`) used no removed API.

## Build and tooling

- `kestraVersion=2.0.5`, Java 25, Gradle wrapper 9.3.1 → 9.8.0. Plugin bumps: vanniktech publish 0.36.0 → 0.37.0,
  `inject-bom-versions` 1.0.2 → 1.2.11, shadow 9.4.0 → 9.6.1, gradle-release 3.1.0 → 3.2.0.
- Allure/AspectJ test agent removed. The `indexer` test dependency was added to match plugin-template `fix/v2`.
- `lombok.config` pins `@Jacksonized` to Jackson 2. That is lombok's default; the pin only silences a warning.
- `main.yml` passes `java-version: '25'` to the shared kestra-io workflow (which defaults to 21).
- Dev `Dockerfile` pinned to `kestra/kestra:v2.0`; devcontainer on Temurin 25.
- `release.yml` is now self-contained (see below).

### Release workflow

The shared `kestra-io/actions/composite/plugin-release@main` action can't be used anymore. It installs Java 21, it
requires kestra-io GitHub App inputs, and its `release.sh` is currently hard-disabled upstream (`exit 1`). The new
workflow:
1. runs `./gradlew check` on Java 25;
2. runs the gradle-release plugin, which tags `v<version>` and bumps to the next `-SNAPSHOT` on `main`;
3. attaches the shadow jar to a GitHub Release.

It also has a `dryRun` input. Maven Central publishing is not wired up: the group `io.kestra.plugin` is a namespace
owned by Kestra.

## Verification

- Baseline on Kestra 1.0.0 / JDK 21: 2/2 tests green.
- After: `./gradlew clean check shadowJar` on JDK 25: **2 tests, 0 failures** (re-run independently).
- The shadow jar's classes are Java 25 bytecode, and the jar manifest carries all five `X-Kestra-*` attributes.
- The release workflow passes `actionlint` (with shellcheck).
- **Not verified:** a run on a live Kestra 2.0 server; the Temurin 25 devcontainer download URL.

## Known pre-existing issues (not changed here)

- No Coda task is implemented yet. `ListTables`, mentioned in the docs, does not exist.
- `settings.gradle` has `rootProject.name = 'plugin-template'` and the main sources still carry the
  `io.kestra.plugin.templates` package. The jar is `plugin-template-<version>.jar`.

## Sources

- Kestra 2.0 migration guide: https://github.com/kestra-io/docs/tree/main/src/contents/docs/11.migration-guide/v2.0.0
- Kestra 2.0 plugins blog: https://github.com/kestra-io/docs/blob/main/src/contents/blogs/kestra-2-0-plugins/index.md
- Plugin template 2.0 branch: https://github.com/kestra-io/plugin-template/tree/fix/v2
- Artifacts: https://repo1.maven.org/maven2/io/kestra/core/2.0.5/
- Shared release action: https://github.com/kestra-io/actions/tree/main/composite/plugin-release
