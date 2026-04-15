---
phase: 23-settlement-governance-and-governor-control
plan: 01
subsystem: persistence
tags: [governance, settlement, claim, saveddata, junit]
requires:
  - phase: 09-settlement-faction-binding-contract
    provides: claim-derived settlement legality statuses and binding rules
  - phase: 22-citizen-role-unification
    provides: stable recruit/citizen identity seams for governor designation
provides:
  - claim-keyed governor snapshot contract for persisted governance state
  - pure governor allow-deny rules for assignment and control legality
  - narrow SavedData governor manager keyed by claim UUID
  - focused JUnit coverage for friendly, hostile, unclaimed, degraded, and persistence round-trip cases
affects: [23-02, 23-03, 23-04, 23-05, governor-service, governor-heartbeat, governor-ui]
tech-stack:
  added: []
  patterns: [claim-keyed SavedData persistence, pure governance rule seam, compact tokenized report fields]
key-files:
  created:
    - src/main/java/com/talhanation/bannermod/governance/BannerModGovernorSnapshot.java
    - src/main/java/com/talhanation/bannermod/governance/BannerModGovernorRules.java
    - src/main/java/com/talhanation/bannermod/governance/BannerModGovernorManager.java
    - src/test/java/com/talhanation/bannermod/governance/BannerModGovernorRulesTest.java
  modified: []
key-decisions:
  - "Persist governor state by claim UUID in one narrow SavedData manager instead of mutating claims or introducing a settlement manager."
  - "Keep legality in one pure rules helper driven by claim-derived settlement binding states before any runtime service or UI wiring."
patterns-established:
  - "Claim-derived governance: settlement identity stays anchored to claim UUID plus anchor chunk."
  - "Tokenized reporting: incidents and recommendations persist as compact string token lists for downstream heartbeat and UI slices."
requirements-completed: [GOV-01]
duration: 2 min
completed: 2026-04-15
---

# Phase 23 Plan 01: Settlement Governance And Governor Control Summary

**Claim-keyed governor snapshots, pure settlement legality rules, and SavedData persistence for downstream governor assignment and reporting**

## Performance

- **Duration:** 2 min
- **Started:** 2026-04-15T15:56:33Z
- **Completed:** 2026-04-15T15:58:03Z
- **Tasks:** 2
- **Files modified:** 4

## Accomplishments
- Added `BannerModGovernorSnapshot` as the persisted governance contract keyed by claim UUID and anchor chunk.
- Added `BannerModGovernorRules` so downstream slices can ask one pure allow/deny seam before mutating runtime state.
- Added `BannerModGovernorManager` plus focused JUnit coverage for friendly allow, hostile/unclaimed/degraded denial, and snapshot round-tripping.

## Task Commits

Each task was committed atomically:

1. **Task 1: Define the claim-keyed governor snapshot and pure rules contract** - `3c336d9` (feat)
2. **Task 2: Implement persistence access and rule coverage for governance foundations** - `0663ed8` (test), `298d667` (feat)

**Plan metadata:** `PENDING`

_Note: Task 2 used TDD-style red/green commits._

## Files Created/Modified
- `src/main/java/com/talhanation/bannermod/governance/BannerModGovernorSnapshot.java` - Immutable persisted snapshot for claim-keyed governor state and heartbeat report fields.
- `src/main/java/com/talhanation/bannermod/governance/BannerModGovernorRules.java` - Pure assignment/control legality seam over settlement binding statuses.
- `src/main/java/com/talhanation/bannermod/governance/BannerModGovernorManager.java` - Narrow SavedData manager that stores snapshots by claim UUID.
- `src/test/java/com/talhanation/bannermod/governance/BannerModGovernorRulesTest.java` - Rule and persistence boundary coverage for friendly and degraded cases.

## Decisions Made
- Persist governance separately from `RecruitsClaim` so settlement identity remains claim-derived instead of forking a second settlement world model.
- Store incidents and recommendations as compact string token lists so later heartbeat and UI plans can reuse one stable persisted seam.

## Deviations from Plan

None - plan code was already executed exactly as written in the existing task commits.

## Issues Encountered
- The plan's targeted Gradle verification command (`./gradlew test --tests com.talhanation.bannermod.governance.BannerModGovernorRulesTest --console=plain`) is currently blocked by 39 pre-existing `compileTestJava` failures in unrelated test files outside the governance package. Per execution scope rules, those unrelated failures were not fixed in this plan.

## Deferred Issues
- Unrelated root test compilation failures currently block targeted Gradle test execution, including missing legacy worker smoke-test symbols and shared-vs-legacy settlement-binding type mismatches in other test packages.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness
- Phase 23-02 can build runtime designation and revocation on top of `BannerModGovernorManager` and `BannerModGovernorRules` without rediscovering persistence or legality seams.
- Phase 23-03 can reuse the snapshot token fields for heartbeat-driven incidents, recommendations, and local tax summaries.

## Self-Check: PASSED
- Found summary file: `.planning/phases/23-settlement-governance-and-governor-control/23-settlement-governance-and-governor-control-01-SUMMARY.md`
- Found task commit: `3c336d9`
- Found task commit: `0663ed8`
- Found task commit: `298d667`
