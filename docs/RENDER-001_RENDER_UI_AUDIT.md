# RENDER-001 Render/UI Audit

Date: 2026-04-28
Branch: `feature/render-001`
Scope: audit only; no production render/UI code was rewritten.

## Acceptance Progress Note

Use this exact backlog progress text if backlog mutation is allowed later:

```bash
tools/backlog progress RENDER-001 "Audit complete in docs/RENDER-001_RENDER_UI_AUDIT.md. Rewrite candidates: recruit/worker/citizen entity renderer stack under client/*/render, vanilla-derived head/bobber/banner snippets, claim/siege HUD overlays, world-map render stack, legacy command/group/player/worker screens, work-area structure preview, and shared dropdown/list/button widgets. Keep candidates: event/registration entry points, server-authoritative packet entry points, localization/assets, client mirror state models, menu/container bindings, and small LOD/profiling helpers unless a rewrite proves them obsolete. References captured: Minecraft/NeoForge renderer and GUI APIs, vanilla CustomHeadLayer/FishingHookRenderer/BannerRenderer patterns, MineColonies worker/request/protection UX wiki pages, Millenaire-like settlement design notes in .planning, Hundred Years Warfare/HYW army-war design input in .planning/STATE.md, Forge GUI/screens docs, ChampionAsh5357 1.20.6->1.21 migration gist, and in-repo War Room/world-map examples. No production render/UI code changed."
```

## Active Renderer Inventory

Rewrite candidates:

- `src/main/java/com/talhanation/bannermod/client/military/render/RecruitHumanRenderer.java` and `RecruitVillagerRenderer.java`: main recruit renderer pair with model/config split and multiple custom layers.
- `src/main/java/com/talhanation/bannermod/client/civilian/render/WorkerHumanRenderer.java` and `WorkerVillagerRenderer.java`: worker renderers inherit recruit renderers and should be reviewed with the recruit rewrite instead of separately patched.
- `src/main/java/com/talhanation/bannermod/client/citizen/render/CitizenRenderer.java`: simple villager-model citizen renderer; likely keep unless citizen visuals are intentionally redesigned.
- `src/main/java/com/talhanation/bannermod/client/civilian/render/WorkerAreaRenderer.java`: work-area entity/block preview renderer, higher risk because it mixes entity rendering, preview caching, and area visualization.
- `src/main/java/com/talhanation/bannermod/client/civilian/render/FishingBobberRenderer.java`: vanilla-derived fishing hook render/string logic adapted for worker fishermen.
- `src/main/java/com/talhanation/bannermod/client/military/render/SiegeStandardBlockEntityRenderer.java`: war objective block-entity renderer; keep server-authoritative data flow, consider visual rewrite only.
- `src/main/java/com/talhanation/bannermod/client/military/render/RecruitCrowdRenderEvents.java`: crowd render event behavior, likely performance-sensitive and should stay isolated from visual redesign.

Render layers and models to rewrite as a set:

- `src/main/java/com/talhanation/bannermod/client/military/render/layer/RecruitArmorLayer.java`
- `src/main/java/com/talhanation/bannermod/client/military/render/layer/RecruitLodArmorLayer.java`
- `src/main/java/com/talhanation/bannermod/client/military/render/layer/RecruitLodItemInHandLayer.java`
- `src/main/java/com/talhanation/bannermod/client/military/render/layer/RecruitLodCustomHeadLayer.java`
- `src/main/java/com/talhanation/bannermod/client/military/render/layer/VillagerRecruitCustomHeadLayer.java`
- `src/main/java/com/talhanation/bannermod/client/military/render/layer/RecruitHumanBiomeLayer.java`
- `src/main/java/com/talhanation/bannermod/client/military/render/layer/RecruitVillagerBiomeLayer.java`
- `src/main/java/com/talhanation/bannermod/client/military/render/layer/RecruitHumanTeamColorLayer.java`
- `src/main/java/com/talhanation/bannermod/client/military/render/layer/RecruitVillagerTeamColorLayer.java`
- `src/main/java/com/talhanation/bannermod/client/military/render/layer/RecruitHumanCompanionLayer.java`
- `src/main/java/com/talhanation/bannermod/client/military/render/layer/RecruitVillagerCompanionLayer.java`
- `src/main/java/com/talhanation/bannermod/client/military/models/RecruitHumanModel.java`
- `src/main/java/com/talhanation/bannermod/client/military/models/RecruitVillagerModel.java`
- `src/main/java/com/talhanation/bannermod/client/military/models/BetterCombatRecruitPose.java`

Kept unless proven obsolete:

- `src/main/java/com/talhanation/bannermod/client/military/render/RecruitRenderLod.java`
- `src/main/java/com/talhanation/bannermod/client/military/render/RecruitRenderProfiling.java`
- `src/main/java/com/talhanation/bannermod/client/civilian/render/IRenderWorkArea.java`
- Renderer registrations in `client/military/events/ClientEvent.java` and `client/civilian/events/ClientEvent.java`; they are wiring seams, not visual policy.

## HUD Overlay Inventory

Rewrite candidates:

- `src/main/java/com/talhanation/bannermod/client/military/gui/overlay/ClaimOverlayManager.java`: stateful claim panel timing, fade, cache invalidation, and event hooks.
- `src/main/java/com/talhanation/bannermod/client/military/gui/overlay/ClaimOverlayRenderer.java`: claim panel paint routine.
- `src/main/java/com/talhanation/bannermod/client/military/api/ClientOverlayEvent.java`: keep event contract if external extension remains desired.
- `src/main/java/com/talhanation/bannermod/client/military/hud/WarSiegeZoneOverlay.java`: active siege banner registered above the hotbar; current hardcoded English should be localized in a rewrite.

UI skill review notes:

- HUD anchors must avoid hotbar, chat, crosshair-critical space, boss bars, debug text, and other BannerMod overlays.
- Overlay content must read synced server state only; claim and war state mirrors are the right source, not client-authored decisions.
- Any rewrite should define stacking priority between claim panel and siege-zone banner before adding visual treatment.

## GUI Screen Inventory

Higher-priority rewrite candidates:

- `src/main/java/com/talhanation/bannermod/client/military/gui/worldmap/WorldMapScreen.java` and helpers under `client/military/gui/worldmap/`: largest render/UI surface; includes chunk tiles, route overlays, claim overlays, formation contacts, context menus, popups, and movement markers.
- `src/main/java/com/talhanation/bannermod/client/military/gui/CommandScreen.java` plus `client/military/gui/commandscreen/**`: army command UI; must preserve server command pipeline and saved server formation semantics.
- `src/main/java/com/talhanation/bannermod/client/civilian/gui/WorkAreaScreen.java` and area subclasses: `AnimalPenAreaScreen`, `BuildAreaScreen`, `CropAreaScreen`, `FishingAreaScreen`, `LumberAreaScreen`, `MarketAreaScreen`, `MiningAreaScreen`, `StorageAreaScreen`.
- `src/main/java/com/talhanation/bannermod/client/civilian/gui/structureRenderer/StructurePreviewWidget.java`: 3D structure preview in GUI; expensive render path and input handling should be audited before visual changes.
- War Room screens under `src/main/java/com/talhanation/bannermod/client/military/gui/war/`: `WarListScreen`, `WarDeclareScreen`, `WarAlliesScreen`, `WarAllyInvitePickerScreen`, `PoliticalEntityListScreen`, `PoliticalEntityInfoScreen`, `PoliticalEntityNameInputScreen`.

Legacy/basic screens to keep functional but consider visual consolidation:

- Military: `RecruitInventoryScreen`, `RecruitMoreScreen`, `RecruitHireScreen`, `PromoteScreen`, `GovernorScreen`, `AssassinLeaderScreen`, `DebugInvScreen`, `MessengerMainScreen`, `MessengerScreen`, `MessengerAnswerScreen`, `NobleTradeScreen`, `PatrolLeaderScreen`, `ScoutScreen`, `RenameRecruitScreen`, `ConfirmScreen`.
- Group/player selectors: `client/military/gui/group/**` and `client/military/gui/player/**`.
- Civilian trade/building: `MerchantTradeScreen`, `MerchantAddEditTradeScreen`, `PlaceBuildingScreen`, `WorkerCommandScreen`.

Kept files/modules:

- Menu/container classes and screen-opening packet handlers unless their UI flow changes require packet-level acceptance updates.
- `src/main/resources/assets/bannermod/lang/**` and textures are kept as assets, but hardcoded English in active screens/overlays should be replaced with localization during the rewrite.
- Server-side authority checks and command packets are not rewrite targets for this audit.

## Shared Widget Inventory

Rewrite or consolidate candidates:

- `src/main/java/com/talhanation/bannermod/client/military/gui/widgets/DropDownMenu.java`
- `src/main/java/com/talhanation/bannermod/client/military/gui/widgets/ScrollDropDownMenu.java`
- `src/main/java/com/talhanation/bannermod/client/civilian/gui/widgets/ScrollDropDownMenuWithFolders.java`
- `src/main/java/com/talhanation/bannermod/client/civilian/gui/widgets/ItemScrollDropDownMenu.java`
- `src/main/java/com/talhanation/bannermod/client/civilian/gui/widgets/DisplayTextItemScrollDropDownMenu.java`
- `src/main/java/com/talhanation/bannermod/client/military/gui/widgets/ListScreenBase.java`
- `src/main/java/com/talhanation/bannermod/client/military/gui/widgets/ListScreenEntryBase.java`
- `src/main/java/com/talhanation/bannermod/client/military/gui/widgets/ListScreenListBase.java`
- `src/main/java/com/talhanation/bannermod/client/military/gui/widgets/RecruitsCheckBox.java`
- `src/main/java/com/talhanation/bannermod/client/military/gui/widgets/BlackShowingTextField.java`
- `src/main/java/com/talhanation/bannermod/client/military/gui/widgets/ItemWithLabelWidget.java`
- `src/main/java/com/talhanation/bannermod/client/military/gui/widgets/SelectedPlayerWidget.java`
- `src/main/java/com/talhanation/bannermod/client/military/gui/component/ActivateableButton.java`
- `src/main/java/com/talhanation/bannermod/client/military/gui/component/RecruitsMultiLineEditBox.java`
- `src/main/java/com/talhanation/bannermod/client/military/gui/component/BannerRenderer.java`
- `src/main/java/com/talhanation/bannermod/client/military/gui/component/ImageToast.java`

Keep principle:

- Preserve vanilla-style focus, escape/back behavior, click target sizes, and narration hooks while replacing ad hoc visual styling.

## Copied/Vanilla-Derived Snippets

Concrete snippets that need source-aware rewrite review:

- `FishingBobberRenderer.java`: mirrors vanilla fishing hook quad/string math and texture use (`textures/entity/fishing_hook.png`).
- `VillagerRecruitCustomHeadLayer.java`: custom adaptation of vanilla head/skull rendering with obfuscated-style parameter names and `SkullBlockRenderer` flow.
- `RecruitLodCustomHeadLayer.java`: thin wrapper over vanilla `CustomHeadLayer` with LOD/profiling gates.
- `BannerRenderer.java`: directly calls `net.minecraft.client.renderer.blockentity.BannerRenderer.renderPatterns` for GUI banner display.
- `WorldMapScreen.java`: custom map tile rendering uses low-level `RenderSystem`, `RenderType`, scissor, manual item/entity pose transforms, and Small Ships compatibility.
- `StructurePreviewWidget.java`: custom GUI block/entity preview uses `BlockRenderDispatcher`, `BlockEntityRenderer`, and render buffers inside widget input handling.

## Reference Examples Captured

Repo/docs references available now:

- MineColonies worker/request/protection UX: `.planning_legacy_workers/research/FEATURES.md` lists concrete wiki pages for builder, farmer, miner, warehouse, deliveryman, worker, request, and protection systems. Use these only for blocked-state feedback, worker assignment clarity, request visibility, and permission UX; do not expand BannerMod into a full colony dashboard.
- Millenaire-like settlement direction: `.planning/ROADMAP.md`, `.planning/STATE.md`, and `.planning/phases/25-treasury-taxes-and-army-upkeep/25-SLICE-STATUS.md` frame Millenaire as settlement/economy/life-sim design input, not a rendering dependency.
- Hundred Years Warfare/HYW: `.planning/STATE.md` names HYW as design input for army-command and warfare work. No concrete HYW source or UI package is present in this worktree, so treat it as conceptual reference only until a source/reference artifact is added.
- Forge/NeoForge GUI/tutorial references: `.planning_legacy_workers/research/PITFALLS.md` cites Forge 1.21 docs for screens and menus; `.planning_legacy_recruits/research/ARCHITECTURE.md` cites Forge/porting docs and the ChampionAsh5357 1.20.6 to 1.21 migration gist.
- GitHub/tutorial example available from docs: ChampionAsh5357 migration gist, `https://gist.github.com/ChampionAsh5357/d895a7b1a34341e19c80870720f9880f`, for 1.21 rendering/API migration hazards.
- Dependency examples from `build.gradle`: Better Combat/playeranimator for animation/pose compatibility; Small Ships for world-map player vehicle icon compatibility; these are integration constraints, not UI style references.

Reference gaps:

- No MineColonies, Millenaire, or Hundred Years Warfare source dependency is present in `build.gradle`.
- No local GitHub example checkout was found beyond docs links and Gradle/plugin metadata.
- `recruits/` and `workers/` archive trees are not present in this worktree; legacy planning mirrors under `.planning_legacy_*` were used as reference context instead.

## Suggested Rewrite Boundaries

1. Renderer rewrite: recruit/worker/citizen renderers, models, and layers as one slice; keep registrations and LOD/profiling seams unless they actively block the design.
2. HUD rewrite: claim overlay and siege overlay as one slice; define stacking/anchors/localization first.
3. World map rewrite: map tiles, claim/route/formation overlays, context menus, and popups as one slice; preserve server-authoritative claim/move packet paths.
4. Screen-system rewrite: command, group/player selector, work-area, merchant, and war screens in smaller feature slices; avoid one giant GUI rewrite.
5. Widget cleanup: dropdown/list/button/text-field consolidation after screen flow decisions, not before.
