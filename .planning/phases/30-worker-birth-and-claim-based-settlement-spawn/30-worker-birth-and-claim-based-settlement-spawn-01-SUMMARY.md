---
phase: 30-worker-birth-and-claim-based-settlement-spawn
plan: 01
subsystem: testing
tags: [workers, settlement, claims, config, junit]
requires:
  - phase: 09-settlement-faction-binding-contract
    provides: settlement binding status vocabulary
provides:
  - pure claim-aware worker birth and settlement spawn decisions
  - explicit Phase 30 config views for birth and spawn rules
affects: [30-02, 30-03, workers]
tech-stack:
  added: []
  patterns: [pure rule seam, config-backed immutable rule views]
key-files:
  created: [workers/src/main/java/com/talhanation/workers/settlement/WorkerSettlementSpawnRules.java, src/test/java/com/talhanation/workers/WorkerSettlementSpawnRulesTest.java]
  modified: [workers/src/main/java/com/talhanation/workers/config/WorkersServerConfig.java]
key-decisions:
  - "Deterministic profession selection rotates by current worker count instead of runtime randomness."
  - "WorkersServerConfig exposes birth and settlement-spawn rule views so runtime hooks stay testable."
patterns-established:
  - "Phase 30 decisions flow through WorkerSettlementSpawnRules before any world mutation."
requirements-completed: [WBSP-01]
duration: 50min
completed: 2026-04-12
---

# Phase 30 Plan 01: Worker Birth And Claim-Based Settlement Spawn Summary

**Claim-aware worker birth and settlement spawn rules with config-backed defaults and focused JUnit coverage**

## Performance

- **Duration:** 50 min
- **Started:** 2026-04-12T11:35:29Z
- **Completed:** 2026-04-12T12:25:00Z
- **Tasks:** 2
- **Files modified:** 3

## Accomplishments
- Added one pure `WorkerSettlementSpawnRules` seam for birth and settlement-spawn allow/deny decisions.
- Added explicit Phase 30 birth/spawn toggles, caps, cooldown days, and profession pool config inputs.
- Covered friendly allow, hostile/unclaimed denial, quota/cooldown denial, and config-backed rule defaults in JUnit.

## Task Commits

1. **Task 1: Create the pure worker settlement spawn rules seam** - `fbf3caa` (test), `26c3f7e` (feat)
2. **Task 2: Add explicit Phase 30 settlement spawn config inputs** - `6688669` (test), `eee0b24` (feat)

## Files Created/Modified
- `workers/src/main/java/com/talhanation/workers/settlement/WorkerSettlementSpawnRules.java` - Pure claim-aware eligibility and profession selection logic.
- `workers/src/main/java/com/talhanation/workers/config/WorkersServerConfig.java` - Phase 30 birth/spawn toggles and immutable rule views.
- `src/test/java/com/talhanation/workers/WorkerSettlementSpawnRulesTest.java` - Focused JUnit coverage for rules and config defaults.

## Decisions Made
- Deterministic profession selection uses the current worker count modulo the allowed pool.
- Config access falls back to defaults when Forge values are not yet attached, keeping tests fast and stable.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 2 - Missing Critical] Added safe config fallbacks for unit tests**
- **Found during:** Task 2
- **Issue:** Forge config values threw `IllegalStateException` during JUnit execution before runtime attachment.
- **Fix:** Added fallback readers so Phase 30 config-derived rule views stay executable in tests and runtime bootstrap.
- **Files modified:** `workers/src/main/java/com/talhanation/workers/config/WorkersServerConfig.java`
- **Verification:** `./gradlew test --tests com.talhanation.workers.WorkerSettlementSpawnRulesTest --console=plain`
- **Committed in:** `eee0b24`

## Issues Encountered
- The active workspace uses a nested `workers/.git` repository, so worker-source commits were recorded there while JUnit coverage stayed in the root repo.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness
- `WorkerSettlementSpawnRules` and config views are ready for live server-event wiring.
- Phase 30 runtime hooks can now consume one deterministic rules contract instead of re-embedding claim logic.

## Self-Check: PASSED
