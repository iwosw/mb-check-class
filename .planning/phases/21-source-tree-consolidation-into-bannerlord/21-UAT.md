---
status: complete
phase: 21-source-tree-consolidation-into-bannerlord
source: [21-01-SUMMARY.md, 21-02-SUMMARY.md, 21-03-SUMMARY.md, 21-04-SUMMARY.md, 21-05-SUMMARY.md, 21-06-SUMMARY.md, 21-07-SUMMARY.md, 21-08-SUMMARY.md, 21-09-SUMMARY.md, 21-10-SUMMARY.md, 21-11-SUMMARY.md]
started: 2026-04-15T20:00:00Z
updated: 2026-04-15T14:30:00Z
---

## Current Test

[testing complete]

## Tests

### 1. Cold Start Smoke Test — compileJava from clean
expected: Run `./gradlew --stop && ./gradlew clean compileJava` from the outer repo root. Build completes `BUILD SUCCESSFUL`, `:compileJava` resolves only outer `src/main/java/com/talhanation/bannermod/**` sources, and no clone source roots (`recruits/src/**`, `workers/src/**`) are pulled in.
result: pass

### 2. Mod loads in dev client under single modId="bannermod"
expected: Run `./gradlew runClient` (or equivalent). Game launches, mods list shows exactly one entry `bannermod` (not `recruits`, `workers`, or `bannerlord`). No ClassNotFoundException / duplicate-class errors in log for `com.talhanation.{bannermod,recruits,workers,bannerlord}.*`.
result: pass
resolution: "Fixed by 21-10-PLAN.md — switched BannerModMain registerConfig calls to 3-arg overload with explicit filenames (bannermod-recruits-client.toml, bannermod-recruits-server.toml, bannermod-workers-server.toml). Re-run `./gradlew runClient` on 2026-04-15T13:05Z confirms the `Config conflict detected!` crash signature is GONE: launch passes BannerModMain.<init>, ModLoadingContext.registerConfig, and ConfigTracker.trackConfig without error. Single `bannermod` mod entry registers in COMMON_SET state. A separate downstream bug (IllegalStateException in ClientEvent.entityRenderersEvent reading RecruitsClientConfig before load) now blocks the main-menu step of verification — captured as a new gap below; does not recur on the test-2 crash signature."

### 3. Recruit entity spawns under bannermod namespace
expected: In dev client with a creative world open, `/summon bannermod:recruit ~ ~ ~` spawns a recruit entity with correct textures and model. Legacy `/summon recruits:recruit` also resolves (via legacy-id migration) or is cleanly rejected — not a silent crash.
result: pass

### 4. Worker entity spawns under bannermod namespace
expected: `/summon bannermod:miner ~ ~ ~` (or any civilian entity migrated from workers) spawns with correct textures and model. No `minecraft:missing` texture, no log errors about unregistered `workers:*` IDs.
result: pass

### 5. POI acquirable_job_site binds villagers to recruit sites
expected: Place a recruit job site block near a villager in dev client. Villager walks to it and claims the site as their profession POI (i.e., the POI tag `acquirable_job_site` resolves against the live `bannermod:*` entity registrations after the Wave-9 namespace rewrite — see 21-VERIFICATION.md Human Verification item 1).
result: pass

### 6. Legacy save migration — workers:/recruits: IDs rewritten on load
expected: Load an existing world created against the pre-Phase-21 build (with `workers:*` / `recruits:*` entity IDs persisted). Entities rehydrate without loss; `WorkersRuntime.migrateLegacyId` rewrites stored IDs to `bannermod:*` on first save. No "unknown entity type" warnings flood the log.
result: pass

### 7. Lang, assets, and recipes resolve
expected: In-game tooltips, GUI labels, and item names for recruit & worker content display translated text (not raw keys like `item.bannermod.recruit_contract`). No `minecraft:missing` textures on any migrated entity, item, or block. Recipes involving recruit/worker items show up in the recipe book as expected.
result: pass

### 8. Network packet round-trip — recruit command + work area update
expected: Issue a recruit command from the GUI (e.g., follow/hold position) and place/edit a worker work area. Both round-trip through the unified `bannermod:network` channel without packet-ID collisions (military offset 0, civilian offset 104 per 21-08 SUMMARY). No `Unknown custom packet identifier` warnings.
result: blocked
resolution: "Right-click-to-hire NPE fixed by 21-11-PLAN.md (registration of seven recruits-side handler classes in BannerModMain.setup()). User confirmed in dev-client on 2026-04-15: Hire GUI opens, no `recruitsPlayerUnitManager is null` in server log. Test 8's command-side half remains blocked: the R/U/M hotkeys that open Command/Faction/Map screens do nothing because the client-side KeyEvents handler class is also unregistered (same root-cause class as the recruits-side gap). Filed as a new gap → 21-12-PLAN.md. Resume test 8 once 21-12 ships."
severity: blocker

## Summary

total: 8
passed: 7
issues: 0
pending: 0
skipped: 0
blocked: 1

## Gaps

- truth: "Mod loads in dev client under single modId='bannermod' without mod-loading errors"
  status: closed
  closed_by: "21-10-PLAN.md"
  reason: "User reported: Mod loading crash at BannerModMain.<init>(BannerModMain.java:55) during ModLoadingContext.registerConfig — 'Config conflict detected!' from ConfigTracker.trackConfig. Single bannermod modId confirmed (mod list shows only one bannermod entry, state ERROR)."
  severity: blocker
  test: 2
  root_cause: "BannerModMain.java:52–55 registers three ModConfigs via the 2-arg ModLoadingContext.registerConfig(Type, Spec) overload. Two of them share ModConfig.Type.SERVER (RecruitsServerConfig.SERVER at line 53, WorkersServerConfig.SERVER at line 55). Forge's 2-arg overload defaults the filename to `<modid>-<type>.toml` → both resolve to `bannermod-server.toml`, and ConfigTracker.trackConfig rejects the second with `Config conflict detected!`. This is an artifact of Wave-4 (events-commands-config) merging both recruits' and workers' ServerConfig into the same modId without assigning distinct filenames. The CLIENT config at line 52 is unaffected because only one CLIENT config exists."
  artifacts:
    - path: "src/main/java/com/talhanation/bannermod/bootstrap/BannerModMain.java"
      issue: "Lines 52–55: two registerConfig(ModConfig.Type.SERVER, ...) calls with no explicit filename → both default to `bannermod-server.toml` and collide in ConfigTracker."
  missing:
    - "Use the 3-arg registerConfig(Type, Spec, fileName) overload for at least one of the two SERVER configs. Suggested filenames: `bannermod-recruits-server.toml` (preserves existing recruits config path when migrated) and `bannermod-workers-server.toml`. Apply consistently to RecruitsServerConfig.SERVER (line 53) and WorkersServerConfig.SERVER (line 55). Consider also renaming the CLIENT config to `bannermod-recruits-client.toml` for symmetry if a future WorkersClientConfig lands."
    - "After the fix, re-run `./gradlew runClient` and confirm crash is resolved and `bannermod` mod state is no longer ERROR. Then resume UAT from test 3."
  debug_session: ""

- truth: "Client reaches main menu in dev client without ForgeConfigSpec timing crash"
  status: closed
  closed_by: "21-10-PLAN.md (follow-up fix commit 14e7684)"
  resolution: "Fixed by deferring `RecruitsClientConfig.RecruitsLookLikeVillagers.get()` out of `EntityRenderersEvent.RegisterRenderers` and into the `EntityRendererProvider` lambda in BOTH `client/civilian/events/ClientEvent.java` and `client/military/events/ClientEvent.java`. The provider is invoked later in `EntityRenderDispatcher.onResourceManagerReload`, after `ModConfigEvent.Loading`, so `.get()` is safe. Verified on 2026-04-15T13:20Z: `./gradlew runClient` reaches main menu, loads a world (`[Server thread/INFO] talhanation joined the game`), and Forge wrote all three expected config files (`run/config/bannermod-recruits-client.toml`, `run/saves/New World/serverconfig/bannermod-recruits-server.toml`, `run/saves/New World/serverconfig/bannermod-workers-server.toml`) containing the expected keys (`PlayVillagerAmbientSound`, `MaxRecruitsForPlayer`, `FarmerCost`)."
  reason: "After 21-10 resolved the ModLoadingContext.registerConfig collision, re-running `./gradlew runClient` on 2026-04-15T13:05Z surfaces a new downstream crash: `java.lang.IllegalStateException: Cannot get config value before config is loaded` thrown from `ClientEvent.entityRenderersEvent(ClientEvent.java:34)` during the `EntityRenderersEvent.RegisterRenderers` event fired inside `Minecraft.<init>` / `initClientHooks`. Crash report UUID 18e6b9f4-f894-49ba-9d41-1d3838448fc7, run/crash-reports/crash-2026-04-15_19.26.49-client.txt. Suspected Mod: bannermod 1.14.3. This is a pre-existing latent defect that was masked by the earlier `Config conflict detected!` crash — not a regression introduced by 21-10."
  severity: blocker
  test: 2
  root_cause: "`com/talhanation/bannermod/client/civilian/events/ClientEvent.java:34` calls `RecruitsClientConfig.RecruitsLookLikeVillagers.get()` inside the static `@SubscribeEvent` handler for `EntityRenderersEvent.RegisterRenderers`. That event fires during client init *before* Forge has finished loading the CLIENT ModConfig spec, so `ForgeConfigSpec.ConfigValue.get()` trips its `Preconditions.checkState` guard. The code path was copied in during one of the recruits→bannermod client event moves (pre-21-10) and never re-examined for config-load ordering."
  artifacts:
    - path: "src/main/java/com/talhanation/bannermod/client/civilian/events/ClientEvent.java"
      issue: "Line 34 reads `RecruitsClientConfig.RecruitsLookLikeVillagers.get()` at renderer-registration time. Forge 1.20.1 does not guarantee CLIENT config is loaded at `EntityRenderersEvent.RegisterRenderers`; the config is loaded on `ModConfigEvent.Loading` which fires later in the lifecycle."
  missing:
    - "Defer the RecruitsLookLikeVillagers branch out of RegisterRenderers. Options: (a) register one renderer that internally reads the config value at render time (lazy), (b) register both renderer variants and swap at `FMLClientSetupEvent.enqueueWork` (by which point CLIENT config is loaded), or (c) register a single default (e.g., WorkerHumanRenderer) at RegisterRenderers and re-bind inside `ModConfigEvent.Loading` if the config flips to villagers. Option (a) is least invasive."
    - "After the fix, re-run `./gradlew runClient` and confirm the crash signature `IllegalStateException: Cannot get config value before config is loaded` from `ClientEvent.entityRenderersEvent` no longer appears, main menu loads, and mod state is LOADED. Then resume UAT from test 3."
  debug_session: ""

- truth: "Right-clicking a recruit opens the Hire GUI without server-side crash"
  status: closed
  closed_by: "21-11-PLAN.md"
  resolution: "Fixed by registering seven recruits-side @SubscribeEvent handler classes (RecruitEvents, ClaimEvents, FactionEvents, CommandEvents, DamageEvent, PillagerEvents, VillagerEvents) on MinecraftForge.EVENT_BUS inside BannerModMain.setup(FMLCommonSetupEvent). RecruitEvents.onServerStarting now fires on world start and assigns recruitsPlayerUnitManager (along with sibling managers) before any player-interact packet can reach openHireGUI. Verified by user in dev-client on 2026-04-15: right-click on a spawned recruit opens the Hire GUI; no `recruitsPlayerUnitManager is null` NPE in server log. Two unrelated client-side defects surfaced in the same dev-client session and are filed as separate gaps below (Command/Faction/Map screens not opening; gui.recruits.* lang keys missing)."
  reason: "User reported: NullPointerException — `Cannot invoke \"com.talhanation.bannermod.persistence.military.RecruitsPlayerUnitManager.canPlayerRecruit(String, java.util.UUID)\" because \"com.talhanation.bannermod.events.RecruitEvents.recruitsPlayerUnitManager\" is null`. Stack: AbstractRecruitEntity.openHireGUI(AbstractRecruitEntity.java:2403) ← mobInteract(AbstractRecruitEntity.java:1607) ← Mob.interact ← ServerboundInteractPacket. Crash fires on server when player right-clicks a recruit to hire. Discovered while attempting Test 8 (network packet round-trip — recruit command GUI path)."
  severity: blocker
  test: 8
  root_cause: "`RecruitEvents` is never registered on the Forge event bus. The static field `recruitsPlayerUnitManager` is assigned inside `onServerStarting(ServerStartingEvent)` at `src/main/java/com/talhanation/bannermod/events/RecruitEvents.java:206`, but that handler never fires because `BannerModMain.setup(FMLCommonSetupEvent)` never calls `MinecraftForge.EVENT_BUS.register(new RecruitEvents())`. The legacy `recruits/src/main/java/com/talhanation/recruits/Main.java` is now a deprecated shim; its original registration call was removed during Phase 21 consolidation but the corresponding call was never ported into `BannerModMain`. `BannerModMain.setup()` currently only registers `WorkersVillagerEvents`, `WorkersCommandEvents`, and `this`. Sibling recruits-side handler classes (`FactionEvents`, `CommandEvents`, `DamageEvent`, `DebugEvents`, `RecruitEvent`) are likely unregistered for the same reason — latent identical NPE class behind other interactions."
  artifacts:
    - path: "src/main/java/com/talhanation/bannermod/bootstrap/BannerModMain.java"
      issue: "setup(FMLCommonSetupEvent) near lines 102–104 registers WorkersVillagerEvents and WorkersCommandEvents but does not register the recruits-side event handlers. Missing: `MinecraftForge.EVENT_BUS.register(new RecruitEvents())` (and likely FactionEvents, CommandEvents, DamageEvent, DebugEvents, RecruitEvent)."
    - path: "src/main/java/com/talhanation/bannermod/events/RecruitEvents.java"
      issue: "Reference only — line 206 `onServerStarting` is the sole assignment site for `recruitsPlayerUnitManager`. Field declaration at line 69. Code is correct; it just never runs because the class is never registered."
  missing:
    - "In `BannerModMain.setup(FMLCommonSetupEvent)`, add `MinecraftForge.EVENT_BUS.register(new RecruitEvents());` alongside the existing `WorkersVillagerEvents` / `WorkersCommandEvents` registrations."
    - "Audit sibling recruits-side event handler classes (`FactionEvents`, `CommandEvents`, `DamageEvent`, `DebugEvents`, `RecruitEvent`) — register each exactly once in `BannerModMain.setup()` to avoid latent NPE-class bugs. Keep the existing `register(this)` call in place for `onRegisterCommands`."
    - "After fix: `./gradlew runClient`, open world, right-click a spawned recruit → Hire GUI should open (no NPE). Confirm `Cannot invoke ... recruitsPlayerUnitManager is null` no longer appears in `logs/latest.log`. Then resume Test 8."
  debug_session: ""

- truth: "Recruits hotkey screens (Command/Faction/Map) and the claim overlay open in dev client"
  status: failed
  reason: "User reported in dev-client on 2026-04-15 (after the 21-11 fix landed): pressing R, U, or M does nothing — Command, Faction, and Map screens never open. No errors in `logs/latest.log`. Hire GUI and recruit inventory still open correctly (those are server-driven via `NetworkHooks.openScreen`, not hotkey-driven)."
  severity: blocker
  test: 8
  root_cause: "Same Phase-21 consolidation defect class as the 21-11 fix, but for client-only handler classes. Three classes in the consolidated tree carry `@SubscribeEvent` methods AND lack `@Mod.EventBusSubscriber` auto-registration AND are never manually registered on `MinecraftForge.EVENT_BUS` inside `BannerModMain.clientSetup(FMLClientSetupEvent)`. Specifically: `client.military.events.KeyEvents` owns `onKeyInput(InputEvent.Key)` which reads `ModShortcuts.COMMAND_SCREEN_KEY/TEAM_SCREEN_KEY/MAP_SCREEN_KEY` and calls `CommandEvents.openCommandScreen` / `Minecraft.setScreen(new FactionMainScreen|WorldMapScreen)` — without registration, the keys are bound but no listener acts on them. `client.military.events.ClientPlayerEvents` owns `ClientTickEvent` + `LevelEvent.Load/Unload` handlers used for client-side state init. `client.military.gui.overlay.ClaimOverlayManager` owns `ClientTickEvent`, `RenderGuiOverlayEvent.Post`, and `ClientPlayerNetworkEvent.LoggingIn/LoggingOut` handlers that drive the on-screen claim overlay. Audit confirmed all other `@SubscribeEvent` classes under `client/` are either manually registered (`ScreenEvents` at BannerModMain.java:133) or auto-registered via `@Mod.EventBusSubscriber` (`civilian/ClientEvent`, `military/events/ClientEvent`, `military/events/ClientSyncLifecycleEvents`)."
  artifacts:
    - path: "src/main/java/com/talhanation/bannermod/bootstrap/BannerModMain.java"
      issue: "clientSetup(FMLClientSetupEvent) at lines 122-134 registers ScreenEvents but does not register KeyEvents, ClientPlayerEvents, or ClaimOverlayManager. Missing: `MinecraftForge.EVENT_BUS.register(new KeyEvents())`, `MinecraftForge.EVENT_BUS.register(new ClientPlayerEvents())`, `MinecraftForge.EVENT_BUS.register(new ClaimOverlayManager())`."
    - path: "src/main/java/com/talhanation/bannermod/client/military/events/KeyEvents.java"
      issue: "Reference only — class has no @Mod.EventBusSubscriber annotation. Lines 22-43 (`onKeyInput`) and 44-66 (`onPlayerPick`) require manual EVENT_BUS registration to fire."
    - path: "src/main/java/com/talhanation/bannermod/client/military/events/ClientPlayerEvents.java"
      issue: "Reference only — class has no @Mod.EventBusSubscriber annotation. `onClientTick` / `onWorldLoad` / `onWorldUnload` handlers require manual registration."
    - path: "src/main/java/com/talhanation/bannermod/client/military/gui/overlay/ClaimOverlayManager.java"
      issue: "Reference only — class has no @Mod.EventBusSubscriber annotation. Tick/render/login handlers require manual registration. Constructor at line 43 takes no args."
  missing:
    - "In `BannerModMain.clientSetup(FMLClientSetupEvent)`, alongside the existing `MinecraftForge.EVENT_BUS.register(new ScreenEvents())` call (line 133), add: `MinecraftForge.EVENT_BUS.register(new KeyEvents())`, `MinecraftForge.EVENT_BUS.register(new ClientPlayerEvents())`, `MinecraftForge.EVENT_BUS.register(new ClaimOverlayManager())`."
    - "Add the three corresponding imports (`com.talhanation.bannermod.client.military.events.KeyEvents`, `...ClientPlayerEvents`, `...gui.overlay.ClaimOverlayManager`)."
    - "After fix: `./gradlew runClient`, open world, press R / U / M → Command / Faction / Map screen opens respectively. Then resume Test 8 (issue a recruit command from the Command screen → confirm round-trip)."
  debug_session: ""

- truth: "All UI strings (gui.recruits.*, key.recruits.*, category.recruits, chat.recruits.*, description.recruits.*, subtitles.recruits.*, recruits.*) render translated text instead of raw translation keys"
  status: failed
  reason: "User reported in dev-client on 2026-04-15: Hire GUI (and other recruits-side UI) shows raw translation keys like `gui.recruits.hire_gui.text.hire` instead of localized labels. Test 7 was originally marked `pass` based on entity / item / block lang sample only — those keys WERE migrated to bannermod namespace in Wave 9. UI keys were not exercised in test 7 because earlier blockers prevented opening any GUI."
  severity: major
  test: 7
  root_cause: "Phase 21 (Wave 9) consolidated lang files under `src/main/resources/assets/bannermod/lang/` for entity / item / block keys (renamed from `<modid>.namespace.*` to `bannermod.namespace.*`), but the recruits-side UI keys were not carried over. The legacy `recruits/src/main/resources/assets/recruits/lang/<locale>.json` (390 keys per locale in en_us) still contains all `gui.recruits.*`, `key.recruits.*`, `category.recruits`, `chat.recruits.*`, `description.recruits.*`, `subtitles.recruits.*`, `recruits.*`, and `gui.multiLineEditBox.*` entries. Code references these keys verbatim — e.g., `RecruitHireScreen.java:34` calls `Component.translatable(\"gui.recruits.hire_gui.text.hire\")` — and Phase 21 deliberately did NOT rename them in code (the consolidation pivot was build/source-tree, not lang-key, surface). Without the keys present in the bannermod-namespaced lang file, Minecraft falls back to rendering the raw key string. Affects all six locales: en_us, ru_ru, de_de, es_es, ja_jp, tr_tr (note: legacy recruits has no es_es; bannermod has it for entity strings)."
  artifacts:
    - path: "src/main/resources/assets/bannermod/lang/en_us.json"
      issue: "Contains 0 keys matching `gui.recruits.*` / `key.recruits.*` / `category.recruits` / `chat.recruits.*` / `description.recruits.*` / `subtitles.recruits.*` / `recruits.*` / `gui.multiLineEditBox.*`. Same gap in ru_ru.json, de_de.json, es_es.json, ja_jp.json, tr_tr.json."
    - path: "recruits/src/main/resources/assets/recruits/lang/en_us.json"
      issue: "Source of truth for the missing keys. Contains 390 keys; counterparts in de_de/ja_jp/ru_ru/tr_tr (~545 lines each) hold the localized values. No es_es file in legacy recruits — Spanish UI strings will need to fall back to en_us until translations are sourced."
  missing:
    - "Mechanically merge each `recruits/src/main/resources/assets/recruits/lang/<locale>.json` into the matching `src/main/resources/assets/bannermod/lang/<locale>.json`. Copy every key whose first dotted segment is one of: `gui`, `key`, `category`, `chat`, `description`, `subtitles`, `recruits`, OR whose key starts with `gui.multiLineEditBox.`. Skip any key already present in the target file (do NOT clobber existing bannermod entries from Wave 9)."
    - "For es_es.json: copy from en_us.json subset as a fallback (no Spanish source in legacy recruits)."
    - "Skip entity/item/block/spawn-egg keys from the legacy file — those were already migrated to the `entity.bannermod.*` / `item.bannermod.*` / `block.bannermod.*` namespace in Wave 9 and the legacy keys are no longer referenced by code."
    - "After fix: `./gradlew runClient`, open Hire GUI, confirm hire button label renders as 'Hire' (en_us) / localized text (other locales), not as `gui.recruits.hire_gui.text.hire`. Sweep at least one screen per category: Hire, Inventory, Command, Faction, Map."
  debug_session: ""
