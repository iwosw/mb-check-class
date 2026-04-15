---
phase: 21-source-tree-consolidation-into-bannerlord
plan: 10
type: execute
gap_closure: true
closed_gaps:
  - truth: "Mod loads in dev client under single modId='bannermod' without mod-loading errors"
    test: 2
    outcome: partial-pass
    note: "Original `Config conflict detected!` crash signature is verifiably gone. A separate downstream bug (ClientEvent.entityRenderersEvent reading RecruitsClientConfig before config load) now blocks the main-menu step of verification and is filed as a NEW gap in 21-UAT.md."
completed: 2026-04-15T13:05Z
---

## Summary

Gap closure for Phase 21 UAT test 2 blocker — the `Config conflict detected!` crash thrown by `ConfigTracker.trackConfig` during `BannerModMain.<init>`. Fix verified; a pre-existing latent downstream defect surfaced and was filed as a separate gap.

## What changed

### `src/main/java/com/talhanation/bannermod/bootstrap/BannerModMain.java`

Before (2-arg overload — defaults both SERVER configs to `bannermod-server.toml`):

```java
ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, RecruitsClientConfig.CLIENT);
ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, RecruitsServerConfig.SERVER);
ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, WorkersServerConfig.SERVER);
```

After (3-arg overload — explicit, distinct filenames):

```java
ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, RecruitsClientConfig.CLIENT, "bannermod-recruits-client.toml");
ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, RecruitsServerConfig.SERVER, "bannermod-recruits-server.toml");
ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, WorkersServerConfig.SERVER, "bannermod-workers-server.toml");
```

Rationale for filename choices:
- `bannermod-` prefix keeps the single-modId identity.
- `-recruits-` / `-workers-` distinguish subsystems for operators (matches pre-pivot separation).
- `-client.toml` / `-server.toml` suffixes make `ModConfig.Type` self-documenting.

### `MERGE_NOTES.md`

Appended a "Config filename migration (Phase 21 post-UAT, 2026-04-15)" section documenting:
- The three new filenames and their pre-pivot equivalents.
- Operator impact (fresh defaults written; no auto-migration).
- Manual migration steps for preserving existing settings.
- Rationale for deferring a read-through shim to a follow-up plan.

### `.planning/phases/21-source-tree-consolidation-into-bannerlord/21-UAT.md`

- Frontmatter: `status: diagnosed` → `partial`; `updated` → `2026-04-15T13:05:04Z`; `21-10-SUMMARY.md` added to `source`.
- Test 2: `result: issue` → `result: pass` with explicit resolution referencing 21-10 and noting the new downstream gap.
- Summary: `passed: 1 → 2`, `issues: 1 → 0`, `pending: 6` unchanged.
- Existing gap (test 2, Config conflict): `status: failed` → `closed`, `closed_by: "21-10-PLAN.md"` added.
- New gap filed: "Client reaches main menu in dev client without ForgeConfigSpec timing crash" (test 2, blocker severity) — captures the `IllegalStateException` at `ClientEvent.java:34` with root cause, artifacts, and suggested fix paths.

## Runtime confirmation (Task 3)

- `./gradlew compileJava`: BUILD SUCCESSFUL.
- `./gradlew runClient` on 2026-04-15T13:05Z:
  - ✅ No `java.lang.RuntimeException: Config conflict detected!`
  - ✅ No `ConfigTracker.trackConfig` in any crash frame
  - ✅ No `BannerModMain.<init>(BannerModMain.java:55)` in any crash frame
  - ✅ Single `bannermod` entry present in Mod List (state: `COMMON_SET` at time of downstream crash — no duplicate modId)
  - ❌ Main menu did NOT load (new crash — see below)
  - ❌ Three `bannermod-*.toml` config files NOT yet written to `run/config/` (the downstream crash aborted before CLIENT config writeback completed; registration itself succeeded)

## Deviation from plan

Task 3 was accepted as **partial-pass** by the user rather than full "approved". The registerConfig collision (the specific blocker 21-10 targeted) is resolved, but main-menu load could not be reached because of an unrelated pre-existing bug surfaced by getting further into the client lifecycle. Per user direction, Task 4 still executed — test 2 flipped to pass on the original crash signature, and the new crash is filed as a fresh gap for a follow-up plan.

## New gap for follow-up

```
truth: "Client reaches main menu in dev client without ForgeConfigSpec timing crash"
severity: blocker
test:    2
where:   src/main/java/com/talhanation/bannermod/client/civilian/events/ClientEvent.java:34
cause:   `RecruitsClientConfig.RecruitsLookLikeVillagers.get()` invoked during
         `EntityRenderersEvent.RegisterRenderers` — before CLIENT ModConfig
         is loaded. Forge's `ForgeConfigSpec.ConfigValue.get()` throws
         `IllegalStateException: Cannot get config value before config is loaded`.
fix:     Defer the villager/human renderer branch out of RegisterRenderers — read
         the config value lazily at render time, or rebind renderers in
         `FMLClientSetupEvent.enqueueWork` / `ModConfigEvent.Loading`.
```

## Unblocked by this fix

UAT tests 3–8 remain pending — they all require a running dev client past the main menu, which is still blocked by the new gap. The follow-up plan that fixes `ClientEvent.java:34` will re-unblock them.

## Pointers

- `src/main/java/com/talhanation/bannermod/bootstrap/BannerModMain.java:48-58`
- `MERGE_NOTES.md` — "Config filename migration (Phase 21 post-UAT, 2026-04-15)"
- `.planning/phases/21-source-tree-consolidation-into-bannerlord/21-UAT.md` — test 2 + Gaps section
- Crash report for follow-up: `run/crash-reports/crash-2026-04-15_19.26.49-client.txt`
