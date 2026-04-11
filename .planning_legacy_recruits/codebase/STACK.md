# Technology Stack

**Analysis Date:** 2026-04-05

## Languages

**Primary:**
- Java 17 - Main gameplay, AI, networking, config, and persistence code in `src/main/java/com/talhanation/recruits/Main.java`, `src/main/java/com/talhanation/recruits/entities/`, `src/main/java/com/talhanation/recruits/network/`, and `src/main/java/com/talhanation/recruits/world/`.

**Secondary:**
- Groovy DSL (Gradle build scripts) - Build and dependency management in `build.gradle` and `settings.gradle`.
- TOML - Forge mod metadata and generated config schemas in `src/main/resources/META-INF/mods.toml`, `src/main/java/com/talhanation/recruits/config/RecruitsClientConfig.java`, and `src/main/java/com/talhanation/recruits/config/RecruitsServerConfig.java`.
- JSON - Update metadata and game resource definitions in `update.json`, `src/main/resources/mixins.recruits.json`, and `src/main/resources/assets/recruits/**`.

## Runtime

**Environment:**
- JVM / Java 17 via Gradle toolchains in `build.gradle`.
- Minecraft 1.20.1 + Minecraft Forge 47.4.10 in `build.gradle` and `gradle.properties`.

**Package Manager:**
- Gradle via wrapper.
- Wrapper distribution: Gradle 8.8 in `gradle/wrapper/gradle-wrapper.properties`.
- Lockfile: missing.

## Frameworks

**Core:**
- Minecraft Forge 47.4.10 - Mod runtime, event bus, registries, config, commands, and networking in `build.gradle` and `src/main/java/com/talhanation/recruits/Main.java`.
- Sponge Mixin / MixinGradle - Runtime patching and mixin processing in `build.gradle`, `src/main/resources/mixins.recruits.json`, and `src/main/java/com/talhanation/recruits/mixin/*.java`.
- Max Henkel CoreLib 1.20.1-1.1.3 - Shared registry and message helpers used by `src/main/java/com/talhanation/recruits/Main.java` and `src/main/java/com/talhanation/recruits/network/*.java`.

**Testing:**
- Not detected. No `src/test/`, JUnit dependency, or dedicated test runner config was found under `/home/kaiserroman/recruits`.

**Build/Dev:**
- ForgeGradle 6.+ - Forge development workspace and run profiles in `build.gradle`.
- Shadow 7.1.0 - Shaded artifact creation and relocation of CoreLib in `build.gradle`.
- CurseGradle 1.4.0 - CurseForge publishing plugin declared in `build.gradle`.
- Foojay Toolchain Resolver 0.7.0 - Java toolchain resolution in `settings.gradle`.

## Key Dependencies

**Critical:**
- `net.minecraftforge:forge:1.20.1-47.4.10` - Base game modding platform and API in `build.gradle`.
- `de.maxhenkel.corelib:corelib:1.20.1-1.1.3` - Provides `CommonRegistry.registerChannel()` and message helpers used in `src/main/java/com/talhanation/recruits/Main.java`.
- `org.spongepowered:mixin:0.8.5:processor` - Required for mixin annotation processing in `build.gradle`.
- `com.electronwill.nightconfig` transitively via Forge - Used for TOML config loading in `src/main/java/com/talhanation/recruits/config/RecruitsClientConfig.java` and `src/main/java/com/talhanation/recruits/config/RecruitsServerConfig.java`.

**Infrastructure:**
- `curse.maven:corpse-316582:5157034` - Optional corpse integration used by `src/main/java/com/talhanation/recruits/compat/Corpse.java`.
- `curse.maven:epic-knights-armor-and-weapons-509041:5254836` - Optional combat equipment compatibility checked in `src/main/java/com/talhanation/recruits/Main.java`.
- `curse.maven:architectury-api-419699:5137938` and `curse.maven:cloth-config-348521:4973441` - UI/config support dependencies declared in `build.gradle`.
- `curse.maven:ewewukeks-musket-mod-354562:5779561` - Optional ranged weapon compatibility implemented in `src/main/java/com/talhanation/recruits/compat/MusketWeapon.java` and related files.
- `curse.maven:worldedit-225608:4586218` - Runtime dependency declared in `build.gradle`.
- `curse.maven:small-ships-450659:5566900` - Optional ship compatibility implemented in `src/main/java/com/talhanation/recruits/compat/SmallShips.java`.

## Configuration

**Environment:**
- Environment variables are not used. No `.env*` files were detected in `/home/kaiserroman/recruits`.
- Runtime configuration uses Forge `ModConfig` registration in `src/main/java/com/talhanation/recruits/Main.java`.
- Client config is loaded from the standard Forge config directory using `FMLPaths.CONFIGDIR` in `src/main/java/com/talhanation/recruits/Main.java` and `src/main/java/com/talhanation/recruits/config/RecruitsClientConfig.java`.
- Server config schema is defined in `src/main/java/com/talhanation/recruits/config/RecruitsServerConfig.java` and registered in `src/main/java/com/talhanation/recruits/Main.java`.

**Build:**
- Primary build file: `build.gradle`.
- Toolchain/plugin configuration: `settings.gradle`.
- Version and mod metadata properties: `gradle.properties`.
- Mod metadata: `src/main/resources/META-INF/mods.toml`.
- Gradle wrapper bootstrap: `gradle/wrapper/gradle-wrapper.properties`.

## Platform Requirements

**Development:**
- Java 17.
- Gradle wrapper (`./gradlew`) with network access to Forge, Sponge, Maven Central, Max Henkel, and CurseMaven repositories configured in `build.gradle` and `settings.gradle`.
- Minecraft Forge 1.20.1 development environment from the run configurations in `build.gradle` (`client`, `server`, and `data`).

**Production:**
- Deployment target is a Forge mod JAR for Minecraft 1.20.1, packaged as `recruits-1.20.1` in `build.gradle`.
- Runtime distribution targets mod marketplaces referenced by `src/main/resources/META-INF/mods.toml`, `update.json`, and `README.md`.

---

*Stack analysis: 2026-04-05*
