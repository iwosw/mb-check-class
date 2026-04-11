---
phase: 02-layered-test-harness-foundations
plan: 02
subsystem: testing
tags: [junit5, forge, nbt, packet-codec, fixtures]
requires:
  - phase: 02-01
    provides: split JVM/GameTest harness and runnable verification entrypoints
provides:
  - reusable JVM fixtures for faction, claim, and movement packet tests
  - shared NBT and FriendlyByteBuf round-trip assertions for future fast tests
  - reference persistence and codec tests that run under ./gradlew test
affects: [phase-03, phase-05, persistence, networking]
tech-stack:
  added: [JUnit 5 helper layer, CoreLib API on test classpath]
  patterns: [fixture-first JVM tests, round-trip assertion helpers, reflection-backed brownfield fixtures]
key-files:
  created:
    - src/test/java/com/talhanation/recruits/testsupport/RecruitsFixtures.java
    - src/test/java/com/talhanation/recruits/testsupport/NbtRoundTripAssertions.java
    - src/test/java/com/talhanation/recruits/testsupport/MessageCodecAssertions.java
  modified:
    - src/test/java/com/talhanation/recruits/world/RecruitsFactionSerializationTest.java
    - src/test/java/com/talhanation/recruits/world/RecruitsClaimSerializationTest.java
    - src/test/java/com/talhanation/recruits/network/MessageMovementCodecTest.java
    - build.gradle
    - .gitignore
key-decisions:
  - "Use test-only fixture builders and round-trip assertions instead of moving brownfield helpers into src/main/java."
  - "Instantiate RecruitsClaim fixtures through its private UUID constructor so pure JVM tests avoid Forge config boot requirements."
  - "Add CoreLib's API artifact to testImplementation so codec tests can compile against existing Message classes."
patterns-established:
  - "Fixture-first JVM coverage: build sample factions, claims, and packets once and reuse them across tests."
  - "Round-trip helpers own NBT and FriendlyByteBuf plumbing so future tests focus on observable fields."
requirements-completed: [TEST-01, TEST-03]
duration: 11min
completed: 2026-04-05
---

# Phase 2 Plan 02: JVM Helper Layer Summary

**Reusable faction/claim fixtures plus NBT and packet round-trip assertions now anchor pure JVM persistence and networking tests.**

## Performance

- **Duration:** 11 min
- **Started:** 2026-04-05T14:36:00Z
- **Completed:** 2026-04-05T14:47:00Z
- **Tasks:** 2
- **Files modified:** 8

## Accomplishments
- Added shared JVM-side fixtures for factions, claims, and `MessageMovement` packets.
- Added reusable NBT and buffer round-trip assertions so future tests avoid bespoke setup.
- Added representative faction, claim, and codec tests that pass under `./gradlew test`.

## Task Commits

Each task was committed atomically:

1. **Task 1: Create reusable JVM fixture and assertion helpers** - `1a8bf95a` (test), `92fc4320` (feat)
2. **Task 2: Prove the helpers with representative persistence and codec tests** - `60d9b8f0` (test)

**Plan metadata:** pending

_Note: TDD tasks may have multiple commits (test → feat → refactor)_

## Files Created/Modified
- `src/test/java/com/talhanation/recruits/testsupport/RecruitsFixtures.java` - canonical faction, claim, and movement fixtures for JVM tests
- `src/test/java/com/talhanation/recruits/testsupport/NbtRoundTripAssertions.java` - reusable faction and claim NBT round-trip assertions
- `src/test/java/com/talhanation/recruits/testsupport/MessageCodecAssertions.java` - shared FriendlyByteBuf round-trip helper for packet tests
- `src/test/java/com/talhanation/recruits/world/RecruitsFactionSerializationTest.java` - reference faction persistence test using shared fixtures
- `src/test/java/com/talhanation/recruits/world/RecruitsClaimSerializationTest.java` - reference claim persistence test using shared fixtures
- `src/test/java/com/talhanation/recruits/network/MessageMovementCodecTest.java` - reference packet codec test using shared buffer assertions
- `build.gradle` - exposes CoreLib API to the JVM test classpath
- `.gitignore` - ignores generated `logs/` runtime output from verification runs

## Decisions Made
- Kept all new support code under `src/test/java` so the production mod stays untouched while future JVM tests gain reusable helpers.
- Used reflective construction for `RecruitsClaim` fixtures because the public constructor eagerly reads Forge config and breaks pure JVM tests.
- Verified the helper layer with explicit observable-field assertions in the reference tests, not just smoke invocations.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] Added CoreLib API to the test classpath**
- **Found during:** Task 1 (Create reusable JVM fixture and assertion helpers)
- **Issue:** `MessageMovement` could not compile in JVM tests because `de.maxhenkel.corelib.net.Message` was absent from the test compile classpath.
- **Fix:** Added `fg.deobf("de.maxhenkel.corelib:corelib:1.20.1-1.1.3:api")` to `testImplementation`.
- **Files modified:** `build.gradle`
- **Verification:** `./gradlew test --tests "com.talhanation.recruits.network.MessageMovementCodecTest"`
- **Committed in:** `92fc4320` (part of task commit)

**2. [Rule 3 - Blocking] Bypassed config-bound claim construction in JVM fixtures**
- **Found during:** Task 1 (Create reusable JVM fixture and assertion helpers)
- **Issue:** `RecruitsClaim`'s public constructor called `resetHealth()`, which reads Forge config before config loading and crashes pure JVM tests.
- **Fix:** Built claim fixtures through the private `(UUID, String, RecruitsFaction)` constructor via reflection, then populated the observable fields in test code.
- **Files modified:** `src/test/java/com/talhanation/recruits/testsupport/RecruitsFixtures.java`
- **Verification:** `./gradlew test --tests "com.talhanation.recruits.world.RecruitsClaimSerializationTest"`
- **Committed in:** `92fc4320` (part of task commit)

---

**Total deviations:** 2 auto-fixed (2 blocking)
**Impact on plan:** Both fixes were required to keep the new helper layer truly pure-JVM. No scope creep.

## Issues Encountered
- Forge config access in `RecruitsClaim` is not JVM-test-friendly, so fixture construction had to avoid the runtime-bound constructor.

## User Setup Required
None - no external service configuration required.

## Next Phase Readiness
- Phase 2 now has a reusable JVM helper layer ready for deeper persistence, networking, AI, and command tests.
- The next plan can focus on GameTest-side recruit helpers without needing to revisit JVM NBT/buffer setup.

## Self-Check: PASSED

---
*Phase: 02-layered-test-harness-foundations*
*Completed: 2026-04-05*
