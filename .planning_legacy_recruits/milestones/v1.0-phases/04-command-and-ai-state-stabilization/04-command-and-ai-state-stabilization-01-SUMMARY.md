---
phase: 04-command-and-ai-state-stabilization
plan: 01
subsystem: testing
tags: [commands, ai, jvm-tests, validation, networking]
requires:
  - phase: 02-layered-test-harness-foundations
    provides: JVM test harness and fixture patterns for pure logic seams
  - phase: 03-battle-and-formation-regression-lockdown
    provides: preserved nearby-owned-recruits radius contract and seam-extraction pattern
provides:
  - Pure command-target filtering seam for later Phase 4 packet rewiring
  - JVM regression coverage for ownership, radius, group, and invalid-input handling
affects: [04-02-PLAN, 04-03-PLAN, command-packets]
tech-stack:
  added: []
  patterns: [value-only selection records, failure-enum diagnostics, fixture-backed JVM targeting tests]
key-files:
  created:
    - src/main/java/com/talhanation/recruits/network/CommandTargeting.java
    - src/test/java/com/talhanation/recruits/network/CommandTargetingTest.java
    - src/test/java/com/talhanation/recruits/testsupport/CommandTargetingFixtures.java
  modified:
    - .gitignore
    - src/main/java/com/talhanation/recruits/network/CommandTargeting.java
key-decisions:
  - "Keep command targeting pure by expressing packet inputs as RecruitSnapshot records and explicit selection result records."
  - "Preserve the legacy 100-block command radius as a named constant and surface invalid flows through Failure enums instead of silent behavior."
patterns-established:
  - "Phase 4 command validation should live in reusable pure helpers before packet handlers mutate entities."
  - "Command tests should use deterministic snapshot fixtures instead of Minecraft runtime mocks."
requirements-completed: [CMD-01, CMD-03, CMD-04]
duration: 19min
completed: 2026-04-07
---

# Phase 4 Plan 1: Command Targeting Seam Summary

**Pure recruit command-target selection with explicit failure diagnostics and JVM regression tests for ownership, group, and 100-block radius rules.**

## Performance

- **Duration:** 19 min
- **Started:** 2026-04-06T23:51:00Z
- **Completed:** 2026-04-07T00:10:12Z
- **Tasks:** 3
- **Files modified:** 4

## Accomplishments
- Added `CommandTargeting` as a reusable pure seam for group-command filtering and explicit single-recruit lookup.
- Locked down command authority expectations with JVM tests covering ownership, group selection, radius, and invalid single-target degradation.
- Prevented Gradle from leaving `.factorypath` as untracked noise during verification runs.

## Task Commits

Each task was committed atomically:

1. **Task 0: Write command-targeting contract** - `b072b0b6` (feat)
2. **Task 1: Lock down command targeting rules in JVM tests** - `54438687` (test)
3. **Task 2: Implement the shared targeting seam** - `59259477` (feat)

**Plan metadata:** recorded in the final docs commit for this plan.

_Note: TDD tasks may have multiple commits (test → feat → refactor)_

## Files Created/Modified
- `src/main/java/com/talhanation/recruits/network/CommandTargeting.java` - Pure helper with snapshot/result records, radius constant, and failure diagnostics.
- `src/test/java/com/talhanation/recruits/network/CommandTargetingTest.java` - JVM contract tests for group and single-recruit targeting behavior.
- `src/test/java/com/talhanation/recruits/testsupport/CommandTargetingFixtures.java` - Deterministic command-target snapshots reused by the new tests.
- `.gitignore` - Ignores generated `.factorypath` output produced by Gradle verification.

## Decisions Made
- Used value-only `RecruitSnapshot`, `GroupCommandSelection`, and `SingleRecruitSelection` records so later packet rewiring can stay Forge-free under JVM tests.
- Returned explicit `Failure` values for invalid sender, missing recruit, ownership mismatch, and radius failures so later packet handlers can no-op safely without guessing why targeting failed.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] Ignored generated `.factorypath` output from Gradle runs**
- **Found during:** Task 2 (Implement the shared targeting seam)
- **Issue:** Verification runs generated an untracked `.factorypath`, which would leave the repository dirty after task execution.
- **Fix:** Added `.factorypath` to `.gitignore`.
- **Files modified:** `.gitignore`
- **Verification:** `git status --short` no longer reports `.factorypath` after verification.
- **Committed in:** `59259477` (part of task commit)

---

**Total deviations:** 1 auto-fixed (1 blocking)
**Impact on plan:** The deviation was limited to generated-file hygiene and did not change command-targeting scope.

## Issues Encountered
- Task 0's prescribed verification target could not succeed before Task 1 created `CommandTargetingTest`; the helper contract was still committed first, and the required Gradle test command passed once the TDD tasks completed.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness
- Phase 4 packet handlers now have a single authority/selection seam to consume when rewiring movement, formation, attack, and shields packets.
- Future command plans can translate live recruits into `RecruitSnapshot` values without redefining ownership or radius rules.

## Self-Check: PASSED

- Verified summary and command-targeting files exist on disk.
- Verified task commits `b072b0b6`, `54438687`, and `59259477` exist in git history.

---
*Phase: 04-command-and-ai-state-stabilization*
*Completed: 2026-04-07*
