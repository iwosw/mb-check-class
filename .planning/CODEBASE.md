# Merged Codebase Readiness

## Active Runtime

- Root Gradle project is the only active build entrypoint.
- Active shipped mod/runtime id: `bannermod`.
- Runtime base: `recruits/`.
- Workers is an in-process subsystem compiled into the same root runtime from `workers/src/main/java`.

## Source-of-Truth Paths

- Planning source of truth: `.planning/`
- Merge decisions and plan/code conflicts: `MERGE_NOTES.md`
- Root build/runtime wiring: `build.gradle`, `settings.gradle`, `src/main/resources/`
- Active root runtime code: `src/main/java/`, `recruits/src/main/java/`, `workers/src/main/java/`
- Root regression coverage: `src/test/java/`
- Root gametest source set: `src/gametest/` and `recruits/src/gametest/`
- Active root docs describe the intended root-owned architecture even while preserved source trees remain in place.

## Legacy Archives

- Recruits planning archive: `.planning_legacy_recruits/`
- Workers planning archive: `.planning_legacy_workers/`
- Legacy archives are preserved for historical context only and are not the active execution root.
- Follow-up agents must not treat archived planning files as active requirements unless a root doc explicitly points to them.

## Verification Entry Points

- Fast compile check: `./gradlew compileJava`
- Resource merge check: `./gradlew processResources`
- Root regression check: `./gradlew test`
- Optional runtime gametest hook: `./gradlew runGameTestServer`

## Current Coverage Baseline

- Root JUnit coverage exists for merged Workers legacy-id migration and lightweight merged-runtime smoke checks.
- Root gametest source set is wired but currently acts as an expansion point rather than an enforced baseline.
- Full gameplay E2E coverage is still deferred; use targeted smoke/regression tests unless a task explicitly needs runtime validation.

## Open Risks

- `runGameTestServer` is configured in the root build but the root gametest source set is effectively empty, so gameplay verification still leans on compile plus unit smoke checks.
- Some preserved legacy namespaces and source trees remain intentionally present during stabilization; future cleanup must keep save/runtime compatibility truthful.
- Unknown third-party datapacks or custom payloads may still contain raw `workers:*` references outside the already-covered migration paths.
