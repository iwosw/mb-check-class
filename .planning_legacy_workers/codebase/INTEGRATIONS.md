# External Integrations

**Analysis Date:** 2026-04-05

## APIs & External Services

**Minecraft Mod Platform APIs:**
- Minecraft Forge - Primary runtime API for events, registries, config, and networking.
  - SDK/Client: `net.minecraftforge:forge:1.20.1-47.4.1` in `build.gradle`
  - Auth: Not applicable
- Sponge Mixin - Bytecode mixin integration enabled at build time.
  - SDK/Client: `org.spongepowered:mixin` and `org.spongepowered:mixingradle` in `build.gradle`
  - Auth: Not applicable

**Mod-to-Mod Integrations:**
- Recruits - Hard functional dependency used for entities, screens, claims, trade systems, and pathfinding in files such as `src/main/java/com/talhanation/workers/entities/AbstractWorkerEntity.java`, `src/main/java/com/talhanation/workers/client/WorkersClientManager.java`, and `src/main/java/com/talhanation/workers/VillagerEvents.java`.
  - SDK/Client: `curse.maven:recruits-523860:7374573` in `build.gradle`
  - Auth: Not applicable
- CoreLib - Shared library for registry channels, message classes, and inventory/screen bases in `src/main/java/com/talhanation/workers/WorkersMain.java`, `src/main/java/com/talhanation/workers/network/*.java`, and `src/main/java/com/talhanation/workers/inventory/*.java`.
  - SDK/Client: `de.maxhenkel.corelib:corelib:1.20.1-1.1.3` in `build.gradle`
  - Auth: Not applicable
- Supplementaries and Let’s Do Herbal Brews - Compatibility currently appears as item ID references in `src/main/java/com/talhanation/workers/config/WorkersServerConfig.java` (`supplementaries:flax`, `herbalbrews:green_tea_leaf`).
  - SDK/Client: `curse.maven:supplementaries-412082:6615104`, `curse.maven:selene-499980:6681465`, and `curse.maven:lets-do-herbal-brews-951221:6458889` in `build.gradle`
  - Auth: Not applicable
- Architectury API, Farmer’s Delight, WorldEdit, and Debug Utils Forge - Declared in `build.gradle`; direct source imports were not detected in `src/main/java/com/talhanation/workers/`.
  - SDK/Client: respective `curse.maven:*` dependencies in `build.gradle`
  - Auth: Not applicable

**Update/Distribution Services:**
- GitHub Raw - Hosts the update metadata feed referenced by `src/main/resources/META-INF/mods.toml` and backed by `update.json`.
  - SDK/Client: Forge `VersionChecker` uses metadata from `src/main/resources/META-INF/mods.toml`
  - Auth: None detected
- CurseForge - Target download URL for update notifications in `src/main/java/com/talhanation/workers/UpdateChecker.java` and homepage in `update.json`.
  - SDK/Client: Browser URL opened from chat click events; no REST client detected
  - Auth: None detected
- Modrinth - Project display URL in `src/main/resources/META-INF/mods.toml`.
  - SDK/Client: URL metadata only
  - Auth: None detected

## Data Storage

**Databases:**
- None. No SQL, NoSQL, ORM, or external database client was detected.
  - Connection: Not applicable
  - Client: Not applicable

**File Storage:**
- Local Minecraft world and resource files only. NBT/resource handling appears in files such as `src/main/java/com/talhanation/workers/world/StructureManager.java` and `src/main/java/com/talhanation/workers/network/MessageUpdateBuildArea.java`.

**Caching:**
- None detected.

## Authentication & Identity

**Auth Provider:**
- None. The codebase is a Forge mod and does not integrate with OAuth, SSO, external identity providers, or custom web authentication.
  - Implementation: Not applicable

## Monitoring & Observability

**Error Tracking:**
- None. No Sentry, Rollbar, Bugsnag, or similar service was detected.

**Logs:**
- Log4j via Forge logging. `src/main/java/com/talhanation/workers/WorkersMain.java` defines the shared logger and `src/main/java/com/talhanation/workers/UpdateChecker.java` logs update status.

## CI/CD & Deployment

**Hosting:**
- Not a hosted service. The deliverable is a Forge mod artifact built from `build.gradle` for Minecraft 1.20.1.

**CI Pipeline:**
- None detected. No workflow files were found under `.github/workflows/`.

## Environment Configuration

**Required env vars:**
- None detected. Configuration is file-based through `gradle.properties`, `src/main/resources/META-INF/mods.toml`, and `src/main/java/com/talhanation/workers/config/WorkersServerConfig.java`.

**Secrets location:**
- Not applicable. No secret-management files or environment-variable conventions were detected in the repository root.

## Webhooks & Callbacks

**Incoming:**
- None. No webhook endpoints, HTTP servers, or callback handlers were detected.

**Outgoing:**
- Update checks flow through Forge `VersionChecker` using `updateJSONURL` in `src/main/resources/META-INF/mods.toml`.
- Player-facing browser callbacks open the CurseForge files page from `src/main/java/com/talhanation/workers/UpdateChecker.java`.
- Internal client/server callbacks use Forge networking over `WorkersMain.SIMPLE_CHANNEL` in `src/main/java/com/talhanation/workers/WorkersMain.java` and the message classes under `src/main/java/com/talhanation/workers/network/`, but these are in-game network messages rather than third-party webhooks.

---

*Integration audit: 2026-04-05*
