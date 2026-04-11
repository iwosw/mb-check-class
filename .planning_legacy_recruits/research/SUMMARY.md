# Project Research Summary

**Project:** Villager Recruits stabilization and 1.21.1 migration prep
**Domain:** Brownfield Minecraft Forge NPC-army mod hardening and migration preparation
**Researched:** 2026-04-05
**Confidence:** HIGH

## Executive Summary

Villager Recruits is not a greenfield feature project; it is a brownfield stabilization effort for a large Forge NPC-army mod with heavy risk concentrated in persistence, networking, AI/entity behavior, client-state sprawl, and version-sensitive platform code. The research is consistent: experts would not attempt a direct 1.21.1 port or a broad rewrite first. They would freeze feature scope, harden the build, add a layered verification stack, and extract seams around the systems most likely to break during stabilization and migration.

The recommended approach is to keep the current branch on Forge 1.20.1 / Java 17, make builds reproducible, and introduce two test layers only: JUnit for extracted pure logic and Forge GameTests for real gameplay/runtime behavior. Architecturally, the mod should move toward a hexagonal-ish core with thin Forge adapters: persistent world systems, packet handling, and client read models should be decomposed before deep entity/pathfinding work. This gives the fastest trust gains while also shrinking the 1.21.1 migration surface.

The biggest risks are locking in broken legacy behavior, writing fake-confidence tests around engine mocks, missing server/client or thread boundaries, and treating persistence or migration prep as secondary. Mitigation is equally clear: define intended behavior before codifying it, use GameTests for runtime contracts, isolate version-sensitive adapters now, and keep this milestone tightly focused on stabilization, diagnostics, and migration-readiness seams rather than new gameplay.

## Key Findings

### Recommended Stack

The stack recommendation is intentionally conservative. Keep the stabilization branch on Forge `1.20.1-47.4.10`, Mojang `official` mappings, and Java 17, while preparing extracted pure-Java logic and tooling for the Java 21 baseline required by Forge 1.21.x. Build reproducibility is a first-order requirement: pin ForgeGradle to an exact 6.x release, keep the Gradle wrapper pinned, add a version catalog, lock dependencies, and add dependency verification because the current mod resolves from many public repositories.

Testing should be split cleanly: JUnit Jupiter for extracted logic, narrow Mockito usage only at owned seams, and Forge GameTests as the main integration layer for AI, packets, persistence, commands, and menu flows. Persistence should stay on `SavedData` + NBT for now, but serialization logic should be isolated so codec-oriented helpers can be introduced incrementally in the riskiest managers.

**Core technologies:**
- **Minecraft Forge `1.20.1-47.4.10`**: runtime mod API and test-capable platform — keeps stabilization separate from platform churn.
- **ForgeGradle 6.x pinned exactly**: workspace and packaging tooling — removes dynamic-version instability.
- **Mojang official mappings**: readable names with minimal churn — avoids remap noise before migration.
- **Java toolchains (17 now, 21 prep lane)**: reproducible JDK selection — matches current branch needs while preparing for 1.21.1.
- **SavedData + NBT**: persistence backbone — stabilize existing storage instead of replacing it midstream.
- **JUnit Jupiter + Forge GameTest**: layered verification — unit-test pure rules and GameTest runtime behavior.
- **GitHub Actions + Gradle locking/verification**: reproducible CI — makes failures attributable to repo changes, not dependency drift.

### Expected Features

This initiative’s “features” are trust features, not new gameplay. Table stakes are regression coverage for the core mod loop, persistence safety, multiplayer/network correctness, a repeatable automated test harness, safer failure handling, baseline performance verification, reproducible builds, and compat containment. These are the minimum needed for players and maintainers to believe the mod is stable again.

The strongest differentiator is migration-readiness seam extraction: if version-sensitive APIs, packet glue, persistence boundaries, and client-state handling are isolated now, the 1.21.1 port becomes a bounded follow-up instead of another destabilizing rewrite. A risk-based verification map and representative scenario suite are also high-value because they turn vague “tested” claims into explicit coverage statements.

**Must have (table stakes):**
- Automated test harness with repeatable GameTests.
- Core mechanic regression coverage for recruiting, commands, battles, formations, patrols, and faction behavior.
- Persistence correctness and save/load safety.
- Multiplayer/network trustworthiness with server-authoritative behavior.
- Build and verification reproducibility.
- Failure visibility and safe degradation for broken or unfinished flows.

**Should have (competitive):**
- Migration-readiness seam extraction around version-sensitive systems.
- Risk-based verification map for the mod surface.
- Representative scenario suite for battle, restart, command, and faction flows.
- Maintainer-facing diagnostics for async AI, menus, networking, and compat paths.

**Defer (v2+):**
- Broad performance optimization beyond regression thresholds.
- New gameplay content, units, factions, or mechanics.
- Broad compatibility expansion.
- Full 1.21.1 migration during the stabilization milestone.

### Architecture Approach

The architectural recommendation is to move toward a hexagonal-ish gameplay core with thin Forge adapters, not a full rewrite. Bootstrap, events, packets, commands, menus, and screens should translate into application services; domain rules for factions, groups, routes, orders, formations, and diplomacy should live outside Forge-heavy code; persistence, entity access, networking, and path jobs should sit behind explicit ports. The cheapest high-value decomposition order is persistent world systems first, then packet handling, then client read models, then entity policy extraction, and only then pathfinding/mixins/compat isolation.

**Major components:**
1. **Bootstrap and registration** — Forge entrypoints, registries, configs, packet wiring, adapter factories.
2. **Server adapters** — events, Brigadier commands, packet handlers, menu openers that translate runtime input into use cases.
3. **Application services** — orchestration for hire, command, diplomacy, formations, routes, upkeep, and sync decisions.
4. **Domain model** — faction, claim, group, route, recruit-order, and rule logic.
5. **Entity façade layer** — narrow interface over recruit entities so services avoid god-class coupling.
6. **Persistence gateway** — `SavedData` / NBT snapshot load-save and dirty marking.
7. **Network and sync gateway** — DTOs, sync topics, and packet mapping without business logic in message classes.
8. **Client read model** — feature-scoped client state replacing mutable static cache sprawl.
9. **Platform compatibility layer** — mixins, optional mod shims, and version-sensitive hooks isolated for migration.

### Critical Pitfalls

The pitfall research strongly reinforces the architecture and feature guidance: trust collapses when teams codify broken behavior, test giant entity monoliths directly, over-mock the engine, miss logical-side/thread boundaries, or ignore persistence until late. These are not edge mistakes; they are the most likely failure modes for this mod’s scope.

1. **Freezing broken behavior into tests** — classify tests as intended behavior, bug repro, or characterization before turning legacy behavior into policy.
2. **Testing monolith gameplay code without seams** — extract pure helpers and façade boundaries first; reserve GameTests for runtime-heavy behavior.
3. **Over-mocking Minecraft/Forge runtime** — unit test deterministic logic only, and verify packet/entity/tick effects in real GameTests.
4. **Missing logical-side and thread boundaries** — audit packet handlers, async callbacks, and client/server separation early.
5. **Treating persistence as incidental** — add save-load-resume and dirty-marking coverage before large refactors.
6. **Assuming the 1.21.1 port is mechanical** — isolate networking, persistence, client, registry, compat, and pathfinding seams now.

## Implications for Roadmap

Based on research, suggested phase structure:

### Phase 1: Build Hardening and Stabilization Baseline
**Rationale:** Every other phase depends on reproducible builds, a frozen target state, and a clear definition of intended behavior.
**Delivers:** Pinned ForgeGradle/tooling, repository cleanup, lockfiles, dependency verification, CI baseline, feature-freeze rules, behavior catalog, and initial side/thread audit.
**Addresses:** Build and verification reproducibility; failure visibility; compatibility containment groundwork.
**Avoids:** Moving-target stabilization, unreproducible CI, and freezing broken behavior into tests.

### Phase 2: Test Harness and Seam Extraction Foundations
**Rationale:** The mod cannot be safely hardened by testing god classes directly; seams and layered tests must exist first.
**Delivers:** JUnit source set, Forge GameTest harness, shared fixtures, first extracted pure logic helpers, packet validation seams, persistence serializer seams, and initial entity façades.
**Uses:** JUnit Jupiter, Mockito sparingly, Forge GameTest, Java 17 toolchains.
**Implements:** Application-service boundaries, persistence gateway seams, DTO/handler split beginnings.

### Phase 3: Critical Regression Coverage for Core Systems
**Rationale:** Once harnesses exist, the highest-risk gameplay contracts should be locked down before broader refactoring.
**Delivers:** Regression suites for battles, commands, formations, patrols, factions, persistence round-trips, invalid packet paths, restart scenarios, and density/performance baselines.
**Addresses:** Core mechanic regression coverage, persistence correctness, multiplayer/network trustworthiness, performance and density verification.
**Avoids:** Happy-path-only confidence, fake runtime coverage, and late discovery of save/load corruption.

### Phase 4: Architecture Reshaping of Persistent, Network, and Client-State Systems
**Rationale:** These systems provide the cheapest high-leverage decomposition and reduce migration risk without starting in the most fragile entity internals.
**Delivers:** Aggregate-style faction/claim/group/route services, thin packet adapters, network gateway, replacement of `ClientManager` statics with client read models, and clearer save/sync ownership.
**Implements:** Use-case handlers, persistence gateways, client reducers/read models, thinner adapters.
**Avoids:** Packet business logic sprawl, static client-state drift, and persistence/gameplay coupling.

### Phase 5: Entity Policy Extraction and Runtime Risk Isolation
**Rationale:** After persistent and network seams exist, recruit behavior can be pulled out of the god entity with less collateral damage.
**Delivers:** Extracted command-state, upkeep/payment, formation, group/team sync, and combat/target-selection policies plus GameTest-backed entity contracts.
**Addresses:** Core mechanic regression coverage and migration-readiness seam extraction.
**Avoids:** Direct unit testing of monolith entities and premature rewrites of unstable runtime code.

### Phase 6: Migration-Prep Platform Isolation
**Rationale:** Only after behavior is bounded should the team isolate the highest-risk 1.21.1 breakpoints.
**Delivers:** Explicit version-seam packages, isolated packet registration glue, pathfinding/navigation adapters, compat strategy interfaces, client/render separation, and a concrete migration checklist.
**Addresses:** Migration-readiness seam extraction and compatibility containment.
**Avoids:** Treating the 1.21.1 port as compile-fix work or combining it with uncontrolled refactors.

### Phase Ordering Rationale

- Build reproducibility and behavior triage come first because tests on moving dependencies or moving feature scope are not trustworthy.
- Seams precede deep coverage because the research is explicit: testing giant Forge entities directly creates brittle, low-value tests.
- Persistent world systems, packet handling, and client-state cleanup are grouped before entity/pathfinding work because they offer the highest trust gain at the lowest port risk.
- Performance work is intentionally constrained to regression thresholds until correctness is locked in.
- Version-sensitive platform isolation is last in stabilization order but mandatory before the actual 1.21.1 port starts.

### Research Flags

Phases likely needing deeper research during planning:
- **Phase 3:** Battle density, async pathfinding saturation, and representative stress scenarios may need targeted scenario research because runtime behavior is complex.
- **Phase 5:** Combat, targeting, and navigation-related policy extraction likely need subsystem-specific research due to deep entity/runtime coupling.
- **Phase 6:** Pathfinding/mixins, rendering/client hooks, and optional compat shims need focused migration research because official guidance is thinner and version churn is high.

Phases with standard patterns (skip research-phase):
- **Phase 1:** Gradle hardening, dependency locking, toolchains, CI, and repository cleanup are well-documented standard practices.
- **Phase 2:** Layered JUnit + Forge GameTest setup and seam-first testing strategy are well supported by official docs and project evidence.
- **Phase 4:** DTO/use-case extraction, persistence gateways, and client read-model replacement follow established patterns already strongly justified by the architecture research.

## Confidence Assessment

| Area | Confidence | Notes |
|------|------------|-------|
| Stack | HIGH | Driven mostly by official Forge, Gradle, GitHub Actions, and testing docs with clear version guidance. |
| Features | HIGH | Strongly grounded in project constraints plus official Forge expectations for persistence, networking, and testing. |
| Architecture | HIGH | Based on direct code inspection plus official Forge docs; exact decomposition details may evolve, but the direction is well supported. |
| Pitfalls | HIGH | Strong overlap between official docs and project-specific concerns; failure modes are concrete and repeatedly evidenced. |

**Overall confidence:** HIGH

### Gaps to Address

- **Exact verification matrix scope:** The research identifies priority systems, but planning should decide the first authoritative scenario matrix for battles, commands, persistence, and networking.
- **Performance thresholds:** The need for density regression checks is clear, but acceptable recruit-count/load targets must be defined from real project expectations.
- **Compat coverage depth:** The research argues for containment and smoke checks, but the exact optional integrations to certify in this milestone should be explicitly chosen.
- **Migration checklist granularity:** High-risk version-sensitive areas are known, but the final planning pass should produce a concrete file/package inventory before port work begins.

## Sources

### Primary (HIGH confidence)
- Forge getting started docs (1.20.x, 1.21.x) — Java baselines, runtime setup, and mod structure expectations.
- Forge GameTest docs — recommended in-game verification approach.
- Forge SavedData docs — persistence contract and dirty-marking behavior.
- Forge SimpleImpl / networking docs — packet handling, threading, and server-authoritative boundaries.
- Forge sides / registries / structuring docs — side safety, organization, and registry concerns.
- Gradle docs — toolchains, dependency locking, dependency verification, version catalogs, repository best practices, JaCoCo, and dynamic-version risks.
- GitHub Actions Gradle guidance — standard CI setup.
- Project evidence: `.planning/PROJECT.md`, `.planning/codebase/ARCHITECTURE.md`, `.planning/codebase/CONCERNS.md`, `.planning/codebase/TESTING.md`.

### Secondary (MEDIUM confidence)
- Parchment documentation — optional mapping enrichment only if refactors demand it.
- JUnit and Mockito project docs — JVM testing strategy and narrow mocking guidance.
- ChampionAsh5357 1.20.6 -> 1.21 primer — likely migration hotspots linked from Forge porting docs.

### Tertiary (LOW confidence)
- None material; the major recommendations are not resting on weak sources.

---
*Research completed: 2026-04-05*
*Ready for roadmap: yes*
