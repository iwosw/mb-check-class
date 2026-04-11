# Phase 1: Build Reproducibility Baseline - Context

**Gathered:** 2026-04-05
**Status:** Ready for planning

<domain>
## Phase Boundary

Make clean-checkout builds and verification runs trustworthy for the current Forge 1.20.1 branch. This phase defines the canonical Gradle entrypoints, the reproducibility contract for dependency and tool resolution, and the maintainer setup/documentation baseline that later testing phases will build on.

</domain>

<decisions>
## Implementation Decisions

### Build and Verification Entrypoints
- **D-01:** Document `./gradlew build` as the one-command clean-checkout build workflow.
- **D-02:** Use Gradle lifecycle tasks rather than a mod-specific wrapper task, with `./gradlew check` as the canonical automated verification entrypoint.

### Verification Stage Reporting
- **D-03:** Keep build, unit-test, and game-test work as distinct Gradle stages/tasks so maintainers can tell which layer failed from normal task output.
- **D-04:** The standard verification flow should try to execute later stages when practical instead of always stopping at the first failing stage, so maintainers can see multiple broken layers in one run.

### Reproducibility Policy
- **D-05:** The default documented workflow must be strict about reproducibility and remove machine-specific or drifting resolution inputs wherever practical.
- **D-06:** Any local override or experimentation path must be explicit opt-in and outside the canonical maintainer workflow.

### Setup and Documentation Contract
- **D-07:** The clean-checkout workflow should assume minimal host prerequisites and automate Java/toolchain provisioning as far as the existing Gradle wrapper and toolchain setup allow.
- **D-08:** Document the canonical workflow in both `README.md` and a dedicated build-focused repo doc.

### the agent's Discretion
- Exact naming of any helper tasks added beneath `build` and `check`, as long as the canonical maintainer commands stay `./gradlew build` and `./gradlew check`.
- Exact formatting of any verification summary output or docs tables, as long as build, unit-test, and game-test stages stay clearly distinguishable.
- The concrete Gradle implementation used to continue into later verification stages when practical.

</decisions>

<specifics>
## Specific Ideas

- Lean on standard Gradle lifecycle commands instead of inventing a mod-specific top-level verification command.
- Make the default path strict and reproducible, while keeping any local-override workflow clearly separate from the documented baseline.
- Keep the initial maintainer setup lightweight rather than requiring a fully provisioned custom environment.

</specifics>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Phase Definition
- `.planning/ROADMAP.md` — Phase 1 goal, success criteria, and dependency position in the stabilization roadmap.
- `.planning/REQUIREMENTS.md` — `BLD-01` through `BLD-04`, which define the required build, verification, reproducibility, and failure-reporting outcomes.

### Project Constraints
- `.planning/PROJECT.md` — Project scope, non-negotiable constraints, and the rule that this initiative hardens the current 1.20.1 branch without expanding gameplay scope.
- `.planning/STATE.md` — Current project focus and the sequencing decision that Phase 1 establishes reproducible build and verification entrypoints before deeper stabilization work.

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `gradle/wrapper/gradle-wrapper.properties`: Wrapper already pins Gradle `8.8`, giving Phase 1 a stable starting point for the documented workflow.
- `settings.gradle`: Foojay toolchain resolver is already configured, so Java provisioning can stay close to standard Gradle toolchain behavior.
- `build.gradle`: The project already exposes Gradle lifecycle tasks like `build`, `check`, and `test`, plus Forge run configs with `forge.enabledGameTestNamespaces` set for `recruits`.

### Established Patterns
- The build is a single-project Groovy ForgeGradle setup centered in `build.gradle`, so Phase 1 changes should likely stay in the existing task graph rather than introducing another orchestration layer.
- Java 17 toolchains are already declared in `build.gradle`, which supports the decision to keep host prerequisites minimal.
- There is currently no `src/test` tree, no GameTest source tree, and almost no maintainer build documentation in `README.md`, so Phase 1 must establish baseline structure without relying on existing test/docs conventions.
- Current reproducibility risks are visible in `build.gradle`: `mavenLocal()`, `ForgeGradle 6.+`, and `0.7-SNAPSHOT` MixinGradle.

### Integration Points
- `build.gradle`: main place to harden repositories, task wiring, and verification-stage boundaries.
- `settings.gradle`: plugin and toolchain resolution entrypoint.
- `gradle.properties`: version pins and JVM defaults that affect reproducible execution.
- `README.md` plus a new dedicated build doc: canonical maintainer workflow documentation surface.

</code_context>

<deferred>
## Deferred Ideas

None — discussion stayed within phase scope.

</deferred>

---

*Phase: 01-build-reproducibility-baseline*
*Context gathered: 2026-04-05*
