---
phase: 21-source-tree-consolidation-into-bannerlord
plan: 12
type: execute
gap_closure: true
closed_gaps:
  - truth: "Recruits hotkey screens (Command/Faction/Map) and the claim overlay open in dev client"
    test: 8
    outcome: pass
    note: "User confirmed in dev-client: pressing R opens Command (now with Combat / Movement / Other / Workers tabs), U opens Faction, M opens Map. Initial fix exposed a follow-on dirt: the three recruits command-screen categories (CombatCategory / MovementCategory / OtherCategory) had also lost their CommandCategoryManager.register calls during the 21-03 bootstrap consolidation, so the Command screen showed only the Workers tab. Re-ported as commit f98245c."
completed: 2026-04-15T15:00Z
---

## Summary

Gap closure for the post-21-11 dev-client blocker — the Command (R), Faction (U), and Map (M) hotkeys did nothing because three client-side `@SubscribeEvent` handler classes (`KeyEvents`, `ClientPlayerEvents`, `ClaimOverlayManager`) were never registered on `MinecraftForge.EVENT_BUS`. Same defect class as 21-11, applied to client-only handlers.

A second defect of the same family surfaced during human verification: `BannerModMain.clientSetup()` registered only `WorkerCommandScreen` on `CommandCategoryManager`, so the Command screen had a single (workers-only) tab. The pre-consolidation `BannerlordMain.clientSetup()` had registered `CombatCategory(-3)`, `MovementCategory(-2)`, `OtherCategory(-1)` alongside the workers screen; those calls were lost during 21-03 bootstrap consolidation. Re-ported with original priorities preserved.

## What changed

### `src/main/java/com/talhanation/bannermod/bootstrap/BannerModMain.java`

Imports added (alphabetical, after the existing `ScreenEvents` import):

```java
import com.talhanation.bannermod.client.military.events.ClientPlayerEvents;
import com.talhanation.bannermod.client.military.events.KeyEvents;
import com.talhanation.bannermod.client.military.gui.overlay.ClaimOverlayManager;
```

Body of `clientSetup(FMLClientSetupEvent)` — three `EVENT_BUS.register` calls added immediately after the existing `ScreenEvents` registration:

```java
MinecraftForge.EVENT_BUS.register(new KeyEvents());
MinecraftForge.EVENT_BUS.register(new ClientPlayerEvents());
MinecraftForge.EVENT_BUS.register(new ClaimOverlayManager());
```

Plus three `CommandCategoryManager.register` calls added before the existing `WorkerCommandScreen` registration (priorities preserved from pre-consolidation `BannerlordMain.clientSetup`):

```java
CommandCategoryManager.register(new CombatCategory(), -3);
CommandCategoryManager.register(new MovementCategory(), -2);
CommandCategoryManager.register(new OtherCategory(), -1);
```

Audit (per plan `<handler_audit>`): three `@SubscribeEvent` classes registered. `civilian/ClientEvent`, `military/events/ClientEvent`, `military/events/ClientSyncLifecycleEvents` were deliberately NOT registered because they carry `@Mod.EventBusSubscriber` and auto-register via Forge.

## Runtime confirmation

- `./gradlew --no-daemon compileJava`: BUILD SUCCESSFUL on both commits (15s each).
- All three `EVENT_BUS.register` and three `CommandCategoryManager.register` lines present in `BannerModMain.clientSetup()`.
- User-reported on 2026-04-15: R / U / M each open their respective screens in dev-client; Command screen shows all four tabs in expected order (Combat / Movement / Other / Workers); no `IllegalArgumentException: Cannot register an object` in `latest.log`.

## Commits

- `202a18a` fix(21-12): register client-side event handlers in BannerModMain.clientSetup
- `f98245c` fix(21-12.1): register CombatCategory/MovementCategory/OtherCategory in clientSetup

## Unblocked by this fix

- UAT test 8 (network packet round-trip — recruit command + work area update) can now be re-attempted: with the Command screen reachable AND populated with the recruits-side categories, the user can issue a recruit command and validate the round-trip half of the original test.
- Claim overlay HUD now renders again (previously silently disabled after consolidation).

## Pointers

- `src/main/java/com/talhanation/bannermod/bootstrap/BannerModMain.java:14-17` (client imports added)
- `src/main/java/com/talhanation/bannermod/bootstrap/BannerModMain.java:144-167` (clientSetup body)
- `.planning/phases/21-source-tree-consolidation-into-bannerlord/21-UAT.md` — test 8 + Gaps section
