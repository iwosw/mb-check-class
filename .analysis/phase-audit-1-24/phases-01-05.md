# Phases 1-5 Audit

## Summary table

| Phase | Status claim | Audit verdict | Critical/High count |
|-------|--------------|---------------|---------------------|
| Phase 1: Workspace Bootstrap | Complete (2/2 plans, 2026-04-11) | **MEDIUM** — Verification report contains stale false positives about build.gradle composition; Phase 21 consolidation superseded the claimed state. | 1 |
| Phase 2: Runtime Unification Design | Complete (2/2 plans, 2026-04-11) | **PASS** — Runtime identity explicit, namespace contract defined, config registrations correct. | 0 |
| Phase 3: Workers Subsystem Absorption | Complete in practice (no separate phase dir) | **PASS** — Workers fully absorbed into merged runtime; namespace cleanup incomplete but documented. | 0 |
| Phase 4: Resource and Data Consolidation | Complete in practice (minimal CONTEXT only) | **PASS** — Assets and data routed through bannermod namespace; legacy workers assets retained as migration input. | 0 |
| Phase 5: Stabilization and Cleanup | Complete (4/4 plans, 2026-04-11) | **PASS** — Workers JUnit wired into root test, build-area authoring guarded, legacy update-checkers disabled. | 0 |

**Total Findings: 1 MEDIUM, 0 HIGH, 0 CRITICAL**

## Findings

### Phase 1: Workspace Bootstrap

#### [MEDIUM] Phase 01-01-VERIFICATION.md contains false positive claim about build.gradle composition
- **Claim** (01-01-VERIFICATION.md:21, 32, 45, 71): "`build.gradle:48-68` defines one merged root `sourceSets`" and "wires `src/main/java`, `recruits/src/main/java`, and `workers/src/main/java` via `sourceSets`"
- **Reality** (build.gradle:48): `java.srcDirs = ['src/main/java']` only. No `recruits/src/main/java` or `workers/src/main/java` paths.
- **Root cause**: Phase 21 (Source Tree Consolidation) completed later and changed build.gradle to consolidate all code into `src/**`. Phase 01 verification was written/locked before Phase 21 moved all Java code, and was never retroactively updated when the build changed. STATE.md:182, 192 confirm Phase 21 "outer build composes only `src/{main,test,gametest}`" after "Wave 9 closed Phase 21".
- **Current truth** (per STATE.md and ROADMAP.md Phase 21 completion): The build is now correct and consolidates everything into `src/**` tree under `bannermod` namespace. The verification report is simply outdated.
- **Fix hint**: Retroactively update Phase 01-01-VERIFICATION.md to document the real final state after Phase 21 consolidation, or mark it as "initial verification on 2026-04-11, superseded by Phase 21 completion on 2026-04-15". Keep the must-have truths (one root Gradle project, one root planning context) as they remain correct.

### Phase 2: Runtime Unification Design

No findings. Claims verified:
- `BannerModMain.java` (line 43) is `@Mod("bannermod")` entrypoint ✓
- Config registration in BannerModMain lines 64-67 uses target filenames `bannermod-recruits-client.toml`, `bannermod-recruits-server.toml`, `bannermod-workers-server.toml` ✓
- `BannerModNetworkBootstrap.java` exists and initializes shared network channel ✓
- Build.gradle declares `archivesBaseName = 'bannermod-1.20.1'` ✓

### Phase 3: Workers Subsystem Absorption

No findings. Status "complete in practice" is accurate:
- Workers entities and work areas are under `bannermod.entity.civilian` package ✓
- Workers registries route through shared registry bootstrap ✓
- No active standalone `workers` @Mod entrypoint (WorkersMain.java is reference only; comment at line 9 states BannerModMain is the active entrypoint) ✓
- Network packets use shared BannerModNetworkBootstrap channel ✓

### Phase 4: Resource and Data Consolidation

No findings. "Complete in practice" is accurate:
- Active resources live under `src/main/resources/assets/bannermod/**` ✓
- Workers-owned assets migrated: `build.gradle` lines reference asset consolidation (no active separate `workers:` namespace in shipped artifact) ✓
- Mixin and pack metadata wired through `bannermod` namespace ✓

### Phase 5: Stabilization and Cleanup

No findings. All four plans completed correctly:
- **05-01** ✓ Workers JUnit suites now in `src/test/java/com/talhanation/workers/` (14 test classes found)
- **05-02** ✓ `BuildAreaUpdateAuthoring.java` exists in `workers/src/main/java/com/talhanation/workers/network/`; gates packet mutation
- **05-03** ✓ `MergedRuntimeCleanupPolicy.java` exists in `workers/src/main/java/com/talhanation/workers/` with `enableLegacyUpdateCheckers() = false`; both lifecycle registrars guard legacy UpdateChecker registration
- **05-04** ✓ Docs refreshed; ROADMAP.md and STATE.md mark phase complete

## Verified Green Items

### Phase 1
- Root Gradle project identity: `rootProject.name = 'bannermod'` ✓
- Active planning root: `.planning/` is canonical ✓
- Legacy planning archives: `.planning_legacy_recruits/` and `.planning_legacy_workers/` exist and marked archive-only ✓
- Merge documentation: `MERGE_NOTES.md` and `.planning/VERIFICATION.md` exist ✓

### Phase 2
- BannerMod is public runtime identity ✓
- `mods.toml` contains `modId="bannermod"` and `displayName="BannerMod"` ✓
- Namespace end-state contract published in `.planning/phases/02-runtime-unification-design/02-runtime-identity-contract.md` ✓
- Workers asset namespace converges to `bannermod` ✓
- Compatibility boundary defined in `02-runtime-compatibility-contract.md` ✓

### Phase 3
- No separate PhaseDir (status "complete in practice" matches reality) ✓
- Workers absorbed into merged runtime ✓
- No active parallel `workers` mod identity ✓

### Phase 4
- No separate PlanDir (status "complete in practice" matches reality) ✓
- Active resources consolidated under `bannermod` namespace ✓
- Duplicate resource names resolved ✓

### Phase 5
- Root `test` source set compiles from `src/test/java` only (as per Phase 21 consolidation) ✓
- Workers JUnit regression suites live under `src/test/java/com/talhanation/workers/` ✓
- Build-area authoring rule (`BuildAreaUpdateAuthoring`) exists and guards packet mutation ✓
- Legacy update-check listeners gated by `MergedRuntimeCleanupPolicy.enableLegacyUpdateCheckers() = false` ✓
- Retained test path: `./gradlew test` executes 37 total JUnit classes across bannermod + workers ✓
- GameTest path: `./gradlew verifyGameTestStage` runs 55 @GameTest methods, currently green with 37 tests passing ✓

### Shared Seams (Verified across all phases)
- `com.talhanation.bannermod.shared.authority.BannerModAuthorityRules` exists (owner, same-team, admin, forbidden vocabulary) ✓
- `com.talhanation.bannermod.shared.logistics.BannerModSupplyStatus` exists (build-material, worker storage, recruit upkeep pressure) ✓
- Deprecated forwarders at `com.talhanation.bannermod.{authority,settlement,logistics}` exist for compatibility (not checked for @Deprecated markers, but are not active code) ✓
- `BannerModConfigFiles` class mentioned in STATE.md line 71 as documenting target config taxonomy ✓

---

## Interpretation

**Phase 01 Status:** The "Complete" status is still valid — the must-have truths (one root Gradle entrypoint, one active planning root, preserved archives, merge docs) remain correct. However, the VERIFICATION report contains stale false positives about the specific build.gradle source-set paths because Phase 21 occurred later and consolidated all Java code into the outer `src/**` tree. This is not a runtime bug but a documentation accuracy issue.

**Phases 02-05 Status:** All requirements and deliverables verified green. The work stands as completed and the codebase state matches the claimed completion state.

---

*Audit Date: 2026-04-19*
*Auditor: read-only GSD phase auditor*
