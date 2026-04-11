# Coding Conventions

**Analysis Date:** 2026-04-05

## Naming Patterns

**Files:**
- Use PascalCase for Java types, with one top-level type per file under `src/main/java/com/talhanation/workers/**`; examples: `src/main/java/com/talhanation/workers/WorkersMain.java`, `src/main/java/com/talhanation/workers/entities/FarmerEntity.java`, `src/main/java/com/talhanation/workers/client/gui/WorkAreaScreen.java`.
- Use descriptive suffixes by concern: `*Entity.java` in `src/main/java/com/talhanation/workers/entities/`, `*WorkGoal.java` in `src/main/java/com/talhanation/workers/entities/ai/`, `*Area.java` in `src/main/java/com/talhanation/workers/entities/workarea/`, `Message*.java` in `src/main/java/com/talhanation/workers/network/`, and `Mod*.java` in `src/main/java/com/talhanation/workers/init/`.

**Functions:**
- Use lowerCamelCase for methods; examples: `setup()` and `addCreativeTabs()` in `src/main/java/com/talhanation/workers/WorkersMain.java`, `scanBreakArea()` in `src/main/java/com/talhanation/workers/entities/workarea/MiningArea.java`, and `updateWorkArea()` in `src/main/java/com/talhanation/workers/network/MessageUpdateWorkArea.java`.
- Boolean methods read as predicates; examples: `isWorking()` in `src/main/java/com/talhanation/workers/entities/AbstractWorkerEntity.java`, `shouldIgnore()` in `src/main/java/com/talhanation/workers/entities/workarea/MiningArea.java`, and `isBuildingAreaAvailable()` in `src/main/java/com/talhanation/workers/entities/ai/BuilderWorkGoal.java`.

**Variables:**
- Use lowerCamelCase for fields and locals; examples: `currentCropArea` in `src/main/java/com/talhanation/workers/entities/FarmerEntity.java`, `bufferSource` in `src/main/java/com/talhanation/workers/client/events/ScreenEvents.java`, and `playerInfo` in `src/main/java/com/talhanation/workers/client/gui/WorkAreaScreen.java`.
- Public mutable fields are common in gameplay classes and message DTOs; examples: `neededItems` in `src/main/java/com/talhanation/workers/entities/AbstractWorkerEntity.java`, `uuid` in `src/main/java/com/talhanation/workers/network/MessageRotateWorkArea.java`, and `errorMessageDone` in `src/main/java/com/talhanation/workers/entities/ai/BuilderWorkGoal.java`.

**Types:**
- Use UPPER_SNAKE_CASE for shared constants and synched data accessors; examples: `MOD_ID` and `LOGGER` in `src/main/java/com/talhanation/workers/WorkersMain.java`, `HEIGHT_OFFSET` and `CLOSE_FLOOR` in `src/main/java/com/talhanation/workers/entities/workarea/MiningArea.java`, and many translation constants in `src/main/java/com/talhanation/workers/Translatable.java`.
- Use nested enums for worker or area state machines; examples: `BuilderWorkGoal.State` in `src/main/java/com/talhanation/workers/entities/ai/BuilderWorkGoal.java` and `MiningArea.MiningMode` in `src/main/java/com/talhanation/workers/entities/workarea/MiningArea.java`.
- Records are used sparingly for compact value carriers; example: `ScannedBlock` in `src/main/java/com/talhanation/workers/world/ScannedBlock.java`.

## Code Style

**Formatting:**
- No formatter configuration file is detected at repository root: no `/.prettierrc*`, `/prettier.config.*`, or `/biome.json`.
- No Java formatter configuration is detected: no Spotless or Checkstyle configuration is present in `/build.gradle`.
- Current style in `src/main/java/com/talhanation/workers/**` uses 4-space indentation, braces on the same line, and blank lines between logical sections; examples: `src/main/java/com/talhanation/workers/WorkersMain.java` and `src/main/java/com/talhanation/workers/entities/workarea/MiningArea.java`.
- Inline comments are used as lightweight markers rather than formal documentation; examples: `// ModSounds.SOUNDS.register(modEventBus);` in `src/main/java/com/talhanation/workers/WorkersMain.java` and `// Test the rotated area before committing` in `src/main/java/com/talhanation/workers/network/MessageRotateWorkArea.java`.

**Linting:**
- No lint configuration is detected: no `/eslint.config.*`, `/.eslintrc*`, or Java lint plugin setup in `/build.gradle`.
- Static analysis is implicit through Java annotations and Forge APIs rather than enforced repository rules; examples: `@Nullable` and `@NotNull` in `src/main/java/com/talhanation/workers/entities/AbstractWorkerEntity.java` and `src/main/java/com/talhanation/workers/inventory/MerchantAddEditTradeContainer.java`.

## Import Organization

**Order:**
1. Package declaration.
2. Project and third-party imports, often mixed rather than grouped strictly; examples: `src/main/java/com/talhanation/workers/WorkersMain.java` and `src/main/java/com/talhanation/workers/init/ModMenuTypes.java`.
3. `javax.*` and `java.*` imports near the end; examples: `src/main/java/com/talhanation/workers/entities/FarmerEntity.java` and `src/main/java/com/talhanation/workers/entities/ai/BuilderWorkGoal.java`.

**Path Aliases:**
- Not applicable. The codebase uses standard Java packages rooted at `com.talhanation.workers`; examples: `com.talhanation.workers.entities`, `com.talhanation.workers.client.gui`, and `com.talhanation.workers.network` under `src/main/java/com/talhanation/workers/**`.

## Error Handling

**Patterns:**
- Use guard clauses to exit early on invalid runtime state; examples: `if(player == null) return;` in `src/main/java/com/talhanation/workers/network/MessageRotateWorkArea.java` and `if(this.getCommandSenderWorld().isClientSide()) return;` in `src/main/java/com/talhanation/workers/entities/AbstractWorkerEntity.java`.
- Prefer silent no-op fallback over throwing in network and UI flows; examples: `updateWorkArea()` in `src/main/java/com/talhanation/workers/network/MessageUpdateWorkArea.java` reverts invalid moves, and `rotate()` in `src/main/java/com/talhanation/workers/network/MessageRotateWorkArea.java` restores the original facing on overlap.
- Use try/catch around IO and screen instantiation. Failures are logged or printed and execution continues; examples: `registerMenu()` in `src/main/java/com/talhanation/workers/init/ModMenuTypes.java` and file operations in `src/main/java/com/talhanation/workers/world/StructureManager.java`.
- Throw explicit exceptions only for invalid enum mapping or impossible states; example: `MiningMode.fromIndex()` in `src/main/java/com/talhanation/workers/entities/workarea/MiningArea.java` throws `IllegalArgumentException`.

## Logging

**Framework:** Log4j when centralized logging is needed.

**Patterns:**
- Reuse a shared mod logger from `src/main/java/com/talhanation/workers/WorkersMain.java` via `WorkersMain.LOGGER` or a local `LogManager.getLogger(WorkersMain.MOD_ID)`; examples: `src/main/java/com/talhanation/workers/UpdateChecker.java`, `src/main/java/com/talhanation/workers/init/ModMenuTypes.java`, `src/main/java/com/talhanation/workers/init/ModPois.java`, and `src/main/java/com/talhanation/workers/init/ModProfessions.java`.
- Use `sendSystemMessage()` for player-facing operational feedback instead of logs; examples: `src/main/java/com/talhanation/workers/entities/MerchantEntity.java`, `src/main/java/com/talhanation/workers/entities/ai/GetNeededItemsFromStorage.java`, and `src/main/java/com/talhanation/workers/entities/ai/DepositItemsToStorage.java`.
- Some IO paths still use `e.printStackTrace()` rather than structured logging; examples: `src/main/java/com/talhanation/workers/world/StructureManager.java`, `src/main/java/com/talhanation/workers/network/MessageUpdateBuildArea.java`, and `src/main/java/com/talhanation/workers/client/gui/widgets/ScrollDropDownMenuWithFolders.java`.

## Comments

**When to Comment:**
- Comment edge cases, temporary workarounds, and intent around Forge behavior; examples: `// Only edit below this line...` in `/build.gradle`, `//ONLY FOR BUILDING AREA WILL REMOVE IT` in `src/main/java/com/talhanation/workers/entities/ai/BuilderWorkGoal.java`, and `// Test the rotated area before committing` in `src/main/java/com/talhanation/workers/network/MessageRotateWorkArea.java`.
- TODO comments exist but are sparse; examples: `src/main/java/com/talhanation/workers/entities/AbstractWorkerEntity.java` and `src/main/java/com/talhanation/workers/Translatable.java`.

**JSDoc/TSDoc:**
- Not applicable.
- JavaDoc usage is minimal. A small explanatory block appears above `registerMenu()` in `src/main/java/com/talhanation/workers/init/ModMenuTypes.java`, but most methods rely on naming and inline comments instead of formal JavaDoc.

## Function Design

**Size:**
- Small helper methods are common for serialization and getters; examples: `toBytes()`/`fromBytes()` in `src/main/java/com/talhanation/workers/network/MessageRotateWorkArea.java` and `isOre()` in `src/main/java/com/talhanation/workers/entities/workarea/MiningArea.java`.
- Long imperative methods are also common in AI and UI orchestration. Keep stateful gameplay logic in a single method when matching existing code; examples: `tick()` in `src/main/java/com/talhanation/workers/entities/ai/BuilderWorkGoal.java` and `setButtons()` in `src/main/java/com/talhanation/workers/client/gui/WorkAreaScreen.java`.

**Parameters:**
- Use concrete game objects directly instead of wrapper DTOs; examples: `BuilderWorkGoal(BuilderEntity builderEntity)` in `src/main/java/com/talhanation/workers/entities/ai/BuilderWorkGoal.java` and `getScreen(Player player)` in `src/main/java/com/talhanation/workers/entities/workarea/MiningArea.java`.
- Network message constructors accept raw serializable values and store them on public fields; example: `MessageUpdateWorkArea(UUID uuid, String name, Vec3 vec3, boolean destroy)` in `src/main/java/com/talhanation/workers/network/MessageUpdateWorkArea.java`.

**Return Values:**
- `null` is an accepted return value in several APIs. Preserve existing null-aware patterns when extending similar code; examples: `getRecruitByUUID()` in `src/main/java/com/talhanation/workers/init/ModMenuTypes.java`, `getMatchingItem()` in `src/main/java/com/talhanation/workers/entities/AbstractWorkerEntity.java`, and `getAllowedItems()` / `inventoryInputHelp()` in `src/main/java/com/talhanation/workers/entities/FarmerEntity.java`.
- Methods that mutate state often return `void` or booleans for “still working / continue” control flow; examples: `mineBlocks()` in `src/main/java/com/talhanation/workers/entities/ai/BuilderWorkGoal.java` and `updateWorkArea()` in `src/main/java/com/talhanation/workers/network/MessageUpdateWorkArea.java`.

## Module Design

**Exports:**
- Not applicable in the Java module sense. The codebase uses public classes under package directories rather than explicit module descriptors.
- Shared entry points and registries are centralized in static holder classes; examples: `src/main/java/com/talhanation/workers/WorkersMain.java`, `src/main/java/com/talhanation/workers/init/ModBlocks.java`, `src/main/java/com/talhanation/workers/init/ModItems.java`, and `src/main/java/com/talhanation/workers/init/ModMenuTypes.java`.

**Barrel Files:**
- Not applicable. There are no barrel exports; package organization under `src/main/java/com/talhanation/workers/**` is the main discovery mechanism.

---

*Convention analysis: 2026-04-05*
