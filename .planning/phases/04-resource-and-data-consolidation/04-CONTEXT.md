# Phase 04 Context

## Phase

- Number: 04
- Name: Resource and Data Consolidation

## Goal

- Record the merged resource/data consolidation state truthfully so the planning tree matches the runtime that already ships through the root `bannermod` workspace.

## Current Reality

- Workers GUI textures used by the merged runtime already resolve from `assets/bannermod/textures/gui/workers/**`.
- Workers bundled structures used by the merged runtime already resolve from `assets/bannermod/structures/workers/**`.
- Safe Workers UI, chat, and description language keys already route through active `bannermod` lang files.
- Workers registry-coupled content now publishes under `bannermod`, with narrow `workers:*` migration support preserved only on documented critical seams.
- The root runtime owns the merged access transformer and active pack wiring; standalone Workers metadata is no longer a live shipped boundary.

## Reconciled Status

- Phase 04 is complete in practice for the active merged runtime.
- Remaining `assets/workers/**` preservation and deeper legacy-tree cleanup are stabilization follow-up, not blocking resource/data consolidation.

## In Scope For Reconciliation

- Document the landed consolidation outcomes.
- Keep the planning tree consistent with the root roadmap and merge notes.
- Preserve the Phase 02 compatibility boundary while treating remaining `workers` namespaces as migration-only.

## Out Of Scope

- Catch-all migration for arbitrary third-party datapacks or payloads.
- Large new resource moves that would change current compatibility guarantees.
- Retiring preserved source/resource trees without a dedicated cleanup slice.

## Evidence

- `.planning/ROADMAP.md`
- `.planning/STATE.md`
- `MERGE_NOTES.md`
- `build.gradle`
- `src/main/resources/META-INF/accesstransformer.cfg`
