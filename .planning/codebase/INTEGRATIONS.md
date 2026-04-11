# External Integrations

**Analysis Date:** 2026-04-11

## APIs & External Services

**Update distribution metadata:**
- GitHub raw content - Forge update feed for the merged mod metadata.
  - SDK/Client: Forge `VersionChecker` via `recruits/src/main/java/com/talhanation/recruits/UpdateChecker.java` and `workers/src/main/java/com/talhanation/workers/UpdateChecker.java`
  - Auth: None detected
  - Config source: `recruits/src/main/resources/META-INF/mods.toml` line 14 points to `https://raw.githubusercontent.com/talhanation/recruits/main/update.json`
  - Update payload: `recruits/update.json`

**Mod marketplace links:**
- Modrinth - player-facing recruits download/update destination.
  - SDK/Client: clickable chat links via `ClickEvent.Action.OPEN_URL` in `recruits/src/main/java/com/talhanation/recruits/UpdateChecker.java`
  - Auth: None detected
  - Metadata: `recruits/src/main/resources/META-INF/mods.toml` line 15 and `recruits/update.json` line 2
- CurseForge - player-facing workers download/update destination and dependency mirror source.
  - SDK/Client: clickable chat links via `ClickEvent.Action.OPEN_URL` in `workers/src/main/java/com/talhanation/workers/UpdateChecker.java`
  - Auth: None detected
  - Metadata: hard-coded URL in `workers/src/main/java/com/talhanation/workers/UpdateChecker.java` lines 28 and 49

**Build/dependency repositories:**
- MinecraftForge Maven - ForgeGradle plugin and Forge artifacts from `build.gradle` lines 3 and 143-145 plus `settings.gradle` lines 5-8.
- Sponge Maven - MixinGradle artifact from `build.gradle` line 4.
- Maven Central - general build dependencies from `build.gradle` line 6 and `settings.gradle` line 4.
- Max Henkel public Maven - CoreLib resolution from `build.gradle` lines 143-146.
- Cursemaven - Curse Maven dependency resolution from `build.gradle` lines 150-152.
- `mavenLocal()` - optional local override enabled only when `allowLocalMaven` is present in `build.gradle` lines 147-149.

**Runtime mod interoperability:**
- Musket Mod (`musketmod`) - optional reflective weapon/item integration in `recruits/src/main/java/com/talhanation/recruits/Main.java` and `recruits/src/main/java/com/talhanation/recruits/compat/MusketWeapon.java` plus related compat classes.
- Small Ships (`smallships`) - optional ship compatibility with version gating in `recruits/src/main/java/com/talhanation/recruits/Main.java` and `recruits/src/main/java/com/talhanation/recruits/compat/SmallShips.java`.
- Corpse (`corpse`) - optional corpse-entity compatibility in `recruits/src/main/java/com/talhanation/recruits/Main.java`, `recruits/src/main/java/com/talhanation/recruits/compat/Corpse.java`, and `recruits/src/main/java/com/talhanation/recruits/config/RecruitsServerConfig.java`.
- Siege Weapons (`siegeweapons`), RPGZ (`rpgz`), and Epic Knights / Magistu Armory (`magistuarmory`) - load-state checks in `recruits/src/main/java/com/talhanation/recruits/Main.java`.
- Supplementaries (`supplementaries`) and Herbal Brews (`herbalbrews`) - config-level item-id interoperability in `workers/src/main/java/com/talhanation/workers/config/WorkersServerConfig.java`.

## Data Storage

**Databases:**
- None.
  - Connection: Not applicable
  - Client: Minecraft/Forge save data and NBT APIs instead of a database, implemented in `recruits/src/main/java/com/talhanation/recruits/world/**` and worker entity/work-area serialization under `workers/src/main/java/com/talhanation/workers/entities/**`

**File Storage:**
- Local filesystem plus packaged mod resources.
  - Forge config directory - client/server config TOML files are loaded from Forge config paths in `recruits/src/main/java/com/talhanation/recruits/init/ModLifecycleRegistrar.java`, `recruits/src/main/java/com/talhanation/recruits/config/RecruitsClientConfig.java`, and `recruits/src/main/java/com/talhanation/recruits/config/RecruitsServerConfig.java`.
  - Minecraft game directory - worker structure scans are saved and loaded under `workers/scan` by `workers/src/main/java/com/talhanation/workers/world/StructureManager.java` lines 98-165.
  - Packaged NBT structures - default worker templates are copied from `bannermod:structures/workers` via `workers/src/main/java/com/talhanation/workers/world/StructureManager.java` and `workers/src/main/java/com/talhanation/workers/WorkersRuntime.java`.

**Caching:**
- None detected outside normal in-memory game state and Forge registries.

## Authentication & Identity

**Auth Provider:**
- None.
  - Implementation: The code relies on Minecraft/Forge player identity and UUIDs rather than any external auth provider; examples include worker owner/message flows in `workers/src/main/java/com/talhanation/workers/network/MessageUpdateOwner.java` and recruit systems under `recruits/src/main/java/com/talhanation/recruits/world/**`.

## Monitoring & Observability

**Error Tracking:**
- None detected. No Sentry, Crashlytics, Rollbar, or similar service is configured in `build.gradle` or source.

**Logs:**
- Log4j through Forge mod loggers - `recruits/src/main/java/com/talhanation/recruits/Main.java` and `workers/src/main/java/com/talhanation/workers/WorkersRuntime.java`.
- Player-facing system messages for update notices and some in-game operations - `recruits/src/main/java/com/talhanation/recruits/UpdateChecker.java`, `workers/src/main/java/com/talhanation/workers/UpdateChecker.java`, and `workers/src/main/java/com/talhanation/workers/world/StructureManager.java`.

## CI/CD & Deployment

**Hosting:**
- Distributed as a Forge mod JAR; marketplace references point to Modrinth and CurseForge from `recruits/src/main/resources/META-INF/mods.toml`, `recruits/update.json`, and `workers/src/main/java/com/talhanation/workers/UpdateChecker.java`.

**CI Pipeline:**
- None detected. No `.github/workflows/**`, other CI YAML, or pipeline config files were found at `/home/kaiserroman/bannermod`.

## Environment Configuration

**Required env vars:**
- None detected in build scripts or runtime code.

**Secrets location:**
- Not applicable. No secret-management files or auth-token based integrations are required by the checked-in code.

## Webhooks & Callbacks

**Incoming:**
- None.

**Outgoing:**
- None beyond HTTP requests performed by Forge `VersionChecker` against the update metadata URL declared in `recruits/src/main/resources/META-INF/mods.toml`.

---

*Integration audit: 2026-04-11*
