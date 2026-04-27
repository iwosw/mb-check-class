---
name: minecraft-ui-design
description: Design and implement BannerMod Minecraft UI/HUD screens that are functional, minimal, readable, non-overlapping, server-authoritative, localized, and visually stronger than vanilla-gray boxes. Use for any GUI, HUD overlay, command screen, world-map panel, settlement/war/political UI, or player-facing status widget.
---

# Minecraft UI Design Skill

Use this skill whenever creating, refactoring, or reviewing BannerMod UI/HUD code.

## Design Goal

Build Minecraft-native interfaces that feel like a serious kingdom/warfare mod: readable at gameplay speed, compact enough for multiplayer, visually distinct enough to understand state, and conservative enough not to obscure the world or fight vanilla controls.

Do not create generic web-dashboard UI. Do not create decorative noise that hides information. Do not stack panels that overlap each other, chat, hotbar, inventory slots, tooltip areas, or existing BannerMod overlays.

## First Read

Before designing or editing UI, inspect the existing active code and patterns:

- Client GUI package: `src/main/java/com/talhanation/bannermod/client/**`
- War UI examples: `client/military/gui/war/**`
- Existing widgets: `client/military/gui/widgets/**`
- HUD overlays: search for `Overlay`, `GuiGraphics`, `RegisterGuiOverlaysEvent`
- Localization files under `src/main/resources/assets/bannermod/lang/`

Use `tools/ai-context-proxy/bin/ctx` for repository inspection.

## Visual Language

- Prefer Minecraft-compatible textures, frames, panels, icons, compact typography, and color-state grammar over web-card layouts.
- Use restrained medieval/kingdom motifs: parchment, dark iron, muted banners, wax/seal accents, shield colors, state colors, and subtle separators.
- Keep vanilla readability: 9-slice-like panels, strong foreground/background contrast, clear hover/focus states, and no low-contrast text on noisy backgrounds.
- Use color as a secondary signal only. Pair color with text/icon/state labels.
- Keep screen density high but scannable: rows, compact status chips, short labels, stable columns, and predictable button placement.

## Functional Rules

- Every action button must have an obvious enabled/disabled reason.
- Every server-authoritative action must show accepted/denied/pending feedback.
- Every list screen needs empty, loading/stale, and selection-cleared states when applicable.
- Any UI showing synced server state must handle stale/missing snapshots safely.
- If an action mutates gameplay, keep validation server-authoritative; client UI may preview, but must not be the source of truth.
- Add or update localization keys for player-facing strings. Avoid hardcoded English in active screens.

## Non-Overlap Rules

- HUD overlays must choose stable anchors and avoid hotbar, chat, crosshair-critical space, boss bars, debug text, and other BannerMod overlays.
- Screens must not place buttons outside scaled viewport bounds at common GUI scales.
- Tooltips must not cover the hovered control's critical value when avoidable.
- Modal dialogs must not hide the information required to decide the modal action.
- If multiple overlays can be active, define priority/stacking and maximum vertical footprint.

## Minecraft Constraints

- Test at desktop and smaller scaled resolutions conceptually; Minecraft GUI scale changes layout behavior.
- Avoid tiny click targets. Buttons and list rows must remain usable at high GUI scale.
- Avoid scroll-only access to primary actions when the screen has enough space for pinned actions.
- Preserve vanilla-style escape/back behavior.
- Do not create animated or per-frame expensive UI unless the render cost is bounded.

## Implementation Checklist

1. Identify the player job: what decision or action must this screen make easier?
2. Inventory source state: local-only, client mirror, or server-authoritative.
3. Define layout zones: title/status, primary content, details, actions, feedback.
4. Define states: empty, loading/stale, selected, denied, success, error.
5. Check overlap: HUD anchors, modal bounds, tooltip bounds, list scroll bounds.
6. Add localization and narration/tooltips for non-obvious controls.
7. Add cheapest verification: compile for code changes, and focused tests where logic/formatting changes.
8. If the UI changes player-facing mechanics or workflow, update both `MULTIPLAYER_GUIDE_RU.md` and `MULTIPLAYER_GUIDE_EN.md` before closing backlog work.

## Review Checklist

Block or revise the design if any item is true:

- The UI looks like a generic SaaS dashboard instead of a Minecraft mod UI.
- Important actions have no denial reason or success acknowledgement.
- The layout can overlap hotbar/chat/crosshair/boss bars/other BannerMod overlays.
- It depends on client-authored state for server gameplay decisions.
- It hardcodes player-facing strings in active code.
- It adds a new mechanic but leaves both multiplayer guides stale.
- It improves aesthetics by adding noise, animation, or large panels that reduce gameplay readability.

## Output Shape

When proposing or implementing a UI change, include:

- The player-facing flow in one or two sentences.
- The layout zones and state handling.
- The overlap/accessibility checks performed.
- The verification command(s) run.
- Whether multiplayer guides needed updates, and why.
