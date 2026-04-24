---
phase: 23-settlement-governance-and-governor-control
verified: 2026-04-24T06:30:00Z
status: verified_with_human_followup
score: 15/15 must-haves verified
overrides_applied: 0
gaps: []
human_verification:
  - test: "Governor control screen layout, copy clarity, and live report behavior in a dev client"
    expected: "Promote a recruit to governor (profession id 6) in a friendly claim, open the governor screen, see live governance status, tax summary, garrison/fortification recommendations, and be able to step the three policy toggles (garrison priority, fortification priority, tax pressure) with server-side persistence across screen reopens."
    why_human: "Carried from 23-VALIDATION.md as a manual-only verification. Automated checks confirm packet wiring and snapshot persistence, but UI readability and end-to-end promote→screen→toggle flow is hard to prove without an interactive session."
---

# Phase 23: Settlement Governance And Governor Control — Verification Report

**Phase Goal (from ROADMAP.md):** "Settlement governance becomes a first-class gameplay system rather than an implied side effect of ownership and work areas." Phase 23 must deliver a real governor role that rules a claim-bound settlement, collects local taxes, coordinates garrison/fortification recommendations, reports incidents/shortages, and keeps governor authority layered on settlement binding + faction/claim legality.

**Verified:** 2026-04-24T06:30:00Z
**Status:** verified_with_human_followup (automated gate green; manual UI flow still recommended)
**Re-verification:** Yes — updated after later root test/gametest consolidation and full root GameTest reruns.

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
| 15 | Phase 23 closes with **root GameTest evidence**, not only unit tests or UI compilation. | VERIFIED | `./gradlew compileJava test compileGameTestJava verifyGameTestStage --console=plain` is now runnable in the active tree, and the root GameTest suite is green with 39 required tests. `BannerModGovernorControlGameTests` lives in the active root GameTest source set and is no longer blocked by the old consolidation debt described in the initial 2026-04-15 verification. |

**Score:** 15/15 truths verified.

### Required Artifacts

| Artifact | Expected | Status | Details |
| --- | --- | --- | --- |
| `src/main/java/com/talhanation/bannermod/governance/BannerModGovernorSnapshot.java` | Claim-keyed persisted snapshot | VERIFIED | Exists, 9528 bytes, persists claim UUID + policy fields + report tokens. |
| `src/main/java/com/talhanation/bannermod/governance/BannerModGovernorRules.java` | Pure allow/deny rules | VERIFIED | Exists, imports `shared.settlement.BannerModSettlementBinding`, used by service + authority. |
| `src/main/java/com/talhanation/bannermod/governance/BannerModGovernorManager.java` | SavedData manager by claim UUID | VERIFIED | Exists, `LinkedHashMap<UUID, BannerModGovernorSnapshot>`, `get`/`getSnapshot`/`saveSnapshot`/`removeSnapshot`. |
| `src/test/java/com/talhanation/bannermod/governance/BannerModGovernorRulesTest.java` | Friendly/degraded rule + persistence coverage | VERIFIED | Exists in the active root test tree with `@Test` coverage; no longer blocked by the old compile-tree failures cited in the initial 2026-04-15 verification. |
| `src/main/java/com/talhanation/bannermod/governance/BannerModGovernorAuthority.java` | Governor authority decisions | VERIFIED | Exists, reuses `shared.authority.BannerModAuthorityRules.recoverControlDecision`. |
| `src/main/java/com/talhanation/bannermod/governance/BannerModGovernorService.java` | Runtime assign/revoke/lookup | VERIFIED | Exists, 10545 bytes, exposes `assignGovernor`, `updatePolicy`, `getOrCreateGovernorSnapshot` over `BannerModGovernorManager`. |
| `src/test/java/com/talhanation/bannermod/governance/BannerModGovernorServiceTest.java` | Service-level auth coverage | VERIFIED | Exists in the active root test tree with service-level auth coverage; no longer blocked by the old compile-tree failures cited in the initial 2026-04-15 verification. |
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
| `src/gametest/java/com/talhanation/bannermod/BannerModGovernorControlGameTests.java` | Root GameTests for GOV-04 | VERIFIED | File exists in the active root GameTest tree and contributes real executable GOV-04 evidence in the now-green root GameTest suite. |
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
| `BannerModGovernorControlGameTests.java` | `BannerModGovernorService.java` | live designation assertions | WIRED | Active root GameTest class over the real service seam. |
| `BannerModGovernorControlGameTests.java` | `BannerModGovernorHeartbeat.java` | live report/tax assertions | WIRED | Active root GameTest class over the real heartbeat seam. |

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
| Full phase validation gate (compile + test + gametest + verify) | `./gradlew compileJava test compileGameTestJava verifyGameTestStage --console=plain` | Root gate is now runnable in the active tree; latest root GameTest verification is green with 39 required tests. | PASS |
| Root gametest compile | `./gradlew compileGameTestJava --console=plain` | Active root GameTest source set compiles in the current tree. | PASS |
| Targeted governance unit tests | `./gradlew test --tests com.talhanation.bannermod.governance.* --console=plain` | No longer structurally blocked by the old compile-tree debt cited in the initial 2026-04-15 verification. | PASSABLE |

### Requirements Coverage

| Requirement | Source Plan(s) | Description | Status | Evidence |
| --- | --- | --- | --- | --- |
| GOV-01 | 23-01 | Settlement governance state exists as one explicit persisted claim-keyed seam with pure legality rules before live governor assignment, heartbeat, or UI wiring lands. | SATISFIED | `BannerModGovernorSnapshot`, `BannerModGovernorRules`, `BannerModGovernorManager` present and used. REQUIREMENTS.md checks `[x]`. |
| GOV-02 | 23-02, 23-04 | Governor designation and revocation route through authority-safe runtime services over existing recruit or citizen identities. | SATISFIED | `BannerModGovernorService` + `BannerModGovernorAuthority` + `RecruitEvents.promoteRecruit` profession-6 branch. REQUIREMENTS.md checks `[x]`. |
| GOV-03 | 23-03, 23-04 | Governed claims publish bounded local tax, incident, and recommendation output without widening into treasury or logistics rewrites. | SATISFIED | `BannerModGovernorHeartbeat` + `ClaimEvents` wiring + compact `BannerModGovernorIncident`/`BannerModGovernorRecommendation` enums. REQUIREMENTS.md checks `[x]`. |
| GOV-04 | 23-05 | Governor control is validated through focused GameTests and player-facing UI flows over the real server-side governance snapshot. | NEEDS HUMAN FOLLOW-UP | Automated root GameTest evidence is now present in the active tree; the remaining follow-up is the manual UI flow from `23-VALIDATION.md`. |

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

**The old blocker is stale; the active Phase 23 gate is now structurally healthy.** Phase 23 has a claim-keyed persisted governance seam, a pure rules layer, an authority-safe runtime service, a bounded heartbeat with tokenised reports, a live promotion path, a dedicated control screen with server-backed sync, and focused root GameTests in the active `src/gametest/java/com/talhanation/bannermod/` tree. The earlier 2026-04-15 verification accurately captured the then-current tree debt, but later consolidation and verification work removed that blocker.

**Current judgement:** automated Phase 23 evidence is sufficient in the active tree. The remaining follow-up is the manual governor UI flow from `23-VALIDATION.md`, which is useful for UX/readability confirmation but no longer a blocking compile/gametest debt story.

---

*Verified: 2026-04-24T06:30:00Z*
*Verifier: Claude (gsd-verifier, Opus 4.6)*
