---
phase: 21-source-tree-consolidation-into-bannerlord
verified: 2026-04-15T15:11:49Z
status: passed
score: 7/7 must-haves verified
---

# Phase 21: Source Tree Consolidation Into BannerMod Verification Report

**Phase Goal:** BannerMod becomes one physical codebase instead of one root build that still composes legacy source trees.
**Verified:** 2026-04-15T15:11:49Z
**Status:** passed
**Re-verification:** Yes — previous docs-truth gap rechecked and closed.

## Goal Achievement

### Observable Truths

| # | Truth | Status | Evidence |
| --- | --- | --- | --- |
| 1 | Outer build uses one root source tree | ✓ VERIFIED | `build.gradle:46-64` only references outer `src/{main,test,gametest}/{java,resources}`. Fresh `rg` found no clone source-set paths. |
| 2 | Active production Java ownership lives under `com.talhanation.bannermod/**` | ✓ VERIFIED | `ls src/main/java/com/talhanation` returns only `bannermod`. |
| 3 | Unified runtime has one live mod entrypoint | ✓ VERIFIED | `rg -n '@Mod\(' src/main/java` returns only `BannerModMain.java:43`. |
| 4 | Shared network is wired through canonical BannerMod bootstrap | ✓ VERIFIED | `BannerModMain.java:131-132` assigns `SIMPLE_CHANNEL = BannerModNetworkBootstrap.createSharedChannel()`; `BannerModNetworkBootstrap.java:33-201` registers military then civilian catalogs; `workerPacketOffset()` returns `MILITARY_MESSAGES.length`. |
| 5 | Unified resources no longer carry stale runtime namespaces | ✓ VERIFIED | `mods.toml:7-24` has one `bannermod` mod block; `acquirable_job_site.json:3-8` uses `bannermod:*`; residual-namespace search returned no matches in `src/main/java`, `src/main/resources`, `build.gradle`, `ROADMAP.md`, or `STATE.md`. |
| 6 | Outer repo builds standalone from root | ✓ VERIFIED | Fresh `./gradlew compileJava` passed (`BUILD SUCCESSFUL in 48s`). |
| 7 | Planning docs reflect the realized post-pivot structure | ✓ VERIFIED | `ROADMAP.md:372-405` describes BannerMod-only consolidation truth; `STATE.md:17-21` now states Phase 21 complete, one source root, bannermod runtime, absorbed workers. Remaining `bannerlord` matches in `STATE.md` are only artifact-path names and historical decision ledger entries (`STATE.md:37-42`, `116-163`), not active current-state claims. |

**Score:** 7/7 truths verified

### Required Artifacts

| Artifact | Expected | Status | Details |
| --- | --- | --- | --- |
| `build.gradle` | Root-only source sets | ✓ VERIFIED | `sourceSets` point only at outer `src/**`; clone paths absent. |
| `src/main/java/com/talhanation/bannermod/bootstrap/BannerModMain.java` | Unified `@Mod` entrypoint | ✓ VERIFIED | Owns `MOD_ID = "bannermod"`, config registration, event-bus wiring, and shared-channel bootstrap. |
| `src/main/java/com/talhanation/bannermod/network/BannerModNetworkBootstrap.java` | Canonical shared channel + packet offset contract | ✓ VERIFIED | Real message catalogs and registration loops present; compatibility bind still intentional. |
| `src/main/resources/META-INF/mods.toml` | Single `bannermod` mod declaration | ✓ VERIFIED | One `[[mods]]` block with `modId="bannermod"`. |
| `.planning/ROADMAP.md` | Phase 21 reflects realized BannerMod structure | ✓ VERIFIED | Phase section is BannerMod-targeted, 13/13 plans closed, follow-up dirt explicitly scoped outside closure. |
| `.planning/STATE.md` | Current-state prose matches realized tree | ✓ VERIFIED | Active summary is bannermod-only; historical `bannerlord` strings are archive/history references, not stale live assertions. |

### Key Link Verification

| From | To | Via | Status | Details |
| --- | --- | --- | --- | --- |
| `build.gradle` | outer `src/**` | `sourceSets` | WIRED | Active Gradle build no longer composes `recruits/` or `workers/` sources/resources. |
| `BannerModMain` | `BannerModNetworkBootstrap` | `createSharedChannel()` | WIRED | Unified entrypoint initializes the shared channel from the canonical bootstrap. |
| `BannerModMain` | Forge config registry | `registerConfig(..., fileName)` | WIRED | Three explicit config filenames prevent the prior collision and match post-UAT docs. |
| `ROADMAP.md` / `STATE.md` | realized Phase 21 outcome | current-state prose | WIRED | Both docs now describe Bannermod convergence and clone-retention-as-archive truth. |

### Data-Flow Trace (Level 4)

| Artifact | Data Variable | Source | Produces Real Data | Status |
| --- | --- | --- | --- | --- |
| `BannerModMain.java` | `SIMPLE_CHANNEL` | `BannerModNetworkBootstrap.createSharedChannel()` | Yes | ✓ FLOWING |
| `BannerModNetworkBootstrap.java` | civilian packet offset | `MILITARY_MESSAGES.length` | Yes | ✓ FLOWING |
| `.planning/STATE.md` | current focus summary | maintained planning state | Yes | ✓ FLOWING |

### Behavioral Spot-Checks

| Behavior | Command | Result | Status |
| --- | --- | --- | --- |
| Root compile gate | `./gradlew compileJava` | `BUILD SUCCESSFUL in 48s` | ✓ PASS |
| Single mod entrypoint | `rg -n '@Mod\(' src/main/java` | one hit in `BannerModMain.java` | ✓ PASS |
| No clone source sets in active build | `rg -n 'recruits/src/main/java|workers/src/main/java|recruits/src/main/resources|workers/src/main/resources' build.gradle` | no output | ✓ PASS |
| No residual runtime namespace refs in active code/resources/docs sweep | `rg -n 'com\.talhanation\.bannerlord|"recruits:"|"workers:"' src/main/java src/main/resources build.gradle .planning/ROADMAP.md .planning/STATE.md` | no output | ✓ PASS |

### Requirements Coverage

| Requirement | Source Plan | Description | Status | Evidence |
| --- | --- | --- | --- | --- |
| SRCMOVE-01 | 21-02 | Canonical shared seams under `bannermod.shared/**` with deprecated forwarders at legacy seam paths | ✓ SATISFIED | `shared/authority/BannerModAuthorityRules.java` is canonical; `authority/BannerModAuthorityRules.java` is `@Deprecated` and delegates. |
| SRCMOVE-02 | 21-03..21-08 | Recruit military/shared and worker civilian gameplay re-homed under canonical `bannermod` packages | ✓ SATISFIED | Production tree under `src/main/java/com/talhanation/` contains only `bannermod/`; compile passes from root. |
| SRCMOVE-03 | 21-09 | Remaining recruit-package surface reduced to narrow compatibility/archive seams | ✓ SATISFIED | Outer build excludes clone paths; retained clone content is archival only and not part of active source sets. |
| SRCMOVE-04 | 21-09 | Remaining worker-package surface reduced to narrow compatibility/archive seams with canonical civilian ownership under `bannermod.*.civilian/**` | ✓ SATISFIED | Civilian code lives under `bannermod.*.civilian/**`; root build no longer composes `workers/src/**`. |
| UAT-21-TEST-8 | 21-11, 21-12 | Post-UAT recruit interaction and hotkey/client-handler fixes | ⚠️ PLAN-LOCAL | Referenced by plan frontmatter but not defined in `.planning/REQUIREMENTS.md`; closure is documented in `21-11-SUMMARY.md` and `21-12-SUMMARY.md`. |
| UAT-21-TEST-7 | 21-13 | Post-UAT lang/UI localization gap closure | ⚠️ PLAN-LOCAL | Referenced by plan frontmatter but not defined in `.planning/REQUIREMENTS.md`; closure is documented in `21-13-SUMMARY.md`. |

No orphaned Phase 21 requirement IDs were found in `REQUIREMENTS.md`; all Phase 21 requirement IDs listed there (`SRCMOVE-01`..`SRCMOVE-04`) are claimed by plan frontmatter.

### Anti-Patterns Found

| File | Line | Pattern | Severity | Impact |
| --- | --- | --- | --- | --- |
| `build.gradle` | 40 | `group = 'com.talhanation.recruits'` legacy coordinate | ℹ️ Info | Packaging coordinate lag only; does not reintroduce clone source trees or block Phase 21 closure. |
| `.planning/phases/21-source-tree-consolidation-into-bannerlord/*-PLAN.md` | various | `UAT-21-TEST-*` IDs not mirrored in `REQUIREMENTS.md` | ℹ️ Info | Accounting gap in requirements indexing, not a failure of source-tree consolidation. |

### Human Verification Required

None.

### Gaps Summary

No closure-blocking gaps remain. The prior verification's `.planning/STATE.md` failure does not hold on re-check: the active current-state section is now aligned with the realized BannerMod-only structure, and the remaining `bannerlord` literals are confined to historical phase names, artifact paths, and decision-ledger context. Those references preserve auditability rather than contradict present-day codebase truth.

Phase 21's goal is achieved: the active build, active production Java tree, active resources, mod entrypoint, and planning docs all describe and use one physical BannerMod codebase rather than composing legacy source roots.

---

_Verified: 2026-04-15T15:11:49Z_
_Verifier: the agent (gsd-verifier)_
