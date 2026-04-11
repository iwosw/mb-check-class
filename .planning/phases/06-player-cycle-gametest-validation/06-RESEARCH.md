# Phase 06 Research

**Date:** 2026-04-11
**Phase:** 06 — Player-Cycle GameTest Validation
**Question:** What do we need to know to plan this phase well?

## Current Baseline

- Root GameTests already run from `src/gametest/java/com/talhanation/bannermod/` through `./gradlew verifyGameTestStage`.
- `src/gametest/java/com/talhanation/bannermod/IntegratedRuntimeGameTests.java` currently proves three narrow seams: merged runtime identity, shared recruit-worker ownership behavior, and one settlement-to-recruit supply transition.
- The phase goal is additive validation first: prefer new root GameTest slices over production rewrites unless a test exposes a real defect.

## Relevant Existing Patterns

### GameTest structure

- Use `@GameTestHolder(Main.MOD_ID)` and `@PrefixGameTestTemplate(false)`.
- Use the shared `harness_empty` template for small deterministic scenarios.
- Use `GameTestHelper.makeMockPlayer()` and real spawned entities instead of mocks.
- Use `helper.assertTrue(...)` / `helper.assertFalse(...)` with explicit failure messages.

### Existing support to reuse

- `recruits/src/gametest/java/com/talhanation/recruits/gametest/support/RecruitsBattleGameTestSupport.java` already provides stable recruit spawn positions and `spawnConfiguredRecruit(...)` helpers.
- `src/gametest/java/com/talhanation/bannermod/IntegratedRuntimeGameTests.java` already contains reusable worker/build-area spawning helpers and a minimal build-template NBT generator.
- `src/main/java/com/talhanation/bannermod/authority/BannerModAuthorityRules.java` defines the merged owner / same-team / admin / forbidden vocabulary.
- `src/main/java/com/talhanation/bannermod/logistics/BannerModSupplyStatus.java` defines the merged build / worker / recruit supply snapshots.

## Planning Recommendation

Split the current monolithic root GameTest file into one small runtime-smoke file plus dedicated player-cycle slice classes.

Recommended file layout:

- `src/gametest/java/com/talhanation/bannermod/BannerModGameTestSupport.java`
- `src/gametest/java/com/talhanation/bannermod/IntegratedRuntimeGameTests.java` (keep runtime smoke only)
- `src/gametest/java/com/talhanation/bannermod/BannerModOwnershipCycleGameTests.java`
- `src/gametest/java/com/talhanation/bannermod/BannerModSettlementLaborGameTests.java`
- `src/gametest/java/com/talhanation/bannermod/BannerModUpkeepFlowGameTests.java`
- `src/gametest/java/com/talhanation/bannermod/BannerModPlayerCycleGameTests.java`

Why:

- avoids one oversized test class
- gives each roadmap slice its own executable artifact
- prevents later plans from repeatedly editing the same test method block
- keeps the existing green runtime smoke intact while new slice coverage grows around it

## Concrete Helper Contract To Establish First

Create a shared root GameTest helper with exact static methods for later plans:

- `spawnOwnedFarmer(GameTestHelper helper, Player player, BlockPos relativePos)`
- `spawnOwnedCropArea(GameTestHelper helper, Player player, BlockPos relativePos)`
- `spawnOwnedStorageArea(GameTestHelper helper, Player player, BlockPos relativePos)`
- `spawnOwnedBuildArea(GameTestHelper helper, Player player, BlockPos relativePos)`
- `createMinimalBuildTemplate()`
- `spawnEntity(GameTestHelper helper, EntityType<T> entityType, BlockPos relativePos)`

This keeps later slice plans concrete and avoids codebase rediscovery.

## Risks / Pitfalls

- Do not move GameTests back under `recruits/src/gametest/java`; Phase 06 is explicitly rooted in `src/gametest/java/com/talhanation/bannermod/`.
- Do not replace real entity spawning with mocks; existing GameTest patterns rely on live entity behavior.
- Do not overreach into AI timing or long async flows unless needed; Phase 06 wants deterministic validation slices, not broad simulation.
- `AbstractWorkerEntity.recoverControl(...)` only allows owner/admin recovery, so same-team checks should validate work-area acceptance separately from recovery authority.
- Multiple plans will need the same helper methods; create the helper in Plan 01 to avoid conflicting edits later.

## Recommended Verification

- Fast syntax/sample gate: `./gradlew compileGameTestJava`
- Phase gate: `./gradlew verifyGameTestStage`

## Validation Architecture

- Every execution task in this phase should end with an automated command.
- Use `compileGameTestJava` after each task commit to catch source-set/import failures quickly.
- Use `verifyGameTestStage` after each completed plan or wave because the phase goal is specifically root GameTest validation.
- No manual-only verification is required for the phase goal; success is observable through passing root GameTests.
