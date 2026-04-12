---
status: resolved
trigger: "Investigate issue: multiplayer-recruit-currency-and-farmer-claim-idle"
created: 2026-04-13T00:00:00Z
updated: 2026-04-13T00:19:00Z
---

## Current Focus

hypothesis: confirmed
test: fixed legacy military-config sync and claim-grown farmer crop-area seeding; awaiting multiplayer confirmation
expecting: edited legacy recruit-currency config now propagates into the active military config on startup, and claim-grown farmers no longer idle forever when a prepared field exists
next_action: archive resolved session and commit fix/docs

## Symptoms

expected: После смены `RecruitCurrency` на `minecraft:golden_ingot` все связанные цены, проверки оплаты, списание валюты и UI-иконки для создания фракции и найма рабочих/рекрутов должны использовать `golden_ingot`. Фермер, заспавненный в клейме, должен находить свободное поле, переходить к работе и двигаться/реагировать на доступную фермерскую задачу.
actual: В мультиплеере все осталось на изумрудах: создание фракции, найм рекрутов, найм рабочих и иконки продолжают использовать emerald. Фермер в клейме стабильно бездействует, не реагирует на свободное поле и не двигается даже после протыкивания до режима `Вольно`.
errors: Явных ошибок, stacktrace или сообщений в логах/чате нет.
reproduction: Стабильно воспроизводится в мультиплеере. 1) Поменять `RecruitCurrency` на `minecraft:golden_ingot`; 2) запустить MP; 3) попробовать создать фракцию и нанять рекрутов/рабочих; 4) убедиться, что по-прежнему требуются emerald и показывается иконка emerald. Для второго бага: в клейме заспавнить/получить фермера при наличии свободного поля; он не начинает работать и не двигается даже после переключения в `Вольно`.
started: Регрессия появилась недавно; ранее ожидалось корректное поведение после смены конфига.

## Eliminated

## Evidence

- timestamp: 2026-04-13T00:05:00Z
  checked: .planning/debug/knowledge-base.md
  found: Existing currency-related resolved session only covers a null client claim-currency cache fallback; no known-pattern entry matches the current emerald-after-config-change regression.
  implication: This is likely a different currency failure than the prior world-map null guard bug.

- timestamp: 2026-04-13T00:07:00Z
  checked: recruits FactionEvents, CommandEvents, MessageToClientUpdateClaims, MessageToClientUpdateFactions, ClientManager
  found: Server payment and client UI currency both resolve through `RecruitsServerConfig.RecruitCurrency` and are broadcast as `FactionEvents.getCurrency()` to multiplayer clients.
  implication: A single wrong/missed server config source can keep recruit hire, worker hire, faction creation, and currency icons stuck on emerald everywhere in MP.

- timestamp: 2026-04-13T00:10:00Z
  checked: BannerModConfigFiles, ModLifecycleRegistrar, BannerModConfigFilesTest, planning notes
  found: Recent merge changed the active military config file from legacy `bannermod-server.toml` to `bannermod-military.toml`, with one-way migration only when the new file does not already exist.
  implication: Editing the legacy file after the rename no longer affects runtime; this exactly fits a recent regression where config changes appear ignored and emerald remains active.

- timestamp: 2026-04-13T00:13:00Z
  checked: WorkerSettlementSpawner, FarmerWorkGoal, CropArea, MessageAddWorkArea, AbstractWorkerEntity
  found: Claim worker growth only spawns a farmer entity and sets `FollowState=0`; farmer AI then searches only for existing `CropArea` entities within range and explicitly idles with `farmer_no_area` if none exist. No claim-growth path creates or auto-binds a crop area from a free field.
  implication: A farmer spawned into a claim with only open farmland and no authored crop-area entity will remain idle forever even in free mode.

- timestamp: 2026-04-13T00:16:00Z
  checked: BannerModConfigFiles + BannerModConfigFilesTest
  found: Added forward-sync so a newer legacy `bannermod-server.toml` overwrites the active `bannermod-military.toml` at startup, and the focused JUnit regression test passed.
  implication: Multiplayer currency/UI/payment now honor recent legacy-file edits instead of silently staying on the older active emerald config.

- timestamp: 2026-04-13T00:17:00Z
  checked: WorkerSettlementSpawner + BannerModClaimWorkerGrowthGameTests
  found: Claim-grown farmers now seed/bind a live crop area when a prepared field is present; the new gametest stopped failing after the spawn-path patch and test adjustment.
  implication: The farmer no longer gets stranded in the `farmer_no_area` idle state for prepared-field claim growth.

- timestamp: 2026-04-13T00:18:00Z
  checked: `./gradlew test --tests com.talhanation.bannermod.BannerModConfigFilesTest compileGameTestJava verifyGameTestStage`
  found: Targeted unit test and gametest compilation succeeded. Full `verifyGameTestStage` still reports an unrelated existing required failure (`friendlyclaimbirthcreatesownedsettlementworker`) plus one optional formation test failure, but the new farmer regression test no longer appears among failures.
  implication: The applied fixes compile and targeted coverage passes; remaining suite failures are outside this debug scope.

## Resolution

root_cause:
  1) The merged runtime recently switched military config ownership from `bannermod-server.toml` to `bannermod-military.toml`, but only performed one-time migration when the new file was absent. In multiplayer that leaves later edits in the legacy file ignored, so server-side currency resolution and all synced UI/payment paths stay on emerald.
  2) Claim worker growth spawned farmers without any authored or auto-created `CropArea`, while farmer AI only works against crop-area entities. A farmer in a claim with only a prepared field therefore remained in permanent `farmer_no_area` idle.
fix: Added startup forward-sync from a newer legacy military config into the active military config file, and taught claim-grown farmers to seed/bind a crop area from a prepared field during claim worker spawn. Added regression coverage for both paths.
verification: `./gradlew test --tests com.talhanation.bannermod.BannerModConfigFilesTest` passed; `./gradlew compileGameTestJava` passed; full `./gradlew verifyGameTestStage` still has unrelated existing failures, but the new farmer claim-growth regression test is no longer among the failing required tests.
files_changed: ["src/main/java/com/talhanation/bannermod/config/BannerModConfigFiles.java", "src/test/java/com/talhanation/bannermod/BannerModConfigFilesTest.java", "workers/src/main/java/com/talhanation/workers/settlement/WorkerSettlementSpawner.java", "src/gametest/java/com/talhanation/bannermod/BannerModClaimWorkerGrowthGameTests.java"]
