# Deferred Items

## 2026-04-08

- `src/gametest/java/com/talhanation/recruits/gametest/battle/BattleStressGameTests.java`
  - `baselinedensebattlecompleteswithoutbrokenloops` still fails during the Phase 6 `./gradlew runGameTestServer --continue` and `./gradlew check --continue` pass.
  - Current behavior: the scenario does not resolve to the expected winning side before the stress deadline.
  - Deferred as **inherited debt** from earlier accepted battle-density instability already tracked in Phase 4, not as a new Phase 6 regression.
  - Phase 6 note: the working tree also contains pre-existing uncommitted edits in the battle-stress fixtures/test files; this documentation pass records the observed failure rather than widening scope to rework battle-density behavior.
- `src/gametest/java/com/talhanation/recruits/gametest/battle/BattleStressGameTests.java`
  - `heavierdensebattlecompleteswithoutbrokenloops` still fails during the Phase 6 `./gradlew runGameTestServer --continue` and `./gradlew check --continue` pass.
  - Current behavior: a surviving recruit escapes the dedicated arena bounds before the heavy stress scenario resolves cleanly.
  - Deferred as **inherited debt** from earlier accepted battle-density instability already tracked in Phase 4, not as a new Phase 6 regression.
  - Phase 6 note: the working tree also contains pre-existing uncommitted edits in the battle-stress fixtures/test files; this documentation pass records the observed failure rather than widening scope to rework battle-density behavior.

## Resolved from prior accepted debt

- `representativemixedsquadsresolveboundedbattle`
  - Previously carried as accepted debt in earlier phase documentation.
  - It did **not** reproduce in the Phase 6 full-surface pass, so it is not listed as remaining debt.
