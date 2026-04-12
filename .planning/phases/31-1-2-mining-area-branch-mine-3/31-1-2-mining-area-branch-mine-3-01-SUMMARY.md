---
phase: 31-1-2-mining-area-branch-mine-3
plan: 01
subsystem: settlement
tags: [claims, workers, config, junit]
requires:
  - phase: 30-worker-birth-and-claim-based-settlement-spawn
    provides: pure worker settlement spawn seam and config-backed worker growth baseline
  - phase: 09-settlement-faction-binding-contract
    provides: claim-derived settlement status vocabulary
provides:
  - pure claim worker growth decisions keyed by settlement binding status
  - explicit Phase 31 claim growth config snapshot and profession pool settings
  - focused JUnit coverage for friendly allow, hostile denial, and diminishing cooldowns
affects: [31-02, claim-worker-growth, workers]
tech-stack:
  added: []
  patterns: [pure rules helper, config snapshot records, status-driven claim evaluation]
key-files:
  created: []
  modified:
    - workers/src/main/java/com/talhanation/workers/settlement/WorkerSettlementSpawnRules.java
    - workers/src/main/java/com/talhanation/workers/config/WorkersServerConfig.java
    - src/test/java/com/talhanation/workers/WorkerSettlementSpawnRulesTest.java
key-decisions:
  - "Claim worker growth evaluates BannerModSettlementBinding.Status directly instead of pulling runtime world state into the rules seam."
  - "Claim growth config is exposed as an immutable ClaimGrowthConfig snapshot carrying toggle, cooldown, cap, and profession pool inputs."
patterns-established:
  - "Pure claim growth rules return both allow/deny and required cooldown ticks so runtime hooks can stay thin."
  - "Phase-specific worker growth knobs live in WorkersServerConfig instead of piggybacking on unrelated worker toggles."
requirements-completed: [CLAIMGROW-01, CLAIMGROW-02]
duration: 3 min
completed: 2026-04-12
---

# Phase 31 Plan 01: Claim Worker Growth Rules Summary

**Claim-backed worker growth now uses one pure status-driven rules seam with diminishing cooldown math and explicit Phase 31 config inputs.**

## Performance

- **Duration:** 3 min
- **Started:** 2026-04-12T12:31:08Z
- **Completed:** 2026-04-12T12:34:17Z
- **Tasks:** 2
- **Files modified:** 3

## Accomplishments
- Added a pure `evaluateClaimWorkerGrowth(...)` path that accepts claim status, worker count, elapsed cooldown, and deterministic profession inputs.
- Extended `Decision` results to publish required cooldown ticks so runtime hooks can reuse the same diminishing cadence calculation.
- Added explicit claim growth config knobs and focused tests for friendly allow, hostile/degraded/unclaimed denial, disabled toggles, and cap enforcement.

## Task Commits

Each task was committed atomically:

1. **Task 1: Extend the worker growth rules seam for claim-as-settlement decisions** - `39dab57` (test), `3439d1d` (feat)
2. **Task 2: Add explicit Phase 31 worker-growth config inputs** - `72d700e` (test), `1d88acf` (feat)

**Plan metadata:** pending final docs commit

## Files Created/Modified
- `workers/src/main/java/com/talhanation/workers/settlement/WorkerSettlementSpawnRules.java` - adds pure claim worker growth evaluation and cooldown reporting.
- `workers/src/main/java/com/talhanation/workers/config/WorkersServerConfig.java` - adds Phase 31 claim growth toggle, cooldown, cap, profession pool, and config snapshot helper.
- `src/test/java/com/talhanation/workers/WorkerSettlementSpawnRulesTest.java` - verifies claim growth allow/deny states, cooldown scaling, and config-backed denial cases.

## Decisions Made
- Used `BannerModSettlementBinding.Status` as the only settlement input for claim growth so the rules seam stays world-agnostic and testable.
- Kept Phase 31 settings separate from Phase 30 birth/spawn settings by introducing a dedicated claim growth config snapshot.

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered
- The merged workspace contains a nested `workers/.git` repository, so task commits were split between the root repo (tests) and the workers repo (implementation/config) while preserving atomic task boundaries.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness
- Plan 31-02 can now consume one deterministic claim growth seam and config snapshot when wiring live runtime hooks.
- Hostile, degraded, and unclaimed denial behavior is already covered before runtime spawning is introduced.

## Self-Check: PASSED
