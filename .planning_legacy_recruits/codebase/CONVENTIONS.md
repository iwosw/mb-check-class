# Coding Conventions

**Analysis Date:** 2026-04-05

## Naming Patterns

**Files:**
- Use PascalCase class-per-file names in `src/main/java/com/talhanation/recruits/**`, such as `src/main/java/com/talhanation/recruits/Main.java`, `src/main/java/com/talhanation/recruits/entities/RecruitEntity.java`, and `src/main/java/com/talhanation/recruits/client/gui/faction/FactionInspectionScreen.java`.
- Prefix registry/bootstrap files with `Mod` in `src/main/java/com/talhanation/recruits/init/`, for example `src/main/java/com/talhanation/recruits/init/ModBlocks.java` and `src/main/java/com/talhanation/recruits/init/ModScreens.java`.
- Prefix network message classes with `Message` in `src/main/java/com/talhanation/recruits/network/`, for example `src/main/java/com/talhanation/recruits/network/MessageWriteSpawnEgg.java` and `src/main/java/com/talhanation/recruits/network/MessageToClientUpdateClaim.java`.
- Keep package names lowercase and domain-oriented, such as `com.talhanation.recruits.entities`, `com.talhanation.recruits.client.gui.faction`, and `com.talhanation.recruits.pathfinding`.

**Functions:**
- Use lowerCamelCase method names, for example `registerMenus()` in `src/main/java/com/talhanation/recruits/init/ModScreens.java`, `fillRecruitsInfo()` in `src/main/java/com/talhanation/recruits/network/MessageWriteSpawnEgg.java`, and `awaitProcessing()` in `src/main/java/com/talhanation/recruits/pathfinding/AsyncPathProcessor.java`.
- Name boolean predicates as verbs or state checks: `wantsToPickUp()`, `canHoldItem()`, `isLoaded()`, `isGun()`, and `canMelee()` in `src/main/java/com/talhanation/recruits/entities/RecruitEntity.java` and `src/main/java/com/talhanation/recruits/compat/MusketWeapon.java`.
- Use event-handler names that state the trigger, such as `onRegisterCommands()` in `src/main/java/com/talhanation/recruits/Main.java` and `entityRenderersEvent()` in `src/main/java/com/talhanation/recruits/client/events/ClientEvent.java`.

**Variables:**
- Use lowerCamelCase for fields and locals, such as `modEventBus` in `src/main/java/com/talhanation/recruits/Main.java`, `workerId` in `src/main/java/com/talhanation/recruits/init/ModScreens.java`, and `buttonY` in `src/main/java/com/talhanation/recruits/client/gui/faction/FactionInspectionScreen.java`.
- Use `UPPER_SNAKE_CASE` for constants and registry singletons, such as `MOD_ID` and `LOGGER` in `src/main/java/com/talhanation/recruits/Main.java`, `BLOCKS` and `RECRUIT_BLOCK` in `src/main/java/com/talhanation/recruits/init/ModBlocks.java`, and `CLAIM_BUTTON` in `src/main/java/com/talhanation/recruits/client/gui/faction/FactionInspectionScreen.java`.
- Expect occasional inconsistent local naming in brownfield code, such as `player_uuid` in `src/main/java/com/talhanation/recruits/init/ModScreens.java`; prefer the dominant lowerCamelCase style for new code.

**Types:**
- Use PascalCase for classes, interfaces, nested event types, and enums, such as `RecruitEntity`, `AsyncPathProcessor`, `RecruitEvent.Hired`, and `RecruitsRoute.WaypointAction.Type` in `src/main/java/com/talhanation/recruits/entities/RecruitEntity.java`, `src/main/java/com/talhanation/recruits/pathfinding/AsyncPathProcessor.java`, `src/main/java/com/talhanation/recruits/events/RecruitEvent.java`, and `src/main/java/com/talhanation/recruits/world/RecruitsRoute.java`.

## Code Style

**Formatting:**
- No formatter configuration is detected at the project root: no `.editorconfig`, Checkstyle, Spotless, or formatter XML files are present under `/home/kaiserroman/recruits`.
- Follow the repository’s de facto Java style from `src/main/java/com/talhanation/recruits/Main.java` and `src/main/java/com/talhanation/recruits/entities/RecruitEntity.java`: 4-space indentation, opening braces on the same line, and chained calls split across lines.
- Keep short guard clauses inline when that matches nearby code, for example `if (executor == null || executor.isShutdown()) return;` in `src/main/java/com/talhanation/recruits/pathfinding/AsyncPathProcessor.java`.
- Use multiline lambdas for Forge/Gradle registration code, as seen in `src/main/java/com/talhanation/recruits/init/ModBlocks.java` and `src/main/java/com/talhanation/recruits/init/ModScreens.java`.

**Linting:**
- No linting tool or lint config is detected in `/home/kaiserroman/recruits`; `build.gradle` does not configure Checkstyle, Spotless, PMD, or Error Prone.
- Preserve existing style manually when editing files like `src/main/java/com/talhanation/recruits/commands/RecruitsAdminCommands.java` and `src/main/java/com/talhanation/recruits/client/gui/faction/FactionInspectionScreen.java`.

## Import Organization

**Order:**
1. Package declaration first in every file, for example `package com.talhanation.recruits...;` in `src/main/java/com/talhanation/recruits/Main.java` and `src/main/java/com/talhanation/recruits/client/events/ClientEvent.java`.
2. Regular imports next, usually grouped loosely by project classes, Minecraft/Forge APIs, and third-party libraries, as seen in `src/main/java/com/talhanation/recruits/Main.java` and `src/main/java/com/talhanation/recruits/client/gui/faction/FactionInspectionScreen.java`.
3. Static imports last when used, for example `import static com.talhanation.recruits.util.RegistryUtils.createSpawnEggItem;` in `src/main/java/com/talhanation/recruits/init/ModItems.java` and `import static com.talhanation.recruits.Main.SIMPLE_CHANNEL;` in `src/main/java/com/talhanation/recruits/network/MessageTransferRoute.java`.

**Path Aliases:**
- Not applicable in this Java codebase; imports use full package names such as `com.talhanation.recruits.network.*` in `src/main/java/com/talhanation/recruits/Main.java` and `net.minecraft.*` / `net.minecraftforge.*` types throughout `src/main/java/com/talhanation/recruits/**`.

## Error Handling

**Patterns:**
- Use guard clauses and sentinel returns (`null`, `false`, `0`) instead of custom exception hierarchies. Examples: `return null;` in `src/main/java/com/talhanation/recruits/init/ModScreens.java`, `return false;` in `src/main/java/com/talhanation/recruits/compat/MusketWeapon.java`, and `return 0;` in `src/main/java/com/talhanation/recruits/commands/RecruitsAdminCommands.java`.
- Wrap Forge GUI/container construction in `try/catch` and log before returning `null`, as done repeatedly in `src/main/java/com/talhanation/recruits/init/ModScreens.java`.
- For compatibility/reflection code, catch specific reflection exceptions and degrade gracefully, as in `src/main/java/com/talhanation/recruits/compat/MusketWeapon.java`.
- Throw `IllegalArgumentException` for invalid enum/index state instead of silent coercion in logic-heavy entity classes, as indicated by matches in `src/main/java/com/talhanation/recruits/entities/ScoutEntity.java`, `src/main/java/com/talhanation/recruits/entities/MessengerEntity.java`, and `src/main/java/com/talhanation/recruits/entities/AbstractLeaderEntity.java`.
- Swallow exceptions only in narrowly defensive code paths, for example `catch (Exception ignored) {}` in `src/main/java/com/talhanation/recruits/world/RecruitsRoute.java`.

## Logging

**Framework:** Log4j via `org.apache.logging.log4j.Logger`.

**Patterns:**
- Use the shared mod logger `Main.LOGGER` for cross-cutting code in `src/main/java/com/talhanation/recruits/Main.java`, `src/main/java/com/talhanation/recruits/pathfinding/AsyncPathProcessor.java`, and `src/main/java/com/talhanation/recruits/compat/MusketWeapon.java`.
- Use file-local `logger` instances in registry/bootstrap classes such as `src/main/java/com/talhanation/recruits/init/ModScreens.java` and `src/main/java/com/talhanation/recruits/init/ModPois.java`.
- Log operational milestones with `info`, degraded behavior with `warn`, and failures with `error`; examples include `logger.info("MenuScreens registered")` in `src/main/java/com/talhanation/recruits/init/ModScreens.java` and `Main.LOGGER.warn("AsyncPathProcessor shutdown interrupted")` in `src/main/java/com/talhanation/recruits/pathfinding/AsyncPathProcessor.java`.
- Match the existing string-heavy style when editing older files, but prefer parameterized logging where a file already uses it, such as `Main.LOGGER.error("No node evaluator generator present for Mob {}", p_77429_);` in `src/main/java/com/talhanation/recruits/pathfinding/AsyncPathfinder.java`.

## Comments

**When to Comment:**
- Use inline comments to label sections or explain Forge-specific intent, such as `//ATTRIBUTES` in `src/main/java/com/talhanation/recruits/entities/RecruitEntity.java`, `//COMPANIONS` in `src/main/java/com/talhanation/recruits/client/events/ClientEvent.java`, and `//TeamCommand` in `src/main/java/com/talhanation/recruits/commands/RecruitsAdminCommands.java`.
- Keep existing TODO comments in place when they document gaps, for example in `src/main/java/com/talhanation/recruits/client/gui/faction/FactionInspectionScreen.java` and `src/main/java/com/talhanation/recruits/Main.java`.

**JSDoc/TSDoc:**
- Not applicable.
- JavaDoc is used selectively for API-like event classes and helper methods. Follow the style in `src/main/java/com/talhanation/recruits/events/RecruitEvent.java` and the method comment above `registerMenu()` in `src/main/java/com/talhanation/recruits/init/ModScreens.java` when documenting extension points or non-obvious behavior.

## Function Design

**Size:**
- Expect large orchestration methods in bootstrap and command files, such as `setup()` in `src/main/java/com/talhanation/recruits/Main.java` and `register()` in `src/main/java/com/talhanation/recruits/commands/RecruitsAdminCommands.java`.
- Prefer small focused methods for entity overrides and utilities when extending existing types, as in `src/main/java/com/talhanation/recruits/entities/RecruitEntity.java` and `src/main/java/com/talhanation/recruits/pathfinding/AsyncPathProcessor.java`.

**Parameters:**
- Pass explicit Minecraft/Forge types rather than wrapper DTOs, for example `FriendlyByteBuf`, `NetworkEvent.Context`, `ServerPlayer`, and `Player` in `src/main/java/com/talhanation/recruits/network/MessageWriteSpawnEgg.java` and `src/main/java/com/talhanation/recruits/init/ModScreens.java`.
- Use nullable annotations where null is part of the contract, as shown by `@Nullable` usage in `src/main/java/com/talhanation/recruits/events/RecruitEvent.java`, `src/main/java/com/talhanation/recruits/world/RecruitsRoute.java`, and `src/main/java/com/talhanation/recruits/client/events/ClientEvent.java`.

**Return Values:**
- Return concrete domain objects when lookup succeeds and `null` when the surrounding Forge API expects nullable construction or lookup, as in `getRecruitByUUID()` in `src/main/java/com/talhanation/recruits/init/ModScreens.java` and `getByResourceLocation()` in `src/main/java/com/talhanation/recruits/world/RecruitsHireTradesRegistry.java`.
- Use fluent/chained builders for registration and config assembly, as in `src/main/java/com/talhanation/recruits/init/ModBlocks.java` and `src/main/java/com/talhanation/recruits/config/RecruitsServerConfig.java`.

## Module Design

**Exports:**
- Use one public top-level class per file under `src/main/java/com/talhanation/recruits/**`.
- Favor static registries and utility-style modules for global game hooks, such as `src/main/java/com/talhanation/recruits/init/ModBlocks.java`, `src/main/java/com/talhanation/recruits/world/RecruitsHireTradesRegistry.java`, and `src/main/java/com/talhanation/recruits/pathfinding/AsyncPathProcessor.java`.
- Use nested public event types to model related extension points in a single file, as in `src/main/java/com/talhanation/recruits/events/RecruitEvent.java`.

**Barrel Files:**
- Not used. There are no Java barrel/export aggregator files beyond normal package organization and wildcard imports such as `com.talhanation.recruits.init.*` in `src/main/java/com/talhanation/recruits/Main.java`.

---

*Convention analysis: 2026-04-05*
