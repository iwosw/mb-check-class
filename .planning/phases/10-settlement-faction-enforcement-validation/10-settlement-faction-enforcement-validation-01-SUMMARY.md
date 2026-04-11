---
phase: 10-settlement-faction-enforcement-validation
plan: 01
subsystem: runtime
tags: [runtime, settlement, faction, claims, workers, gametest, bannermod]
requires:
  - phase: 09-settlement-faction-binding-contract
    provides: explicit settlement-faction runtime seam and shared status vocabulary
provides:
  - root GameTest coverage for friendly settlement placement and operation inside faction claims
  - root GameTest coverage for hostile and unclaimed placement denial when claim restriction is enabled
  - a direct server-side placement entrypoint that lets GameTests exercise the real work-area placement path without mocking network context
affects: [10-02, settlement, claims, worker-authoring, worker-participation]
tech-stack:
  added: []
  patterns: [settlement enforcement validation now runs through live GameTests and the shared BannerMod settlement binding seam]
key-files:
  created:
    - src/gametest/java/com/talhanation/bannermod/BannerModSettlementFactionEnforcementGameTests.java
  modified:
    - src/gametest/java/com/talhanation/bannermod/BannerModDedicatedServerGameTestSupport.java
    - src/gametest/java/com/talhanation/bannermod/BannerModGameTestSupport.java
    - workers/src/main/java/com/talhanation/workers/network/MessageAddWorkArea.java
key-decisions:
  - "Use the real server-side work-area placement logic in GameTests by adding a minimal direct-player entrypoint instead of mocking Forge network context."
  - "Keep claim and faction setup inside additive GameTest helpers so the validation stays close to live runtime behavior without introducing a new settlement test subsystem."
patterns-established:
  - "Later settlement validation slices can seed or mutate live claims through the shared dedicated-server GameTest helpers instead of duplicating setup logic."
requirements-completed: []
duration: pending
completed: 2026-04-11
---

# Phase 10 Plan 01: Settlement-Faction Enforcement Validation Summary

**BannerMod now has root GameTest coverage proving that friendly faction claims allow settlement placement and operation while hostile or unclaimed territory is denied through the shared settlement-binding seam.**

## Accomplishments

- Added `BannerModSettlementFactionEnforcementGameTests` for friendly placement plus hostile and unclaimed denial scenarios.
- Exercised the real `MessageAddWorkArea` server placement logic through a minimal direct-player entrypoint instead of reimplementing placement rules inside tests.
- Added claim and faction seeding helpers so the tests can assert `FRIENDLY_CLAIM`, `HOSTILE_CLAIM`, and `UNCLAIMED` using live runtime state.

## Files Created/Modified

- `src/gametest/java/com/talhanation/bannermod/BannerModSettlementFactionEnforcementGameTests.java` - Root GameTests for friendly settlement binding and hostile or unclaimed denial.
- `src/gametest/java/com/talhanation/bannermod/BannerModDedicatedServerGameTestSupport.java` - Additive helpers for seeding factions and claims used by the new settlement enforcement tests.
- `src/gametest/java/com/talhanation/bannermod/BannerModGameTestSupport.java` - Owned work-area helpers now also preserve the player's current team string when present.
- `workers/src/main/java/com/talhanation/workers/network/MessageAddWorkArea.java` - Added a direct server-player execution path so GameTests can exercise real placement behavior without a mocked network context.

## Decisions Made

- Kept enforcement validation grounded in the existing claim-derived settlement seam.
- Limited runtime changes to testability support around the existing placement path rather than introducing new settlement orchestration.

## Deviations from Plan

None - plan executed within the intended scope.

## Issues Encountered

- None in-scope.

## Next Phase Readiness

- Phase 10 plan 02 can now mutate live claims and verify degradation behavior on top of the same settlement-binding and GameTest setup seam.

---
*Phase: 10-settlement-faction-enforcement-validation*
*Completed: 2026-04-11*
