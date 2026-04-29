# RENDER-002 Render/UI Architecture

Date: 2026-04-28
Branch: `feature/render-002`
Input: `docs/RENDER-001_RENDER_UI_AUDIT.md`
Scope: architecture definition only; no production render/UI code was rewritten.

## Acceptance Progress Note

Use this exact backlog progress text if backlog mutation is allowed later:

```bash
tools/backlog progress RENDER-002 "Architecture definition complete in docs/RENDER-002_RENDER_UI_ARCHITECTURE.md. Package boundaries: client render runtime is split into entity renderers/layers/models, block-entity and projectile renderers, world-preview renderers, HUD overlays, shared Minecraft-native widgets, screen flows, world-map drawing, and client state/adapters. Ownership rules: render packages own visuals only, screens/widgets own presentation and input only, network/server packages own gameplay validation and mutation, assets/lang own player-facing text and textures, and event registration remains a thin wiring seam. Server-authoritative UI constraints: gameplay actions must call existing packet/server command paths, show pending/accepted/denied feedback, never trust client-authored authority/formation/claim state, and preserve the unified army command pipeline. Localization expectations: no new hardcoded player-facing English in active UI/HUD; all labels, tooltips, denial reasons, status chips, and toasts use assets/bannermod/lang keys with RU/EN guide updates only when player-facing mechanics change. Selected reference patterns: MineColonies-style blocked-state/request clarity, Millenaire-like settlement ambience only, HYW-inspired compact warfare command readability, Forge/NeoForge screen/menu/HUD API patterns, ChampionAsh5357 1.21 migration cautions, and vanilla renderer calls only as API contracts. Direct vanilla copy-paste is prohibited except unavoidable API calls such as BannerRenderer.renderPatterns, CustomHeadLayer/SkullBlockRenderer integration points, fishing-line math equivalents, RenderSystem/scissor use, and block/entity preview dispatch; retained references are documented in the architecture file. No production render/UI code or backlog JSON changed."
```

## Goals

- Replace the current patch pile with clear module boundaries before any rewrite starts.
- Keep Minecraft-native visuals: compact, readable, medieval/kingdom flavored, and safe at common GUI scales.
- Preserve server authority for commands, claims, war actions, hiring, trading, work areas, and world-map interactions.
- Avoid direct vanilla copy-paste unless the Minecraft client API effectively requires the call shape.

Non-goals:

- No Java renderer, HUD, screen, widget, packet, or asset rewrite in this task.
- No redesign of gameplay rules, network protocols, or saved data.
- No generic web-dashboard UI architecture.

## Package Boundaries

The rewrite should keep current `client/military` and `client/civilian` domains where they express gameplay ownership, but each future slice should follow these boundaries.

| Boundary | Owns | Must Not Own |
| --- | --- | --- |
| Entity renderers/layers/models | Recruit, worker, citizen model selection, texture selection, armor/item/head/team/biome/companion layers, pose presentation, LOD/profiling hooks | Gameplay state mutation, network sends, server permission checks |
| Block-entity, projectile, and line renderers | Siege standard visuals, worker fishing bobber/string visuals, any future projectile or command-line visuals | Worker AI, combat decisions, authority decisions |
| World-preview renderers | Work-area previews, structure previews, bounded block/entity preview drawing inside GUI widgets | Building placement authority, inventory mutation, worker assignment decisions |
| HUD overlays | Claim and siege status panels, stacking/anchor policy, short synced-state feedback | Creating, editing, or deleting claims/wars; client-side truth for ownership |
| Shared widgets | Buttons, dropdowns, list rows, text fields, banner/icon display, focus/narration/hover behavior | Screen-specific business rules, packet construction beyond exposing selected values |
| List and flow screens | Command, group/player, hiring/trade, worker area, War Room, political, and confirmation flows | Final authority for actions; direct entity mutation; direct saved-data writes |
| World-map drawing | Map tiles, claim/route/formation overlays, movement markers, context menus, popups, visual caches | Server claim storage, movement execution, formation selection authority unless explicitly sent by a server-approved UI flow |
| Client state/adapters | Read-only adapters from synced mirrors into UI view models, stale/loading/error flags | Inventing state that affects gameplay outcomes |
| Assets/localization | Textures, icons, lang keys, concise translated player-facing copy | Runtime behavior or Java logic |
| Event registration | Thin renderer/screen/HUD wiring seams | Visual policy, layout rules, gameplay decisions |

Concrete source areas from the audit:

- Entity renderer slice: `client/military/render/**`, `client/civilian/render/**`, `client/citizen/render/**`, and model classes under `client/military/models/**`.
- HUD slice: `client/military/gui/overlay/**`, `client/military/hud/**`, and overlay registration wiring.
- World-map slice: `client/military/gui/worldmap/**`.
- Screen-system slices: `CommandScreen`, `commandscreen/**`, War Room screens, work-area screens, group/player selectors, trade/hiring screens, and confirm/rename/debug support screens.
- Widget slice: `client/military/gui/widgets/**`, `client/civilian/gui/widgets/**`, and `client/military/gui/component/**`.

## Ownership Rules

- Render code is presentation-only. It reads entity/block/client mirror state and emits pixels.
- Screen code owns layout zones, input affordances, disabled states, local selection, and visible feedback.
- Widget code owns reusable behavior and visual grammar, not gameplay-specific decisions.
- Packet handlers, command services, and server runtime own all gameplay mutation and validation.
- Client mirrors are snapshots. UI must display missing, stale, or sync-pending states instead of fabricating authority.
- Registration files stay boring. They bind Minecraft events to renderer/screen/HUD factories and should not accumulate layout or policy.
- LOD/profiling helpers are allowed only around bounded render cost decisions; they must not change gameplay-visible state.
- Shared visual helpers must remain Minecraft-native and small. Do not add a framework-like design system before at least two real screens need the same primitive.

## Server-Authoritative UI Constraints

- Every gameplay-changing button must route through the existing server packet or command pipeline and render a pending, accepted, denied, or stale response when the state source supports it.
- Army movement, face, attack, aggro, stance, and strategic-fire flows must continue through `CommandIntentDispatcher` and `CommandEvents`; UI code must not call recruit movement methods directly.
- Formation is server-authoritative. World-map or command UI may preview a target, but the server must read saved formation state or validate an explicitly selected formation flow.
- Claim creation, deletion, war outcome, ally invite, hiring, trade, worker assignment, and build-area actions must never trust client-authored owner IDs, permission flags, or resource counts.
- Screens that depend on war/claim/settlement mirrors must show loading or stale states when mirrors are absent after login/logout or before sync completion.
- Denied actions need a visible reason when the server provides one; otherwise the UI should display a generic localized denial and refresh the relevant mirror.
- Confirmation dialogs must keep the information needed for the decision visible: target name, owner/political entity, cost, consequence, and authority requirement.

## Localization Expectations

- No new hardcoded player-facing English in active UI, HUD, tooltips, toasts, denial reasons, status chips, list empty states, or modal copy.
- Add language keys under `src/main/resources/assets/bannermod/lang/` when implementation begins.
- Keep labels short enough for RU/EN translations and Minecraft GUI scale constraints.
- Use text plus icon/color for state; color alone is not acceptable.
- If a later implementation changes player-facing mechanics or workflows, update `MULTIPLAYER_GUIDE_RU.md`, `MULTIPLAYER_GUIDE_EN.md`, and `docs/BANNERMOD_ALMANAC.html` in that implementation slice.
- This architecture-only task does not change player-facing mechanics, so guide updates are not required here.

## Visual And Interaction Rules

- HUD overlays must reserve stable anchors and avoid hotbar, chat, crosshair-critical space, boss bars, debug text, and other BannerMod overlays.
- Claim and siege overlays need a single stacking policy before either gets visual polish.
- Screens use five predictable zones where applicable: title/status, primary list/content, details, actions, and feedback.
- Lists must define empty, loading/stale, selected, selection-cleared, and denied/error states.
- Primary actions should stay pinned when space allows; do not make core actions scroll-only.
- Buttons need obvious enabled/disabled states and concise denial tooltips.
- Preserve vanilla-style escape/back behavior, focus traversal, narration hooks where practical, and usable click targets at high GUI scale.
- Avoid decorative noise, unbounded animation, and large opaque panels that reduce gameplay readability.

## Slice Architecture

1. Renderer rewrite: rebuild recruit/worker/citizen renderers, models, and layers together. Keep registration seams and evaluate `RecruitRenderLod`/`RecruitRenderProfiling` as bounded support utilities.
2. HUD rewrite: define overlay stack, anchors, stale-state behavior, and localization before repainting claim and siege overlays.
3. World-map rewrite: isolate tile drawing, claim overlays, route overlays, formation contacts, context menus, popups, and movement markers behind read-only client view models.
4. Screen-flow rewrite: update command, group/player, work-area, trade/hiring, and war/political screens in focused feature slices, not as one giant GUI rewrite.
5. Widget consolidation: replace dropdown/list/button/text-field duplication only after screen-flow needs are proven.
6. Preview renderer cleanup: treat work-area and structure previews as a bounded render subsystem with explicit cache lifetime and input ownership.

## Selected Reference Patterns

- MineColonies: use blocked-state clarity, request visibility, worker assignment readability, and permission feedback as UX patterns only. Do not copy colony-dashboard scope.
- Millenaire-like settlement direction: use ambience cues such as compact settlement identity, profession/status readability, and medieval civic tone. Do not introduce settlement simulation scope through UI architecture.
- Hundred Years Warfare/HYW input: use compact army/war command readability and fast battlefield decision support as conceptual guidance only until concrete source references are added.
- Forge/NeoForge screen/menu/HUD docs: follow current API contracts for screen lifecycle, menu binding, event registration, GUI graphics, and overlay registration.
- ChampionAsh5357 1.20.6 to 1.21 migration notes: use as a migration hazard checklist for render API changes, not as code to copy.
- Existing in-repo War Room/world-map screens: keep useful state-sync and server-action seams, but rewrite visual/layout policy where needed.
- Vanilla Minecraft: use as API reference only for unavoidable renderer calls, focus behavior, and screen lifecycle expectations.

## Retained Vanilla/API References

Direct vanilla copy-paste is prohibited in rewrite slices except where the Minecraft API effectively requires the call shape. Retained references must be documented in the implementation PR or backlog progress note.

Known unavoidable or likely retained references from the audit:

- GUI banner display may keep `net.minecraft.client.renderer.blockentity.BannerRenderer.renderPatterns` as an API call, wrapped by BannerMod-owned layout and localization rules.
- Head rendering may keep `CustomHeadLayer`, `SkullBlockRenderer`, or equivalent API integration points, but BannerMod layer structure and naming should be rewritten clearly.
- Worker fishing line rendering may need vanilla-equivalent line math and fishing hook texture compatibility; if retained, document the API reason and keep it isolated to the projectile renderer.
- World-map drawing may keep low-level `RenderSystem`, `RenderType`, scissor, pose stack, item/entity render, and compatibility calls where Minecraft rendering requires them.
- Structure previews may keep `BlockRenderDispatcher`, block-entity renderer, render buffers, and pose stack usage as API calls, with cache/input ownership moved into BannerMod code.

## Verification Checklist For Future Rewrite Slices

- Compile gate: `tools/ai-context-proxy/bin/ctx log -- ./gradlew compileJava`.
- UI authority check: inspect every gameplay action path and confirm server validation remains the source of truth.
- Localization check: search touched UI code for new hardcoded player-facing strings.
- Overlay check: confirm anchors and stacking avoid hotbar, chat, crosshair, boss bars, debug text, and existing BannerMod overlays.
- State check: confirm empty, loading/stale, selected, denied, success, and error states where applicable.
- Guide check: update both multiplayer guides and the almanac only when player-facing mechanics or workflows change.
