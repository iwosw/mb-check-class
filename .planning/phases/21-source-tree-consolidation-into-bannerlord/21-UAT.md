---
status: complete
phase: 21-source-tree-consolidation-into-bannerlord
source: [21-01-SUMMARY.md, 21-02-SUMMARY.md, 21-03-SUMMARY.md, 21-04-SUMMARY.md, 21-05-SUMMARY.md, 21-06-SUMMARY.md, 21-07-SUMMARY.md, 21-08-SUMMARY.md, 21-09-SUMMARY.md, 21-10-SUMMARY.md]
started: 2026-04-15T20:00:00Z
updated: 2026-04-15T14:00:00Z
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
result: issue
reported: "NullPointerException: Cannot invoke RecruitsPlayerUnitManager.canPlayerRecruit because RecruitEvents.recruitsPlayerUnitManager is null — thrown from AbstractRecruitEntity.openHireGUI(AbstractRecruitEntity.java:2403) via mobInteract on right-click to hire recruit."
severity: blocker

## Summary

total: 8
passed: 7
issues: 1
pending: 0
skipped: 0
blocked: 0

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
  status: failed
  reason: "User reported: NullPointerException — `Cannot invoke \"com.talhanation.bannermod.persistence.military.RecruitsPlayerUnitManager.canPlayerRecruit(String, java.util.UUID)\" because \"com.talhanation.bannermod.events.RecruitEvents.recruitsPlayerUnitManager\" is null`. Stack: AbstractRecruitEntity.openHireGUI(AbstractRecruitEntity.java:2403) ← mobInteract(AbstractRecruitEntity.java:1607) ← Mob.interact ← ServerboundInteractPacket. Crash fires on server when player right-clicks a recruit to hire. Discovered while attempting Test 8 (network packet round-trip — recruit command GUI path)."
  severity: blocker
  test: 8
  artifacts: []
  missing: []
  debug_session: ""
