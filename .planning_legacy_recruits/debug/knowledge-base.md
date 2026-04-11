# GSD Debug Knowledge Base

Resolved debug sessions. Used by `gsd-debugger` to surface known-pattern hypotheses at the start of new investigations.

---

## milestone-audit-blocker — milestone audit blocked by uncommitted battle test fixes
- **Date:** 2026-04-09
- **Error patterns:** milestone audit blocker, Mixed squad GameTest fails, representativemixedsquadsresolveboundedbattle, baselinedensebattlecompleteswithoutbrokenloops, heavierdensebattlecompleteswithoutbrokenloops, verification matrix drift
- **Root cause:** The committed milestone checkout is not trustworthily green because HEAD still contains stale battle GameTest fixtures/assertions that fail on clean audit runs (mixed squad at the old 140-tick/4-HP setup plus two old stress deadlines), while the local workspace already has the unpublished fixes in BattleStressFixtures.java, BattleStressGameTests.java, and MixedSquadBattleGameTests.java. VERIFICATION_MATRIX.md still describes the old accepted-red state, so docs and code diverged.
- **Fix:** Keep the existing battle GameTest fix set in the working tree and update VERIFICATION_MATRIX.md to describe the actual now-green verification state instead of the old accepted battle debt.
- **Files changed:** src/gametest/java/com/talhanation/recruits/gametest/battle/BattleStressFixtures.java, src/gametest/java/com/talhanation/recruits/gametest/battle/BattleStressGameTests.java, src/gametest/java/com/talhanation/recruits/gametest/battle/MixedSquadBattleGameTests.java, VERIFICATION_MATRIX.md
---
