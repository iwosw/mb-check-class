# Technology Stack

**Analysis Date:** 2026-04-11

## Languages

**Primary:**
- Java 17 - All runtime gameplay, Forge bootstrap, networking, configs, persistence, and tests live in `build.gradle`, `recruits/src/main/java/**`, `workers/src/main/java/**`, `src/test/java/**`, and `recruits/src/gametest/java/**`.

**Secondary:**
- Gradle Groovy DSL - Build orchestration, dependency resolution, source-set merging, shading, and Forge run profiles are defined in `build.gradle` and `settings.gradle`.
- TOML - Forge metadata and generated runtime config files are driven by `recruits/src/main/resources/META-INF/mods.toml`, `recruits/src/main/java/com/talhanation/recruits/config/RecruitsClientConfig.java`, `recruits/src/main/java/com/talhanation/recruits/config/RecruitsServerConfig.java`, and `workers/src/main/java/com/talhanation/workers/config/WorkersServerConfig.java`.
- JSON - Mixin configs and update metadata live in `recruits/src/main/resources/mixins.recruits.json`, `workers/src/main/resources/mixins.workers.json`, and `recruits/update.json`.
- CFG - Access transformer rules are merged through `src/main/resources/META-INF/accesstransformer.cfg`, with source slices in `recruits/src/main/resources/META-INF/accesstransformer.cfg` and `workers/src/main/resources/META-INF/accesstransformer.cfg`.

## Runtime

**Environment:**
- JVM / Java 17 - enforced by `build.gradle` line 45 via `java.toolchain.languageVersion = JavaLanguageVersion.of(17)`.
- Minecraft 1.20.1 - pinned in `gradle.properties` and `build.gradle`.
- Minecraft Forge 47.4.10 - declared in `build.gradle` line 156 and versioned in `gradle.properties`.

**Package Manager:**
- Gradle - root build uses `build.gradle`, `settings.gradle`, `gradlew`, and `gradlew.bat`.
- Version: Not pinned in a root `gradle/wrapper/gradle-wrapper.properties`; the closest checked-in wrapper version is Gradle 8.8 in `recruits/gradle/wrapper/gradle-wrapper.properties`.
- Lockfile: missing.

## Frameworks

**Core:**
- Minecraft Forge / JavaFML 47.4.10 - mod loader, event bus, registries, config system, run targets, and game-test integration in `build.gradle`, `gradle.properties`, `recruits/src/main/java/com/talhanation/recruits/Main.java`, and `workers/src/main/java/com/talhanation/workers/init/WorkersLifecycleRegistrar.java`.
- Sponge Mixin - runtime patching configured in `build.gradle`, `recruits/src/main/resources/mixins.recruits.json`, and `workers/src/main/resources/mixins.workers.json`.

**Testing:**
- JUnit 5.10.2 - unit-style tests configured in `build.gradle` lines 162-164 and used in `src/test/java/com/talhanation/workers/WorkersRuntimeSmokeTest.java` plus `recruits/src/test/java/**`.
- Forge GameTest - dedicated `gametest` source set and `runGameTestServer` stage defined in `build.gradle` lines 61-73, 111-124, and 218-221; active tests are in `recruits/src/gametest/java/**`.

**Build/Dev:**
- ForgeGradle 6.0.52 - Forge workspace, mappings, reobf, and run profiles in `build.gradle` lines 8-10 and 19.
- Shadow 7.1.0 - shaded artifact build and CoreLib relocation in `build.gradle` lines 16, 21, and 192-199.
- CurseGradle 1.4.0 - publishing plugin declared in `build.gradle` line 15.
- Foojay Toolchain Resolver 0.7.0 - Java toolchain resolution in `settings.gradle` lines 12-14.

## Key Dependencies

**Critical:**
- `net.minecraftforge:forge:1.20.1-47.4.10` - base modding runtime for the whole merged mod in `build.gradle` line 156.
- `de.maxhenkel.corelib:corelib:1.20.1-1.1.3` - shared networking, screen, and registry helpers used by `recruits/src/main/java/com/talhanation/recruits/Main.java` and `workers/src/main/java/com/talhanation/workers/network/WorkersNetworkRegistrar.java`.
- `org.spongepowered:mixin:0.8.5:processor` - mixin annotation processing in `build.gradle` line 166.

**Infrastructure:**
- `curse.maven:corpse-316582:5157034` - optional corpse compatibility surfaced in `recruits/src/main/java/com/talhanation/recruits/Main.java` and `recruits/src/main/java/com/talhanation/recruits/compat/Corpse.java`.
- `curse.maven:epic-knights-armor-and-weapons-509041:5254836` - optional equipment compatibility toggled in `recruits/src/main/java/com/talhanation/recruits/Main.java`.
- `curse.maven:architectury-api-419699:5137938` and `curse.maven:cloth-config-348521:4973441` - declared support libraries in `build.gradle` lines 170-171.
- `curse.maven:ewewukeks-musket-mod-354562:5779561` - optional ranged-weapon compatibility implemented reflectively in `recruits/src/main/java/com/talhanation/recruits/compat/MusketWeapon.java`, `MusketBayonetWeapon.java`, `MusketScopeWeapon.java`, `BlunderbussWeapon.java`, and `PistolWeapon.java`.
- `curse.maven:worldedit-225608:4586218` - bundled dev/runtime dependency in `build.gradle` line 173.
- `curse.maven:small-ships-450659:5566900` - optional ship compatibility checked in `recruits/src/main/java/com/talhanation/recruits/Main.java` and implemented in `recruits/src/main/java/com/talhanation/recruits/compat/SmallShips.java`.
- `curse.maven:lets-do-herbal-brews-951221:6458889`, `curse.maven:supplementaries-412082:6615104`, `curse.maven:selene-499980:6681465`, `curse.maven:debug-utils-forge-783008:5337491`, and `curse.maven:farmers-delight-398521:6597298` - declared in `build.gradle` lines 176-180; item IDs from Supplementaries and Herbal Brews are hard-coded in `workers/src/main/java/com/talhanation/workers/config/WorkersServerConfig.java`.

## Configuration

**Environment:**
- Environment variables are not used by the checked-in runtime/build code. No `.env*` files were detected at `/home/kaiserroman/bannermod`.
- Recruits server and client config specs are registered in `recruits/src/main/java/com/talhanation/recruits/init/ModLifecycleRegistrar.java` and implemented in `recruits/src/main/java/com/talhanation/recruits/config/RecruitsServerConfig.java` plus `recruits/src/main/java/com/talhanation/recruits/config/RecruitsClientConfig.java`.
- Workers server config is registered in `workers/src/main/java/com/talhanation/workers/init/WorkersLifecycleRegistrar.java` and implemented in `workers/src/main/java/com/talhanation/workers/config/WorkersServerConfig.java`.
- Recruits client config is explicitly loaded from Forge's config directory into `bannermod-client.toml` in `recruits/src/main/java/com/talhanation/recruits/init/ModLifecycleRegistrar.java` line 34.

**Build:**
- Root merged build composes source from `src/main/java`, `recruits/src/main/java`, and `workers/src/main/java` in `build.gradle` lines 47-67.
- Resource merging excludes workers legacy metadata and remaps workers assets into the `bannermod` namespace in `build.gradle` lines 233-252.
- Root mod metadata is driven by `gradle.properties` and `recruits/src/main/resources/META-INF/mods.toml`.
- Mixin configs are `recruits/src/main/resources/mixins.recruits.json` and `workers/src/main/resources/mixins.workers.json`.
- Access transformer input for the merged runtime is `src/main/resources/META-INF/accesstransformer.cfg`.

## Platform Requirements

**Development:**
- Java 17.
- Gradle/Forge development environment capable of resolving repositories declared in `build.gradle` and `settings.gradle`: Forge Maven, Sponge Maven, Maven Central, Max Henkel Maven, Cursemaven, and optionally `mavenLocal()` when `allowLocalMaven` is set.
- Forge run targets `client`, `server`, `gameTestServer`, and `data` are defined in `build.gradle` lines 81-139.

**Production:**
- Deployment target is a single Forge mod JAR for Minecraft 1.20.1 with merged mod id `bannermod`, versioned by `gradle.properties` and `recruits/src/main/resources/META-INF/mods.toml`.
- Workers functionality ships as a subsystem inside the merged runtime via `workers/src/main/java/com/talhanation/workers/WorkersSubsystem.java` and `workers/src/main/java/com/talhanation/workers/WorkersRuntime.java` rather than as a separate root mod entrypoint.

---

*Stack analysis: 2026-04-11*
