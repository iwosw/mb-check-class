---
phase: 23-settlement-governance-and-governor-control
plan: 04
subsystem: ui
tags: [governor, governance, gui, packets, forge]
requires:
  - phase: 23-02
    provides: governor designation authority and snapshot-backed runtime service
  - phase: 23-03
    provides: heartbeat-driven governor reports and bounded recommendation tokens
provides:
  - governor promotion now routes through the live designation service from the existing promote flow
  - dedicated governor control screen with live settlement report sync
  - bounded governor policy toggle persistence and server-backed packet updates
affects: [phase-23-validation, governor-ui, later-economy-phases]
tech-stack:
  added: []
  patterns: [packet-driven governor screen refresh, snapshot-backed bounded policy toggles]
key-files:
  created: [src/main/java/com/talhanation/bannermod/governance/BannerModGovernorPolicy.java, src/main/java/com/talhanation/bannermod/network/messages/military/MessageUpdateGovernorPolicy.java]
  modified: [src/main/java/com/talhanation/bannermod/client/military/gui/GovernorScreen.java, src/main/java/com/talhanation/bannermod/events/RecruitEvents.java, src/main/java/com/talhanation/bannermod/governance/BannerModGovernorSnapshot.java, src/main/java/com/talhanation/bannermod/governance/BannerModGovernorService.java, src/main/java/com/talhanation/bannermod/network/messages/military/MessageToClientUpdateGovernorScreen.java, src/main/java/com/talhanation/bannermod/client/military/gui/PromoteScreen.java, src/main/java/com/talhanation/bannermod/network/messages/military/MessagePromoteRecruit.java]
key-decisions:
  - "Persist the three governor policy toggles on BannerModGovernorSnapshot so UI changes stay bounded and server authoritative."
  - "Keep governor promotion on MessagePromoteRecruit and branch profession id 6 into designation instead of adding a second entrypoint."
patterns-established:
  - "Governor UI refreshes through a dedicated client update packet sourced from RecruitEvents.syncGovernorScreen."
  - "Governor controls mutate claim-keyed snapshot state through service methods rather than direct GUI-side state."
requirements-completed: [GOV-02, GOV-03]
duration: 8 min
completed: 2026-04-15
---

# Phase 23 Plan 04: Governor Control Surface Summary

**Live governor designation plus a packet-synced control screen with bounded garrison, fortification, and tax policy toggles.**

## Performance

- **Duration:** 8 min
- **Started:** 2026-04-15T16:10:16Z
- **Completed:** 2026-04-15T16:18:36Z
- **Tasks:** 2
- **Files modified:** 11

## Accomplishments
- Activated the governor promotion slot through the existing recruit promotion workflow with server-side eligibility guards.
- Expanded the governor report packet and screen to show live settlement state, tax totals, incidents, garrison guidance, and fortification advice.
- Added server-backed bounded policy toggles for `garrison priority`, `fortification priority`, and `tax pressure` with persisted snapshot state.

## Task Commits

Each task was committed atomically:

1. **Task 1: Turn the dormant governor promotion slot into a real designation action** - `38a0f01` (feat)
2. **Task 2: Add a dedicated governor control screen and report sync path** - `f34aeec` (feat)

**Plan metadata:** pending

## Files Created/Modified
- `src/main/java/com/talhanation/bannermod/client/military/gui/PromoteScreen.java` - keeps governor promotion in the existing recruit promotion UI and gates it to eligible owned recruits.
- `src/main/java/com/talhanation/bannermod/network/messages/military/MessagePromoteRecruit.java` - preserves the existing promotion packet while hardening server handling.
- `src/main/java/com/talhanation/bannermod/events/RecruitEvents.java` - wires designation, governor screen sync, and bounded policy updates through the live server flow.
- `src/main/java/com/talhanation/bannermod/client/military/gui/GovernorScreen.java` - renders live governor report fields and client controls for the three bounded policies.
- `src/main/java/com/talhanation/bannermod/governance/BannerModGovernorSnapshot.java` - persists bounded governor policy values beside claim-keyed governor report state.
- `src/main/java/com/talhanation/bannermod/governance/BannerModGovernorService.java` - exposes server-side policy mutation over the persisted governor snapshot.
- `src/main/java/com/talhanation/bannermod/governance/BannerModGovernorPolicy.java` - defines the bounded governor policy vocabulary and value labels.
- `src/main/java/com/talhanation/bannermod/network/messages/military/MessageToClientUpdateGovernorScreen.java` - carries report fields plus bounded policy state to the client screen.
- `src/main/java/com/talhanation/bannermod/network/messages/military/MessageUpdateGovernorPolicy.java` - sends governor policy changes back to the server.
- `src/main/java/com/talhanation/bannermod/network/BannerModNetworkBootstrap.java` - registers the new governor policy update packet.
- `.planning/phases/23-settlement-governance-and-governor-control/deferred-items.md` - records unrelated pre-existing root test compilation failures.

## Decisions Made
- Persisted bounded governor policy state in the existing claim-keyed governor snapshot instead of inventing a second governance store.
- Reused the existing promote packet and recruit event flow for governor designation to keep profession id `6` inside the current promotion path.

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered
- `./gradlew compileJava --console=plain` passed for both tasks.
- `./gradlew compileJava test --console=plain` still fails because of the known 39 pre-existing compile errors in unrelated root test sources. Logged to `.planning/phases/23-settlement-governance-and-governor-control/deferred-items.md` and left out of scope.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness
- Governor designation, live report sync, and bounded policy controls are now available for follow-up validation/UAT work.
- Full root `test` remains blocked by the pre-existing unrelated compile errors recorded in the phase deferred-items file.

## Self-Check: PASSED

---
*Phase: 23-settlement-governance-and-governor-control*
*Completed: 2026-04-15*
