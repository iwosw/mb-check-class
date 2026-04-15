---
phase: 23-settlement-governance-and-governor-control
plan: 03
subsystem: gameplay
tags: [governance, heartbeat, settlement, claims, junit]
requires:
  - phase: 23-01
    provides: claim-keyed governor snapshot persistence and legality rules
  - phase: 23-02
    provides: authority-safe governor assignment service
provides:
  - bounded governor incident and recommendation vocabularies with stable tokens
  - heartbeat-driven local tax and advisory recomputation for governed claims
  - live claim-loop wiring for coarse server-side governor updates
affects: [23-04 governor ui, 23-05 gametest validation, GOV-03]
tech-stack:
  added: []
  patterns: [stable enum-backed report tokens, coarse server tick heartbeat updates]
key-files:
  created: []
  modified:
    - src/main/java/com/talhanation/bannermod/governance/BannerModGovernorIncident.java
    - src/main/java/com/talhanation/bannermod/governance/BannerModGovernorRecommendation.java
    - src/main/java/com/talhanation/bannermod/governance/BannerModGovernorHeartbeat.java
    - src/main/java/com/talhanation/bannermod/events/ClaimEvents.java
    - src/test/java/com/talhanation/bannermod/governance/BannerModGovernorHeartbeatTest.java
key-decisions:
  - "Keep governor report payloads token-backed via enums so later snapshots and UI use one bounded vocabulary instead of free-form strings."
  - "Run claim heartbeat work only on server tick END phase with an existing coarse cadence to avoid double-firing and scope creep into per-tick simulation."
patterns-established:
  - "Governor reporting pattern: recompute local claim state on heartbeat and persist compact incident/recommendation tokens."
  - "Claim-loop extension pattern: add one bounded service seam from ClaimEvents instead of mutating unrelated economy systems."
requirements-completed: [GOV-03]
duration: 7 min
completed: 2026-04-15
---

# Phase 23 Plan 03: Governor Heartbeat Summary

**Heartbeat-driven governor reports now publish stable local tax, incident, and recommendation tokens from governed claims without touching treasury or logistics systems.**

## Performance

- **Duration:** 7 min
- **Started:** 2026-04-15T16:00:00Z
- **Completed:** 2026-04-15T16:06:41Z
- **Tasks:** 2
- **Files modified:** 5

## Accomplishments
- Pinned the governor heartbeat contract with focused JUnit coverage for tax, instability, and shortage-driven advisory output.
- Converted governor incidents and recommendations into explicit stable-token vocabularies for snapshot/UI reuse.
- Wired the live claim loop through one bounded heartbeat seam and fixed the server tick hook to run only once per tick end-phase.

## Task Commits

Each task was committed atomically:

1. **Task 1: Define and test the bounded governor heartbeat outputs** - `bf284ed` (test), `7ce8cfc` (feat)
2. **Task 2: Wire governor heartbeat updates into the live server claim loop** - `13654ab` (feat)

**Plan metadata:** pending docs commit

_Note: Task 1 used TDD-style test then implementation commits._

## Files Created/Modified
- `src/test/java/com/talhanation/bannermod/governance/BannerModGovernorHeartbeatTest.java` - pins friendly tax accrual plus explicit advisory token output.
- `src/main/java/com/talhanation/bannermod/governance/BannerModGovernorIncident.java` - defines bounded governor incident tokens.
- `src/main/java/com/talhanation/bannermod/governance/BannerModGovernorRecommendation.java` - defines bounded governor recommendation tokens.
- `src/main/java/com/talhanation/bannermod/governance/BannerModGovernorHeartbeat.java` - maps heartbeat reports through stable tokens and distinguishes unclaimed settlements.
- `src/main/java/com/talhanation/bannermod/events/ClaimEvents.java` - invokes the heartbeat on server tick END phase with coarse cadence guards.

## Decisions Made
- Use enum-owned token strings for governor incidents and recommendations so persistence and later UI consumers share one compact vocabulary.
- Keep heartbeat computation claim-local and cadence-driven; do not widen into treasury mutation, logistics route evaluation, or per-tick scans.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 1 - Bug] Prevented the heartbeat tick from running twice per server tick**
- **Found during:** Task 2 (Wire governor heartbeat updates into the live server claim loop)
- **Issue:** `ClaimEvents.onServerTick` did not gate on `TickEvent.Phase.END`, so the governor cadence counter advanced on both START and END phases.
- **Fix:** Added END-phase and null-manager guards before invoking the governor heartbeat.
- **Files modified:** `src/main/java/com/talhanation/bannermod/events/ClaimEvents.java`
- **Verification:** `./gradlew compileJava --console=plain`
- **Committed in:** `13654ab`

---

**Total deviations:** 1 auto-fixed (1 Rule 1 bug)
**Impact on plan:** Necessary correctness fix for the live heartbeat cadence; no scope creep.

## Issues Encountered
- `./gradlew test --tests com.talhanation.bannermod.governance.BannerModGovernorHeartbeatTest --console=plain` is still blocked by the pre-existing unrelated test-tree compile failures already recorded in `STATE.md` and `ROADMAP.md`. The plan's runtime wiring was verified with `./gradlew compileJava --console=plain` while leaving those out-of-scope failures untouched.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness
- Governor snapshots now carry bounded heartbeat output that the governor UI and later validation plans can consume directly.
- The remaining blocker for clean targeted JUnit execution is still the unrelated legacy/root test-tree compile debt outside governance scope.

## Self-Check: PASSED
- Found summary path `.planning/phases/23-settlement-governance-and-governor-control/23-settlement-governance-and-governor-control-03-SUMMARY.md`
- Found commits `bf284ed`, `7ce8cfc`, and `13654ab`
- Found modified runtime files listed above

---
*Phase: 23-settlement-governance-and-governor-control*
*Completed: 2026-04-15*
