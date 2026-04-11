---
phase: 11-large-battle-ai-profiling-baseline
plan: 02
subsystem: runtime
tags: [runtime, performance, profiling, ai, pathfinding, gametest, bannermod]
requires:
  - phase: 11-large-battle-ai-profiling-baseline
    plan: 01
    provides: canonical large-battle profiling contract and evidence format
provides:
  - resettable runtime counters for recruit target-search activity
  - resettable runtime counters for async path-processing activity
  - dense-battle GameTest snapshots that log baseline profiling evidence during live runs
affects: [recruits-ai, pathfinding, gametest-battle-harness, planning]
tech-stack:
  added: []
  patterns: [observation-only profiling via tiny static snapshots and reuse of existing stress-battle GameTests]
key-files:
  created:
    - .planning/phases/11-large-battle-ai-profiling-baseline/11-large-battle-ai-profiling-baseline-02-SUMMARY.md
  modified:
    - recruits/src/main/java/com/talhanation/recruits/pathfinding/AsyncPathProcessor.java
    - recruits/src/main/java/com/talhanation/recruits/entities/AbstractRecruitEntity.java
    - recruits/src/gametest/java/com/talhanation/recruits/gametest/battle/BattleStressGameTests.java
    - recruits/src/gametest/java/com/talhanation/recruits/gametest/battle/BattleStressFixtures.java
    - recruits/src/gametest/java/com/talhanation/recruits/gametest/support/RecruitsBattleGameTestSupport.java
    - .planning/phases/11-large-battle-ai-profiling-baseline/11-PROFILING-BASELINE.md
    - .planning/phases/11-large-battle-ai-profiling-baseline/11-VALIDATION.md
key-decisions:
  - "Keep profiling state as tiny resettable static snapshot APIs instead of adding a persistent profiling manager."
  - "Capture snapshots inside the existing dense-battle GameTests at progress and resolution checkpoints instead of introducing a separate benchmark framework."
  - "Treat external profilers as still required for tick-time and latency metrics, while built-in counters cover target-search and async path-processing volume."
patterns-established:
  - "Later performance phases can reset, rerun, and compare the same dense-battle scenarios using one built-in counter vocabulary plus external profiler artifacts."
requirements-completed: []
duration: pending
completed: 2026-04-11
---

# Phase 11 Plan 02: Large-Battle AI Profiling Baseline Summary

**BannerMod now exposes resettable profiling counters from the live recruit runtime and logs repeatable baseline snapshots from the existing dense-battle GameTests without changing battle behavior.**

## Accomplishments

- Added target-search profiling snapshots to `AbstractRecruitEntity` covering total searches, async-vs-sync path, observed candidate volume, and target assignments.
- Added async path-processing profiling snapshots to `AsyncPathProcessor` covering queue submissions, synchronous fallbacks, await calls, and delivered paths.
- Extended `BattleStressGameTests` and `RecruitsBattleGameTestSupport` so baseline and heavy dense battles reset counters, capture profiling snapshots at deterministic checkpoints, assert coarse non-zero activity, and log stable snapshot lines for evidence collection.
- Updated Phase 11 baseline and validation docs so the documented counter names and capture procedure match the implemented runtime and GameTest seams.

## Files Created/Modified

- `recruits/src/main/java/com/talhanation/recruits/pathfinding/AsyncPathProcessor.java` - Added resettable async path-processing snapshot counters.
- `recruits/src/main/java/com/talhanation/recruits/entities/AbstractRecruitEntity.java` - Added resettable target-search snapshot counters on the real recruit search path.
- `recruits/src/gametest/java/com/talhanation/recruits/gametest/battle/BattleStressGameTests.java` - Added counter reset, snapshot capture, coarse profiling invariants, and stable log output.
- `recruits/src/gametest/java/com/talhanation/recruits/gametest/support/RecruitsBattleGameTestSupport.java` - Added snapshot capture and formatting helpers for stress-battle evidence.
- `.planning/phases/11-large-battle-ai-profiling-baseline/11-PROFILING-BASELINE.md` - Updated to reflect the implemented counter names and harness capture path.
- `.planning/phases/11-large-battle-ai-profiling-baseline/11-VALIDATION.md` - Updated to describe the built-in snapshot fields and actual capture workflow.

## Decisions Made

- Kept profiling additive and observation-only.
- Reused the existing stress harness instead of adding a benchmark subsystem.
- Left latency and server-tick metrics to external profiler tooling.

## Deviations From Plan

- Full `verifyGameTestStage` remains blocked by an unrelated existing failure in `validleaderpacketsonlymutatetargetedleader`; the new Phase 11 battle profiling snapshots still executed successfully inside that run.

## Issues Encountered

- A plain JUnit smoke test against `AbstractRecruitEntity` triggered Minecraft static initialization and was removed as unsuitable for this environment.

## Next Phase Readiness

- Phase 11 now has an executable baseline capture path for dense recruit battles, so later optimization phases can compare built-in target-search and async path-processing counters against the same scenario matrix.
- No transition/state update was performed because execution was requested with `--no-transition`.

---
*Phase: 11-large-battle-ai-profiling-baseline*
*Completed: 2026-04-11*
