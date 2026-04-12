---
phase: 20-runtime-audit-and-bannerlord-target-architecture
plan: 01
subsystem: architecture
tags: [audit, ownership, bannermod, bannerlord, migration]
requires:
  - phase: 19-large-battle-performance-validation
    provides: performance validation is complete and Phase 20 can pivot to structural planning
provides:
  - code-backed runtime ownership audit for the merged bannermod runtime
  - technical-surface ownership matrix for the Phase 21 bannerlord package move
  - recorded merge-note correction for the bannerlord destination wording
affects: [21-source-tree-consolidation-into-bannerlord, architecture, migration]
tech-stack:
  added: []
  patterns: [code-backed planning docs, source-root ownership matrix, narrow compatibility audit]
key-files:
  created:
    - .planning/phases/20-runtime-audit-and-bannerlord-target-architecture/20-RUNTIME-AUDIT.md
    - .planning/phases/20-runtime-audit-and-bannerlord-target-architecture/20-OWNERSHIP-MATRIX.md
  modified:
    - MERGE_NOTES.md
key-decisions:
  - "Treat the live merged runtime as recruit-led with workers absorbed through subsystem composition, not as two independent mods."
  - "Keep the Phase 21 physical move target at src/main/java/com/talhanation/bannerlord/** while preserving the live bannermod mod id."
  - "Carry forward only narrow save/runtime-critical workers compatibility seams during source-root retirement planning."
patterns-established:
  - "Audit Pattern: inventory runtime ownership by technical surface before package relocation."
  - "Migration Pattern: separate Java package destination decisions from runtime mod-id and asset-namespace stability."
requirements-completed: []
duration: 7 min
completed: 2026-04-12
---

# Phase 20 Plan 01: Runtime Audit And Bannerlord Target Architecture Summary

**Runtime ownership audit and move-order matrix for the recruit-led bannermod runtime heading toward com.talhanation.bannerlord packages**

## Performance

- **Duration:** 7 min
- **Started:** 2026-04-12T07:19:04Z
- **Completed:** 2026-04-12T07:26:13Z
- **Tasks:** 2
- **Files modified:** 3

## Accomplishments
- Published a narrative audit tying the live runtime back to `build.gradle`, recruit bootstrap, worker subsystem composition, and root integration seams.
- Published a concise ownership matrix covering bootstrap/runtime identity, registries, entities, AI/pathfinding, networking, persistence/storage, config, client/UI, assets/resources, shared seams, compatibility, and root-side jars.
- Corrected `MERGE_NOTES.md` so active planning truth now points at the `com.talhanation.bannerlord` package destination instead of older `bannermod` destination wording.

## Task Commits

Each task was committed atomically:

1. **Task 1: Publish the runtime audit of physical ownership, bootstrap composition, and legacy pressure** - `721c572` (docs)
2. **Task 2: Publish the ownership matrix across technical surfaces and migration blockers** - `d36c504` (chore)

**Plan metadata:** pending

## Files Created/Modified
- `.planning/phases/20-runtime-audit-and-bannerlord-target-architecture/20-RUNTIME-AUDIT.md` - Narrative audit of the live merged runtime, ownership split, cross-package dependencies, and jar pressure.
- `.planning/phases/20-runtime-audit-and-bannerlord-target-architecture/20-OWNERSHIP-MATRIX.md` - Technical-surface matrix for Phase 21 move sequencing and blockers.
- `MERGE_NOTES.md` - Recorded the active bannerlord destination wording as source-of-truth planning guidance.

## Decisions Made
- Treat the current codebase as one merged runtime with recruit-owned bootstrap and workers absorbed through explicit subsystem composition.
- Keep the future Java move target at `src/main/java/com/talhanation/bannerlord/**` while preserving the runtime-facing `bannermod` identity.
- Treat `WorkersRuntime`, `WorkersLegacyMappings`, and structure-id migration as narrow compatibility seams that must survive the source-tree move until retired deliberately.

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered
- The provided verification snippets assumed a `python` executable, but the workspace exposes `python3`; verification was rerun successfully with `python3` without changing repository files.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness
- Phase 21 can now sequence package moves from an explicit inventory of live ownership, coupling, and compatibility pressure.
- The highest blockers are worker dependence on recruit entity/pathfinding/client infrastructure and the need to preserve the shared `bannermod` runtime identity during the Java package move.

## Self-Check: PASSED

- FOUND: `.planning/phases/20-runtime-audit-and-bannerlord-target-architecture/20-runtime-audit-and-bannerlord-target-architecture-01-SUMMARY.md`
- FOUND: `721c572`
- FOUND: `d36c504`
