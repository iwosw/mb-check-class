---
phase: 05-persistence-and-multiplayer-sync-hardening
plan: 03
subsystem: testing
tags: [junit5, client-sync, packets, cache-reset, routes]
requires:
  - phase: 04-02
    provides: packet-path authority validation patterns for multiplayer commands
provides:
  - explicit synchronized-cache reset contract in ClientManager
  - client lifecycle reset hooks for login/logout boundaries
  - JVM packet validation coverage for malformed route transfers
affects: [phase-05, multiplayer-sync, client-cache]
tech-stack:
  added: []
  patterns: [explicit client sync reset entrypoint, route payload validation before client mutation]
key-files:
  created:
    - src/main/java/com/talhanation/recruits/client/events/ClientSyncLifecycleEvents.java
    - src/test/java/com/talhanation/recruits/network/ClientSyncPacketContractTest.java
  modified:
    - src/main/java/com/talhanation/recruits/client/ClientManager.java
    - src/main/java/com/talhanation/recruits/network/MessageTransferRoute.java
    - src/main/java/com/talhanation/recruits/network/MessageToClientReceiveRoute.java
key-decisions:
  - "ClientManager now owns one explicit synchronized-cache reset path while leaving the local route library intact."
  - "Route-transfer packets must validate payload shape before any local route save occurs."
patterns-established:
  - "Login/logout lifecycle hooks should call a shared reset helper instead of clearing client caches ad hoc."
requirements-completed: [DATA-03, DATA-04]
duration: 15min
completed: 2026-04-07
---

# Phase 5 Plan 03: Client Sync Cache Summary

**Client sync caches now reset cleanly across multiplayer session boundaries, and malformed route-transfer packets fail before touching local route state.**

## Performance

- **Duration:** 15 min
- **Started:** 2026-04-07T12:53:00Z
- **Completed:** 2026-04-07T13:08:00Z
- **Tasks:** 2
- **Files modified:** 5

## Accomplishments
- Added JVM coverage for synchronized-cache reset behavior and route-transfer payload validation.
- Added one explicit `ClientManager` reset path and wired it to client login/logout lifecycle events.
- Prevented invalid route payloads from decoding into client-side saved routes.

## Task Commits

1. **Task 1: Write client-cache lifecycle and invalid packet tests** - `051db6d4` (test)
2. **Task 2: Implement cache reset hooks and safe route-transfer validation** - `73004606` (fix)

**Plan metadata:** pending final docs commit

## Files Created/Modified
- `src/test/java/com/talhanation/recruits/network/ClientSyncPacketContractTest.java` - JVM coverage for cache reset and route-transfer validation.
- `src/main/java/com/talhanation/recruits/client/ClientManager.java` - explicit synchronized-cache reset helper.
- `src/main/java/com/talhanation/recruits/client/events/ClientSyncLifecycleEvents.java` - login/logout reset hooks.
- `src/main/java/com/talhanation/recruits/network/MessageTransferRoute.java` - server-side route payload validation before forwarding.
- `src/main/java/com/talhanation/recruits/network/MessageToClientReceiveRoute.java` - client-side route decode guard.

## Decisions Made
- Kept the editable local route library out of reset scope while still protecting it from malformed remote payloads.
- Used route decode validation as the shared contract for both server forwarding and client receipt.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] Removed JVM-hostile `ItemStack.EMPTY` cache reset usage**
- **Found during:** Task 2 (Implement cache reset hooks and safe route-transfer validation)
- **Issue:** `ItemStack.EMPTY` triggered runtime-heavy static initialization and broke the pure JVM test contract for cache reset coverage.
- **Fix:** Switched the reset baseline to null-safe cache sentinels in `ClientManager` and updated the tests accordingly.
- **Files modified:** `src/main/java/com/talhanation/recruits/client/ClientManager.java`, `src/test/java/com/talhanation/recruits/network/ClientSyncPacketContractTest.java`
- **Verification:** `./gradlew test --tests "com.talhanation.recruits.network.ClientSyncPacketContractTest"`
- **Committed in:** `73004606`

---

**Total deviations:** 1 auto-fixed (1 blocking)
**Impact on plan:** The deviation kept the new client-sync coverage pure JVM as intended. No scope creep.

## Issues Encountered
- The first implementation hit Minecraft runtime initialization through `ItemStack.EMPTY`, so the reset contract was adjusted to stay testable without booting the client.

## User Setup Required
None - no external service configuration required.

## Next Phase Readiness
- Runtime persistence verification can now assume a stable client reset contract and safe route-transfer validation.

## Self-Check: PASSED

---
*Phase: 05-persistence-and-multiplayer-sync-hardening*
*Completed: 2026-04-07*
