---
phase: 03-battle-and-formation-regression-lockdown
verified: 2026-04-06T12:04:39Z
status: gaps_found
score: 3/4 must-haves verified
---

# Phase 3: Battle and Formation Regression Lockdown Verification Report

**Phase Goal:** Players can run representative recruit battles and formation flows without obvious regressions, and maintainers can catch battle-heavy failures automatically.
**Verified:** 2026-04-06T12:04:39Z
**Status:** gaps_found

## Goal Achievement

### Observable Truths

| # | Truth | Status | Evidence |
|---|-------|--------|----------|
| 1 | Player can run representative recruit battles on the dev branch without obvious combat-state regressions. | ✗ FAILED | `./gradlew check --continue` failed in `runGameTestServer`; `LogTestReporter` reported `baselinedensebattlecompleteswithoutbrokenloops` and `heavierdensebattlecompleteswithoutbrokenloops` missing their stability deadlines. |
| 2 | Player can issue formation-related commands and observe stable formation behavior during movement and combat. | ✓ VERIFIED | `FormationRecoveryGameTests.java` exists, is wired through `MessageMovement` / `MessageFormationFollowMovement`, and key-link verification passed 3/3 for plan `03-03`. |
| 3 | Maintainer can run automated tests that cover both expected battle flows and regression-prone edge cases. | ✓ VERIFIED | `./gradlew test --tests com.talhanation.recruits.entities.ai.controller.BattleTacticDeciderTest` passed; plan artifact verification passed for the JVM seam plus mixed-battle and formation GameTest classes. |
| 4 | Maintainer can use defined baseline or stress scenarios to detect battle-density regressions instead of relying only on ad hoc play sessions. | ✓ VERIFIED | `BattleStressFixtures.java` and `BattleStressGameTests.java` exist and are wired; the canonical `check --continue` run surfaced the dense-battle regression automatically. |

**Score:** 3/4 truths verified

### Required Artifacts

| Artifact | Expected | Status | Details |
|----------|----------|--------|---------|
| `src/gametest/java/com/talhanation/recruits/gametest/support/RecruitsBattleGameTestSupport.java` | Reusable mixed-squad battle helpers | ✓ EXISTS + SUBSTANTIVE | Artifact verification passed; helper owns deterministic spawn/loadout setup. |
| `src/gametest/java/com/talhanation/recruits/gametest/support/RecruitsFormationAsserts.java` | Observable formation assertions | ✓ EXISTS + SUBSTANTIVE | Artifact verification passed; asserts hold-position, return intent, spacing, and anchors. |
| `src/main/java/com/talhanation/recruits/entities/ai/controller/BattleTacticDecider.java` | Extracted JVM-testable battle tactic seam | ✓ EXISTS + SUBSTANTIVE | Artifact verification passed; backed by passing targeted JUnit run. |
| `src/gametest/java/com/talhanation/recruits/gametest/battle/MixedSquadBattleGameTests.java` | Representative battle GameTests | ✓ EXISTS + SUBSTANTIVE | Artifact verification passed for plan `03-03`. |
| `src/gametest/java/com/talhanation/recruits/gametest/battle/FormationRecoveryGameTests.java` | Command-path formation recovery GameTests | ✓ EXISTS + SUBSTANTIVE | Artifact verification passed for plan `03-03`. |
| `src/gametest/java/com/talhanation/recruits/gametest/battle/BattleStressFixtures.java` | Dense battle scenario matrix | ✓ EXISTS + SUBSTANTIVE | Artifact verification passed for plan `03-04`. |
| `src/gametest/java/com/talhanation/recruits/gametest/battle/BattleStressGameTests.java` | Battle-density stability alarms | ⚠️ EXISTS + FAILING BEHAVIOR | File is present and wired, but both dense-battle scenarios fail under the canonical Forge GameTest run. |
| `src/gametest/resources/data/recruits/structures/battle_density_field.nbt` | Dedicated dense-battle battlefield | ✓ EXISTS + WIRED | File exists and is referenced by `@GameTest(template = "battle_density_field")`; artifact verifier's missing-pattern warning is a binary NBT false negative. |

**Artifacts:** 7/8 verified without blocker; 1/8 exposes an active runtime gap

### Key Link Verification

| From | To | Via | Status | Details |
|------|----|----|--------|---------|
| `BattleHarnessGameTests.java` | `battle_harness_field.nbt` | `@GameTest` template | ✓ WIRED | Key-link verification passed for plan `03-01`. |
| `PatrolLeaderAttackController.java` | `BattleTacticDecider.java` | shared tactic selection | ✓ WIRED | Key-link verification passed for plan `03-02`. |
| `MessageMovement.java` | `CommandEvents.java` | packet handler dispatch | ✓ WIRED | Key-link verification passed for plan `03-03`. |
| `MessageFormationFollowMovement.java` | `CommandEvents.java` | formation packet dispatch | ✓ WIRED | Key-link verification passed for plan `03-03`. |
| `FormationRecoveryGameTests.java` | `FormationUtils.java` | observable formation assertions | ✓ WIRED | Key-link verification passed for plan `03-03`. |
| `BattleStressFixtures.java` | `BattleStressGameTests.java` | shared scenario matrix | ✓ WIRED | Key-link verification passed for plan `03-04`. |
| `BattleStressGameTests.java` | `battle_density_field.nbt` | `@GameTest` template | ✓ WIRED | Key-link verification passed for plan `03-04`. |

**Wiring:** 7/7 connections verified

## Requirements Coverage

| Requirement | Status | Blocking Issue |
|-------------|--------|----------------|
| `BATL-01`: Player can run representative recruit battles without obvious combat-state regressions in the current dev branch | ✗ BLOCKED | Dense battle GameTests do not resolve before their configured deadlines. |
| `BATL-02`: Player can issue formation-related commands and observe stable formation behavior in representative combat and movement scenarios | ✓ SATISFIED | Packet-driven formation recovery coverage is present and wired. |
| `BATL-03`: Maintainer can verify battle-critical behaviors with automated tests that cover both expected flows and regression-prone edge cases | ✓ SATISFIED | JVM tactic tests plus multiple Forge GameTest battle/formation classes are present. |
| `BATL-04`: Maintainer can detect battle-density regressions through defined stress or baseline scenarios instead of ad hoc manual play only | ✓ SATISFIED | Dense battle stress scenarios exist and failed loudly during the canonical verification run. |

**Coverage:** 3/4 requirements satisfied

## Anti-Patterns Found

| File | Line | Pattern | Severity | Impact |
|------|------|---------|----------|--------|
| `/home/kaiserroman/.local/share/opencode/tool-output/tool_d62ace0600016QwXtkjL74WSIQ` | 1334 | `baselinedensebattlecompleteswithoutbrokenloops failed!` | 🛑 Blocker | Baseline dense battle does not resolve within the configured bound. |
| `/home/kaiserroman/.local/share/opencode/tool-output/tool_d62ace0600016QwXtkjL74WSIQ` | 1340 | `heavierdensebattlecompleteswithoutbrokenloops failed!` | 🛑 Blocker | Heavy dense battle also misses the configured stability deadline. |

**Anti-patterns:** 2 found (2 blockers, 0 warnings)

## Human Verification Required

None — the blocking issue is already reproduced programmatically by the canonical verification suite.

## Gaps Summary

### Critical Gaps (Block Progress)

1. **Dense battle scenarios do not resolve under canonical verification**
   - Missing: Stable dense-combat behavior that completes within the configured BattleStress deadlines.
   - Impact: Phase 3 cannot be considered complete while `./gradlew check --continue` is red on the new battle-density alarms.
   - Fix: Investigate whether the regression is in battle behavior, target persistence, or the new stress thresholds/layouts, then update code or thresholds with supporting coverage.

## Recommended Fix Plans

### 03-05-PLAN.md: Stabilize dense battle stress scenarios

**Objective:** Make the new battle-density GameTests pass under the canonical `check --continue` flow.

**Tasks:**
1. Reproduce the dense-battle failures with focused GameTest runs and isolate whether recruits are stalling, pathing, or holding stale combat state.
2. Fix the blocking combat or fixture threshold issue with regression coverage in `BattleStressGameTests` and any affected gameplay code.
3. Re-run `./gradlew verifyGameTestStage` and `./gradlew check --continue` to confirm all Phase 3 battle and formation verification is green.

**Estimated scope:** Small / Medium

---

## Verification Metadata

**Verification approach:** Goal-backward using Phase 3 success criteria from `ROADMAP.md`
**Must-haves source:** ROADMAP.md success criteria, cross-checked against PLAN frontmatter and key-link verification
**Automated checks:** 2 passed (`verifyUnitTestStage`, targeted JVM test), 1 failed (`runGameTestServer` inside `check --continue`)
**Human checks required:** 0
**Total verification time:** ~5 min

---
*Verified: 2026-04-06T12:04:39Z*
*Verifier: the agent (subagent)*
