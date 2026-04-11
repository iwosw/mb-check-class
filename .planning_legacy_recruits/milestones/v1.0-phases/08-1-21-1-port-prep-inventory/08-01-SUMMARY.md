---
phase: 08-1-21-1-port-prep-inventory
plan: 01
subsystem: infra
tags: [migration, inventory, planning, networking, persistence, compat, pathfinding]
requires:
  - phase: 07-migration-ready-internal-seams
    provides: seam inventory and extracted migration helpers that define the future port surface
  - phase: 06-full-surface-verification-and-safe-degradation
    provides: verification baseline and accepted debt rules the later port must preserve
provides:
  - Canonical seam-grounded inventory for the future 1.21.1 port
  - Bounded version-sensitive work-package map for later migration execution
affects: [08-02-PLAN, migration-prep, future-port]
tech-stack:
  added: []
  patterns: [seam-grounded migration inventory, bounded work-package mapping, carry-forward debt interpretation]
key-files:
  created:
    - .planning/phases/08-1-21-1-port-prep-inventory/08-PORT-INVENTORY.md
    - .planning/phases/08-1-21-1-port-prep-inventory/08-VERSION-SURFACE-MAP.md
  modified: []
key-decisions:
  - "Anchor the port inventory to Phase 7 seam helpers and summaries instead of reopening repo-wide source discovery."
  - "Group the future 1.21.1 work into three bounded packages so verification debt and preserved behavior stay reviewable during the port."
patterns-established:
  - "Migration-prep docs should point to seam helpers, preserved behavior, verification evidence, and likely API drift in one place."
  - "Future port planning should classify failures against accepted debt before treating them as new migration regressions."
requirements-completed: [MIG-03]
duration: 3min
completed: 2026-04-08
---

# Phase 8 Plan 1: Port inventory and version-surface map Summary

**A seam-grounded 1.21.1 port inventory plus three bounded migration work packages tied to preserved behavior and accepted verification debt.**

## Performance

- **Duration:** 3 min
- **Started:** 2026-04-08T01:37:24Z
- **Completed:** 2026-04-08T01:40:36Z
- **Tasks:** 2
- **Files modified:** 2

## Accomplishments
- Published the canonical Phase 8 inventory covering networking, registration glue, persistence, client state, compat, pathfinding, and support touchpoints.
- Connected each migration surface to its extracted Phase 7 seam/helper files, preserved behavior contract, and likely 1.21.1 drift points.
- Turned the raw inventory into three bounded future-port work packages with explicit carry-forward verification and accepted-debt interpretation rules.

## Task Commits

Each task was committed atomically:

1. **Task 1: Publish the canonical Phase 8 port inventory** - `895b4563` (feat)
2. **Task 2: Translate the inventory into bounded version-surface work packages** - `b7c99612` (feat)

**Plan metadata:** pending final docs commit

## Files Created/Modified
- `.planning/phases/08-1-21-1-port-prep-inventory/08-PORT-INVENTORY.md` - Canonical source-anchor inventory for all six migration surfaces plus build/test/documentation touchpoints.
- `.planning/phases/08-1-21-1-port-prep-inventory/08-VERSION-SURFACE-MAP.md` - Three bounded future-port work packages with drift categories, preservation rules, and debt carry-forward guidance.

## Decisions Made
- Used the Phase 7 seam inventory and summaries as the primary source of truth so the later port starts from narrowed contracts instead of broad source rediscovery.
- Kept accepted Phase 6 battle-density debt explicit in the map so future verification can separate inherited failures from new migration regressions.

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered
- `.planning/` is gitignored in this repository, so the task docs had to be staged with `git add -f` to preserve the required per-task commits.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness
- `08-02` can now turn this inventory into the ordered future-port checklist and follow-up brief without re-mapping seam surfaces.
- The later 1.21.1 migration can now start from explicit work packages, preserved behavior constraints, and accepted-debt interpretation rules.

## Self-Check: PASSED

- Verified `08-PORT-INVENTORY.md`, `08-VERSION-SURFACE-MAP.md`, and `08-01-SUMMARY.md` exist on disk.
- Verified task commits `895b4563` and `b7c99612` exist in git history.

---
*Phase: 08-1-21-1-port-prep-inventory*
*Completed: 2026-04-08*
