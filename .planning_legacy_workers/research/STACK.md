# Technology Stack

**Project:** Villager Workers Revival
**Researched:** 2026-04-05

## Recommended Stack

### Core Framework
| Technology | Version | Purpose | Why |
|------------|---------|---------|-----|
| Minecraft Forge | `1.21.1-52.1.14` or latest stable `52.1.x` | Loader, registries, events, networking, GameTest runtime | This is the official 1.21.1 Forge line and the direct target requested. Use a fresh 1.21.1 MDK baseline instead of mutating the legacy 1.20.1 build in place. **Confidence: HIGH** |
| ForgeGradle | `7.0.3+` (from 1.21.1 MDK) | Build pipeline, mappings, run configs, reobf | The official 1.21.1 MDK moved to FG 7 and a simpler plugins DSL. Porting on top of that removes old buildscript/plugin debt immediately. **Confidence: HIGH** |
| Java | `21` | Compile/runtime target | Forge 1.21.x docs require Java 21; staying on Java 17 will create avoidable build/runtime issues. **Confidence: HIGH** |
| Mojang official mappings | `1.21.1` | Names for porting and maintenance | Official mappings are the default and least risky path for a brownfield Forge port. Keep the code readable and aligned with current docs/primer references. **Confidence: HIGH** |

### Database
| Technology | Version | Purpose | Why |
|------------|---------|---------|-----|
| Minecraft world save data (`NBT` + vanilla/Forge codecs where practical) | 1.21.1 runtime | Persist worker/work-area/entity state | This mod is world-state-driven, not app-database-driven. Keep persistent gameplay state in entity/block/world save data; do not add an external database. **Confidence: HIGH** |

### Infrastructure
| Technology | Version | Purpose | Why |
|------------|---------|---------|-----|
| Gradle Wrapper | Use wrapper shipped by fresh Forge 1.21.1 MDK | Reproducible builds | The current repo is still on an older wrapper/build style. Resetting to the MDK wrapper avoids invisible tooling drift. **Confidence: HIGH** |
| Foojay toolchain resolver | `1.0.0` | Reliable Java 21 toolchain provisioning | This is what the current 1.21.1 MDK ships with; keep it instead of the older `0.5.0` setup. **Confidence: HIGH** |
| Forge Data Generator (`runData`) | 1.21.1 runtime | Generate/update data assets and catch path/schema drift | 1.21 changed several data folder names to singular forms; datagen should be part of the port baseline, not an afterthought. **Confidence: HIGH** |
| Dedicated server + GameTest server runs | `runServer`, `runGameTestServer` | Server-authoritative validation | Automation-heavy villager logic must be validated where authority actually lives. Forge explicitly recommends dedicated-server testing. **Confidence: HIGH** |

### Supporting Libraries
| Library | Version | Purpose | When to Use |
|---------|---------|---------|-------------|
| `de.maxhenkel.corelib:corelib` | `1.21.1-2.1.11` or latest compatible `1.21.1` release | Preserve existing menu/network/screen abstractions with minimal churn | Keep it if the 1.21.1 API remains close enough to current usage; it already has 1.21.1 artifacts, so it is the lowest-risk path. **Confidence: HIGH** |
| Recruits (Forge) | **Must be a 1.21.1-compatible build; public Modrinth currently shows no 1.21.1 release** | Mandatory upstream dependency for AI/pathing/group systems already embedded in the mod design | Treat this as the gating dependency. Either obtain/port a 1.21.1 Forge build first or maintain a fork during the Workers port. Shipping Workers on Forge 1.21.1 is blocked until this exists. **Confidence: MEDIUM** |
| JUnit Jupiter | `5.14.x` stable | Fast pure-JVM regression tests | Use for deterministic code only: area math, serialization helpers, inventory rules, item filtering, config parsing, template parsing. Do not use it to fake full world/entity behavior. **Confidence: MEDIUM** |
| Forge GameTest | Forge 1.21.1 built-in | In-game integration tests | Use for worker task loops, work-area validation, packet-triggered server mutations, inventory deposit/withdraw flows, and structure/template behaviors that need a real level. **Confidence: HIGH** |

## Recommended 1.21.1 Migration Approach

1. **Rebase the build onto a fresh Forge 1.21.1 MDK, not the current `build.gradle`.**
   - Replace the old `buildscript {}` / `apply plugin` layout with the official 1.21.1 MDK plugin DSL.
   - Move to Java 21, ForgeGradle 7, current run configs, and the MDK `gradle.properties` defaults.
   - Then copy over only the mod-specific pieces: source, resources, access transformer, generated resources, and needed repositories/dependencies.

2. **Port dependencies before porting gameplay code.**
   - `corelib` is available for 1.21.1, so keep it.
   - `recruits` is the real blocker. Public release pages still expose Forge builds through 1.20.1, and Modrinth API returns no 1.21.1 version. If Recruits is not ported, Workers cannot be shipped on Forge 1.21.1.

3. **Keep the existing architecture; update APIs, not concepts.**
   - Preserve entity-centric worker logic, packet-per-action flows, and work-area entities.
   - Port vanilla/Forge API breaks in place rather than redesigning around new abstractions.

4. **Target the high-probability 1.21 breakpoints first.**
   - `ResourceLocation` factory migration (`parse`, `withDefaultNamespace`, `fromNamespaceAndPath`).
   - Datapack/resource folder singularization (`recipe`, `loot_table`, `structure`, `tags/item`, etc.).
   - Rendering API churn for custom area/entity rendering and overlays.
   - Attribute modifier identifiers now using `ResourceLocation`.
   - Any villager trade or server-only offer access paths, because 1.21 tightened some logical-side expectations.

5. **Make server validation part of the default build workflow.**
   - `./gradlew test` for pure-JVM tests.
   - `./gradlew runGameTestServer` for in-world automation tests.
   - `./gradlew runServer` for dedicated-server smoke validation of login, entity spawn, UI-triggered packets, work-area creation, and long-tick worker loops.

## Alternatives Considered

| Category | Recommended | Alternative | Why Not |
|----------|-------------|-------------|---------|
| Build migration | Fresh 1.21.1 MDK baseline | Edit the legacy 1.20.1 build in place | The current build carries outdated FG wiring, old wrapper assumptions, `jcenter`, old Foojay config, Shadow, CurseGradle, and Mixin plumbing. Porting on that base adds avoidable failure surface. |
| Loader target | Forge 1.21.1 | NeoForge or dual-loader rewrite | The milestone explicitly targets Forge 1.21.1, and the mod has a hard dependency chain already tied to Forge-era dependencies. Dual-loader work is scope creep. |
| Dependency strategy | Keep `corelib`, gate on `recruits` 1.21.1 | Rewrite away from both during the same milestone | That turns a port/revival into a redesign. Only replace a dependency if the 1.21.1-compatible artifact truly does not exist. |
| Packaging | Plain Forge dependency graph | Shadowing libraries by default | Shadow is not part of the official 1.21.1 MDK path and increases remap/reobf/debug complexity. Keep it only if a specific private library truly must be embedded. |
| Injection tooling | No mixins unless a concrete 1.21.1 need appears | Keep current Mixin setup | The repo currently has Mixin build plumbing but no actual `@Mixin` classes. Empty Mixin infrastructure is port debt, not value. |
| Compat dependencies | Add back only proven-needed compat mods | Keep all current CurseMaven compat deps during first port pass | Optional compat mods muddy dependency resolution and dedicated-server debugging. First ship a stable Workers + Recruits baseline; reintroduce compat later. |
| Testing style | JUnit + Forge GameTest + dedicated-server smoke tests | Mockito-heavy unit tests only | Mock-heavy tests will miss tick timing, side separation, entity state, and packet authority bugs—the exact risks for this mod. |

## Testing Strategy

### What to test with JUnit
- Work-area bounds/overlap math
- Serialization/deserialization helpers
- Needed-item and inventory decision logic
- Structure/template parsing helpers that do not require a live level
- Config validation/defaulting

### What to test with Forge GameTest
- Creating each work-area type from packet-triggered/server-authoritative flows
- Worker assignment and persistence across save/load
- Deposit/withdraw logic against containers
- Profession loops with controlled fixtures (crop growth, animals present, storage present, missing tools/food)
- Structure/template placement and failure handling

### Dedicated-server validation checklist
- Server boots with dependency set resolved
- Client can join dedicated server without classloading/client-only crashes
- Hiring worker, assigning area, opening GUI, and applying updates works over packets
- Core worker professions tick correctly for several in-game days
- Save/restart preserves workers, areas, and inventories

## What NOT to Use

- **Do not keep Java 17.** Forge 1.21.x expects Java 21.
- **Do not keep `jcenter()` in the build.** It is obsolete and unnecessary for the target stack.
- **Do not carry forward CurseGradle as part of the port baseline.** Add publishing automation later, after the mod builds and tests cleanly.
- **Do not keep Shadow unless you confirm a real jar-embedding need.** It is extra complexity for no clear current benefit.
- **Do not keep empty Mixin setup.** Remove it unless the 1.21.1 port proves a genuine need.
- **Do not re-add optional compat mods during the first green build.** They are noise until the core Forge 1.21.1 + Recruits stack is stable.
- **Do not rely on singleplayer-only verification.** This mod is server-authoritative by design.

## Installation / Bootstrap

```bash
# 1) Start from fresh Forge 1.21.1 MDK
# 2) Reapply mod sources/resources on top of it

# Core verification tasks
./gradlew build
./gradlew test
./gradlew runGameTestServer
./gradlew runServer
```

## Sources

- Forge 1.21.x Getting Started — Java 21, run tasks, dedicated-server testing: https://docs.minecraftforge.net/en/1.21.x/gettingstarted/ (**HIGH**)
- Forge 1.21.x GameTest docs — registration and `runGameTestServer`: https://docs.minecraftforge.net/en/1.21.x/misc/gametest/ (**HIGH**)
- Forge 1.21.x Data Generation docs: https://docs.minecraftforge.net/en/1.21.x/datagen/ (**HIGH**)
- Forge 1.21 porting page: https://docs.minecraftforge.net/en/latest/legacy/porting/ (**HIGH**)
- Official Forge 1.21.1 downloads / recommended MDK coordinates: https://files.minecraftforge.net/net/minecraftforge/forge/index_1.21.1.html (**HIGH**)
- Official Forge Maven metadata — confirms 1.21.1 `52.x` line: https://maven.minecraftforge.net/net/minecraftforge/forge/maven-metadata.xml (**HIGH**)
- Official ForgeGradle Maven metadata — current FG 7-era stack supersedes this repo’s old setup: https://maven.minecraftforge.net/net/minecraftforge/gradle/ForgeGradle/maven-metadata.xml (**HIGH**)
- Official 1.21.1 MDK build files inspected from MDK zip: https://maven.minecraftforge.net/net/minecraftforge/forge/1.21.1-52.1.14/forge-1.21.1-52.1.14-mdk.zip (**HIGH**)
- ChampionAsh5357 1.20.6 -> 1.21 migration primer: https://gist.github.com/ChampionAsh5357/d895a7b1a34341e19c80870720f9880f (**MEDIUM** — community primer linked from Forge docs)
- CoreLib Maven metadata — confirms 1.21.1 artifacts exist: https://maven.maxhenkel.de/repository/public/de/maxhenkel/corelib/corelib/maven-metadata.xml (**HIGH**)
- Villager Workers Modrinth versions API — still only public 1.20.1 release line: https://api.modrinth.com/v2/project/villager-workers/version (**MEDIUM**)
- Villager Recruits Modrinth page — public compatibility page lists up to 1.20.1: https://modrinth.com/mod/villager-recruits (**MEDIUM**)
- Villager Recruits Modrinth versions API checked for `1.21.1` and returned zero matches on 2026-04-05 (**MEDIUM**)
- Gradle JVM testing guide — JUnit Platform setup: https://docs.gradle.org/current/userguide/java_testing.html (**HIGH**)
- JUnit Jupiter Maven metadata — current stable line available for Gradle test setup: https://repo1.maven.org/maven2/org/junit/jupiter/junit-jupiter/maven-metadata.xml (**MEDIUM**)
