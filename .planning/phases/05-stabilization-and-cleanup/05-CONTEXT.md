# Phase 05 Context

## Phase

- Number: 05
- Name: Stabilization and Cleanup

## Goal

- Turn the already-merged BannerMod runtime into a lower-risk baseline by validating the current merged behavior, removing clearly dead transition-only seams where safe, and keeping cleanup claims truthful.

## Current Reality

- Workers runtime ownership is already absorbed into the merged BannerMod runtime.
- Active runtime identity is `bannermod`.
- Root verification already runs through `./gradlew compileJava`, `./gradlew processResources`, and `./gradlew test`.
- Known `workers:*` compatibility support is intentionally narrow and limited to documented merged-runtime migration seams.
- Root GameTest wiring exists, but active root gameplay smoke coverage is still sparse.

## In Scope

- Add or tighten targeted merged-runtime smoke validation where it reduces stabilization risk.
- Audit transition-only adapters and remove only seams that are clearly unused and no longer part of the active compatibility contract.
- Update planning/state docs so the active phase, verification baseline, and remaining cleanup scope are consistent.

## Out Of Scope

- Broad new compatibility guarantees for arbitrary third-party `workers:*` integrations.
- Reviving a standalone `workers` runtime identity.
- Large package/tree rewrites unless a slice proves ownership has fully moved and cleanup is low-risk.

## Constraints

- Preserve the Phase 02 compatibility contract as the truth boundary.
- Prefer root smoke/regression verification unless a slice changes actual gameplay/runtime flow.
- Keep preserved legacy source trees in place unless a slice can retire them without changing shipped behavior.

## Evidence

- `.planning/ROADMAP.md`
- `.planning/STATE.md`
- `.planning/CODEBASE.md`
- `.planning/VERIFICATION.md`
- `.planning/REQUIREMENTS.md`
- `MERGE_NOTES.md`
