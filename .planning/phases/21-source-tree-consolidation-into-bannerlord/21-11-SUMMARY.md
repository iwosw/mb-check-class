---
phase: 21-source-tree-consolidation-into-bannerlord
plan: 11
type: execute
gap_closure: true
closed_gaps:
  - truth: "Right-clicking a recruit opens the Hire GUI without server-side crash"
    test: 8
    outcome: pass
    note: "User confirmed in dev-client: right-clicking a spawned recruit now opens the Hire GUI; no `recruitsPlayerUnitManager is null` NPE in server log. Two follow-up gaps surfaced during the same dev-client session and are filed below for 21-12 and 21-13."
completed: 2026-04-15T14:30Z
---

## Summary

Gap closure for Phase 21 UAT test 8 blocker — server-side `NullPointerException` thrown from `AbstractRecruitEntity.openHireGUI` because `RecruitEvents.recruitsPlayerUnitManager` was never initialized. Root cause: after the Phase-21 consolidation deprecated `recruits/src/main/java/com/talhanation/recruits/Main.java` to a no-op shim, the seven recruits-side `@SubscribeEvent` handler classes that used to be registered there were never re-wired in `BannerModMain.setup(FMLCommonSetupEvent)`. Their `onServerStarting` hooks (which assign the static manager fields read by gameplay code) therefore never fired.

## What changed

### `src/main/java/com/talhanation/bannermod/bootstrap/BannerModMain.java`

Imports added (alphabetical, after the existing `WorkersCommandEvents` import):

```java
import com.talhanation.bannermod.events.ClaimEvents;
import com.talhanation.bannermod.events.CommandEvents;
import com.talhanation.bannermod.events.DamageEvent;
import com.talhanation.bannermod.events.FactionEvents;
import com.talhanation.bannermod.events.PillagerEvents;
import com.talhanation.bannermod.events.RecruitEvents;
import com.talhanation.bannermod.events.VillagerEvents;
```

Body of `setup(FMLCommonSetupEvent)`, after the existing Workers registrations and before `register(this)`:

```java
// Recruits runtime events — ports the legacy recruits/Main.java registrations into the
// unified entrypoint. RecruitEvents.onServerStarting is what initializes the static
// recruitsPlayerUnitManager / recruitsGroupsManager fields read by AbstractRecruitEntity.
// Without these, right-click-to-hire (and every other recruits-side flow) trips an NPE.
// See 21-UAT.md gap "Right-clicking a recruit opens the Hire GUI without server-side crash".
MinecraftForge.EVENT_BUS.register(new RecruitEvents());
MinecraftForge.EVENT_BUS.register(new ClaimEvents());
MinecraftForge.EVENT_BUS.register(new FactionEvents());
MinecraftForge.EVENT_BUS.register(new CommandEvents());
MinecraftForge.EVENT_BUS.register(new DamageEvent());
MinecraftForge.EVENT_BUS.register(new PillagerEvents());
MinecraftForge.EVENT_BUS.register(new VillagerEvents());
```

Audit (per plan `<handler_audit>`): seven classes registered, each with at least one `@SubscribeEvent`-annotated method; abstract event-type classes (`RecruitEvent`, `ClaimEvent`, `FactionEvent`, etc.), `@Mod.EventBusSubscriber`-managed classes (`AttributeEvent`, `WorkersAttributeEvent`), dead-body `AssassinEvents`, and pure-utility `DebugEvents` were deliberately NOT registered.

### `.planning/phases/21-source-tree-consolidation-into-bannerlord/21-UAT.md`

- Test 8 `result: issue` → `result: pass` with resolution citing user dev-client confirmation.
- Test-8 gap `status: failed` → `closed`, `closed_by: 21-11-PLAN.md`.
- Two new gaps filed (see "New gaps for follow-up" below).

## Runtime confirmation (Task 1)

- `./gradlew --no-daemon compileJava`: BUILD SUCCESSFUL (16s, only pre-existing deprecation warnings).
- All seven `EVENT_BUS.register(new X())` lines present in `BannerModMain.java:116-122`.
- All seven new imports resolve (no "cannot find symbol").

## Runtime confirmation (Task 2 — human verification)

User-reported on 2026-04-15: right-click on a spawned recruit opens the Hire GUI; no server NPE; no recurrence of the two already-closed gaps (config collision, ForgeConfigSpec timing). Same dev-client session also surfaced two unrelated post-consolidation defects (filed below) — those do NOT regress the 21-11 fix.

## New gaps for follow-up

```
truth: "Recruits hotkey screens (Command/Faction/Map) and claim overlay open in dev client"
severity: blocker
test:    8 (and follow-on UI tests)
where:   src/main/java/com/talhanation/bannermod/client/military/events/KeyEvents.java
         src/main/java/com/talhanation/bannermod/client/military/events/ClientPlayerEvents.java
         src/main/java/com/talhanation/bannermod/client/military/gui/overlay/ClaimOverlayManager.java
cause:   Same Phase-21 consolidation defect class as the 21-11 fix, but for client-only
         handlers. Three classes carry @SubscribeEvent methods and are never registered
         on MinecraftForge.EVENT_BUS in BannerModMain.clientSetup(). KeyEvents owns the
         R/U/M hotkey handler that opens Command, Faction, and Map screens; without
         registration the keys are bound but no listener acts on them.
fix:     Add three EVENT_BUS.register(new X()) calls in BannerModMain.clientSetup()
         alongside the existing ScreenEvents registration. Filed as 21-12-PLAN.md.
```

```
truth: "All UI strings under gui.recruits.*, key.recruits.*, category.recruits, etc.
        render translated text instead of raw translation keys"
severity: major (UX, not a crash)
test:    7 (lang/assets/recipes resolve)
where:   src/main/resources/assets/bannermod/lang/{en_us,ru_ru,de_de,es_es,ja_jp,tr_tr}.json
cause:   Phase 21 (Wave 9) consolidated entity/item/block lang keys into bannermod
         namespace but did NOT carry the recruits-side UI keys (gui.recruits.*,
         key.recruits.*, category.recruits, chat.recruits.*, description.recruits.*,
         subtitles.recruits.*, recruits.*) over from `recruits/src/main/resources/
         assets/recruits/lang/*.json` (390 keys per locale in en_us). Code still
         references those keys verbatim (e.g., RecruitHireScreen.java:34 calls
         `Component.translatable("gui.recruits.hire_gui.text.hire")`), so they
         render as raw key strings. Note: test 7 was originally marked `pass` on the
         entity/item/block sample only; UI keys were not exercised because earlier
         blockers prevented opening any GUI.
fix:     Mechanically merge each `recruits/src/main/resources/assets/recruits/lang/
         <locale>.json` into the matching `src/main/resources/assets/bannermod/lang/
         <locale>.json`, copying every key whose first dotted segment is one of:
         gui, key, category, chat, description, subtitles, recruits, multiLineEditBox.
         Skip keys already present in the target file (do NOT clobber bannermod-
         namespaced entries from Wave 9). Filed as 21-13-PLAN.md.
```

## Unblocked by this fix

- UAT test 8 (network packet round-trip — recruit command + work area update) can resume once 21-12 ships (Command/Faction screens are needed to issue the command-side half of the round-trip).
- Latent NPEs on `recruitsClaimManager`, `recruitsFactionManager`, `recruitsDiplomacyManager`, `recruitsTreatyManager`, `recruitsGroupsManager` are all closed as a side effect — those static fields are now initialized by their respective `onServerStarting` handlers when a world starts.

## Pointers

- `src/main/java/com/talhanation/bannermod/bootstrap/BannerModMain.java:3-12` (imports)
- `src/main/java/com/talhanation/bannermod/bootstrap/BannerModMain.java:107-122` (registrations inside `setup()`)
- `.planning/phases/21-source-tree-consolidation-into-bannerlord/21-UAT.md` — test 8 + Gaps section
- Follow-ups: `21-12-PLAN.md` (client handlers), `21-13-PLAN.md` (lang merge)
