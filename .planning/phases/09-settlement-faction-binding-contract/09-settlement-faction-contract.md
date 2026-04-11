# BannerMod Settlement-Faction Contract

## Status

- Active Phase 09 contract for the merged `bannermod` runtime.
- Scope is intentionally derived and low-risk: this contract defines settlement-faction vocabulary and one shared query seam without introducing a new standalone settlement manager or save-data format.

## Definition

- A `settlement` is a derived operational footprint, not a required new persisted manager.
- The footprint exists where active worker infrastructure operates inside faction-owned claim coverage.
- The current code seams that ground this contract are claim lookup in `RecruitsClaimManager`, claim owner faction in `RecruitsClaim`, work-area authoring access in `AbstractWorkAreaEntity`, and worker participation checks in `canWorkHere(...)`.

## Binding Rule

- A settlement is faction-bound when its authored work-area footprint and the covering claim resolve to the same faction id.
- The binding is derived from claim ownership plus authored infrastructure metadata such as a work area's owner/team context.
- Phase 09 does not require a dedicated settlement registry, transfer manager, or broad persistence rewrite to make that binding explicit.

## Lifecycle Vocabulary

### Friendly claim settlement

- `FRIENDLY_CLAIM` means the current faction context matches the claim owner faction for the covered chunk.
- This is the legal placement and operation state for current worker infrastructure.
- Work-area creation, client-side placement previews, and worker participation may treat this as allowed.

### Hostile claim settlement

- `HOSTILE_CLAIM` means a claim exists, but the caller faction context does not match the claim owner faction.
- This is the explicit denial state for new settlement placement and other faction-aware legality checks.
- Phase 09 keeps the behavior narrow: it routes existing placement checks through one shared resolver rather than promising a broader hostile-territory simulation.

### Unclaimed footprint

- `UNCLAIMED` means no valid claim owner covers the queried chunk.
- When work-area placement is configured to require claim coverage, this is not a legal settlement-authoring state.
- Downstream military, logistics, or validation slices should read this as a settlement footprint without faction authority, not as an implicitly neutral owned settlement.

### Degraded settlement

- `DEGRADED_MISMATCH` means a settlement footprint still carries authored settlement affiliation, but the current claim owner no longer matches that affiliation.
- This is the explicit claim-loss or faction-mismatch state for existing infrastructure.
- Claim loss should degrade or disable civilian throughput before ownership is silently transferred.

## Interpretation By Current Systems

- Placement: `MessageAddWorkArea` should treat only `FRIENDLY_CLAIM` as legal when claim-restricted placement is enabled.
- Shared authoring: owner, same-team, and admin access rules remain a separate authority contract; settlement binding does not widen who may author or recover control.
- Worker participation: existing work-area participation may read the same shared settlement binding and stop treating hostile, unclaimed, or degraded footprints as fully operational settlement sites.
- Client helpers: placement previews and other legality hints should use the same faction-aware query vocabulary as the server.
- Military/logistics readers: later slices may consume the same status names without inferring settlement state from scattered claim checks.

## Current Runtime Seams

- `recruits/src/main/java/com/talhanation/recruits/world/RecruitsClaimManager.java`
  - `getClaim(ChunkPos chunkPos)` resolves the covering claim on the server.
  - `getClaimAt(ChunkPos pos, List<RecruitsClaim> allClaims)` resolves the same idea from mirrored client state.
- `recruits/src/main/java/com/talhanation/recruits/world/RecruitsClaim.java`
  - `containsChunk(...)`, `getOwnerFaction()`, and `getOwnerFactionStringID()` define whether a chunk is covered and by whom.
- `workers/src/main/java/com/talhanation/workers/network/MessageAddWorkArea.java`
  - Work-area placement legality already depends on whether the target lies inside the acting faction's claim.
- `workers/src/main/java/com/talhanation/workers/entities/workarea/AbstractWorkAreaEntity.java`
  - `getAuthoringAccess(...)` and `canWorkHere(...)` already represent authored settlement participation and ownership/team boundaries.
- `workers/src/main/java/com/talhanation/workers/network/WorkAreaAuthoringRules.java`
  - Creation and mutation decisions remain the caller-facing feedback seam for worker-area authoring.

## Explicit Non-Goals

- No new standalone settlement saved-data manager.
- No broad rewrite of worker AI, military behavior, or faction persistence.
- No claim that all settlement-faction enforcement is fully validated in Phase 09; Phase 10 remains the validation-first follow-up.
