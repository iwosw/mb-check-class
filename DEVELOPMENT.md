# Development Map

## Active Truth

- Build from the repository root with `build.gradle` and `settings.gradle`.
- Active code lives under `src/**`.
- The only live mod entrypoint is `src/main/java/com/talhanation/bannermod/bootstrap/BannerModMain.java`.
- Active planning lives under `.planning/`.
- Unfinished work lives in `BANNERMOD_BACKLOG.md` only.

## Ignore By Default

- `recruits/`
- `workers/`
- `workers/.planning/`
- `workers/CLAUDE.md`

Those paths are retained for archive/reference only. They are not active build or planning inputs.

## First Places To Read

- Player/server flow: `MULTIPLAYER_GUIDE.md`
- Unfinished work: `BANNERMOD_BACKLOG.md`
- Runtime/bootstrap: `src/main/java/com/talhanation/bannermod/bootstrap/`
- Shared packet contract: `src/main/java/com/talhanation/bannermod/network/BannerModNetworkBootstrap.java`
- Military gameplay: `src/main/java/com/talhanation/bannermod/entity/military/`
- Military AI: `src/main/java/com/talhanation/bannermod/ai/military/`
- Civilian gameplay: `src/main/java/com/talhanation/bannermod/entity/civilian/`
- Civilian AI: `src/main/java/com/talhanation/bannermod/ai/civilian/`
- Shared seams: `src/main/java/com/talhanation/bannermod/shared/`

## Hotspots

- Combat target finding: `src/main/java/com/talhanation/bannermod/entity/military/AbstractRecruitEntity.java`
- Formation target reuse: `src/main/java/com/talhanation/bannermod/ai/military/FormationTargetSelectionController.java`
- Leader combat orchestration: `src/main/java/com/talhanation/bannermod/entity/military/AbstractLeaderEntity.java`
- Patrol attack logic: `src/main/java/com/talhanation/bannermod/ai/military/controller/PatrolLeaderAttackController.java`
- Worker hot-tick scans: `src/main/java/com/talhanation/bannermod/entity/civilian/AbstractWorkerEntity.java`
- Storage/work-area coupling: `src/main/java/com/talhanation/bannermod/entity/civilian/workarea/AbstractWorkAreaEntity.java`

## Config Truth

- The active bootstrap currently registers:
- `bannermod-recruits-client.toml`
- `bannermod-recruits-server.toml`
- `bannermod-workers-server.toml`
- `bannermod-war-server.toml`

Check `BannerModMain` before assuming config taxonomy from old planning notes.

## Validation Shortcuts

- `./gradlew compileJava`
- `./gradlew test`
- `./gradlew verifyGameTestStage`

Use GameTests when changing runtime behavior, AI, ownership, networking, or persistence.
