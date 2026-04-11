---
phase: 06-player-cycle-gametest-validation
plan: 02
subsystem: testing
tags: [gametest, settlement-labor, authority, workers, bannermod]
requires:
  - phase: 06-player-cycle-gametest-validation
    provides: shared GameTest support helpers and dedicated ownership slice baseline
provides:
  - dedicated settlement labor GameTest coverage
  - explicit owner-versus-outsider worker recovery authority regression coverage
  - worker recovery authority fix aligned with shared BannerMod rules
affects: [06-04, authority, worker-control]
tech-stack:
  added: []
  patterns: [distinct fake outsider identities for authority-bound GameTests]
key-files:
  created:
    - src/gametest/java/com/talhanation/bannermod/BannerModSettlementLaborGameTests.java
  modified:
    - workers/src/main/java/com/talhanation/workers/entities/AbstractWorkerEntity.java
key-decisions:
  - "Model the outsider requester with a distinct fake player identity so GameTest authority assertions reflect real runtime ownership boundaries."
  - "Enforce recovery authority inside AbstractWorkerEntity.recoverControl itself so direct calls match the shared BannerMod authority contract."
patterns-established:
  - "Settlement labor tests pin work-area authorization separately from owner-only recovery control."
requirements-completed: []
duration: 5min
completed: 2026-04-11
---

# Phase 06 Plan 02: Settlement Labor Summary

**A dedicated settlement labor GameTest now proves owned worker participation in owned infrastructure while pinning outsider recovery denial through the shared authority seam.**

## Performance

- **Duration:** 5 min
- **Started:** 2026-04-11T10:00:33Z
- **Completed:** 2026-04-11T10:05:38Z
- **Tasks:** 1
- **Files modified:** 2

## Accomplishments
- Added `BannerModSettlementLaborGameTests` to validate owned crop-area participation and same-owner storage ownership in one root GameTest.
- Proved outsider recovery denial preserves the claimed crop area and current work-area binding.
- Fixed direct worker recovery authority so runtime behavior matches the shared BannerMod owner-or-admin control rule.

## Task Commits

1. **Task 1: Validate owned worker settlement labor participation in dedicated root GameTests** - `53ecb75` (feat)
2. **Task 1 fix: use a distinct outsider identity in the settlement test** - `eb0fdc7` (fix)
3. **Task 1 fix: enforce recovery authority in the workers runtime** - `workers@2698c82` (fix)

**Plan metadata:** pending

## Files Created/Modified
- `src/gametest/java/com/talhanation/bannermod/BannerModSettlementLaborGameTests.java` - Settlement labor root GameTest covering authorization and outsider recovery denial.
- `workers/src/main/java/com/talhanation/workers/entities/AbstractWorkerEntity.java` - Recovery authority now checks owner/admin relationship directly instead of relying on owner entity lookup.

## Decisions Made
- Used a Forge `FakePlayerFactory` outsider with a random UUID so the authority boundary is explicit and deterministic in GameTests.
- Hardened `recoverControl(...)` itself because the new GameTest exposed a real runtime bug when direct recovery calls occurred without a resolved owner player entity.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 1 - Bug] Fixed direct worker recovery bypass when owner player lookup was absent**
- **Found during:** Task 1 (settlement labor GameTest verification)
- **Issue:** `AbstractWorkerEntity.recoverControl(...)` only rejected outsiders when `getOwner()` resolved a live player, so direct calls could bypass the owner check.
- **Fix:** Aligned `recoverControl(...)` with `BannerModAuthorityRules` using owner/admin relationship checks based on UUID and permissions.
- **Files modified:** `workers/src/main/java/com/talhanation/workers/entities/AbstractWorkerEntity.java`
- **Verification:** `./gradlew verifyGameTestStage` passed with 26 required tests after the fix.
- **Committed in:** `workers@2698c82`

---

**Total deviations:** 1 auto-fixed (1 bug)
**Impact on plan:** Necessary correctness fix discovered by the planned GameTest. No scope creep.

## Issues Encountered

- The initial outsider requester used the same mock-player mechanism as the owner path, so the test was tightened to use a distinct fake-player identity before diagnosing the actual runtime bug.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness

- Settlement labor now has its own executable root artifact.
- The stitched full-cycle plan can rely on the hardened recovery boundary without duplicating authority debugging.

## Self-Check

PASSED

---
*Phase: 06-player-cycle-gametest-validation*
*Completed: 2026-04-11*
