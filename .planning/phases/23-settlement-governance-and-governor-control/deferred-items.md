# Deferred Items

## 2026-04-15 — 23-02 targeted JUnit gate blocked by unrelated test-tree compile failures

- **Plan:** `23-02`
- **Task:** `Task 1: Add governor authority and assignment service coverage first`
- **Command:** `./gradlew test --tests com.talhanation.bannermod.governance.BannerModGovernorServiceTest --console=plain`
- **Outcome:** Blocked before targeted governor tests could execute because `:compileTestJava` still fails on 39 pre-existing errors in unrelated merged-runtime and workers test files.
- **Out of scope because:** The failing symbols and type mismatches are outside the governance files owned by Plan 23-02, and `.planning/STATE.md` already records this root test-tree issue as deferred background work.
- **Representative failures:**
  - `src/test/java/com/talhanation/bannermod/BannerModIntegratedRuntimeSmokeTest.java`
  - `src/test/java/com/talhanation/workers/WorkersRuntimeSmokeTest.java`
  - `src/test/java/com/talhanation/workers/WorkerSettlementSpawnRulesTest.java`
- **Action taken here:** Left the unrelated failures untouched, verified the in-scope code with `./gradlew compileJava --console=plain`, and recorded the blocker for later cleanup.
