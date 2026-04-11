---
phase: 09-settlement-faction-binding-contract
plan: 01
subsystem: planning
tags: [planning, settlement, faction, claims, workers, recruits, bannermod]
requires: []
provides:
  - explicit settlement-faction lifecycle vocabulary for the merged runtime
  - active planning docs aligned to a derived claim-plus-infrastructure settlement contract
affects: [09-02, 10-01, 10-02, settlement, claims, logistics]
tech-stack:
  added: []
  patterns: [settlement binding stays derived from claims plus active worker infrastructure until code proves a dedicated manager is necessary]
key-files:
  created:
    - .planning/phases/09-settlement-faction-binding-contract/09-settlement-faction-contract.md
  modified:
    - .planning/CODEBASE.md
    - .planning/codebase/INTEGRATED_SYSTEM_ARCHITECTURE.md
    - MERGE_NOTES.md
key-decisions:
  - "Define settlement as a derived claim-plus-infrastructure footprint instead of promising a new persistence manager."
  - "Use explicit friendly, hostile, unclaimed, and degraded vocabulary before adding deeper runtime enforcement."
patterns-established:
  - "Settlement legality and later degradation work should cite one shared contract instead of re-deriving semantics from scattered architecture notes."
requirements-completed: []
duration: pending
completed: 2026-04-11
---

# Phase 09 Plan 01: Settlement-Faction Binding Contract Summary

**Phase 09 now has one active contract document that defines settlement-faction binding as a derived claim-plus-work-area footprint with explicit friendly, hostile, unclaimed, and degraded states.**

## Accomplishments

- Added `09-settlement-faction-contract.md` as the active settlement-faction contract for the merged runtime.
- Aligned `.planning/CODEBASE.md` and `.planning/codebase/INTEGRATED_SYSTEM_ARCHITECTURE.md` to the same settlement-binding vocabulary and scope boundary.
- Recorded the older implied-settlement wording conflict in `MERGE_NOTES.md` so future slices do not assume a deeper manager than current code justifies.

## Files Created/Modified

- `.planning/phases/09-settlement-faction-binding-contract/09-settlement-faction-contract.md` - Active contract for settlement binding, lifecycle states, and runtime seam grounding.
- `.planning/CODEBASE.md` - Shared vocabulary updated with the settlement-faction contract seam.
- `.planning/codebase/INTEGRATED_SYSTEM_ARCHITECTURE.md` - Settlement/claim relationship tightened around explicit degradation and derived binding.
- `MERGE_NOTES.md` - Logged the conflict between earlier implied wording and the new explicit contract.

## Decisions Made

- Kept the contract planning-first and grounded in existing claim/work-area seams.
- Avoided promising a standalone settlement manager or save rewrite before runtime evidence exists.

## Deviations from Plan

None - plan executed within the intended scope.

## Issues Encountered

- None in-scope.

## Next Phase Readiness

- Plan 02 can now expose one runtime settlement-binding query seam against the same vocabulary already documented here.

---
*Phase: 09-settlement-faction-binding-contract*
*Completed: 2026-04-11*
