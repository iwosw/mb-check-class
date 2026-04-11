# Workers Registry Namespace Merge Slice Summary

- Unified Workers deferred registers and `WorkersRuntime.id()/modId()` onto the active `bannermod` mod namespace.
- Added registry-id fallback for legacy `workers:*` structure entity references so existing scans/templates still resolve after the namespace move.
- Routed worker spawn-egg item models into `assets/bannermod/models/**`, added active `entity.bannermod.*` / `item.bannermod.*` lang entries, and verified `./gradlew compileJava processResources test` passes from the root.

## Changed Files

- `build.gradle`
- `workers/src/main/java/com/talhanation/workers/WorkersRuntime.java`
- `workers/src/main/java/com/talhanation/workers/entities/workarea/BuildArea.java`
- `workers/src/main/java/com/talhanation/workers/entities/ai/BuilderWorkGoal.java`
- `workers/src/main/java/com/talhanation/workers/client/gui/StorageAreaScreen.java`
- `workers/src/main/java/com/talhanation/workers/client/gui/MarketAreaScreen.java`
- `recruits/src/main/resources/assets/bannermod/lang/en_us.json`
- `recruits/src/main/resources/assets/bannermod/lang/de_de.json`
- `recruits/src/main/resources/assets/bannermod/lang/es_es.json`
- `recruits/src/main/resources/assets/bannermod/lang/ru_ru.json`
- `recruits/src/main/resources/assets/bannermod/lang/tr_tr.json`
- `MERGE_NOTES.md`
- `.planning/ROADMAP.md`
- `.planning/REQUIREMENTS.md`
- `.planning/STATE.md`

## Verification

- `./gradlew compileJava processResources test` ✅

## Residual Risks

- Existing worlds/NBT that persist raw `workers:*` registry ids outside the structure placement path may still need explicit migration handling.
- Legacy `assets/workers/**` resources are still present as a preservation layer even though active registry ownership is now `bannermod`.
- No git repository is initialized at workspace root, so task commits could not be created for this execution.
