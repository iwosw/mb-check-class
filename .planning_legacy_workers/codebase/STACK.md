# Technology Stack

**Analysis Date:** 2026-04-05

## Languages

**Primary:**
- Java 17 - Main mod code under `src/main/java/com/talhanation/workers/`; Java 17 is enforced in `build.gradle` and `src/main/resources/mixins.workers.json`.

**Secondary:**
- Groovy DSL - Gradle build logic in `build.gradle` and `settings.gradle`.
- TOML - Forge mod metadata in `src/main/resources/META-INF/mods.toml`.
- JSON - Update metadata in `update.json`, mixin config in `src/main/resources/mixins.workers.json`, and game assets under `src/main/resources/assets/workers/`.
- CFG - Access transformer rules in `src/main/resources/META-INF/accesstransformer.cfg`.

## Runtime

**Environment:**
- Minecraft Forge mod runtime for Minecraft 1.20.1 via `net.minecraftforge:forge:1.20.1-47.4.1` declared in `build.gradle`.
- Java 17 toolchain configured in `build.gradle`.

**Package Manager:**
- Gradle 8.1.1 via the wrapper in `gradle/wrapper/gradle-wrapper.properties`.
- Lockfile: missing; dependency resolution is driven by `build.gradle` and the Gradle wrapper.

## Frameworks

**Core:**
- Minecraft Forge / JavaFML 47.x - Mod loader, registries, config, event bus, and networking; see `build.gradle`, `src/main/resources/META-INF/mods.toml`, and `src/main/java/com/talhanation/workers/WorkersMain.java`.
- Sponge Mixin / Mixingradle - Mixin processing is enabled in `build.gradle` and configured in `src/main/resources/mixins.workers.json`.

**Testing:**
- Not detected. No JUnit, Mockito, GameTest source set, or dedicated test configuration files were found.

**Build/Dev:**
- ForgeGradle 6.x - Minecraft/Forge build pipeline in `build.gradle`.
- Shadow 7.1.0 - Shades and relocates CoreLib in `build.gradle`.
- CurseGradle 1.4.0 - Publishing-oriented Gradle plugin declared in `build.gradle`.
- Foojay toolchain resolver 0.5.0 - Java toolchain resolution in `settings.gradle`.

## Key Dependencies

**Critical:**
- `net.minecraftforge:forge:1.20.1-47.4.1` - Core modding API and runtime; wired through `build.gradle` and used throughout `src/main/java/com/talhanation/workers/`.
- `de.maxhenkel.corelib:corelib:1.20.1-1.1.3` - Provides common registry, networking, containers, and screens; used in `src/main/java/com/talhanation/workers/WorkersMain.java`, `src/main/java/com/talhanation/workers/network/*.java`, and `src/main/java/com/talhanation/workers/inventory/*.java`.
- `curse.maven:recruits-523860:7374573` - Hard dependency for worker entities, pathfinding, GUI widgets, and player/group systems; required in `src/main/resources/META-INF/mods.toml` and imported across files such as `src/main/java/com/talhanation/workers/entities/AbstractWorkerEntity.java` and `src/main/java/com/talhanation/workers/client/gui/WorkAreaScreen.java`.

**Infrastructure:**
- `org.spongepowered:mixin:0.8.5:processor` - Annotation processor configured in `build.gradle`.
- `curse.maven:architectury-api-419699:5137938` - Declared in `build.gradle`; direct source imports were not detected.
- `curse.maven:lets-do-herbal-brews-951221:6458889` - Compatibility item IDs appear in `src/main/java/com/talhanation/workers/config/WorkersServerConfig.java`.
- `curse.maven:supplementaries-412082:6615104` and `curse.maven:selene-499980:6681465` - Compatibility dependencies declared in `build.gradle`; `supplementaries:flax` is referenced in `src/main/java/com/talhanation/workers/config/WorkersServerConfig.java`.
- `curse.maven:farmers-delight-398521:6597298` - Declared in `build.gradle`; direct source imports were not detected.
- `curse.maven:worldedit-225608:4586218` - Declared in `build.gradle`; direct source imports were not detected.
- `curse.maven:debug-utils-forge-783008:5337491` - Declared in `build.gradle`; direct source imports were not detected.

## Configuration

**Environment:**
- Runtime configuration is file-based, not environment-variable-based. Server config is registered in `src/main/java/com/talhanation/workers/WorkersMain.java` and defined with `ForgeConfigSpec` in `src/main/java/com/talhanation/workers/config/WorkersServerConfig.java`.
- Mod metadata and remote update metadata live in `src/main/resources/META-INF/mods.toml` and `update.json`.
- No `.env`, `.env.*`, `.nvmrc`, `tsconfig.json`, or `.python-version` files were detected at the repository root.

**Build:**
- `build.gradle` - dependencies, repositories, run configs, shading, mixins, and Java toolchain.
- `settings.gradle` - plugin repositories and Foojay toolchain resolver.
- `gradle.properties` - Minecraft/Forge version properties and template metadata values.
- `gradle/wrapper/gradle-wrapper.properties` - fixed Gradle distribution version.
- `src/main/resources/META-INF/mods.toml` - mod ID, version, update feed URL, and mandatory dependency metadata.
- `src/main/resources/META-INF/accesstransformer.cfg` - access transformation rules for the Forge runtime.
- `src/main/resources/mixins.workers.json` - mixin configuration; currently enabled with empty `mixins` and `client` arrays.

## Platform Requirements

**Development:**
- Java 17 and Gradle wrapper support are required to build and run dev tasks from `gradlew`.
- A Forge 1.20.1 development environment is configured in `build.gradle` with `client`, `server`, and `data` run targets.
- The mod expects the Recruits mod at runtime because `src/main/resources/META-INF/mods.toml` marks it as a mandatory dependency.

**Production:**
- Deployment target is a Forge mod JAR for Minecraft 1.20.1, published as `workers-1.20.1` per `build.gradle` and identified as mod `workers` in `src/main/resources/META-INF/mods.toml`.

---

*Stack analysis: 2026-04-05*
