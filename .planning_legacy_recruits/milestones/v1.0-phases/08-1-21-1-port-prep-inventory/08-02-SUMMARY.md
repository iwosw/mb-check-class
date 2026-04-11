---
phase: 08-1-21-1-port-prep-inventory
plan: 02
subsystem: infra
tags: [migration, checklist, handoff, verification, bounded-scope]
requires:
  - phase: 08-1-21-1-port-prep-inventory
    provides: Phase 8 inventory and bounded work-package map for the future port
  - phase: 06-full-surface-verification-and-safe-degradation
    provides: accepted debt and verification interpretation rules that the later port must carry forward
provides:
  - Ordered future-port checklist with verification commands and scope boundaries
  - Bounded follow-up brief proving the later 1.21.1 port is no longer open-ended
affects: [phase-completion, future-port, migration-prep]
tech-stack:
  added: []
  patterns: [ordered migration checklist, bounded handoff brief, phase-level accepted-debt carry-forward]
key-files:
  created:
    - .planning/phases/08-1-21-1-port-prep-inventory/08-PORT-CHECKLIST.md
    - .planning/phases/08-1-21-1-port-prep-inventory/08-BOUNDED-FOLLOW-UP.md
  modified: []
key-decisions:
  - "Use the Phase 8 work-package map as the checklist spine so the later port starts with ordered workstreams instead of ad hoc sequencing."
  - "Carry the Phase 6 accepted battle-density debt into the handoff brief so future verification distinguishes inherited failures from new migration regressions."
patterns-established:
  - "Future port execution should begin from inventory, map, then checklist, with the bounded brief acting as the scope guardrail."
  - "Phase-complete migration prep must name preserved seam contracts, accepted debt, out-of-scope limits, and done criteria in one handoff set."
requirements-completed: [MIG-03]
duration: 2min
completed: 2026-04-08
---

# Phase 8 Plan 2: Port checklist and bounded follow-up Summary

**An ordered 1.21.1 port checklist plus bounded handoff brief that turns Phase 8 from inventory-only preparation into an executable, scope-guarded migration follow-up.**

## Performance

- **Duration:** 2 min
- **Started:** 2026-04-08T01:42:35Z
- **Completed:** 2026-04-08T01:44:49Z
- **Tasks:** 2
- **Files modified:** 2

## Accomplishments
- Published an ordered checklist that tells a future maintainer how to execute the bounded 1.21.1 migration workstreams.
- Added a short handoff brief that names the starting files, preserved seam contracts, accepted debt, and out-of-scope boundaries.
- Completed the Phase 8 artifact set so MIG-03 now has inventory, work-package map, executable checklist, and bounded follow-up framing.

## Task Commits

Each task was committed atomically:

1. **Task 1: Write the ordered future-port checklist** - `69214518` (feat)
2. **Task 2: Publish the bounded follow-up brief** - `bde2e610` (feat)

**Plan metadata:** pending final docs commit

## Files Created/Modified
- `.planning/phases/08-1-21-1-port-prep-inventory/08-PORT-CHECKLIST.md` - Ordered checklist with workstream sequence, touchpoints, preserved behavior, and verification commands.
- `.planning/phases/08-1-21-1-port-prep-inventory/08-BOUNDED-FOLLOW-UP.md` - Concise handoff brief covering starting points, preserved contracts, accepted debt, scope limits, and done criteria.

## Decisions Made
- Built the checklist directly from the new Phase 8 inventory and version-surface map so no fresh repo exploration was needed.
- Made accepted debt interpretation explicit in both artifacts so future maintainers can classify port verification results consistently.

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered
- `.planning/` remains gitignored in this repository, so the plan artifacts again required `git add -f` for the required task and metadata commits.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness
- Phase 8 now provides a complete migration-prep artifact chain: inventory, version-surface map, ordered checklist, and bounded follow-up brief.
- The future 1.21.1 port can begin from named seams, ordered workstreams, and explicit verification interpretation rules rather than open-ended source discovery.

## Self-Check: PASSED

- Verified `08-PORT-CHECKLIST.md`, `08-BOUNDED-FOLLOW-UP.md`, and `08-02-SUMMARY.md` exist on disk.
- Verified task commits `69214518` and `bde2e610` exist in git history.

---
*Phase: 08-1-21-1-port-prep-inventory*
*Completed: 2026-04-08*
