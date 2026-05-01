# AI Minecraft UI Style Guide

This guide is mandatory for AI agents changing BannerMod player-facing GUI, HUD, overlays, or placement holograms.

## Core Rule

BannerMod UI must feel like a Minecraft kingdom/settlement mod, not a generic web dashboard. Keep previews readable in-world, compact in HUD, server-authoritative for gameplay, and useful while the player is moving.

## Holograms And Overlays

- Render the actual intended shape, not a placeholder rectangle.
- Preserve the plan after validation when the player still needs to compare the build against it.
- Use distinct line colors for structure roles: warm wood/gold for walls and towers, blue for usable courtyard/interior, brown for storage or barracks wings, orange for anchor/authority.
- Keep dimensions practical for Minecraft building. Interior wings meant as rooms should be at least 5 blocks wide unless the feature explicitly requires a tighter passage.
- Avoid visual clutter at the crosshair. Prefer line boxes and sparse markers over filled panels in world space.
- Never make the client preview authoritative. Server validation remains the source of truth.

## GUI And HUD

- Use compact medieval/Minecraft language: parchment, dark wood, iron, banner colors, wax/gold accents.
- Keep HUD panels away from hotbar, chat, boss bars, debug text, and the crosshair.
- Every actionable state needs a visible next step and denial reason.
- Color is a secondary signal. Pair it with text, icons, checklist marks, or stable geometry.
- Localize player-facing text in both EN and RU when adding or changing strings.

## Surveyor And Building Placement Flow

- The player job is to understand what to build, mark zones, and validate with the server.
- Holograms must show enough structure to build from: footprint, vertical height, entrance, anchor, courtyard/interior, and role-specific wings.
- Captured zone outlines may overlay the hologram, but must not replace it unless the user explicitly requests a simpler contour.
- If a building is player-facing, update `MULTIPLAYER_GUIDE_EN.md`, `MULTIPLAYER_GUIDE_RU.md`, and `docs/BANNERMOD_ALMANAC.html` in the same slice.

## Verification

- Run `./gradlew compileJava --console=plain` with Java 21 after code changes.
- Run `git diff --check` before commit.
- Visually test at least one fresh in-game placement when changing hologram geometry.
