---
status: complete
phase: 21-source-tree-consolidation-into-bannerlord
source: [21-01-SUMMARY.md, 21-02-SUMMARY.md, 21-03-SUMMARY.md, 21-04-SUMMARY.md, 21-05-SUMMARY.md, 21-06-SUMMARY.md, 21-07-SUMMARY.md, 21-08-SUMMARY.md, 21-09-SUMMARY.md, 21-10-SUMMARY.md, 21-11-SUMMARY.md, 21-12-SUMMARY.md, 21-13-SUMMARY.md]
started: 2026-04-15T20:00:00Z
updated: 2026-04-15T15:20:00Z
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
resolution: "Fixed by 21-10-PLAN.md — switched BannerModMain registerConfig calls to the 3-arg overload with explicit filenames (bannermod-recruits-client.toml, bannermod-recruits-server.toml, bannermod-workers-server.toml). Follow-up fix 14e7684 deferred `RecruitsClientConfig.RecruitsLookLikeVillagers.get()` out of renderer registration so the client reaches the main menu and world load path cleanly."

### 3. Recruit entity spawns under bannermod namespace
expected: In dev client with a creative world open, `/summon bannermod:recruit ~ ~ ~` spawns a recruit entity with correct textures and model. Legacy `/summon recruits:recruit` also resolves (via legacy-id migration) or is cleanly rejected — not a silent crash.
result: pass

### 4. Worker entity spawns under bannermod namespace
expected: `/summon bannermod:miner ~ ~ ~` (or any civilian entity migrated from workers) spawns with correct textures and model. No `minecraft:missing` texture, no log errors about unregistered `workers:*` IDs.
result: pass

### 5. POI acquirable_job_site binds villagers to recruit sites
expected: Place a recruit job site block near a villager in dev client. Villager walks to it and claims the site as their profession POI.
result: pass

### 6. Legacy save migration — workers:/recruits: IDs rewritten on load
expected: Load an existing world created against the pre-Phase-21 build (with `workers:*` / `recruits:*` entity IDs persisted). Entities rehydrate without loss; `WorkersRuntime.migrateLegacyId` rewrites stored IDs to `bannermod:*` on first save. No "unknown entity type" warnings flood the log.
result: pass

### 7. Lang, assets, and recipes resolve
expected: In-game tooltips, GUI labels, and item names for recruit & worker content display translated text (not raw keys like `item.bannermod.recruit_contract`). No `minecraft:missing` textures on any migrated entity, item, or block. Recipes involving recruit/worker items show up in the recipe book as expected.
result: pass
resolution: "Fixed by 21-13-PLAN.md — recruits-side UI lang keys were merged into the active `assets/bannermod/lang/*.json` files. User confirmed in dev-client that Hire / Command / Faction surfaces now render localized labels instead of raw `gui.recruits.*` keys."

### 8. Network packet round-trip — recruit command + work area update
expected: Issue a recruit command from the GUI (e.g., follow/hold position) and place/edit a worker work area. Both round-trip through the unified `bannermod:network` channel without packet-ID collisions (military offset 0, civilian offset 104 per 21-08 SUMMARY). No `Unknown custom packet identifier` warnings.
result: deferred
resolution: "The UI-side blockers that originally prevented this test are closed: 21-11 restored the server-side recruit event handlers, 21-12 restored the Command / Faction / Map hotkeys plus missing command categories, and 21-13 restored the recruits UI localization needed to operate those screens sanely. The full end-to-end packet round-trip itself was not re-run in this UAT file after those fixes landed, so the test is no longer blocked but remains optional follow-up verification rather than a Phase 21 closure gate."
severity: info

## Summary

total: 8
passed: 7
issues: 0
pending: 1
skipped: 0
blocked: 0

## Gaps

- truth: "Mod loads in dev client under single modId='bannermod' without mod-loading errors"
  status: closed
  closed_by: "21-10-PLAN.md"
  severity: blocker
  test: 2
  root_cause: "BannerModMain initially registered two SERVER configs with the default `<modid>-server.toml` filename, which caused Forge's config tracker to reject the second registration as a collision."
  resolution: "21-10 switched to explicit config filenames and the follow-up client-config timing fix removed the downstream startup crash."
  debug_session: ""

- truth: "Client reaches main menu in dev client without ForgeConfigSpec timing crash"
  status: closed
  closed_by: "21-10-PLAN.md (follow-up fix commit 14e7684)"
  severity: blocker
  test: 2
  root_cause: "Client renderer registration read a Forge config value before the CLIENT spec was loaded."
  resolution: "The config read moved out of renderer registration time so the main menu and world load path complete normally."
  debug_session: ""

- truth: "Right-clicking a recruit opens the Hire GUI without server-side crash"
  status: closed
  closed_by: "21-11-PLAN.md"
  severity: blocker
  test: 8
  root_cause: "Recruits-side event handlers were not registered on the Forge event bus after consolidation, so manager initialization never ran before recruit interaction."
  resolution: "21-11 restored the missing server-side handler registrations in `BannerModMain.setup(FMLCommonSetupEvent)` and the Hire GUI now opens without the `recruitsPlayerUnitManager is null` crash."
  debug_session: ""

- truth: "Recruits hotkey screens (Command/Faction/Map) and the claim overlay open in dev client"
  status: closed
  closed_by: "21-12-PLAN.md"
  severity: blocker
  test: 8
  root_cause: "Client-only handler classes that are not auto-registered lost their manual EVENT_BUS registration during the consolidation, leaving hotkeys and the claim overlay inert."
  resolution: "21-12 restored `KeyEvents`, `ClientPlayerEvents`, and `ClaimOverlayManager` registration in `BannerModMain.clientSetup(FMLClientSetupEvent)`, and the follow-up fix restored the missing command categories."
  debug_session: ""

- truth: "All UI strings (gui.recruits.*, key.recruits.*, category.recruits, chat.recruits.*, description.recruits.*, subtitles.recruits.*, recruits.*) render translated text instead of raw translation keys"
  status: closed
  closed_by: "21-13-PLAN.md"
  severity: major
  test: 7
  root_cause: "Wave 9 consolidated entity/item/block translations but initially omitted the recruits-side UI translation families that code still referenced verbatim."
  resolution: "21-13 merged the missing UI key families into the active BannerMod language files for all shipped locales, with `es_es` seeded from `en_us` as fallback."
  debug_session: ""
