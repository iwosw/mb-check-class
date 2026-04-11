# Phase 6: Full-Surface Verification and Safe Degradation - Context

**Gathered:** 2026-04-07
**Status:** Ready for planning

<domain>
## Phase Boundary

Define and document a trustworthy full-surface verification matrix for the mod, extend or organize verification coverage where needed, and ensure optional or risky paths fail safely without destabilizing core behavior. This phase should make the verification surface explicit, reviewable, and repeatable without turning into a broad compatibility expansion or documentation-system rewrite.

</domain>

<decisions>
## Implementation Decisions

### Coverage Matrix Shape
- Structure the verification matrix to distinguish deep coverage areas from baseline smoke coverage, with extra depth on battles, persistence, commands, AI, networking, and formations.
- The matrix should include documented manual or lighter smoke coverage where full automation is not practical yet, instead of pretending unautomated areas are covered.
- Prior accepted gaps should appear explicitly as accepted deferred gaps with rationale so maintainers can separate known debt from new regressions.
- Build the matrix on the existing Gradle lifecycle and documented verification commands rather than inventing a separate verification runner.

### Gap Handling and Stabilization Bar
- Logic gaps found during Phase 6 should either be fixed in scope or documented explicitly as deferred with rationale.
- Preserve explicit attribution for existing accepted external failures so they are not mistaken for new Phase 6 failures.
- Optional paths must fail safely, stay bounded, and avoid destabilizing core mod flows when dependencies or contexts are absent.
- Safe degradation should be verified through focused tests and documented expectations for absent-mod and invalid-context behavior, not just code inspection.

### Optional Compat and Risky Surfaces
- Prioritize optional compatibility paths, mixins, pathfinding fallbacks, and similar risky surfaces where absence or mismatch can destabilize the core mod.
- Compat coverage in this phase is about verifying safe absence or failure behavior first, not certifying full compatibility across every optional mod.
- Treat mixin and pathfinding safety as in scope where fallback or degraded behavior is essential to core stability.
- Broad expansion of supported compat integrations and unrelated feature polish stay out of scope.

### Documentation and Phase Output
- Phase 6 should leave behind a documented verification matrix plus whatever supporting tests, fixes, and deferred rationale are needed to make it trustworthy.
- Maintainers should consume the matrix as an explicit repo document tied to the canonical Gradle commands and coverage categories.
- Deferred gaps should be recorded in a clear, reviewable form with rationale and context rather than scattered notes.
- Add the needed verification documentation with minimal disruption to existing build docs and planning artifacts.

### the agent's Discretion
- Exact document/file naming, matrix layout, and verification-category labels, as long as deep vs smoke coverage and accepted-gap attribution stay explicit.
- Exact compat and fallback scenarios chosen for representative safe-degradation coverage, provided they target real destabilization risk rather than expansion scope.

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Phase Definition
- `.planning/ROADMAP.md` — Phase 6 goal and success criteria for full-surface verification and safe degradation.
- `.planning/REQUIREMENTS.md` — `TEST-04`, `STAB-01`, `STAB-02`, and `STAB-03`, which define the matrix, gap handling, and optional-path degradation outcomes.

### Prior Decisions
- `.planning/phases/05-persistence-and-multiplayer-sync-hardening/05-CONTEXT.md` — preserves the use of explicit attribution for previously accepted external failures and the reuse of JVM/GameTest layers.
- `.planning/phases/04-command-and-ai-state-stabilization/04-CONTEXT.md` — preserves server-authoritative safe degradation for invalid flows.
- `.planning/phases/01-build-reproducibility-baseline/01-CONTEXT.md` — keeps the canonical verification entrypoint on standard Gradle lifecycle commands.

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- The repo already has a layered verification stack under `check`, plus reusable JVM and GameTest helpers from Phases 1-5.
- Existing tests already cover many high-risk areas that the matrix can classify rather than rebuild.
- `BUILDING.md` already documents the canonical build and verification commands and can anchor matrix-facing docs.

### Established Patterns
- Prior phases prefer narrow extracted seams and observable runtime assertions over monolithic or private-field testing.
- Safe degradation already appears across menus, packet validation, client cache reset, async pathfinding fallback, and optional compat/reflection code.
- Accepted external failures can be scoped and attributed explicitly rather than blocking every later phase.

### Integration Points
- `BUILDING.md`
- `build.gradle`
- `src/test/java/com/talhanation/recruits/**`
- `src/gametest/java/com/talhanation/recruits/**`
- `src/main/java/com/talhanation/recruits/compat/**`
- `src/main/java/com/talhanation/recruits/mixin/**`
- `src/main/java/com/talhanation/recruits/pathfinding/**`
- `src/main/java/com/talhanation/recruits/init/ModScreens.java`
- `.planning/STATE.md`

</code_context>

<specifics>
## Specific Ideas

- Make the verification matrix explicit about what is deeply covered, what only has smoke/manual coverage, and what remains deferred.
- Treat accepted earlier gaps as visible debt, not hidden noise.
- Verify optional-path absence and fallback behavior first, not full third-party compatibility certification.

</specifics>

<deferred>
## Deferred Ideas

- Broad compatibility expansion beyond safe absence/failure handling.
- Large doc-system rewrite.
- Unrelated feature polish.

</deferred>

---

*Phase: 06-full-surface-verification-and-safe-degradation*
*Context gathered: 2026-04-07*
