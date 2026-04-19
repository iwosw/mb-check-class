---
phase: 23-settlement-governance-and-governor-control
verified: 2026-04-15T23:55:00Z
status: gaps_found
score: 14/15 must-haves verified
overrides_applied: 0
gaps:
  - truth: "Phase 23 closes with root GameTest evidence, not only unit tests or UI compilation."
    status: failed
    reason: "The phase-level validation command `./gradlew compileJava test compileGameTestJava verifyGameTestStage --console=plain` does not pass. `compileTestJava` fails with 39 errors in unrelated root-test files (settlement-binding type mismatches, missing `MergedRuntimeCleanupPolicy` symbol) and `compileGameTestJava` fails with 34 errors in unrelated gametest files (stale `Main.MOD_ID`, `com.talhanation.recruits.ClaimEvents`, `com.talhanation.workers.*` references, plus unvendored `RecruitsBattleGameTestSupport`/`RecruitsCommandGameTestSupport`). Neither `BannerModGovernorControlGameTests` nor any other GameTest can actually run until that consolidation debt from Phase 21 is cleaned up. The three plan-scoped Phase 23 gametest files are correct in isolation, but no executable GameTest evidence exists for GOV-04 yet. No later roadmap phase (24 logistics, 25 treasury, 26 supply, 27 combat, 28 telemetry) schedules this cleanup, so it will not self-resolve."
    artifacts:
      - path: "src/gametest/java/com/talhanation/bannermod/BannerModGovernorControlGameTests.java"
        issue: "File is syntactically valid and references the real BannerModGovernorService/BannerModGovernorHeartbeat, but cannot be compiled or run because sibling gametest files in the same source set fail compilation."
      - path: "src/test/java/com/talhanation/workers/WorkerSettlementSpawnRulesTest.java"
        issue: "39 pre-existing compileTestJava errors (settlement-binding type mismatches between `com.talhanation.bannermod.shared.settlement.BannerModSettlementBinding` and legacy `com.talhanation.bannermod.settlement.BannerModSettlementBinding`) block the root `test` task."
      - path: "src/gametest/java/com/talhanation/bannermod/BannerModDedicatedServerReconnectGameTests.java"
        issue: "Representative of ~16 gametest files with stale `Main.MOD_ID` / `com.talhanation.recruits.ClaimEvents` references + missing vendored support classes."
    missing:
      - "A root-test compile fix plan (30-minute mechanical sweep): remove/migrate `WorkerSettlementSpawnRulesTest`, `MergedRuntimeCleanupPolicyTest`, and the remaining Phase-21 legacy fixtures so `./gradlew test` at least compiles."
      - "A gametest-tree consolidation plan: vendor `RecruitsBattleGameTestSupport` and `RecruitsCommandGameTestSupport` into `src/gametest/java/com/talhanation/bannermod/gametest/support/`, plus a bulk `Main.MOD_ID` → `BannerModMain.MOD_ID` and `com.talhanation.{recruits,workers}.*` → `com.talhanation.bannermod.{events,bootstrap,civilian}.*` rewrite across the ~16 affected gametest files."
      - "Alternatively: an explicit ROADMAP entry (e.g., Phase 21 re-opened, or a new Phase 23a) that captures this cleanup and acknowledges Phase 23 GOV-04 is contingent on it."
human_verification:
  - test: "Governor control screen layout, copy clarity, and live report behavior in a dev client"
    expected: "Promote a recruit to governor (profession id 6) in a friendly claim, open the governor screen, see live governance status, tax summary, garrison/fortification recommendations, and be able to step the three policy toggles (garrison priority, fortification priority, tax pressure) with server-side persistence across screen reopens."
    why_human: "Carried from 23-VALIDATION.md as a manual-only verification. Automated checks confirm packet wiring and snapshot persistence, but UI readability and end-to-end promote→screen→toggle flow is hard to prove without an interactive session."
---

# Phase 23: Settlement Governance And Governor Control — Verification Report

**Phase Goal (from ROADMAP.md):** "Settlement governance becomes a first-class gameplay system rather than an implied side effect of ownership and work areas." Phase 23 must deliver a real governor role that rules a claim-bound settlement, collects local taxes, coordinates garrison/fortification recommendations, reports incidents/shortages, and keeps governor authority layered on settlement binding + faction/claim legality.

**Verified:** 2026-04-15T23:55:00Z
**Status:** gaps_found (1 blocking gap + 1 human verification item)
**Re-verification:** No — initial verification.

## Goal Achievement

### Observable Truths

| # | Truth | Status | Evidence |
| --- | --- | --- | --- |
| 1 | Governance state exists as a persisted claim-keyed seam, not ad-hoc fields on claims/recruits. | VERIFIED | `src/main/java/com/talhanation/bannermod/governance/BannerModGovernorSnapshot.java` + `BannerModGovernorManager.java` store snapshots keyed by claim UUID in a `SavedData`-style map; `RecruitsClaim.java` is untouched. |
| 2 | Governor assignment can be described against a friendly claim without creating a second settlement world model. | VERIFIED | `BannerModGovernorRules.java` imports `com.talhanation.bannermod.shared.settlement.BannerModSettlementBinding` and delegates legality to its `Binding.Status`; no settlement manager introduced. |
| 3 | Downstream plans call one pure governance rule layer before mutating state. | VERIFIED | `BannerModGovernorRules.assignmentDecision(...)` and `controlDecision(...)` are pure static helpers over binding + authority inputs; called from `BannerModGovernorService` and `BannerModGovernorAuthority`. |
| 4 | One recruit can be designated governor without spawning a new entity type. | VERIFIED | `RecruitEvents.promoteRecruit` branches `profession == 6` into `BannerModGovernorService.assignGovernor(claim, player, recruit)` with the existing `AbstractRecruitEntity` UUID; no new `EntityType` registered. |
| 5 | Governor assignment/revocation respects owner/admin/friendly-claim authority. | VERIFIED | `BannerModGovernorAuthority` reuses `com.talhanation.bannermod.shared.authority.BannerModAuthorityRules.recoverControlDecision`; `BannerModGovernorServiceTest` exercises owner/admin allow + same-team/outsider/hostile/degraded/unclaimed denial. |
| 6 | Governor designation persists independently of transient entity state. | VERIFIED | `BannerModGovernorManager` (SavedData) persists `BannerModGovernorSnapshot` records; `BannerModGovernorRulesTest` includes persistence round-trip coverage. |
| 7 | Governors publish bounded settlement outputs instead of a title with no gameplay effect. | VERIFIED | `BannerModGovernorHeartbeat.java` computes local tax totals, incidents, and recommendations from `BannerModSupplyStatus` + claim state; rendered in `GovernorScreen`. |
| 8 | Phase 23 collects local tax state, garrison guidance, fortification suggestions, and incidents without forcing treasury or logistics rewrites. | VERIFIED | `BannerModGovernorIncident` + `BannerModGovernorRecommendation` enums define compact tokenised vocabulary; heartbeat does not mutate faction treasuries or logistics routes. |
| 9 | Governance updates run on a heartbeat and can be revalidated automatically. | VERIFIED | `ClaimEvents.java:3,115` imports `BannerModGovernorHeartbeat` and invokes `runGovernedClaimHeartbeat(level, recruitsClaimManager, BannerModGovernorManager.get(level))` gated on `TickEvent.Phase.END`. |
| 10 | The governor promotion slot is a real action, not a dead button. | VERIFIED | `PromoteScreen.java:121` now passes `canDesignateGovernor()` (eligibility-gated) for `BUTTON_GOVERNOR`; `MessagePromoteRecruit` routes profession id 6 into `RecruitEvents.promoteRecruit` which calls `BannerModGovernorService.assignGovernor`. |
| 11 | Players can open a dedicated governor control surface that shows governance status and outputs. | VERIFIED | `GovernorScreen.java`, `GovernorContainer.java`, `MessageOpenGovernorScreen.java`, and `RecruitEvents.openGovernorScreen(...)` form an open-path chain that opens the screen and kicks off `syncGovernorScreen`. |
| 12 | Governor control UI reads live governed-settlement data. | VERIFIED | `MessageToClientUpdateGovernorScreen` carries settlement status, tax summary, garrison/fortification recommendations, and incident tokens from the real snapshot (`RecruitEvents.syncGovernorScreen`:176-199). `GovernorScreen` renders those fields, not placeholder strings. |
| 13 | Players can view and change the three bounded governor policy toggles (garrison priority, fortification priority, tax pressure). | VERIFIED | `BannerModGovernorPolicy` enum + `MessageUpdateGovernorPolicy` packet + `BannerModGovernorService.updatePolicy(...)` + `GovernorScreen.stepPolicy(...)` with `+`/`-` buttons for each of the three policies at y-offsets 88/112/136. Values persist on `BannerModGovernorSnapshot`. |
| 14 | Automated validation proves degraded/hostile denial and friendly designation paths. | VERIFIED | `BannerModGovernorServiceTest` + `BannerModGovernorHeartbeatTest` + `BannerModGovernorRulesTest` exist and assert those cases at the unit/service layer. `BannerModGovernorControlGameTests` defines three `@GameTest` methods (friendly designation/persistence, hostile-swap degradation, live heartbeat reporting) against the real service + heartbeat. |
| 15 | Phase 23 closes with **root GameTest evidence**, not only unit tests or UI compilation. | **FAILED** | `./gradlew compileJava test compileGameTestJava verifyGameTestStage --console=plain` aborts at `compileTestJava` with 39 errors in unrelated test files (pre-existing from Phase 21 consolidation), and `compileGameTestJava` separately fails with 34 errors across ~16 sibling gametest files. `BannerModGovernorControlGameTests` never actually executes. The "root GameTest evidence" success criterion is literally unmet. See gap below. |

**Score:** 14/15 truths verified.

### Required Artifacts

| Artifact | Expected | Status | Details |
| --- | --- | --- | --- |
| `src/main/java/com/talhanation/bannermod/governance/BannerModGovernorSnapshot.java` | Claim-keyed persisted snapshot | VERIFIED | Exists, 9528 bytes, persists claim UUID + policy fields + report tokens. |
| `src/main/java/com/talhanation/bannermod/governance/BannerModGovernorRules.java` | Pure allow/deny rules | VERIFIED | Exists, imports `shared.settlement.BannerModSettlementBinding`, used by service + authority. |
| `src/main/java/com/talhanation/bannermod/governance/BannerModGovernorManager.java` | SavedData manager by claim UUID | VERIFIED | Exists, `LinkedHashMap<UUID, BannerModGovernorSnapshot>`, `get`/`getSnapshot`/`saveSnapshot`/`removeSnapshot`. |
| `src/test/java/com/talhanation/bannermod/governance/BannerModGovernorRulesTest.java` | Friendly/degraded rule + persistence coverage | VERIFIED | Exists, contains `@Test` methods. Cannot currently run as part of `./gradlew test` due to unrelated `compileTestJava` failures. |
| `src/main/java/com/talhanation/bannermod/governance/BannerModGovernorAuthority.java` | Governor authority decisions | VERIFIED | Exists, reuses `shared.authority.BannerModAuthorityRules.recoverControlDecision`. |
| `src/main/java/com/talhanation/bannermod/governance/BannerModGovernorService.java` | Runtime assign/revoke/lookup | VERIFIED | Exists, 10545 bytes, exposes `assignGovernor`, `updatePolicy`, `getOrCreateGovernorSnapshot` over `BannerModGovernorManager`. |
| `src/test/java/com/talhanation/bannermod/governance/BannerModGovernorServiceTest.java` | Service-level auth coverage | VERIFIED | Exists, contains `@Test`; run blocked by same unrelated test-tree compile failures. |
| `src/main/java/com/talhanation/bannermod/governance/BannerModGovernorHeartbeat.java` | Heartbeat recomputation | VERIFIED | Exists, imports `BannerModSupplyStatus`, produces tax summary + tokens. |
| `src/main/java/com/talhanation/bannermod/governance/BannerModGovernorIncident.java` | Incident vocabulary | VERIFIED | Exists as enum with compact tokens. |
| `src/main/java/com/talhanation/bannermod/governance/BannerModGovernorRecommendation.java` | Recommendation vocabulary | VERIFIED | Exists as enum with compact tokens (`HOLD_COURSE`, `INCREASE_GARRISON`, `STRENGTHEN_FORTIFICATIONS`, ...). |
| `src/main/java/com/talhanation/bannermod/events/ClaimEvents.java` | Heartbeat hook | VERIFIED (modified) | Imports and calls `BannerModGovernorHeartbeat.runGovernedClaimHeartbeat`, gated on `TickEvent.Phase.END`. |
| `src/test/java/com/talhanation/bannermod/governance/BannerModGovernorHeartbeatTest.java` | Heartbeat report coverage | VERIFIED | Exists, contains `@Test`. |
| `src/main/java/com/talhanation/bannermod/client/military/gui/GovernorScreen.java` | Governor control UI | VERIFIED | Renders settlement status, tax, recommendations, incidents; `+`/`-` widgets for three policies. |
| `src/main/java/com/talhanation/bannermod/inventory/military/GovernorContainer.java` | Server-side container | VERIFIED | Exists (906 bytes, container seam); opened via factory in `RecruitEvents.openGovernorScreen`. |
| `src/main/java/com/talhanation/bannermod/network/messages/military/MessageOpenGovernorScreen.java` | Open-path packet | VERIFIED | Exists, used from both client (`GovernorScreen.init`) and server (`RecruitEvents.openGovernorScreen`). |
| `src/main/java/com/talhanation/bannermod/network/messages/military/MessageToClientUpdateGovernorScreen.java` | Report + policy sync | VERIFIED | Exists, carries settlement status, tax, incidents, recommendations, and three policy values. |
| `src/main/java/com/talhanation/bannermod/network/messages/military/MessageUpdateGovernorPolicy.java` | Policy change packet | VERIFIED | Exists, consumed by `RecruitEvents.updateGovernorPolicy` which calls `BannerModGovernorService.updatePolicy`. |
| `src/main/java/com/talhanation/bannermod/network/messages/military/MessagePromoteRecruit.java` | Promotion packet with governor branch | VERIFIED | Exists; `RecruitEvents.promoteRecruit` handles profession id 6 via the service. |
| `src/main/java/com/talhanation/bannermod/events/RecruitEvents.java` | Promotion, open, sync, policy-update wiring | VERIFIED (modified) | All four flows in place. |
| `src/main/java/com/talhanation/bannermod/registry/military/ModScreens.java` | Screen registration | NOT DIRECTLY VERIFIED | Not grepped in this pass; `GovernorContainer` is registered via `RegistryObject` pattern and the screen factory lambda in `RecruitEvents.openGovernorScreen` works with `SIMPLE_CHANNEL`. Compile-green (`./gradlew compileJava` PASSES) confirms registration is coherent. |
| `src/main/java/com/talhanation/bannermod/client/military/gui/PromoteScreen.java` | Governor button enabled | VERIFIED | Line 121 passes `canDesignateGovernor()` to `createProfessionButtons(..., 6, ...)`. |
| `src/gametest/java/com/talhanation/bannermod/BannerModGovernorControlGameTests.java` | Root GameTests for GOV-04 | VERIFIED (as source) / FAILING (as executable) | File exists, contains three `@GameTest` methods under `@GameTestHolder(BannerModMain.MOD_ID)`. Plan-scoped imports clean. **Cannot be compiled as part of `compileGameTestJava` or executed via `verifyGameTestStage`** because sibling gametest files (~16 of them) have pre-existing stale references. |
| `src/gametest/java/com/talhanation/bannermod/BannerModGameTestSupport.java` | Reusable helpers | VERIFIED | `spawnGovernorCandidateRecruit` helper added. |
| `src/gametest/java/com/talhanation/bannermod/BannerModDedicatedServerGameTestSupport.java` | Reusable claim/faction helpers | VERIFIED | `seedFriendlyLeaderClaim` helper added. |

### Key Link Verification

| From | To | Via | Status | Details |
| --- | --- | --- | --- | --- |
| `BannerModGovernorRules.java` | `shared.settlement.BannerModSettlementBinding` | friendly/degraded legality | WIRED | Explicit import at line 3, used in `assignmentDecision`, `controlDecision`, `settlementDecision`. |
| `BannerModGovernorManager.java` | `persistence.military.RecruitsClaim*` | claim UUID keyed persistence | WIRED | Snapshots keyed by `UUID` via `claimUuid()`; used from `RecruitEvents` with `claimManager.getClaim(...)`. |
| `BannerModGovernorAuthority.java` | `shared.authority.BannerModAuthorityRules` | owner/admin mapping | WIRED | Uses `recoverControlDecision(...)` and `Relationship` enum. |
| `BannerModGovernorService.java` | `BannerModGovernorManager.java` | claim-keyed snapshot mutation | WIRED | Service constructor takes manager; `assignGovernor`/`updatePolicy` mutate via `saveSnapshot`. |
| `BannerModGovernorHeartbeat.java` | `shared.logistics.BannerModSupplyStatus` | shortage/blocked-state reasons | WIRED | Imported at line 3, used in `WorkerSupplyStatus`/`RecruitSupplyStatus` mapping. |
| `ClaimEvents.java` | `BannerModGovernorHeartbeat.java` | server heartbeat invocation | WIRED | Line 115: `runGovernedClaimHeartbeat(level, recruitsClaimManager, BannerModGovernorManager.get(level))`. |
| `MessagePromoteRecruit.java` | `BannerModGovernorService.java` | profession id 6 governor designation branch | WIRED | `RecruitEvents.promoteRecruit` (line 96-114) branches on `profession == 6` and calls `service.assignGovernor`. |
| `GovernorScreen.java` | `MessageToClientUpdateGovernorScreen.java` | live report + policy toggle sync | WIRED | `latestState` static is updated by the client handler of that message; screen renders from `latestState`. |
| `BannerModGovernorControlGameTests.java` | `BannerModGovernorService.java` | live designation assertions | WIRED (as source), UNEXECUTED (as runtime) | Source asserts against real service; never actually runs. |
| `BannerModGovernorControlGameTests.java` | `BannerModGovernorHeartbeat.java` | live report/tax assertions | WIRED (as source), UNEXECUTED (as runtime) | Same as above. |

### Data-Flow Trace (Level 4)

| Artifact | Data Variable | Source | Produces Real Data | Status |
| --- | --- | --- | --- | --- |
| `GovernorScreen.java` | `latestState` | `MessageToClientUpdateGovernorScreen` → `RecruitEvents.syncGovernorScreen` | Yes — fields come from `service.getOrCreateGovernorSnapshot(claim)` (real manager state). | FLOWING |
| `BannerModGovernorHeartbeat.java` | tax/incident/recommendation tokens | Computed each tick end from claim + supply status + villager/worker counts | Yes — `runGovernedClaimHeartbeat` reads the live `BannerModGovernorManager` for each governed claim and writes tokens back. | FLOWING |
| `BannerModGovernorService.java` | snapshot under mutation | `BannerModGovernorManager.getOrCreateSnapshot` | Yes — real persisted map. | FLOWING |

### Behavioral Spot-Checks

| Behavior | Command | Result | Status |
| --- | --- | --- | --- |
| Production code compiles | `./gradlew compileJava --console=plain` | `BUILD SUCCESSFUL in 7s` | PASS |
| Full phase validation gate (compile + test + gametest + verify) | `./gradlew compileJava test compileGameTestJava verifyGameTestStage --console=plain` | `BUILD FAILED in 9s` — 39 `compileTestJava` errors in `src/test/java/com/talhanation/{bannermod,workers}/**` (pre-existing, unrelated to governance). | FAIL |
| Root gametest compile | `./gradlew compileGameTestJava --console=plain` | `BUILD FAILED in 9s` — 34 errors across ~16 gametest files for stale `Main.MOD_ID` / `com.talhanation.recruits.ClaimEvents` / `com.talhanation.workers.*` / missing `RecruitsBattleGameTestSupport` / `RecruitsCommandGameTestSupport`. None are in Phase 23 plan-scoped files. | FAIL |
| Targeted governance unit tests | `./gradlew test --tests com.talhanation.bannermod.governance.* --console=plain` | Blocked by root `compileTestJava` failure before any governance test class runs. | FAIL (indirect) |

### Requirements Coverage

| Requirement | Source Plan(s) | Description | Status | Evidence |
| --- | --- | --- | --- | --- |
| GOV-01 | 23-01 | Settlement governance state exists as one explicit persisted claim-keyed seam with pure legality rules before live governor assignment, heartbeat, or UI wiring lands. | SATISFIED | `BannerModGovernorSnapshot`, `BannerModGovernorRules`, `BannerModGovernorManager` present and used. REQUIREMENTS.md checks `[x]`. |
| GOV-02 | 23-02, 23-04 | Governor designation and revocation route through authority-safe runtime services over existing recruit or citizen identities. | SATISFIED | `BannerModGovernorService` + `BannerModGovernorAuthority` + `RecruitEvents.promoteRecruit` profession-6 branch. REQUIREMENTS.md checks `[x]`. |
| GOV-03 | 23-03, 23-04 | Governed claims publish bounded local tax, incident, and recommendation output without widening into treasury or logistics rewrites. | SATISFIED | `BannerModGovernorHeartbeat` + `ClaimEvents` wiring + compact `BannerModGovernorIncident`/`BannerModGovernorRecommendation` enums. REQUIREMENTS.md checks `[x]`. |
| GOV-04 | 23-05 | Governor control is validated through focused GameTests and player-facing UI flows over the real server-side governance snapshot. | **NEEDS HUMAN + BLOCKED** | GameTest **sources** for designation, hostile-swap, and heartbeat-reporting exist and reference the real service/heartbeat. However `verifyGameTestStage` does not run due to unrelated tree-wide compile debt. Player-facing UI flow is a manual/human-verification item (see 23-VALIDATION.md). REQUIREMENTS.md correctly shows `[ ]` (unchecked). |

No orphaned requirement IDs: every plan's `requirements:` entry maps to GOV-01..GOV-04 and every GOV-* ID is claimed by at least one plan.

### Anti-Patterns Found

| File | Line | Pattern | Severity | Impact |
| --- | --- | --- | --- | --- |
| — | — | No TODO/FIXME/PLACEHOLDER hot-spots detected in new Phase 23 code. | Info | Phase 23 governance code is production-shaped, not stub-shaped. |
| `ClaimEvents.java` | 115 | Heartbeat cadence gate is coarse (`TickEvent.Phase.END` + counter). Good — matches D-09. | Info | Intentional; documented in 23-03 SUMMARY as a Rule 1 auto-fix. |
| `GovernorScreen.java` | 26 | `private static GovernorViewState latestState = GovernorViewState.empty();` — static mutable shared across screen instances. | Info | Standard pattern in this repo for client-side packet-driven screens; not a regression. |

### Human Verification Required

1. **Governor control screen end-to-end flow**
   - **Test:** In a dev client, own a friendly claim with a citizen/worker population. Designate a recruit as governor via the promote screen. Open the governor screen.
   - **Expected:** Governor status, settlement status, local tax summary, incidents, garrison/fortification recommendations are all visible and non-placeholder. The three policy toggles (garrison priority, fortification priority, tax pressure) can be stepped `+`/`-` and values persist across screen reopen. Hostile swap of the claim's owning faction degrades the display (denial or incident-flagged).
   - **Why human:** UI readability/copy and smooth end-to-end flow cannot be verified from compile/grep alone. Carried from 23-VALIDATION.md manual-only table.

### Gaps Summary

**The governance code is strong; the validation gate is not.** Phase 23 has delivered a claim-keyed persisted governance seam, a pure rules layer, an authority-safe runtime service, a bounded heartbeat with tokenised reports, a live promotion path, a dedicated control screen with server-backed sync, and three bounded policy toggles. Data flows through real snapshots, real authority rules, and the real claim loop. `./gradlew compileJava` is green. Unit/service/heartbeat test **sources** exist for each governance concern.

**However, the single phase-level success criterion "Phase 23 closes with root GameTest evidence, not only unit tests or UI compilation" is literally unmet.** The phase's own validation command (`./gradlew compileJava test compileGameTestJava verifyGameTestStage --console=plain`) fails at `compileTestJava` with 39 pre-existing errors in unrelated test files, and `compileGameTestJava` separately fails with 34 errors across ~16 unrelated gametest files. The root cause is consolidation debt left over from Phase 21: stale `Main.MOD_ID` / `com.talhanation.recruits.*` / `com.talhanation.workers.*` references plus two never-vendored support classes. Plan 23-05 correctly identified this as Rule 4 architectural scope and deferred it — but no later phase in the ROADMAP (24, 25, 26, 27, 28, 30, 31) picks this cleanup up. The deferred-items note is accurate but self-orphaning.

**Judgement:** The deferral is legitimate on Rule 4 grounds (three-file plan scope vs. ~16-file tree-wide sweep), but it blocks the phase *goal*, not just a nice-to-have. GOV-04 is correctly unchecked in REQUIREMENTS.md for the same reason. Escalation to the developer is required to decide: (a) open a dedicated follow-up phase for root-test + gametest-tree consolidation before GOV-04 can be closed, (b) accept an override documenting that `BannerModGovernorControlGameTests` source integrity + unit-test coverage is enough evidence for Phase 23 closure pending a cleanup phase, or (c) do a short (likely < 1 day) Phase 23 extension plan that does the `Main.MOD_ID` + FQN sweep and vendors the two support classes so `verifyGameTestStage` actually runs.

---

*Verified: 2026-04-15T23:55:00Z*
*Verifier: Claude (gsd-verifier, Opus 4.6)*
