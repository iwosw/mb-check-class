---
phase: 31-1-2-mining-area-branch-mine-3
plan: 03
subsystem: workers-mining
tags: [workers, mining, ui, networking, tdd]
requires:
  - phase: 29-1-3-3-2-branch-mining-strip-mining-3-21-26
    provides: explicit tunnel/branch mining settings across UI, packet, and persisted area state
provides:
  - fixed internal pattern segment budget for tunnel and branch mining
  - mining update packet without legacy z-size authoring
  - mining screen with tunnel/branch settings only
affects: [workers-mining, workers-ui, workers-networking]
tech-stack:
  added: [MiningPatternContract]
  patterns: [internal mining pattern budget, tunnel-branch-only authoring contract, nested-repo verification via isolated worktree]
key-files:
  created:
    - .planning/phases/31-1-2-mining-area-branch-mine-3/31-1-2-mining-area-branch-mine-3-03-SUMMARY.md
    - workers/src/main/java/com/talhanation/workers/entities/workarea/MiningPatternContract.java
  modified:
    - workers/src/main/java/com/talhanation/workers/entities/workarea/MiningArea.java
    - workers/src/main/java/com/talhanation/workers/network/MessageUpdateMiningArea.java
    - workers/src/main/java/com/talhanation/workers/client/gui/MiningAreaScreen.java
    - src/test/java/com/talhanation/workers/MiningAreaPatternContractTest.java
    - src/test/java/com/talhanation/workers/MessageUpdateMiningAreaCodecTest.java
key-decisions:
  - "Tunnel and branch mining now use one fixed internal segment budget instead of reusing legacy depth authoring."
  - "The miner update packet and screen no longer expose z-size; only tunnel/branch settings remain player-authored."
patterns-established:
  - "Contract Pattern: keep miner-facing authoring separate from legacy work-area depth storage."
  - "Verification Pattern: use an isolated root worktree with symlinked nested sources when root and nested repos must be validated together."
requirements-completed: [MINERCFG-01]
duration: 14 min
completed: 2026-04-12
---

# Phase 31 Plan 03: Miner Authoring Contract Cleanup Summary

**Miner tunnel and branch authoring now use a fixed internal segment budget, a z-size-free packet contract, and a screen that only exposes tunnel/branch controls.**

## Performance

- **Duration:** 14 min
- **Started:** 2026-04-12T12:28:47Z
- **Completed:** 2026-04-12T12:43:00Z
- **Tasks:** 2
- **Files modified:** 5

## Accomplishments
- Added explicit contract coverage for bounded tunnel/branch segment progression without legacy depth authoring.
- Introduced `MiningPatternContract` so runtime pattern budgeting and authored-setting projection stay separate from entity bootstrap concerns.
- Removed z-size from the miner packet and screen so player-facing mining authoring is limited to tunnel/branch settings.

## Task Commits

Each task was committed atomically:

1. **Task 1: Decouple pattern length from legacy miner box depth** - `0f36c69` (test, root RED), `4621c41` (feat, root GREEN test refinement), `43fe5e5` (feat, workers GREEN)
2. **Task 2: Remove legacy x/y/z box editing from the miner packet and screen** - `6296a18` (test, root RED), `291c033` (feat, workers GREEN)

**Plan metadata:** pending

## Files Created/Modified
- `workers/src/main/java/com/talhanation/workers/entities/workarea/MiningPatternContract.java` - Pure helper for bounded pattern segments and authored-setting projection.
- `workers/src/main/java/com/talhanation/workers/entities/workarea/MiningArea.java` - Delegates tunnel/branch segment counts to the internal contract and preserves legacy depth.
- `workers/src/main/java/com/talhanation/workers/network/MessageUpdateMiningArea.java` - Drops z-size from codec and server update behavior.
- `workers/src/main/java/com/talhanation/workers/client/gui/MiningAreaScreen.java` - Removes legacy box-depth controls and relabels remaining mining geometry inputs.
- `src/test/java/com/talhanation/workers/MiningAreaPatternContractTest.java` - Covers internal segment budgeting and depth-preserving pattern projection.
- `src/test/java/com/talhanation/workers/MessageUpdateMiningAreaCodecTest.java` - Verifies the packet no longer includes `zSize`.

## Decisions Made
- Used a small pure helper seam (`MiningPatternContract`) so mining contract tests stay executable without requiring Forge entity bootstrap.
- Kept legacy `depthSize` as stored entity state for shared work-area compatibility while removing it from miner-facing authoring.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] Added a pure mining contract helper for testable brownfield verification**
- **Found during:** Task 1
- **Issue:** Direct `MiningArea` entity bootstrap is not unit-test friendly in the root JUnit environment because class initialization depends on Forge entity bootstrap state.
- **Fix:** Added `MiningPatternContract` and routed `MiningArea` through it so the bounded segment and pattern-application contract can be tested without bootstrapping a live entity.
- **Files modified:** `workers/src/main/java/com/talhanation/workers/entities/workarea/MiningArea.java`, `workers/src/main/java/com/talhanation/workers/entities/workarea/MiningPatternContract.java`, `src/test/java/com/talhanation/workers/MiningAreaPatternContractTest.java`
- **Verification:** `"/home/kaiserroman/bannermod/recruits/gradlew" -p "/tmp/bannermod-31-03-red" test --tests com.talhanation.workers.MiningAreaPatternContractTest --tests com.talhanation.workers.MessageUpdateMiningAreaCodecTest --console=plain`
- **Committed in:** `43fe5e5` and `4621c41`

---

**Total deviations:** 1 auto-fixed (1 blocking)
**Impact on plan:** The helper kept the plan's behavior intact while making the contract verifiable across the split root/workers repo layout.

## Issues Encountered
- Root plan execution targets files in the nested `workers/` repo, so task implementation required separate workers-repo commits plus isolated root-worktree verification to keep root tests and nested sources aligned.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness
- Plan 31-04 can now add hostile-claim excavation guardrails on top of a cleaned-up miner authoring contract.
- Miner authoring no longer leaks the legacy box-depth concept through the packet or UI seam.

---
*Phase: 31-1-2-mining-area-branch-mine-3*
*Completed: 2026-04-12*

## Self-Check: PASSED

- FOUND: `.planning/phases/31-1-2-mining-area-branch-mine-3/31-1-2-mining-area-branch-mine-3-03-SUMMARY.md`
- FOUND: root `0f36c69`
- FOUND: root `4621c41`
- FOUND: root `6296a18`
- FOUND: workers `43fe5e5`
- FOUND: workers `291c033`
