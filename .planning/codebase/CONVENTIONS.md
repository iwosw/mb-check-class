# Coding Conventions

**Analysis Date:** 2026-04-11

## Naming Patterns

**Files:**
- Use one top-level Java type per file with PascalCase names across `recruits/src/main/java/com/talhanation/recruits/**`, `workers/src/main/java/com/talhanation/workers/**`, and root merge helpers like `workers/src/main/java/com/talhanation/workers/WorkersRuntime.java`.
- Keep concern-specific suffixes already used by the codebase: `*Entity.java` in `recruits/src/main/java/com/talhanation/recruits/entities/` and `workers/src/main/java/com/talhanation/workers/entities/`, `*Goal.java` in AI packages, `Message*.java` in `recruits/src/main/java/com/talhanation/recruits/network/` and `workers/src/main/java/com/talhanation/workers/network/`, `Mod*.java` in both `init/` packages, and `*GameTests.java` / `*Test.java` under test source sets.
- Use package names as lowercase feature folders. Examples: `recruits/src/main/java/com/talhanation/recruits/pathfinding/`, `recruits/src/main/java/com/talhanation/recruits/gametest/support/`, and `workers/src/main/java/com/talhanation/workers/client/gui/widgets/`.

**Functions:**
- Use lowerCamelCase for methods and name them after the trigger or effect: `onRegisterCommands()` and `clientSetup()` in `recruits/src/main/java/com/talhanation/recruits/Main.java`, `bindChannel()` and `migrateStructureNbt()` in `workers/src/main/java/com/talhanation/workers/WorkersRuntime.java`, and `registerAll()` in `workers/src/main/java/com/talhanation/workers/network/WorkersNetworkRegistrar.java`.
- Name boolean methods as predicates or state checks: `isInsideOwnFactionClaim()` in `workers/src/main/java/com/talhanation/workers/network/MessageAddWorkArea.java`, `shouldProcessSynchronously()` in `recruits/src/main/java/com/talhanation/recruits/pathfinding/AsyncPathProcessor.java`, and `isAllowed()` in `workers/src/main/java/com/talhanation/workers/network/WorkAreaAuthoringRules.java`.
- Use verb-heavy helper names in tests and support code: `assertMovementRoundTrip()` in `recruits/src/test/java/com/talhanation/recruits/testsupport/MessageCodecAssertions.java`, `spawnCommandScenario()` in `recruits/src/gametest/java/com/talhanation/recruits/gametest/support/RecruitsCommandGameTestSupport.java`, and `assertUnchanged()` in the same support package.

**Variables:**
- Use lowerCamelCase for locals and fields, including mutable gameplay state: `modEventBus` in `recruits/src/main/java/com/talhanation/recruits/Main.java`, `simpleChannel` in `workers/src/main/java/com/talhanation/workers/WorkersRuntime.java`, `teamStringID` in `workers/src/main/java/com/talhanation/workers/network/MessageAddWorkArea.java`, and `registrations` in `recruits/src/test/java/com/talhanation/recruits/network/RecruitsNetworkRegistrarTest.java`.
- Preserve existing legacy field names when testing brownfield packet DTOs rather than renaming them. Examples: reflective test access to `player_uuid`, `state`, `group`, and `formation` in `recruits/src/test/java/com/talhanation/recruits/network/MessageMovementCodecTest.java` and `recruits/src/test/java/com/talhanation/recruits/testsupport/MessageCodecAssertions.java`.

**Types:**
- Use `UPPER_SNAKE_CASE` for constants and shared singleton handles: `MOD_ID`, `LOGGER`, and `SIMPLE_CHANNEL` in `recruits/src/main/java/com/talhanation/recruits/Main.java`; `LEGACY_MOD_ID` and `ROOT_NETWORK_ID_OFFSET` in `workers/src/main/java/com/talhanation/workers/WorkersRuntime.java`; `MESSAGE_TYPES` in `workers/src/main/java/com/talhanation/workers/network/WorkersNetworkRegistrar.java`.
- Use nested enums and records for compact domain state where they already exist: `BuilderBuildProgress.State` in `workers/src/main/java/com/talhanation/workers/entities/ai/BuilderBuildProgress.java`, `WorkersRuntimeLegacyIdMigrationTest` assertions against `BuilderBuildProgress.State` in `src/test/java/com/talhanation/workers/BuilderBuildProgressSmokeTest.java`, and `BattleSquad` record in `recruits/src/gametest/java/com/talhanation/recruits/gametest/support/RecruitsBattleGameTestSupport.java`.

## Code Style

**Formatting:**
- No repository formatter config was detected at `/home/kaiserroman/bannermod`: no `.editorconfig`, Spotless, Checkstyle, PMD, or formatter XML files are present, and `build.gradle` only enables Forge/Mixin/Shadow/JUnit tasks.
- Follow the de facto Java style used in `recruits/src/main/java/com/talhanation/recruits/Main.java`, `workers/src/main/java/com/talhanation/workers/WorkersRuntime.java`, and `workers/src/main/java/com/talhanation/workers/network/MessageAddWorkArea.java`: 4-space indentation, same-line braces, blank lines between logical sections, and no fluent wrapping unless the expression is long.
- Keep compact early-return guards on one line when the surrounding file does that already, for example `if(player == null) return;` in `workers/src/main/java/com/talhanation/workers/network/MessageAddWorkArea.java` and similar packet handlers under `workers/src/main/java/com/talhanation/workers/network/`.
- Match local file style instead of normalizing everything. `recruits/src/main/java/com/talhanation/recruits/Main.java` mixes explicit imports with wildcard imports, while `workers/src/main/java/com/talhanation/workers/WorkersRuntime.java` stays explicit.

**Linting:**
- No lint tool is configured in `build.gradle`, `settings.gradle`, or repository root config files. There is no enforced auto-fix step.
- Use compiler correctness and test coverage as the practical quality gate, especially `test`, `runGameTestServer`, and `check` from `build.gradle` lines 202-259.

## Import Organization

**Order:**
1. Prefer feature-local imports first when the file is domain heavy, as seen in `recruits/src/main/java/com/talhanation/recruits/Main.java` and `workers/src/main/java/com/talhanation/workers/network/MessageAddWorkArea.java`.
2. Group Minecraft/Forge or third-party imports next, for example `net.minecraft.*`, `net.minecraftforge.*`, and `de.maxhenkel.corelib.*` in `workers/src/main/java/com/talhanation/workers/network/WorkersNetworkRegistrar.java` and `workers/src/main/java/com/talhanation/workers/WorkersRuntime.java`.
3. Put JDK imports in their own block, usually near the end of non-test files and before static imports in tests, as shown by `java.util.Objects` in `workers/src/main/java/com/talhanation/workers/WorkersRuntime.java` and `java.util.List` / `java.util.Set` in `recruits/src/test/java/com/talhanation/recruits/network/RecruitsNetworkRegistrarTest.java`.

**Path Aliases:**
- Not applicable. All production and test code uses full Java package imports; no aliasing mechanism exists in `build.gradle` or `settings.gradle`.
- Wildcard imports are already present in older bootstrap files such as `recruits/src/main/java/com/talhanation/recruits/Main.java` (`com.talhanation.recruits.init.*`, `com.talhanation.recruits.network.*`, `net.minecraftforge.fml.*`). Preserve nearby style when touching those files rather than forcing a project-wide cleanup inside unrelated changes.
- Tests place static assertion imports last, as shown in `recruits/src/test/java/com/talhanation/recruits/network/RecruitsNetworkRegistrarTest.java` and `src/test/java/com/talhanation/workers/WorkersRuntimeLegacyIdMigrationTest.java`.

## Error Handling

**Patterns:**
- Prefer guard clauses and safe no-op returns over custom exception trees. Examples: `if (player == null) return;` in `workers/src/main/java/com/talhanation/workers/network/MessageAddWorkArea.java`, nullable menu returns in `workers/src/main/java/com/talhanation/workers/init/ModMenuTypes.java`, and null-preserving delivery in `recruits/src/test/java/com/talhanation/recruits/pathfinding/AsyncPathProcessorTest.java`.
- Throw `IllegalArgumentException` for impossible internal states in support or validation code rather than silently continuing. Examples: `throw new IllegalArgumentException(...)` in `recruits/src/gametest/java/com/talhanation/recruits/gametest/support/RecruitsBattleGameTestSupport.java` and packet/game-test assertions in `recruits/src/gametest/java/com/talhanation/recruits/gametest/command/CommandAuthorityGameTests.java`.
- For screen and menu registration, catch broad exceptions only at the Forge boundary and log before returning `null`, as in `workers/src/main/java/com/talhanation/workers/init/ModMenuTypes.java`.
- For migration helpers and domain utilities, prefer defensive conversion instead of throwing: `migrateLegacyId(String rawId)` in `workers/src/main/java/com/talhanation/workers/WorkersRuntime.java` returns the raw id if parsing fails.
- Brownfield IO code still uses `printStackTrace()` in legacy workers paths such as `workers/src/main/java/com/talhanation/workers/world/StructureManager.java` and `workers/src/main/java/com/talhanation/workers/network/MessageUpdateBuildArea.java`. New code should prefer the logger style already used in `workers/src/main/java/com/talhanation/workers/init/ModMenuTypes.java` and `recruits/src/main/java/com/talhanation/recruits/Main.java`.

## Logging

**Framework:** Log4j via Forge / `org.apache.logging.log4j`

**Patterns:**
- Reuse a shared mod logger when a file is part of global runtime wiring: `Main.LOGGER` in `recruits/src/main/java/com/talhanation/recruits/Main.java` and `WorkersRuntime.logger()` / local `LOGGER` in `workers/src/main/java/com/talhanation/workers/WorkersRuntime.java`.
- Local bootstrap files sometimes create their own logger with the active mod id, as in `workers/src/main/java/com/talhanation/workers/init/ModMenuTypes.java`.
- Use `sendSystemMessage()` for player-visible failures and authoring feedback instead of only logging. Examples: `workers/src/main/java/com/talhanation/workers/network/MessageAddWorkArea.java`, `workers/src/main/java/com/talhanation/workers/network/MessageRotateWorkArea.java`, and `workers/src/main/java/com/talhanation/workers/entities/MerchantEntity.java`.
- Logging style is mixed between string concatenation and parameterized messages. Match the surrounding file; do not mix styles within a small edit.

## Comments

**When to Comment:**
- Add comments for Forge-specific pitfalls, boundary behavior, or temporary compatibility notes. The clearest example is the explanatory block above `registerMenu()` in `workers/src/main/java/com/talhanation/workers/init/ModMenuTypes.java`.
- Keep short inline comments for operational hints only when the code path is otherwise hard to follow, such as the compatibility TODO in `recruits/src/main/java/com/talhanation/recruits/Main.java`.
- Avoid narrating obvious control flow. Most tested code in `recruits/src/test/java/com/talhanation/recruits/**` and merge helpers in `src/test/java/com/talhanation/workers/**` is self-documenting through method names.

**JSDoc/TSDoc:**
- Not applicable. This repository is Java-based.
- JavaDoc usage is selective. Keep it for API-like helpers or Forge edge cases, following `workers/src/main/java/com/talhanation/workers/init/ModMenuTypes.java` rather than documenting every method.

## Function Design

**Size:**
- Allow long imperative orchestration methods in gameplay and packet code when state mutation is the main job, for example `setup()` in `recruits/src/main/java/com/talhanation/recruits/Main.java` and `executeServerSide()` in `workers/src/main/java/com/talhanation/workers/network/MessageAddWorkArea.java`.
- Keep utility and registry helpers small and single-purpose, like `networkIdOffset()` in `workers/src/main/java/com/talhanation/workers/WorkersRuntime.java`, `messageCount()` in `workers/src/main/java/com/talhanation/workers/network/WorkersNetworkRegistrar.java`, and round-trip helpers in `recruits/src/test/java/com/talhanation/recruits/testsupport/MessageCodecAssertions.java`.

**Parameters:**
- Pass concrete Minecraft/Forge types directly instead of wrapping them in service objects: `GameTestHelper`, `SimpleChannel`, `FriendlyByteBuf`, `ServerPlayer`, `BlockPos`, and entity types appear throughout `recruits/src/gametest/java/com/talhanation/recruits/**` and `workers/src/main/java/com/talhanation/workers/network/**`.
- Packet/message constructors usually store raw serializable fields directly and deserialize through `fromBytes()` / `toBytes()`, as in `workers/src/main/java/com/talhanation/workers/network/MessageAddWorkArea.java`.

**Return Values:**
- Return `void` for mutating packet handlers and setup flows, `boolean` for decision or migration helpers, and nullable objects when the Forge API expects them. Examples: `migrateStructureNbt()` in `workers/src/main/java/com/talhanation/workers/WorkersRuntime.java`, `registerAll()` in `workers/src/main/java/com/talhanation/workers/network/WorkersNetworkRegistrar.java`, and nullable menu/container creation in `workers/src/main/java/com/talhanation/workers/init/ModMenuTypes.java`.
- Tests often encode failure by throwing assertion exceptions or `IllegalArgumentException` instead of returning status objects, especially in `recruits/src/gametest/java/com/talhanation/recruits/gametest/command/CommandAuthorityGameTests.java`.

## Module Design

**Exports:**
- Use package organization and public classes as the module boundary. There is no Java module descriptor and no separate export layer in `build.gradle` or source roots.
- Centralize registries and runtime wiring in holder classes: `recruits/src/main/java/com/talhanation/recruits/init/ModLifecycleRegistrar.java`, `workers/src/main/java/com/talhanation/workers/init/ModMenuTypes.java`, and `workers/src/main/java/com/talhanation/workers/network/WorkersNetworkRegistrar.java`.
- Keep test helpers in dedicated support packages instead of duplicating setup inside each test class: `recruits/src/test/java/com/talhanation/recruits/testsupport/` and `recruits/src/gametest/java/com/talhanation/recruits/gametest/support/`.

**Barrel Files:**
- Not used as a formal pattern.
- Wildcard imports in older files such as `recruits/src/main/java/com/talhanation/recruits/Main.java` are the closest equivalent; do not create new cross-package barrel abstractions.

---

*Convention analysis: 2026-04-11*
