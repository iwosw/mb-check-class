---
phase: 20-runtime-audit-and-bannerlord-target-architecture
plan: 02
subsystem: architecture
tags: [architecture, bannerlord, bannermod, migration, compatibility]
requires:
  - phase: 20-runtime-audit-and-bannerlord-target-architecture
    provides: runtime ownership audit and matrix baseline for the move decision
provides:
  - canonical Bannerlord package destination and move-order contract for Phase 21
  - explicit adapter and compatibility boundary for source-tree consolidation
  - root roadmap/state handoff from Phase 20 planning to Phase 21 execution
affects: [21-source-tree-consolidation-into-bannerlord, architecture, migration]
tech-stack:
  added: []
  patterns: [wave-based package migration, narrow compatibility boundary, adapter-first shared seam relocation]
key-files:
  created:
    - .planning/phases/20-runtime-audit-and-bannerlord-target-architecture/20-TARGET-ARCHITECTURE.md
  modified:
    - .planning/phases/20-runtime-audit-and-bannerlord-target-architecture/20-OWNERSHIP-MATRIX.md
    - .planning/ROADMAP.md
    - .planning/STATE.md
key-decisions:
  - "Move shared BannerMod seam classes first into com.talhanation.bannerlord before re-homing bootstrap or worker-heavy packages."
  - "Keep the live bannermod mod id, shared channel, config filenames, and workers legacy-id migration helpers stable through the Phase 21 move."
  - "Treat worker package relocation as a dependent wave that follows recruit-owned entity, pathfinding, persistence, and client-base relocation."
patterns-established:
  - "Migration Pattern: separate Java package relocation from runtime-facing mod-id and asset-namespace stability."
  - "Execution Pattern: use explicit move waves so worker code never moves ahead of the recruit-owned seams it imports."
requirements-completed: []
duration: 4 min
completed: 2026-04-12
---

# Phase 20 Plan 02: Runtime Audit And Bannerlord Target Architecture Summary

**Bannerlord package-move contract with wave-based relocation, narrow workers compatibility seams, and a Phase 21 handoff anchored to one bannermod runtime**

## Performance

- **Duration:** 4 min
- **Started:** 2026-04-12T07:29:35Z
- **Completed:** 2026-04-12T07:34:00Z
- **Tasks:** 2
- **Files modified:** 4

## Accomplishments
- Published `20-TARGET-ARCHITECTURE.md` as the canonical Phase 21 move contract for `com.talhanation.bannerlord/**`.
- Expanded the ownership matrix with explicit move waves so shared seams, bootstrap, recruit-owned bases, and worker packages have a concrete relocation order.
- Updated root roadmap/state context so Phase 20 is complete and Phase 21 source-tree consolidation now has an explicit planning baseline.

## Task Commits

Each task was committed atomically:

1. **Task 1: Publish the Bannerlord target architecture, move map, and compatibility boundary** - `dee238f` (docs)
2. **Task 2: Refresh root planning docs to hand off from Phase 20 planning to Phase 21 execution planning** - `2415170` (docs)

**Plan metadata:** pending

## Files Created/Modified
- `.planning/phases/20-runtime-audit-and-bannerlord-target-architecture/20-TARGET-ARCHITECTURE.md` - Defines target package families, move waves, adapter policy, compatibility boundary, and retirement preconditions.
- `.planning/phases/20-runtime-audit-and-bannerlord-target-architecture/20-OWNERSHIP-MATRIX.md` - Aligns each technical surface with the new move-wave order.
- `.planning/ROADMAP.md` - Marks Phase 20 complete and points Phase 21 at the new target-architecture contract.
- `.planning/STATE.md` - Updates active focus and artifact references to the completed Phase 20 handoff set.

## Decisions Made
- Move the existing root `com.talhanation.bannermod/**` seam classes first so later recruit and worker package moves import a stable `com.talhanation.bannerlord` shared layer.
- Preserve runtime-facing stability during the Java package move: `bannermod` stays the mod id, the shared network channel stays singular, and `workers:*` migration helpers remain adapter-scoped.
- Keep worker relocation behind recruit-owned base/system relocation because the current civilian code is still materially coupled to recruit entity, pathfinding, persistence, and client infrastructure.

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered
- The provided verification snippets assumed a `python` executable, but the workspace exposes `python3`; verification was run successfully with `python3` without changing repository files.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness
- Phase 21 now has an explicit move order: shared seams/config first, bootstrap/network/registries second, recruit-owned bases third, worker packages fourth, compatibility cleanup last.
- The remaining execution risk is not ambiguity but disciplined sequencing around worker dependence on recruit-owned systems and the requirement to keep legacy `workers:*` migration paths truthful until formally retired.


## Self-Check: PASSED

- FOUND: `.planning/phases/20-runtime-audit-and-bannerlord-target-architecture/20-runtime-audit-and-bannerlord-target-architecture-02-SUMMARY.md`\n- FOUND: `dee238f`\n- FOUND: `2415170`
