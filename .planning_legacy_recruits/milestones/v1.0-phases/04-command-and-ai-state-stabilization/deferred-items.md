# Deferred Items

## 2026-04-07

- `src/gametest/java/com/talhanation/recruits/gametest/battle/BattleStressGameTests.java`
  - `baselinedensebattlecompleteswithoutbrokenloops` fails during `runGameTestServer` / `check --continue`.
  - Failure is out of scope for 04-02 because it predates the area-command packet changes.
- `src/gametest/java/com/talhanation/recruits/gametest/battle/BattleStressGameTests.java`
  - `heavierdensebattlecompleteswithoutbrokenloops` fails during `runGameTestServer` / `check --continue`.
  - Failure is out of scope for 04-02 because it predates the area-command packet changes.
- `src/gametest/java/com/talhanation/recruits/gametest/battle/MixedSquadBattleGameTests.java`
  - `representativemixedsquadsresolveboundedbattle` fails during phase 4 verification.
  - Failure is out of scope for plans 04-03 and 04-04 because it predates the command/AI packet and transition changes.
