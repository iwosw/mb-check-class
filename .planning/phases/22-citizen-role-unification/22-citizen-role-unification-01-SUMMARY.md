---
phase: 22-citizen-role-unification
plan: 01
subsystem: runtime
tags: [citizen, persistence, nbt, compatibility]
requires: []
provides:
  - shared citizen core contract
  - immutable citizen snapshot value object
  - compatibility-safe recruit and worker persistence bridge
affects: [recruits, workers, gametest]
tech-stack:
  added: []
  patterns: [wrapper-friendly citizen seam, legacy-nbt bridge]
key-files:
  created: [src/main/java/com/talhanation/bannermod/citizen/CitizenCore.java, src/main/java/com/talhanation/bannermod/citizen/CitizenStateSnapshot.java, src/main/java/com/talhanation/bannermod/citizen/CitizenPersistenceBridge.java, src/test/java/com/talhanation/bannermod/citizen/CitizenPersistenceBridgeTest.java]
  modified: []
key-decisions:
  - "Keep the citizen seam additive by centering it on an interface plus immutable snapshot rather than a new base entity."
  - "Bridge legacy recruit and worker NBT into one snapshot shape before any live wrapper delegates to it."
patterns-established:
  - "CitizenCore exposes shared wrapper-facing state without changing runtime ids or entrypoints."
  - "CitizenPersistenceBridge owns legacy key compatibility for recruit and worker save shapes."
requirements-completed: [CITIZEN-01]
duration: not recorded
completed: 2026-04-13
---

# Phase 22 Plan 01: Citizen Role Unification Summary

**Citizen core contracts plus a legacy-safe NBT bridge now carry shared recruit and worker state through one bannermod seam.**

## Performance

- **Duration:** not recorded
- **Started:** 2026-04-13T00:00:00Z
- **Completed:** 2026-04-13T00:00:00Z
- **Tasks:** 2
- **Files modified:** 4

## Accomplishments
- Added `CitizenCore` as the shared wrapper-friendly state contract.
- Added immutable `CitizenStateSnapshot` for shared recruit and worker state capture.
- Added `CitizenPersistenceBridge` plus targeted JUnit coverage for recruit and worker legacy NBT round-trips.

## Task Commits

1. **Task 1: Define the citizen core contracts and shared snapshot types** - `db84066` (feat)
2. **Task 2: Implement the compatibility-safe citizen persistence bridge** - `bc16934`, `97f7a25` (test → feat)

## Files Created/Modified
- `src/main/java/com/talhanation/bannermod/citizen/CitizenCore.java` - shared citizen-owned runtime contract
- `src/main/java/com/talhanation/bannermod/citizen/CitizenStateSnapshot.java` - immutable shared citizen state carrier
- `src/main/java/com/talhanation/bannermod/citizen/CitizenPersistenceBridge.java` - legacy recruit/worker NBT bridge
- `src/test/java/com/talhanation/bannermod/citizen/CitizenPersistenceBridgeTest.java` - focused persistence regression coverage

## Decisions Made
- Keep the first citizen slice pure and persistence-focused.
- Preserve legacy recruit and worker key names on write paths.

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered
- None.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness
- Plan 22-02 can bind recruit and worker wrappers to the shared citizen seam.

## Self-Check: PASSED

---
*Phase: 22-citizen-role-unification*
*Completed: 2026-04-13*
