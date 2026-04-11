# Codebase Structure

**Analysis Date:** 2026-04-11

## Directory Layout

```text
[project-root]/
├── .planning/                     # Active planning context and generated codebase docs
├── .planning_legacy_recruits/     # Archived recruits planning context
├── .planning_legacy_workers/      # Archived workers planning context
├── recruits/                      # Runtime-base source tree and most active gameplay code
├── workers/                       # Preserved worker subsystem source tree
├── src/                           # Root merge-only resources, tests, and game-test scaffold
├── build.gradle                   # Single merged build entrypoint
├── settings.gradle                # Root Gradle settings
├── gradle.properties              # Root merged mod metadata and versions
├── MERGE_NOTES.md                 # Merge-specific repository notes
└── MERGE_PLAN.md                  # Merge execution plan history
```

## Directory Purposes

**`.planning/`:**
- Purpose: Store active orchestration state and generated repository maps.
- Contains: `STATE.md`, `CODEBASE.md`, `VERIFICATION.md`, and `codebase/` outputs.
- Key files: `.planning/STATE.md`, `.planning/CODEBASE.md`, `.planning/VERIFICATION.md`

**`recruits/`:**
- Purpose: Hold the active runtime-base mod code that defines the merged `bannermod` entrypoint.
- Contains: primary Java source, resources, tests, and game tests.
- Key files: `recruits/src/main/java/com/talhanation/recruits/Main.java`, `recruits/src/main/resources/META-INF/mods.toml`, `recruits/src/main/resources/mixins.recruits.json`

**`workers/`:**
- Purpose: Hold the preserved legacy worker subsystem that is compiled into the root runtime.
- Contains: worker Java source, resources, and generated resources.
- Key files: `workers/src/main/java/com/talhanation/workers/WorkersSubsystem.java`, `workers/src/main/java/com/talhanation/workers/WorkersRuntime.java`, `workers/src/main/resources/mixins.workers.json`

**`src/`:**
- Purpose: Hold root-level merge support material rather than a standalone gameplay module.
- Contains: root access transformer in `src/main/resources/`, root unit tests in `src/test/java/`, and an empty root game-test scaffold in `src/gametest/`.
- Key files: `src/main/resources/META-INF/accesstransformer.cfg`, `src/test/java/com/talhanation/workers/WorkersRuntimeSmokeTest.java`

**`recruits/src/main/java/com/talhanation/recruits/`:**
- Purpose: Primary runtime package tree.
- Contains: bootstrap files, events, commands, entities, AI, world managers, screens, networking, mixins, and compatibility helpers.
- Key files: `recruits/src/main/java/com/talhanation/recruits/Main.java`, `RecruitEvents.java`, `FactionEvents.java`, `ClaimEvents.java`

**`workers/src/main/java/com/talhanation/workers/`:**
- Purpose: Worker subsystem package tree.
- Contains: subsystem composition, worker entities, work-area entities, network messages, GUI, and world structure helpers.
- Key files: `workers/src/main/java/com/talhanation/workers/WorkersSubsystem.java`, `WorkersLegacyMappings.java`, `world/StructureManager.java`

## Key File Locations

**Entry Points:**
- `build.gradle`: merged build entrypoint that combines `src/`, `recruits/`, and `workers/` source trees.
- `recruits/src/main/java/com/talhanation/recruits/Main.java`: only active Forge `@Mod` entrypoint.
- `recruits/src/main/java/com/talhanation/recruits/init/ModLifecycleRegistrar.java`: root runtime registrar.
- `workers/src/main/java/com/talhanation/workers/WorkersSubsystem.java`: worker subsystem composition entry.

**Configuration:**
- `settings.gradle`: root project name and plugin management.
- `gradle.properties`: merged mod metadata and version values.
- `recruits/src/main/resources/META-INF/mods.toml`: runtime mod metadata for `bannermod`.
- `src/main/resources/META-INF/accesstransformer.cfg`: active access transformer for the merged root build.
- `recruits/src/main/resources/mixins.recruits.json`: active mixin config.
- `workers/src/main/resources/mixins.workers.json`: worker mixin config placeholder.

**Core Logic:**
- `recruits/src/main/java/com/talhanation/recruits/entities/`: recruit entities and AI roots.
- `recruits/src/main/java/com/talhanation/recruits/world/`: persistent managers and `SavedData` classes.
- `recruits/src/main/java/com/talhanation/recruits/network/`: recruit packet classes and network registration.
- `workers/src/main/java/com/talhanation/workers/entities/`: worker entities and profession logic.
- `workers/src/main/java/com/talhanation/workers/entities/workarea/`: work-area entities and area geometry.
- `workers/src/main/java/com/talhanation/workers/network/`: worker authoring and sync packets.

**Testing:**
- `recruits/src/test/java/com/talhanation/recruits/`: main unit/integration-style test tree.
- `recruits/src/gametest/java/com/talhanation/recruits/gametest/`: Forge game tests and harness support.
- `src/test/java/com/talhanation/workers/`: merged root smoke tests for worker/runtime compatibility.
- `src/gametest/java/`: present but currently does not contain Java files.

## Naming Conventions

**Files:**
- `PascalCase.java` for Java types: `recruits/src/main/java/com/talhanation/recruits/entities/CommanderEntity.java`
- `Mod*.java` for registry/bootstrap holders: `recruits/src/main/java/com/talhanation/recruits/init/ModItems.java`, `workers/src/main/java/com/talhanation/workers/init/ModMenuTypes.java`
- `Message*.java` for network packets: `recruits/src/main/java/com/talhanation/recruits/network/MessageSaveTeamSettings.java`, `workers/src/main/java/com/talhanation/workers/network/MessageUpdateWorkArea.java`
- `*Events.java` for Forge runtime subscribers: `recruits/src/main/java/com/talhanation/recruits/RecruitEvents.java`, `workers/src/main/java/com/talhanation/workers/VillagerEvents.java`
- `*Screen.java` and `*Menu`/`*Container` for GUI pairs: `recruits/src/main/java/com/talhanation/recruits/client/gui/RecruitInventoryScreen.java`, `recruits/src/main/java/com/talhanation/recruits/inventory/RecruitInventoryMenu.java`

**Directories:**
- Lowercase package directories by concern: `recruits/src/main/java/com/talhanation/recruits/client/gui/faction`, `workers/src/main/java/com/talhanation/workers/entities/workarea`
- Feature-first groupings inside `client/`, `entities/`, `network/`, and `world/` packages.

## Where to Add New Code

**New Feature:**
- Primary code: add recruit-runtime features under `recruits/src/main/java/com/talhanation/recruits/` unless the feature is specifically a worker mechanic.
- Worker-specific code: add to `workers/src/main/java/com/talhanation/workers/` and wire it through `workers/src/main/java/com/talhanation/workers/WorkersSubsystem.java` or `workers/src/main/java/com/talhanation/workers/init/WorkersLifecycleRegistrar.java`.
- Tests: add unit tests under `recruits/src/test/java/com/talhanation/recruits/` for recruit features or `src/test/java/com/talhanation/workers/` for merged worker smoke/compatibility coverage.

**New Component/Module:**
- Implementation: place Forge registries in `recruits/src/main/java/com/talhanation/recruits/init/` or `workers/src/main/java/com/talhanation/workers/init/`.
- Screen/menu pairs: place screens in `recruits/src/main/java/com/talhanation/recruits/client/gui/` or `workers/src/main/java/com/talhanation/workers/client/gui/`, with paired menus/containers in the corresponding `inventory/` package.
- Persistent world manager: place recruit save-backed systems in `recruits/src/main/java/com/talhanation/recruits/world/` next to existing `*Manager` and `*SaveData` pairs.

**Utilities:**
- Shared recruit helpers: `recruits/src/main/java/com/talhanation/recruits/util/`
- Merge seams and temporary adapters: `recruits/src/main/java/com/talhanation/recruits/migration/`
- Worker world/structure helpers: `workers/src/main/java/com/talhanation/workers/world/`

## Special Directories

**`src/main/resources/`:**
- Purpose: hold root-only merged resources.
- Generated: No.
- Committed: Yes.

**`src/generated/resources/`:**
- Purpose: root datagen output referenced by `build.gradle`.
- Generated: Yes.
- Committed: Not detected.

**`recruits/src/generated/resources/`:**
- Purpose: recruits datagen output that is still part of the merged main source set.
- Generated: Yes.
- Committed: Yes.

**`workers/src/generated/resources/`:**
- Purpose: worker generated resources copied into the merged runtime during `processResources`.
- Generated: Yes.
- Committed: Yes.

**`recruits/src/gametest/java/`:**
- Purpose: active game-test suite for recruit runtime verification.
- Generated: No.
- Committed: Yes.

**`src/test/java/com/talhanation/workers/`:**
- Purpose: root-level smoke tests that validate merged worker/runtime helpers.
- Generated: No.
- Committed: Yes.

**`build/`:**
- Purpose: Gradle outputs, expanded dependency caches, processed resources, and test artifacts.
- Generated: Yes.
- Committed: No.

## Practical Placement Rules

- Do not add new gameplay Java classes under `src/main/java/` unless the code is intentionally root-only merge scaffolding; the active gameplay trees are `recruits/src/main/java/` and `workers/src/main/java/`.
- Put new recruit assets under `recruits/src/main/resources/assets/bannermod/`.
- Put worker-authored legacy assets under `workers/src/main/resources/assets/workers/` only when they are meant to be remapped into merged assets by `build.gradle`; merged runtime consumers should resolve them through `workers/src/main/java/com/talhanation/workers/WorkersRuntime.java`.
- Add new worker structures under `workers/src/main/resources/assets/workers/structures/`; the root build copies them into `assets/bannermod/structures/workers`.
- Keep root tests in `src/test/java/` limited to cross-tree or merged-runtime coverage; feature-specific tests belong beside the owning subtree.

---

*Structure analysis: 2026-04-11*
