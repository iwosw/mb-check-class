---
status: diagnosed
phase: 21-source-tree-consolidation-into-bannerlord
source: [21-01-SUMMARY.md, 21-02-SUMMARY.md, 21-03-SUMMARY.md, 21-04-SUMMARY.md, 21-05-SUMMARY.md, 21-06-SUMMARY.md, 21-07-SUMMARY.md, 21-08-SUMMARY.md, 21-09-SUMMARY.md]
started: 2026-04-15T20:00:00Z
updated: 2026-04-15T20:25:00Z
---

## Current Test

[testing paused — blocker in test 2; 6 tests deferred until fix lands]

## Tests

### 1. Cold Start Smoke Test — compileJava from clean
expected: Run `./gradlew --stop && ./gradlew clean compileJava` from the outer repo root. Build completes `BUILD SUCCESSFUL`, `:compileJava` resolves only outer `src/main/java/com/talhanation/bannermod/**` sources, and no clone source roots (`recruits/src/**`, `workers/src/**`) are pulled in.
result: pass

### 2. Mod loads in dev client under single modId="bannermod"
expected: Run `./gradlew runClient` (or equivalent). Game launches, mods list shows exactly one entry `bannermod` (not `recruits`, `workers`, or `bannerlord`). No ClassNotFoundException / duplicate-class errors in log for `com.talhanation.{bannermod,recruits,workers,bannerlord}.*`.
result: issue
reported: "Mod loading crash during ./gradlew runClient. `java.lang.RuntimeException: Config conflict detected!` thrown from `ConfigTracker.trackConfig(ConfigTracker.java:40)` via `BannerModMain.<init>(BannerModMain.java:55)` → `ModLoadingContext.registerConfig(ModLoadingContext.java:115)`. Crash report UUID 74db50b8-1d90-4fb9-be6f-a85658792338, ./crash-reports/crash-2026-04-15_19.08.23-fml.txt. Mod List shows single `bannermod` entry (good — no duplicate modId) but state is ERROR."
severity: blocker

### 3. Recruit entity spawns under bannermod namespace
expected: In dev client with a creative world open, `/summon bannermod:recruit ~ ~ ~` spawns a recruit entity with correct textures and model. Legacy `/summon recruits:recruit` also resolves (via legacy-id migration) or is cleanly rejected — not a silent crash.
result: [pending]

### 4. Worker entity spawns under bannermod namespace
expected: `/summon bannermod:miner ~ ~ ~` (or any civilian entity migrated from workers) spawns with correct textures and model. No `minecraft:missing` texture, no log errors about unregistered `workers:*` IDs.
result: [pending]

### 5. POI acquirable_job_site binds villagers to recruit sites
expected: Place a recruit job site block near a villager in dev client. Villager walks to it and claims the site as their profession POI (i.e., the POI tag `acquirable_job_site` resolves against the live `bannermod:*` entity registrations after the Wave-9 namespace rewrite — see 21-VERIFICATION.md Human Verification item 1).
result: [pending]

### 6. Legacy save migration — workers:/recruits: IDs rewritten on load
expected: Load an existing world created against the pre-Phase-21 build (with `workers:*` / `recruits:*` entity IDs persisted). Entities rehydrate without loss; `WorkersRuntime.migrateLegacyId` rewrites stored IDs to `bannermod:*` on first save. No "unknown entity type" warnings flood the log.
result: [pending]

### 7. Lang, assets, and recipes resolve
expected: In-game tooltips, GUI labels, and item names for recruit & worker content display translated text (not raw keys like `item.bannermod.recruit_contract`). No `minecraft:missing` textures on any migrated entity, item, or block. Recipes involving recruit/worker items show up in the recipe book as expected.
result: [pending]

### 8. Network packet round-trip — recruit command + work area update
expected: Issue a recruit command from the GUI (e.g., follow/hold position) and place/edit a worker work area. Both round-trip through the unified `bannermod:network` channel without packet-ID collisions (military offset 0, civilian offset 104 per 21-08 SUMMARY). No `Unknown custom packet identifier` warnings.
result: [pending]

## Summary

total: 8
passed: 1
issues: 1
pending: 6
skipped: 0
blocked: 0

## Gaps

- truth: "Mod loads in dev client under single modId='bannermod' without mod-loading errors"
  status: failed
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
