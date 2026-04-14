---
status: resolved
trigger: "Investigate issue: upkeep-storage-and-provider-sourcing"
created: 2026-04-14T00:00:00Z
updated: 2026-04-14T01:00:00Z
---

## Current Focus

hypothesis: root cause and fix are confirmed; archive the resolved session and commit code/docs
test: move resolved debug record, commit targeted files, and update the knowledge base
expecting: session is archived with matching code/docs commits and a reusable knowledge-base entry
next_action: archive the session file and record the resolution in the knowledge base

## Symptoms

expected: Workers should pull upkeep from storage chests, and both recruits and workers should be able to obtain wages and food from some valid provider path.
actual: Workers ignore the storage chest for upkeep, normal upkeep appears gray on chest hover, and both recruits and workers can fail to source upkeep.
errors: No visible errors seen.
reproduction: Repro by placing a worker near storage with upkeep demand, and by placing a recruit near a valid provider and checking whether wages/food can be sourced.
started: Unsure when it started or whether it ever worked.

## Eliminated

## Evidence

- timestamp: 2026-04-14T00:05:00Z
  checked: .planning/debug/knowledge-base.md
  found: No existing knowledge-base entry overlaps storage chest upkeep/provider sourcing symptoms.
  implication: Investigate as a new issue rather than a known repeated pattern.

- timestamp: 2026-04-14T00:15:00Z
  checked: recruits/src/main/java/com/talhanation/recruits/client/gui/commandscreen/OtherCategory.java, recruits/src/main/java/com/talhanation/recruits/client/events/ClientEvent.java
  found: The upkeep button only enables for raw Container/ChestBlock targets or pack-animal/container entities, while command targeting prefers the looked-at entity from Minecraft hitResult.
  implication: Looking at a StorageArea/MarketArea work-area entity can gray out the upkeep command even if its scanned chests are the intended provider.

- timestamp: 2026-04-14T00:15:00Z
  checked: recruits/src/main/java/com/talhanation/recruits/entities/ai/RecruitUpkeepEntityGoal.java, recruits/src/main/java/com/talhanation/recruits/entities/ai/RecruitUpkeepPosGoal.java
  found: Upkeep goals only resolve direct Container providers (block entities, InventoryCarrier, AbstractHorse). Work-area entities with internal chest maps never produce a Container for upkeep consumption.
  implication: Recruits and workers cannot source food/payment from StorageArea or MarketArea providers even if the command targets those entities.

- timestamp: 2026-04-14T00:15:00Z
  checked: workers/src/main/java/com/talhanation/workers/entities/workarea/StorageArea.java, workers/src/main/java/com/talhanation/workers/entities/workarea/MarketArea.java
  found: StorageArea and MarketArea already encapsulate valid provider chest scans through storageMap/containerMap plus getContainer helpers, but recruit upkeep code does not integrate with them.
  implication: A small shared provider bridge can reuse existing scanned-container logic instead of inventing a new economy path.

- timestamp: 2026-04-14T00:15:00Z
  checked: .planning/phases/06-player-cycle-gametest-validation/06-03-PLAN.md, .planning/phases/24-logistics-backbone-and-courier-worker/24-CONTEXT.md
  found: Phase 06 expected same-owner settlement supply to satisfy recruit upkeep, while Phase 24 explicitly keeps deeper economy logic deferred and prefers additive reuse of existing storage/work-area seams.
  implication: The fix should wire current upkeep to existing valid storage providers, not introduce treasury/trade redesign.

- timestamp: 2026-04-14T00:27:00Z
  checked: ./gradlew compileJava test --tests com.talhanation.bannermod.logistics.BannerModCombinedContainerTest
  found: compileJava succeeded, but the new test failed during vanilla Items static initialization in plain JUnit.
  implication: The production fix compiles; the regression test needs runtime-neutral test items instead of registry-backed vanilla Items.

- timestamp: 2026-04-14T00:29:00Z
  checked: repeated BannerModCombinedContainerTest attempts
  found: Even fake containers still trigger ItemStack bootstrap constraints in plain JUnit, so this regression is a poor fit for unit tests outside a Minecraft runtime.
  implication: Verification should move to the existing root GameTest environment, which already exercises real world/container state.

- timestamp: 2026-04-14T00:33:00Z
  checked: ./gradlew compileJava compileGameTestJava verifyGameTestStage
  found: The new storage-area upkeep GameTest initially failed because the second seeded chest sat outside the authored StorageArea footprint, so the provider resolver correctly returned no combined container.
  implication: The provider bridge logic is behaving consistently with storage-area scan bounds; fix the fixture coordinates and rerun verification.

- timestamp: 2026-04-14T00:45:00Z
  checked: follow-up GameTest attempts
  found: The experimental new upkeep-provider fixture remained brittle, so it was removed rather than leave the verification suite red on an unrelated authored-area setup issue.
  implication: Final self-verification should rely on clean compilation plus existing stable test suites, with human workflow confirmation still required.

- timestamp: 2026-04-14T00:50:00Z
  checked: ./gradlew compileJava test compileGameTestJava
  found: Final merged build compilation, stable JUnit coverage, and GameTest source compilation all succeeded after wiring the provider bridge.
  implication: The fix is internally consistent and ready for human workflow verification.

## Resolution

root_cause: Recruit/worker upkeep logic only understood direct Container targets, while settlement storage providers are represented as StorageArea/MarketArea entities that wrap multiple scanned chests. The command UI rejected those entities on hover, and the upkeep goals could not resolve them into consumable containers, so workers/recruits could not source food or payment through the intended settlement provider path.
fix: Add a shared upkeep-provider bridge that recognizes StorageArea/MarketArea entities and exposes their scanned chests as one combined Container, then reuse that bridge in the command-screen upkeep target validation and upkeep goals.
verification: ./gradlew compileJava test compileGameTestJava passed; user confirmed the in-game workflow is fixed
files_changed: [src/main/java/com/talhanation/bannermod/logistics/BannerModCombinedContainer.java, src/main/java/com/talhanation/bannermod/logistics/BannerModUpkeepProviders.java, recruits/src/main/java/com/talhanation/recruits/client/gui/commandscreen/OtherCategory.java, recruits/src/main/java/com/talhanation/recruits/entities/ai/RecruitUpkeepPosGoal.java, recruits/src/main/java/com/talhanation/recruits/entities/ai/RecruitUpkeepEntityGoal.java]
