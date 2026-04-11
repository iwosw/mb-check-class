# Phase 2: Layered Test Harness Foundations - Context

**Gathered:** 2026-04-05
**Status:** Ready for planning
**Mode:** Auto-generated (infrastructure phase)

<domain>
## Phase Boundary

Establish the reusable testing foundation for the mod by separating pure JVM unit tests from Forge runtime GameTests, standardizing where those tests live, and introducing shared helpers that later stabilization phases can build on.

</domain>

<decisions>
## Implementation Decisions

### the agent's Discretion
- All implementation choices are at the agent's discretion for this phase because it is a pure infrastructure phase.
- Keep Phase 1 decisions intact: `./gradlew check` remains the canonical verification entrypoint, build/unit/game-test stages remain explicitly distinguishable, and local overrides stay outside the canonical path.
- Favor the smallest durable test harness that makes adding later battle, command, persistence, and networking tests fast and consistent.

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Phase Definition
- `.planning/ROADMAP.md` — Phase 2 goal, success criteria, and sequencing after the reproducibility baseline.
- `.planning/REQUIREMENTS.md` — `TEST-01`, `TEST-02`, and `TEST-03`, which define the required unit-test, GameTest, and shared-fixture outcomes.

### Prior Decisions
- `.planning/phases/01-build-reproducibility-baseline/01-CONTEXT.md` — Locked build and verification decisions Phase 2 must extend rather than replace.
- `.planning/phases/01-build-reproducibility-baseline/01-RESEARCH.md` — Phase 1 research on visible verification stages and the intended `runGameTestServer` integration path.

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `build.gradle` already contains JUnit 5 wiring, `verifyUnitTestStage`, `verifyGameTestStage`, and the `check` task integration added in Phase 1.
- `src/test/java/com/talhanation/recruits/build/BuildBaselineTest.java` is the current pure JVM smoke-test anchor.
- `BUILDING.md` already documents the staged verification contract that this phase needs to make real.

### Established Patterns
- The project currently has a JVM `src/test/java` tree but no Forge GameTest source tree yet.
- `verifyGameTestStage` currently allows a documented `NO-SOURCE` state, which Phase 2 is expected to replace with actual structure and cases.

### Integration Points
- `build.gradle` for source-set and task wiring.
- `src/test/java/**` for pure JVM tests and helpers.
- A new standard GameTest source location under `src/gametest/java` or equivalent Forge-supported path.

</code_context>

<specifics>
## Specific Ideas

No specific requirements — infrastructure phase.

</specifics>

<deferred>
## Deferred Ideas

None — infrastructure phase.

</deferred>

---

*Phase: 02-layered-test-harness-foundations*
*Context gathered: 2026-04-05*
