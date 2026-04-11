# Codebase Structure

**Analysis Date:** 2026-04-05

## Directory Layout

```text
workers/
├── gradle/                                # Gradle wrapper files
├── src/main/java/com/talhanation/workers/ # Java source for the mod
│   ├── client/                            # Client-only screens, renderers, and events
│   ├── config/                            # Forge config definitions
│   ├── entities/                          # Worker entities and AI/work-area subpackages
│   ├── init/                              # Deferred registry declarations
│   ├── inventory/                         # Merchant menu/container classes
│   ├── items/                             # Custom item implementations
│   ├── network/                           # Packet classes for client/server actions
│   └── world/                             # Structure scanning and domain helper classes
├── src/main/resources/                    # Mod metadata, assets, bundled structures
│   ├── META-INF/                          # `mods.toml` and access transformer
│   └── assets/workers/                    # Lang, textures, models, structures
├── build.gradle                           # Build, Forge runs, and dependencies
├── settings.gradle                        # Gradle plugin repositories/toolchains
├── gradle.properties                      # Gradle project properties
├── update.json                            # Update metadata referenced by `mods.toml`
└── README.md                              # Minimal project description
```

## Directory Purposes

**`src/main/java/com/talhanation/workers/`:**
- Purpose: Top-level mod bootstrap and global event classes.
- Contains: `WorkersMain.java`, `AttributeEvent.java`, `VillagerEvents.java`, `UpdateChecker.java`, `CommandEvents.java`.
- Key files: `src/main/java/com/talhanation/workers/WorkersMain.java`, `src/main/java/com/talhanation/workers/VillagerEvents.java`

**`src/main/java/com/talhanation/workers/client/`:**
- Purpose: Client-only behavior.
- Contains: Event subscribers in `client/events/`, screens in `client/gui/`, renderers in `client/render/`, and the claim helper `WorkersClientManager.java`.
- Key files: `src/main/java/com/talhanation/workers/client/events/ClientEvent.java`, `src/main/java/com/talhanation/workers/client/gui/BuildAreaScreen.java`, `src/main/java/com/talhanation/workers/client/render/WorkerAreaRenderer.java`

**`src/main/java/com/talhanation/workers/config/`:**
- Purpose: Server config schema.
- Contains: `WorkersServerConfig.java` only.
- Key files: `src/main/java/com/talhanation/workers/config/WorkersServerConfig.java`

**`src/main/java/com/talhanation/workers/entities/`:**
- Purpose: Worker entity hierarchy.
- Contains: `AbstractWorkerEntity.java`, profession entities, `ai/`, and `workarea/`.
- Key files: `src/main/java/com/talhanation/workers/entities/AbstractWorkerEntity.java`, `src/main/java/com/talhanation/workers/entities/FarmerEntity.java`

**`src/main/java/com/talhanation/workers/entities/ai/`:**
- Purpose: Goal logic and storage/pathfinding helpers.
- Contains: Profession goals like `FarmerWorkGoal.java`, `BuilderWorkGoal.java`, plus shared helpers like `AbstractChestGoal.java`.
- Key files: `src/main/java/com/talhanation/workers/entities/ai/FarmerWorkGoal.java`, `src/main/java/com/talhanation/workers/entities/ai/DepositItemsToStorage.java`

**`src/main/java/com/talhanation/workers/entities/workarea/`:**
- Purpose: Placeable area entities that workers operate against.
- Contains: `AbstractWorkAreaEntity.java` and concrete area types such as `CropArea.java`, `BuildArea.java`, `MarketArea.java`.
- Key files: `src/main/java/com/talhanation/workers/entities/workarea/AbstractWorkAreaEntity.java`, `src/main/java/com/talhanation/workers/entities/workarea/BuildArea.java`

**`src/main/java/com/talhanation/workers/init/`:**
- Purpose: Registry declarations and menu/screen registration helpers.
- Contains: `ModEntityTypes.java`, `ModItems.java`, `ModMenuTypes.java`, `ModProfessions.java`, `ModPois.java`, `ModBlocks.java`, `ModShortcuts.java`, `ModSounds.java`.
- Key files: `src/main/java/com/talhanation/workers/init/ModEntityTypes.java`, `src/main/java/com/talhanation/workers/init/ModMenuTypes.java`

**`src/main/java/com/talhanation/workers/inventory/`:**
- Purpose: Merchant menu containers.
- Contains: `MerchantTradeContainer.java` and `MerchantAddEditTradeContainer.java`.
- Key files: `src/main/java/com/talhanation/workers/inventory/MerchantTradeContainer.java`

**`src/main/java/com/talhanation/workers/items/`:**
- Purpose: Custom item implementations.
- Contains: `WorkersSpawnEgg.java`.
- Key files: `src/main/java/com/talhanation/workers/items/WorkersSpawnEgg.java`

**`src/main/java/com/talhanation/workers/network/`:**
- Purpose: One class per networked action.
- Contains: Nineteen packet classes including work-area creation, merchant actions, owner updates, and config syncing.
- Key files: `src/main/java/com/talhanation/workers/network/MessageAddWorkArea.java`, `src/main/java/com/talhanation/workers/network/MessageUpdateBuildArea.java`, `src/main/java/com/talhanation/workers/network/MessageToClientOpenWorkAreaScreen.java`

**`src/main/java/com/talhanation/workers/world/`:**
- Purpose: World-data helpers and structure tooling.
- Contains: `StructureManager.java`, trade and block helper models, and scanned-structure representations.
- Key files: `src/main/java/com/talhanation/workers/world/StructureManager.java`, `src/main/java/com/talhanation/workers/world/WorkersMerchantTrade.java`

**`src/main/resources/META-INF/`:**
- Purpose: Forge metadata and access transformer configuration.
- Contains: `mods.toml`, `accesstransformer.cfg`.
- Key files: `src/main/resources/META-INF/mods.toml`

**`src/main/resources/assets/workers/`:**
- Purpose: Runtime assets bundled into the mod jar.
- Contains: `lang/`, `models/`, `textures/`, and default scanned structures in `structures/`.
- Key files: `src/main/resources/assets/workers/lang/en_us.json`, `src/main/resources/assets/workers/structures/vanilla/plains/plains_house_1.nbt`

## Key File Locations

**Entry Points:**
- `src/main/java/com/talhanation/workers/WorkersMain.java`: Forge mod entrypoint and wiring hub.
- `src/main/java/com/talhanation/workers/AttributeEvent.java`: Attribute registration hook.
- `src/main/java/com/talhanation/workers/client/events/ClientEvent.java`: Client renderer registration hook.

**Configuration:**
- `build.gradle`: ForgeGradle setup, run configs, dependencies, Java 17 toolchain.
- `settings.gradle`: Plugin management.
- `gradle.properties`: Gradle properties used by the build.
- `src/main/java/com/talhanation/workers/config/WorkersServerConfig.java`: Forge server config schema.
- `src/main/resources/META-INF/mods.toml`: Mod metadata and dependency declaration.
- `src/main/resources/mixins.workers.json`: Mixin config file. The configured package `com.talhanation.workers.mixin` is present in config, but no mixin source files were detected in `src/main/java/`.

**Core Logic:**
- `src/main/java/com/talhanation/workers/entities/AbstractWorkerEntity.java`: Shared worker behavior.
- `src/main/java/com/talhanation/workers/entities/workarea/AbstractWorkAreaEntity.java`: Shared work-area state and permissions.
- `src/main/java/com/talhanation/workers/entities/workarea/BuildArea.java`: Build-template execution.
- `src/main/java/com/talhanation/workers/world/StructureManager.java`: Scan/load/save/copy structure templates.

**Testing:**
- Not detected. No `src/test/` directory and no `*Test*.java` files were found under `/home/kaiserroman/workers`.

## Naming Conventions

**Files:**
- Java classes use PascalCase filenames that match the class name: `WorkersMain.java`, `FarmerEntity.java`, `MessageUpdateWorkArea.java`.
- Registry holders use the `Mod*` prefix: `ModEntityTypes.java`, `ModMenuTypes.java`.
- Network packets use the `Message*` prefix and are grouped by feature/action: `MessageAddWorkArea.java`, `MessageUpdateMerchantTrade.java`.
- Screen files end with `Screen`: `BuildAreaScreen.java`, `MerchantTradeScreen.java`.
- AI goal files commonly end with `Goal`: `FarmerWorkGoal.java`, `DepositItemsToStorage.java` is the main exception because it is still a goal class.

**Directories:**
- Packages are lower-case and grouped by role: `client`, `config`, `entities`, `init`, `inventory`, `items`, `network`, `world`.
- Feature-specific subpackages are nested under broader layers, such as `entities/workarea/` and `client/gui/widgets/`.

## Where to Add New Code

**New worker profession:**
- Primary code: add the entity under `src/main/java/com/talhanation/workers/entities/`, the goal under `src/main/java/com/talhanation/workers/entities/ai/`, and register the type in `src/main/java/com/talhanation/workers/init/ModEntityTypes.java`.
- Related bootstrap: add attributes in `src/main/java/com/talhanation/workers/AttributeEvent.java`, spawn egg/item hooks in `src/main/java/com/talhanation/workers/init/ModItems.java`, and renderer bindings in `src/main/java/com/talhanation/workers/client/events/ClientEvent.java`.

**New work area:**
- Implementation: create the entity in `src/main/java/com/talhanation/workers/entities/workarea/`.
- Client screen: add a matching screen in `src/main/java/com/talhanation/workers/client/gui/`.
- Networking: add packet classes in `src/main/java/com/talhanation/workers/network/` if the screen mutates server state.
- Registration: wire the type into `src/main/java/com/talhanation/workers/init/ModEntityTypes.java` and the placement UI in `src/main/java/com/talhanation/workers/client/gui/WorkerCommandScreen.java`.

**New merchant UI/menu flow:**
- Implementation: add menu classes in `src/main/java/com/talhanation/workers/inventory/`.
- Screen: add the client screen in `src/main/java/com/talhanation/workers/client/gui/`.
- Registration: bind the `MenuType` and `MenuScreens` entry in `src/main/java/com/talhanation/workers/init/ModMenuTypes.java`.

**Utilities and domain helpers:**
- Shared world/domain helpers belong in `src/main/java/com/talhanation/workers/world/`.
- General bootstrap/event helpers belong at `src/main/java/com/talhanation/workers/` only when they are genuinely app-wide, following the pattern of `VillagerEvents.java` and `UpdateChecker.java`.

**Tests:**
- Not applicable in the current structure because no test source tree exists. Create `src/test/java/` only if the project adopts automated tests.

## Special Directories

**`src/main/resources/assets/workers/structures/`:**
- Purpose: Bundled default build templates copied into the client scan directory by `src/main/java/com/talhanation/workers/world/StructureManager.java`.
- Generated: No.
- Committed: Yes.

**`src/generated/resources/`:**
- Purpose: Declared as an additional resource source in `build.gradle` for data-generation output.
- Generated: Yes.
- Committed: Not detected in the current repository snapshot.

**`run/`:**
- Purpose: Declared in `build.gradle` as the working directory for client, server, and data runs.
- Generated: Yes, during local Forge runs.
- Committed: Not detected in the current repository snapshot.

**`.planning/codebase/`:**
- Purpose: Generated analysis documents for GSD planning workflows.
- Generated: Yes.
- Committed: Intended to be committed when the orchestrator performs git operations.

---

*Structure analysis: 2026-04-05*
