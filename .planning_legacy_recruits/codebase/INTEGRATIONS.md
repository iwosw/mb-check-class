# External Integrations

**Analysis Date:** 2026-04-05

## APIs & External Services

**Mod distribution and update services:**
- GitHub Raw - Forge update metadata source.
  - SDK/Client: Forge `VersionChecker` reads the URL declared in `src/main/resources/META-INF/mods.toml` (`updateJSONURL="https://raw.githubusercontent.com/talhanation/recruits/main/update.json"`).
  - Auth: None.
- Modrinth - User-facing download/update destination.
  - SDK/Client: URL opened from `src/main/java/com/talhanation/recruits/UpdateChecker.java` and declared as `displayURL` in `src/main/resources/META-INF/mods.toml`.
  - Auth: None.
- CurseForge - Project homepage/files listing.
  - SDK/Client: Static URLs in `README.md`, `update.json`, and the `com.matthewprenger.cursegradle` plugin declaration in `build.gradle`.
  - Auth: None detected in repository files.

**Build-time artifact repositories:**
- MinecraftForge Maven - ForgeGradle and Forge artifacts in `build.gradle` and `settings.gradle`.
  - SDK/Client: Gradle repository definitions.
  - Auth: None.
- Sponge Maven - MixinGradle and mixin artifacts in `build.gradle`.
  - SDK/Client: Gradle repository definitions.
  - Auth: None.
- Maven Central / JCenter / mavenLocal / Max Henkel public repo / CurseMaven - Dependency resolution in `build.gradle`.
  - SDK/Client: Gradle repository definitions.
  - Auth: None detected.

**Optional mod compatibility integrations:**
- CoreLib - Networking/registry helper library used directly by `src/main/java/com/talhanation/recruits/Main.java` and `src/main/java/com/talhanation/recruits/network/*.java`.
  - SDK/Client: `de.maxhenkel.corelib` dependency from `build.gradle`.
  - Auth: Not applicable.
- Small Ships - Ship detection and control via reflection in `src/main/java/com/talhanation/recruits/compat/SmallShips.java` and runtime load checks in `src/main/java/com/talhanation/recruits/Main.java`.
  - SDK/Client: reflected classes like `com.talhanation.smallships.world.entity.ship.Ship`.
  - Auth: Not applicable.
- Musket Mod - Weapon integration via reflection in `src/main/java/com/talhanation/recruits/compat/MusketWeapon.java`, `src/main/java/com/talhanation/recruits/compat/PistolWeapon.java`, and `src/main/java/com/talhanation/recruits/entities/ai/compat/RecruitRangedMusketAttackGoal.java`.
  - SDK/Client: reflected `ewewukek.musketmod.*` classes.
  - Auth: Not applicable.
- Corpse / Epic Knights / RPGZ / Siege Weapons / WorldEdit - Compatibility flags and item/entity handling in `src/main/java/com/talhanation/recruits/Main.java`, `src/main/java/com/talhanation/recruits/compat/Corpse.java`, and config defaults in `src/main/java/com/talhanation/recruits/config/RecruitsServerConfig.java`.
  - SDK/Client: Forge mod presence checks and declared dependencies in `build.gradle`.
  - Auth: Not applicable.

## Data Storage

**Databases:**
- None.
  - Connection: Not applicable.
  - Client: Not applicable.
- Persistent world state is stored with Minecraft `SavedData`/NBT in `src/main/java/com/talhanation/recruits/world/RecruitsTeamSaveData.java`, `src/main/java/com/talhanation/recruits/world/RecruitsClaimSaveData.java`, `src/main/java/com/talhanation/recruits/world/RecruitsGroupsSaveData.java`, `src/main/java/com/talhanation/recruits/world/RecruitsDiplomacySaveData.java`, `src/main/java/com/talhanation/recruits/world/RecruitsTreatySaveData.java`, and `src/main/java/com/talhanation/recruits/world/RecruitPlayerUnitSaveData.java`.

**File Storage:**
- Local filesystem only.
- Client route files are written as `.nbt` files under `recruits/routes/<world-or-server>/` by `src/main/java/com/talhanation/recruits/world/RecruitsRoute.java`.

**Caching:**
- None external.
- In-memory caches exist only inside runtime code such as `src/main/java/com/talhanation/recruits/pathfinding/NodeEvaluatorCache.java` and `src/main/java/com/talhanation/recruits/client/gui/overlay/ClaimOverlayRenderer.java`.

## Authentication & Identity

**Auth Provider:**
- None.
  - Implementation: The mod relies on Minecraft/Forge player identity provided by the game runtime; no OAuth, API keys, or third-party identity provider code was detected.

## Monitoring & Observability

**Error Tracking:**
- None.

**Logs:**
- Log4j-style Forge logging through `Main.LOGGER` in `src/main/java/com/talhanation/recruits/Main.java`.
- Integration failures are logged in compatibility classes such as `src/main/java/com/talhanation/recruits/compat/SmallShips.java`, `src/main/java/com/talhanation/recruits/compat/MusketWeapon.java`, and `src/main/java/com/talhanation/recruits/UpdateChecker.java`.

## CI/CD & Deployment

**Hosting:**
- End-user distribution points are Modrinth and CurseForge, referenced in `src/main/resources/META-INF/mods.toml`, `update.json`, and `README.md`.
- Source-hosted update metadata is served from GitHub Raw via the URL in `src/main/resources/META-INF/mods.toml`.

**CI Pipeline:**
- None detected. No `.github/workflows/*` or other CI configuration files were found in `/home/kaiserroman/recruits`.

## Environment Configuration

**Required env vars:**
- None detected.

**Secrets location:**
- Not detected. No `.env*`, credential files, or secret management configuration files were found in `/home/kaiserroman/recruits`.

## Webhooks & Callbacks

**Incoming:**
- None.

**Outgoing:**
- Update checks route through Forge's version-check mechanism using the external JSON feed declared in `src/main/resources/META-INF/mods.toml` and mirrored in `update.json`.
- No custom webhook sender or callback endpoint code was detected in `src/main/java`.

---

*Integration audit: 2026-04-05*
