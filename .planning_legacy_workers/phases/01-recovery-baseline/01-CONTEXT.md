# Phase 1: Recovery Baseline - Context

**Gathered:** 2026-04-05
**Status:** Ready for planning

<domain>
## Phase Boundary

Restore a clean runnable 1.20.1 recovery baseline for the existing mod so build, startup, and a minimal startup-oriented smoke path work without critical failures. This phase is about recovering baseline bootstrapping and startup confidence, not finishing profession loops, full UI flows, or the deferred 1.21.1 port.

</domain>

<decisions>
## Implementation Decisions

### Baseline Proof
- **D-01:** Phase 1 is not done until the project builds cleanly, both client and dedicated-server startup succeed, and a minimal in-game smoke path is exercised.
- **D-02:** The smoke path should stay startup-oriented and spawn-focused: prove that at least one worker-related item or entity is registered and usable without crashing, rather than expanding into broader UI or profession behavior.

### Dependency Cleanup
- **D-03:** Dependency and metadata cleanup in Phase 1 should stay minimal and unblock-oriented. Only change what is required to recover a clean 1.20.1 baseline.
- **D-04:** If a non-core compatibility dependency blocks the baseline, it may be disabled or gated temporarily when that is the safest way to restore a runnable core mod.

### Startup Success Bar
- **D-05:** Startup-critical scope for Phase 1 is bootstrap/core wiring only: registry registration, config registration, packet channel and message registration, menu wiring, and client setup hooks must initialize cleanly.
- **D-06:** Full worker profession loops, work-area editing flows, and deeper gameplay behavior are not required proof targets in this phase unless they directly block baseline startup.

### Guardrails Now
- **D-07:** Phase 1 should add one lightweight automated regression check now instead of waiting entirely for Phase 9.
- **D-08:** That automated check should target a pure logic seam such as validation, serialization, or similarly isolated non-runtime behavior, not a full game bootstrap harness.

### Degraded Startup Tolerance
- **D-09:** Non-fatal startup warnings or temporarily disabled non-core compatibility paths are acceptable in Phase 1 if the core mod baseline loads, the spawn-focused smoke path works, and the remaining rough edges are documented for later phases.

### the agent's Discretion
- Select the exact spawn-focused smoke interaction, as long as it remains minimal and baseline-oriented.
- Select the most valuable pure logic seam for the single automated check based on the real regression risks found during implementation.
- Decide case-by-case how to document any tolerated non-core degraded paths, as long as those follow-up items stay visible for later phases.

</decisions>

<specifics>
## Specific Ideas

- The smoke path should stay intentionally small and startup-oriented, not turn into an early UI or profession validation phase.
- If compatibility modules are the blocker, keeping the core mod runnable matters more than preserving every non-core integration in Phase 1.

</specifics>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Phase Definition
- `.planning/ROADMAP.md` §Phase 1: Recovery Baseline — Defines the fixed phase goal, success criteria, and dependency boundary.
- `.planning/REQUIREMENTS.md` §QUAL-03 and Traceability — Defines the clean build requirement and confirms Phase 1 owns it.

### Project Constraints
- `.planning/PROJECT.md` — Defines recovery-first scope, 1.20.1 baseline constraints for v1, and the rule that existing code is the primary source of truth.
- `.planning/STATE.md` — Captures the current project focus plus the explicit constraint that the 1.21.1 port must not leak into v1 execution scope.

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `build.gradle`: Existing `client` and `server` run configurations provide the natural baseline verification entry points for this phase.
- `src/main/java/com/talhanation/workers/WorkersMain.java`: Central bootstrap path already wires registries, config, packet registration, menu registration, client setup hooks, and structure copy setup.
- `src/main/resources/META-INF/mods.toml`: Current mod metadata and hard Recruits dependency are the main startup compatibility gatekeepers.

### Established Patterns
- Bootstrap is registry-driven and centralized in `WorkersMain`, so Phase 1 should prefer fixing startup from that composition root outward rather than redesigning feature logic.
- The mod uses packet-per-action flows and entity-centric gameplay logic, so later gameplay recovery should stay separate from this startup baseline phase.
- No meaningful automated test harness exists yet, which supports keeping the initial automated check focused on a small pure-logic seam.

### Integration Points
- Baseline startup work will connect through `WorkersMain`, registry classes under `src/main/java/com/talhanation/workers/init/`, and metadata/build files such as `build.gradle`, `gradle.properties`, and `src/main/resources/META-INF/mods.toml`.
- Any tolerated degraded compatibility path will likely be expressed in dependency declarations or guarded initialization logic near bootstrap and metadata boundaries.

</code_context>

<deferred>
## Deferred Ideas

None — discussion stayed within phase scope.

</deferred>

---

*Phase: 01-recovery-baseline*
*Context gathered: 2026-04-05*
