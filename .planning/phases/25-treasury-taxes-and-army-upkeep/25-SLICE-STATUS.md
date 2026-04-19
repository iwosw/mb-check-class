# Phase 25 Slice Status: Settlement Growth/Project Candidate Seed

## Slice

Compact Phase 25 bridge slice that persists one additive settlement `projectCandidateSeed` from the already-landed building profiles, stockpile summary, desired goods seed, market state, and governor/claim settlement seams.

## Status

Complete for the `projectCandidateSeed` slice only; this file is not the full Phase 25 status summary, and later governance/settlement bugfix closeouts landed after this slice.

## Delivered

- Added persisted `BannerModSettlementProjectCandidateSeed` so settlement snapshots now carry one compact candidate id, target building-profile hint, priority, governance/claim readiness flags, and stable driver ids.
- Updated `BannerModSettlementService` to derive `projectCandidateSeed` from existing building-profile counts, stockpile presence, desired-goods pressure, market readiness, and governor/claim settlement seams.
- Kept the slice additive and settlement-owned: no project manager, construction runtime, worker AI, or settlement growth loop was introduced.
- Extended focused settlement JUnit coverage to verify candidate derivation and full snapshot persistence round-trip alongside the already-landed settlement seeds.

## Not Delivered In This Slice

- Any real settlement project executor, build queue, housing growth loop, or claim growth rewrite.
- Any change to current worker gameplay, pathing, command handling, or work-goal selection.
- Any new manager that owns project progression, construction consumption, or villager replacement behavior.

## Verification Notes

- `./gradlew test --tests com.talhanation.bannermod.settlement.BannerModSettlementManagerTest --tests com.talhanation.bannermod.settlement.BannerModSettlementServiceTest --console=plain` succeeded on 2026-04-19.

## Later Confirmed Closeouts

- Governor heartbeat accounting now derives live worker/recruit supply-upkeep state, and governor UI surfaces the persisted fiscal rollup instead of leaving it server-only.
- Controlled-worker settlement semantics were tightened for unassigned and missing-building cases.
- Settlement heuristics now prefer live sea-trade entrypoint sets where available, and targeted refresh hooks now run on storage updates plus worker work-area binding changes.
- Enum-load hardening improved for several resident and service seeds, but remaining raw `Enum.valueOf(...)` persistence paths mean this closeout is only partial today.
