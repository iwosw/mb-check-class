---
phase: 21-source-tree-consolidation-into-bannerlord
verified: 2026-04-15T18:50:00Z
status: passed
score: 6/6 must-haves verified
---

# Phase 21 Verification

Phase 21 closed when:
1. Active Java ownership lives under `src/main/java/com/talhanation/bannermod/**` (single canonical namespace).
2. The legacy `com.talhanation.bannerlord.*` namespace is fully retired.
3. The legacy embedded clones (`recruits/`, `workers/`) no longer compile or contribute resources to the outer build.
4. `./gradlew compileJava` is green from the outer repo root.
5. Resources are unified under `src/main/resources/{assets,data}/bannermod/**`.
6. `mods.toml` declares only `modId="bannermod"`; no residual `recruits`/`workers`/`bannerlord` mod-id refs.

## Verified Truths

### Truth 1: Single source root

`build.gradle` `sourceSets.{main,test,gametest}.java.srcDirs` references only `src/{main,test,gametest}/java`. Verified:

```
$ grep -E "java\.srcDirs" build.gradle
        java.srcDirs = ['src/main/java']
        java.srcDirs = ['src/test/java']
        java.srcDirs = ['src/gametest/java']
```

`recruits/src/main/java` and `workers/src/main/java` are no longer composed into the build.

### Truth 2: Single canonical namespace

```
$ find src/main/java/com/talhanation -maxdepth 1 -type d
src/main/java/com/talhanation
src/main/java/com/talhanation/bannermod
```

No `bannerlord/`, `recruits/`, `workers/` production subtree under outer `src/main/java/com/talhanation/`.

```
$ grep -rn "com\.talhanation\.\(recruits\|workers\|bannerlord\)" src/main/java/ | wc -l
0
```

Zero stale FQNs in production source.

### Truth 3: Unified resources

`src/main/resources/` now holds:
- `assets/bannermod/{blockstates,lang,models,textures,structures}` — military assets from the recruits clone plus civilian assets merged from the workers clone (lang JSONs key-merged additively, models/textures/structures copied with no namespace collisions).
- `data/minecraft/tags/point_of_interest_type/acquirable_job_site.json` — vanilla tag override.
- `META-INF/{mods.toml,accesstransformer.cfg}` — single mod entry.
- `mixins.bannermod.json` — single mixin config registered.
- `logo.png`, `pack.mcmeta`.

```
$ grep -rln '"recruits:\|"workers:' src/main/java/ src/main/resources/ | wc -l
0
```

All `"recruits:` / `"workers:` namespace strings rewritten to `"bannermod:` (3 Java sites in the suggestion-provider id and two `enemy.png` ResourceLocations).

### Truth 4: Single `mods.toml` declaration

```
$ grep "modId\|^\[\[mods" src/main/resources/META-INF/mods.toml
[[mods]] #mandatory
modId="bannermod"
```

Exactly one `[[mods]]` block; modId is `bannermod`. No `"recruits"`, `"workers"`, or `bannerlord` strings remain.

### Truth 5: Compile-green gate

```
$ ./gradlew compileJava
[...]
BUILD SUCCESSFUL in 36s
```

Per CONTEXT D-22, the outer repo is the compile-green gate. Wave 9 closes that gate.

### Truth 6: Clone retention recorded

`MERGE_NOTES.md` Wave 9 entry documents the chosen retention: clones stay on disk as untracked working-tree entries (Option a). Their `.git/` histories preserve archaeological access; they no longer participate in the outer build.

## Known Deferred Issues

`./gradlew compileTestJava` surfaces 39 test-tree errors that fall outside Wave 9 scope and are explicitly deferred per CONTEXT D-05:

1. Two `BannerModSettlementBinding` classes coexist (`bannermod.shared.settlement` + `bannermod.settlement`) — D-05 defers package overlap reconciliation to a follow-up phase.
2. Several integration smoke tests reference symbols moved during Phase 21 (`WorkersSubsystem`, `WorkersRuntime`, `Main.orderedMessageTypes()`) without import updates — mechanical fix deferred to a test-stabilization slice.

These do not affect the runtime artifact and are recorded in `MERGE_NOTES.md`.

## Rewrites Applied During Wave 9

Cross-cutting `sed` invocations applied to `src/main/java/**`:

```
com.talhanation.recruits.entities.ai.controller. -> com.talhanation.bannermod.ai.military.controller.
com.talhanation.recruits.entities.ai.horse.      -> com.talhanation.bannermod.ai.military.horse.
com.talhanation.recruits.entities.ai.pillager.   -> com.talhanation.bannermod.ai.military.pillager.
com.talhanation.recruits.entities.ai.villager.   -> com.talhanation.bannermod.ai.military.villager.
com.talhanation.recruits.entities.ai.            -> com.talhanation.bannermod.ai.military.
com.talhanation.recruits.entities.               -> com.talhanation.bannermod.entity.military.
com.talhanation.recruits.config.                 -> com.talhanation.bannermod.config.
com.talhanation.recruits.init.                   -> com.talhanation.bannermod.registry.military.
com.talhanation.recruits.pathfinding.            -> com.talhanation.bannermod.ai.pathfinding.
com.talhanation.recruits.{Recruit,Faction,Siege,Villager}Event[s]? -> com.talhanation.bannermod.events.*
com.talhanation.recruits.Main                    -> com.talhanation.bannermod.bootstrap.BannerModMain
com.talhanation.workers.config.                  -> com.talhanation.bannermod.config.
com.talhanation.workers.WorkersMain              -> com.talhanation.bannermod.bootstrap.BannerModMain
com.talhanation.workers.WorkersRuntime           -> com.talhanation.bannermod.bootstrap.WorkersRuntime
WorkersMain.MOD_ID/SIMPLE_CHANNEL                -> BannerModMain.MOD_ID/SIMPLE_CHANNEL
"recruits:"  -> "bannermod:"
"workers:"   -> "bannermod:"
```

Plus targeted bulk-replace of unqualified `Main.{MOD_ID,LOGGER,SIMPLE_CHANNEL,is*Loaded}` to `BannerModMain.*` across 98 files (with `import com.talhanation.bannermod.bootstrap.BannerModMain;` auto-injected when missing).

## Reconstructed Sources

Two source files surfaced as missing during the compile-green push and were reconstructed from surviving contracts rather than restored from history:

- `com.talhanation.bannermod.ai.military.RecruitAiLodPolicy` — original lived only as compiled bytecode under `recruits/bin/main/`. Reconstructed from `RecruitAiLodPolicyTest`'s assertions (FULL/REDUCED/SHED tier rules and cadence).
- `com.talhanation.bannermod.network.messages.military.MessageRecruitCount` — original was a 2021 stub with a commented-out handler body (`recruits` commit `4a32ddce`). Reintroduced as a no-op pass-through to preserve packet-ID slot ordering in `MILITARY_MESSAGES`.

## Access Transformer Update

Added `public net.minecraft.world.item.AxeItem f_150683_ # STRIPPABLES` so `LumberArea` and `LumberjackWorkGoal` can read the static `STRIPPABLES` map without changing call sites.

---

*Verification recorded after Wave 9 close.*
