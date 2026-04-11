# Domain Pitfalls

**Domain:** Brownfield Minecraft Forge NPC-army mod stabilization, test hardening, and near-term version-migration prep
**Researched:** 2026-04-05

## Critical Pitfalls

Mistakes that commonly trigger rewrites, endless bug-chasing, or a failed port.

### Pitfall 1: Freezing broken behavior into tests
**What goes wrong:** Teams add coverage against current dev-branch behavior before deciding what is intended versus what is already a bug.
**Why it happens:** Brownfield mods often treat "existing behavior" as ground truth because there is no spec, only player reports and fragile code.
**Consequences:** The suite becomes a bug-preservation system; later fixes look like regressions; migration work inherits bad assumptions.
**Prevention:**
- Start with a behavior catalog for high-risk systems: battles, formations, commands, persistence, networking, and AI.
- Mark each new test as one of: `intended behavior`, `bug repro`, or `characterization only`.
- Require a short intent note on tests covering unclear legacy behavior.
- Convert characterization tests into normative tests only after expected behavior is agreed.
**Detection / warning signs:**
- PRs argue about whether a failing test indicates a bug or a behavior change.
- Tests assert obviously wrong UX because "that is what the branch does today."
- Multiple flaky regressions trace back to giant entity classes with mixed responsibilities.
**Phase should address it:** Phase 1 - Stabilization triage and behavior baseline

### Pitfall 2: Trying to test monolith gameplay code without first creating seams
**What goes wrong:** Teams attempt direct unit coverage on huge entity classes that mix AI, inventory, commands, persistence, targeting, and upkeep.
**Why it happens:** Legacy Forge gameplay code is heavily runtime-coupled, so developers jump straight into mocks instead of extracting smaller decision points.
**Consequences:** Tests become brittle, unreadable, and expensive to maintain; refactors stall; important behavior remains untested.
**Prevention:**
- Extract pure or mostly-pure helpers first: order validation, payment rules, target selection policy, formation math, NBT translation, packet validation.
- Keep entity classes as orchestration points and test them mostly through GameTests or integration-style tests.
- Use JUnit for logic seams and Forge GameTests for world/entity interaction.
- Introduce small adapters around Minecraft/Forge types instead of mocking everything directly.
**Detection / warning signs:**
- A single "unit" test needs world, player, network context, and entity setup just to assert one branch.
- Mockito-heavy tests break on harmless refactors.
- Coverage rises in numbers but not in confidence for battle or persistence bugs.
**Phase should address it:** Phase 2 - Test harness and seam extraction

### Pitfall 3: Over-mocking the engine instead of testing real server behavior
**What goes wrong:** Projects build fake `Level`, `ServerPlayer`, packet context, or pathing state that does not match Forge/Minecraft runtime behavior.
**Why it happens:** Legacy mods want fast tests, but Forge logic depends on real tick timing, logical side, loaded chunks, entity lifecycle, and main-thread execution.
**Consequences:** False confidence; packet handlers pass in tests but fail in multiplayer; AI/pathing bugs only appear in live worlds.
**Prevention:**
- Use JUnit only for deterministic logic that can be separated from runtime.
- Use GameTests for entity interaction, ticking, persistence, registration, and world-state assertions.
- For networking, validate decoded payloads with unit tests but verify handler effects in game-backed tests.
- Build a thin reusable test fixture library instead of ad hoc mocks.
**Detection / warning signs:**
- Tests pass locally while dedicated-server behavior still breaks.
- Bugs reproduce only with real ticking, chunk loading, or multiplayer.
- Test doubles implement more behavior over time than the production seam itself.
**Phase should address it:** Phase 2 - Test harness and seam extraction, then Phase 3 - Critical system regression coverage

### Pitfall 4: Missing logical-side and thread boundaries during hardening
**What goes wrong:** Stabilization changes accidentally mix client logic, server logic, network-thread work, and async callbacks.
**Why it happens:** Forge code often works in singleplayer even when it is wrong, because logical client and logical server share one JVM. Async systems make this worse.
**Consequences:** Ghost state, race conditions, null paths, desyncs, dedicated-server-only crashes, and heisenbugs in large recruit battles.
**Prevention:**
- Audit all packet handlers to ensure game mutations run through `NetworkEvent.Context#enqueueWork`.
- Audit delayed/async callbacks so world mutation returns to the main server executor.
- Prefer `Level#isClientSide` for logical-side checks; isolate physical-client code behind side-safe boundaries.
- Add regression tests around async pathfinding saturation, callback ordering, and packet-driven commands.
**Detection / warning signs:**
- Behavior differs between singleplayer and dedicated server.
- Failures cluster around pathfinding, packet handling, and delayed faction/team updates.
- Code uses thread-group guesses or client classes in common code paths.
**Phase should address it:** Phase 1 - Stabilization triage for audit, Phase 3 - Critical system regression coverage for permanent protection

### Pitfall 5: Treating persistence as incidental instead of a first-class contract
**What goes wrong:** Teams focus on combat and UI bugs first, but world/team/faction/route data remains under-specified and under-tested.
**Why it happens:** Persistence bugs are slower to reproduce than combat bugs, and `SavedData`/NBT code often looks simple until cross-session failures appear.
**Consequences:** Silent data loss, corrupted recruit/team state, broken migrations, and worlds that only fail after save/reload.
**Prevention:**
- Create save-load-resume tests for claims, factions, groups, routes, recruit ownership, and formation state.
- Explicitly test dirty-marking and reload semantics for every `SavedData` owner.
- Separate serialization code from gameplay code so it can be directly asserted.
- Capture a handful of golden-world scenarios for regression verification before major refactors.
**Detection / warning signs:**
- Bugs only appear after restart or dimension/world reload.
- Fixes touch NBT or `SavedData` but add no persistence tests.
- Data managers mutate state in many places with inconsistent dirty signaling.
**Phase should address it:** Phase 3 - Critical system regression coverage

### Pitfall 6: Refactoring for migration while still allowing the unstable branch to drift
**What goes wrong:** The team starts deep cleanup, but the dev branch keeps accepting unrelated gameplay edits, balance tweaks, and partial features.
**Why it happens:** Brownfield mods often have no stabilization freeze line; everything is "in progress" at once.
**Consequences:** Baselines go stale, regression triage never converges, and the migration-prep work has no trustworthy target state.
**Prevention:**
- Establish a temporary stabilization rule: no new gameplay features, only bug fixes, tests, and seam-oriented refactors.
- Gate unfinished systems and TODO-backed content instead of leaving them half-live.
- Track regressions against a fixed validation matrix, not against shifting branch behavior.
- Split "stabilize now" changes from "nice cleanup" changes.
**Detection / warning signs:**
- Test failures are regularly caused by unrelated feature churn.
- TODO-backed systems remain reachable in production flows.
- Rebase pain dominates actual debugging time.
**Phase should address it:** Phase 1 - Stabilization triage and branch freeze

### Pitfall 7: Assuming the next Minecraft version port is mostly mechanical
**What goes wrong:** Teams postpone migration prep because they expect the next port to be a rename pass plus compile fixes.
**Why it happens:** Small mods sometimes survive this way; large Forge mods with networking, custom entities, async systems, mixins, and compatibility layers usually do not.
**Consequences:** Porting reveals hidden coupling to registry timing, lifecycle ordering, data formats, client/server boundaries, and third-party mods.
**Prevention:**
- Isolate version-sensitive seams now: registration, data access, packet glue, rendering/client hooks, compat adapters, and world persistence boundaries.
- Reduce direct dependency on fragile internals and reflection-heavy cross-mod shims.
- Keep gameplay rules independent from Forge event plumbing where possible.
- Maintain a migration checklist of version-sensitive subsystems before the actual port starts.
**Detection / warning signs:**
- Core gameplay classes import many Forge/Minecraft runtime classes directly.
- Compat code degrades to `null` returns or exact-version string checks.
- The build relies on floating plugin versions and snapshot tooling.
**Phase should address it:** Phase 4 - Migration seam extraction and build hardening

## Moderate Pitfalls

### Pitfall 1: Building CI on unreproducible tooling
**What goes wrong:** Stabilization work runs on moving plugin/tooling versions, so failures come from upstream changes instead of mod changes.
**Prevention:**
- Pin ForgeGradle and other build plugins to exact known-good versions.
- Remove deprecated/frozen repositories where possible.
- Record the known-good Java, Gradle wrapper, Forge, and dependency set used by CI.
**Warning signs:**
- Clean builds fail intermittently across machines.
- "Nothing changed" but dependency resolution or reobf behavior changes.
**Phase should address it:** Phase 1 - Build and branch hardening

### Pitfall 2: Covering only the happy path for battles and commands
**What goes wrong:** Tests validate recruit basics but skip high-density combat, invalid commands, unload/reload edges, and menu/network failures.
**Prevention:**
- Build scenario matrices for: recruit count stress, command spam, ownership edge cases, distant menu opens, and async queue saturation.
- Add at least one failure-path or abuse-path test for every important packet and command flow.
**Warning signs:**
- Coverage reports look respectable, but every player bug comes from edge conditions.
- Packet and menu bugs keep escaping despite new tests.
**Phase should address it:** Phase 3 - Critical system regression coverage

### Pitfall 3: Leaving compatibility shims untested until after migration starts
**What goes wrong:** Optional integrations are treated as secondary, then explode during the port because they depend on reflection or exact external versions.
**Prevention:**
- Put compat behind explicit adapters and fallback paths.
- Smoke-test startup and critical actions with and without each major optional dependency.
- Replace exact-version gates with capability/API checks where feasible.
**Warning signs:**
- Runtime logs contain reflection failures or silent null fallbacks.
- Compat behavior depends on hard-coded version substrings.
**Phase should address it:** Phase 4 - Migration seam extraction and compat audit

### Pitfall 4: Mixing correctness refactors with performance rewrites too early
**What goes wrong:** Teams try to solve battle lag, AI correctness, and migration prep in one pass.
**Prevention:**
- First make behavior reproducible and testable.
- Add metrics/logging for target scans, path queue saturation, and chunk-driven spikes.
- Optimize only after tests lock the intended behavior.
**Warning signs:**
- Large pathfinding or combat rewrites land without regression coverage.
- Performance fixes change gameplay behavior and no one can say whether it is intended.
**Phase should address it:** Phase 3 after baseline tests exist

## Minor Pitfalls

### Pitfall 1: Using GameTests without structure discipline
**What goes wrong:** Tests depend on ad hoc structures, implicit coordinates, or hard-to-maintain templates.
**Prevention:**
- Standardize template naming, batch setup, and fixture ownership.
- Keep structures minimal and focused on one behavior.
**Warning signs:**
- Small gameplay changes require touching many NBT templates.
- Tests fail because fixture scenes are hard to reason about.
**Phase should address it:** Phase 2 - Test harness and fixture conventions

### Pitfall 2: Letting low-value UI/client bugs dominate early stabilization
**What goes wrong:** The team spends too much early effort on polish while battle, persistence, and networking regressions remain unbounded.
**Prevention:**
- Triage by blast radius: server logic and persistence first, UI polish later unless it blocks core flows.
- Keep a separate backlog for non-blocking client cleanup.
**Warning signs:**
- Early milestones close many cosmetic issues but not core multiplayer bugs.
**Phase should address it:** Phase 1 - Risk-based prioritization

## Phase-Specific Warnings

| Phase Topic | Likely Pitfall | Warning Signs | Mitigation |
|-------------|---------------|---------------|------------|
| Phase 1: Build and branch hardening | Stabilizing on moving dependencies and moving feature scope | Random build breakage, unrelated behavior drift, TODO-backed features reachable | Freeze feature scope, pin tooling, gate unfinished systems, publish a validation matrix |
| Phase 1: Risk audit | Missing dedicated-server and async-only bugs | Singleplayer looks fine; server reports differ; queue/thread issues hard to repro | Audit side/thread boundaries first, especially packets and delayed executors |
| Phase 2: Test harness | Writing tests directly against giant entity classes | Tests need full world bootstrap for tiny logic branches | Extract seams first; JUnit for pure rules, GameTests for world behavior |
| Phase 2: Fixture design | Overgrown GameTest structures and fake runtime mocks | Fixtures become harder to maintain than code under test | Build a small shared fixture library and minimal structure templates |
| Phase 3: Critical regression coverage | Happy-path-only coverage | Regressions still come from save/load, command spam, high-density combat, invalid packets | Add edge-case matrices and persistence round-trip tests |
| Phase 3: Performance/correctness overlap | Premature optimization alters gameplay logic | AI/perf patches change combat outcomes with no clear intent | Lock expected behavior in tests before optimizing |
| Phase 4: Migration prep | Treating 1.21.1 migration as compile-error cleanup | Many direct runtime imports, fragile reflection shims, exact-version compat checks | Extract version-sensitive adapters, isolate compat, document migration checklist |
| Phase 4: Compatibility | Optional mod integrations break late | Reflection exceptions, null fallbacks, integration-specific regressions after toolchain changes | Add smoke tests with and without optional deps; prefer API/capability checks |

## Sources

- **HIGH:** Minecraft Forge docs - Game Tests: https://docs.minecraftforge.net/en/1.20.1/misc/gametest/
- **HIGH:** Minecraft Forge docs - Sides in Minecraft: https://docs.minecraftforge.net/en/1.20.1/concepts/sides/
- **HIGH:** Minecraft Forge docs - SimpleImpl networking: https://docs.minecraftforge.net/en/1.20.1/networking/simpleimpl/
- **HIGH:** Minecraft Forge docs - Saved Data: https://docs.minecraftforge.net/en/1.20.1/datastorage/saveddata/
- **HIGH:** Gradle docs - dynamic/changing versions cause unreproducible builds: https://docs.gradle.org/current/userguide/dependency_versions.html
- **MEDIUM:** JUnit User Guide (current JVM testing baseline and Java 17 support): https://junit.org/junit5/docs/current/user-guide/
- **HIGH (project evidence):** `/home/kaiserroman/recruits/.planning/PROJECT.md`
- **HIGH (project evidence):** `/home/kaiserroman/recruits/.planning/codebase/CONCERNS.md`
- **HIGH (project evidence):** `/home/kaiserroman/recruits/.planning/codebase/TESTING.md`
