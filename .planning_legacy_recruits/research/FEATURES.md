# Feature Landscape

**Domain:** Stabilization, verification, and migration preparation for a large Forge NPC-army mod
**Researched:** 2026-04-05

## Table Stakes

Features users and maintainers should expect from a trust-restoring stabilization initiative. Missing these means the mod still feels unsafe to play, maintain, or port.

| Feature | Why Expected | Complexity | Notes |
|---------|--------------|------------|-------|
| Core mechanic regression coverage | Players must be able to trust that recruiting, commanding, battles, formations, patrols, and faction interactions still behave as intended after fixes/refactors. | High | Highest priority for this project. Should cover both happy paths and failure paths for battles, commands, AI state transitions, and formation behavior. |
| Persistence correctness and save/load safety | For a long-lived army mod, users expect squads, factions, claims, routes, and team state to survive world saves/restarts without corruption or silent loss. | High | Forge `SavedData` only persists when data is marked dirty, so requirements should explicitly cover save triggers, restart verification, and backward-safe serialization checks. |
| Multiplayer/network trustworthiness | Large Forge gameplay mods are expected to behave correctly on logical server/client boundaries; desync, ghost state, and packet-side bugs quickly destroy trust. | High | Particularly important here because the mod has a very large packet surface and static client caches. Requirements should cover command packets, UI sync, invalid input handling, and server-authoritative outcomes. |
| Automated test harness with repeatable game tests | Maintainers need a repeatable way to verify in-game behavior before merging changes. Forge officially supports scalable Game Tests and a dedicated `runGameTestServer` flow. | High | Should include both unit-style tests for pure logic and Game Tests for entity/UI/world interactions. This is table stakes because the project currently has no real safety net. |
| Failure visibility and safe degradation | Users should see broken flows blocked clearly, not hidden behind `null`, silent no-ops, or half-implemented gameplay paths. | Medium | Requirements should cover explicit gating of unfinished features, structured logging, and predictable fallback behavior for menus, compat shims, and async failures. |
| Performance and density verification | NPC-army mods are judged not just by correctness but by whether battles remain playable at meaningful recruit counts. | High | Trust requires baseline scenarios for heavy combat, target scans, pathfinding saturation, and faction-scale limits near configured caps. Not full optimization work, but enough measurement to catch regressions. |
| Build and verification reproducibility | Maintainers need a stable way to build, run tests, and reproduce failures across machines and CI. | Medium | Particularly important because current tooling uses floating/snapshot dependencies. Requirements should cover pinned build inputs where possible and one-command verification paths. |
| Compatibility containment | Optional integrations should fail gracefully instead of destabilizing the core mod when external mods change. | Medium | Especially relevant for reflection-heavy compat. Requirements should cover isolation, smoke verification, and graceful disable behavior rather than broad new compat support. |

## Differentiators

Capabilities that make this initiative stronger than a normal bugfix pass and directly improve confidence in the eventual 1.21.1 port.

| Feature | Value Proposition | Complexity | Notes |
|---------|-------------------|------------|-------|
| Migration-readiness seam extraction | Makes the later 1.21.1 port short and mechanical instead of another destabilizing rewrite. | High | Requirements should target isolating version-sensitive APIs, engine patches, packet registration glue, and persistence boundaries behind narrower interfaces/helpers. |
| Risk-based verification map for the full mod surface | Shows maintainers exactly what is covered deeply vs lightly, instead of claiming vague “tested” status. | Medium | Strong fit for this project because the user wants full-surface verification with extra depth on battles, persistence, commands, AI, networking, and formations. |
| Representative scenario suite | Gives maintainers trusted before/after scenarios for large-battle, restart, command, and faction flows instead of isolated micro-tests only. | High | Best implemented as a curated set of Game Tests plus manual verification scripts/checklists for systems Game Tests cannot cover well. |
| Maintainer-facing diagnostics | Speeds triage when async AI, menus, networking, or compat paths fail. | Medium | Useful requirements: structured logs, invariant checks in risky paths, and profiler markers around hot loops/pathfinding so regressions are easier to localize. |

## Anti-Features

Features or requirement patterns to explicitly avoid during this initiative.

| Anti-Feature | Why Avoid | What to Do Instead |
|--------------|-----------|-------------------|
| New gameplay content, units, factions, or mechanics | Expands blast radius and makes it harder to distinguish regressions from intentional changes. | Freeze player-facing feature growth; only fix broken or incomplete existing behavior. |
| Treating current behavior as sacred when tests reveal obvious bugs | Locks in broken logic and turns the test suite into a bug-preservation harness. | Let tests encode intended behavior, and explicitly document bug-fix behavior changes. |
| UI-only polish work without trust gains | Cosmetic cleanup does not materially improve stability, maintainability, or port readiness. | Prioritize correctness, diagnostics, and broken-flow handling first. |
| Full 1.21.1 migration during stabilization | Merges two high-risk efforts and makes failures harder to attribute. | Prepare seams, adapters, and inventory of version-sensitive code now; do the actual port next. |
| Broad compatibility expansion | Each extra integration multiplies test burden and migration risk. | Stabilize current compat points and ensure graceful disable/fallback behavior. |
| Over-reliance on flaky end-to-end automation | Minecraft-level full-stack tests can become slow and brittle if used for everything. | Use a layered strategy: pure logic unit tests, Game Tests for gameplay contracts, and targeted manual smoke checks. |

## Feature Dependencies

```text
Build and verification reproducibility → Automated test harness with repeatable game tests
Automated test harness with repeatable game tests → Core mechanic regression coverage
Automated test harness with repeatable game tests → Persistence correctness and save/load safety
Automated test harness with repeatable game tests → Multiplayer/network trustworthiness
Failure visibility and safe degradation → Representative scenario suite
Core mechanic regression coverage → Performance and density verification
Persistence correctness and save/load safety → Migration-readiness seam extraction
Multiplayer/network trustworthiness → Migration-readiness seam extraction
Compatibility containment → Migration-readiness seam extraction
Risk-based verification map for the full mod surface → Maintainer-facing diagnostics
```

## MVP Recommendation

Prioritize:
1. **Automated test harness with repeatable game tests** - without this, stabilization claims are not credible and refactors stay risky.
2. **Core mechanic regression coverage** - especially battles, commands, AI, formations, and patrol/faction behaviors that define user trust.
3. **Persistence correctness and save/load safety** - world state loss or corruption is one of the fastest ways to lose users.
4. **Multiplayer/network trustworthiness** - this mod’s packet-heavy architecture makes side/desync correctness a core requirement, not an edge case.
5. **Migration-readiness seam extraction** - once core behavior is guarded, isolate version-sensitive code so the 1.21.1 port becomes a bounded follow-up.

Defer: **Broad performance optimization**: establish measurement and regression thresholds now, but avoid turning this milestone into an open-ended optimization project unless testing proves a blocker.

## Sources

- Project requirements and constraints: `/home/kaiserroman/recruits/.planning/PROJECT.md` — HIGH confidence
- Current architecture and risk surface: `/home/kaiserroman/recruits/.planning/codebase/ARCHITECTURE.md` — HIGH confidence
- Current defects, fragility, and coverage gaps: `/home/kaiserroman/recruits/.planning/codebase/CONCERNS.md` — HIGH confidence
- Forge Game Tests documentation: https://docs.minecraftforge.net/en/1.20.x/gametest/ — HIGH confidence for test-framework capability and CI/game-test expectations
- Forge Saved Data documentation: https://docs.minecraftforge.net/en/1.20.x/datastorage/saveddata/ — HIGH confidence for persistence expectations
- Forge SimpleImpl documentation: https://docs.minecraftforge.net/en/1.20.x/networking/simpleimpl/ — HIGH confidence for packet/threading and server-authoritative networking expectations
- Forge Sides documentation: https://docs.minecraftforge.net/en/1.20.x/concepts/sides/ — HIGH confidence for client/server trust boundaries
- Forge Debug Profiler documentation: https://docs.minecraftforge.net/en/1.20.x/debugprofiler/ — HIGH confidence for profiling/diagnostic expectations
