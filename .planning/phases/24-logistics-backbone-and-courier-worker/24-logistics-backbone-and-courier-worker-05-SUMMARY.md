---
phase: 24-logistics-backbone-and-courier-worker
plan: 05
type: execute
completed: 2026-04-20T00:35:00Z
---

## Summary

Compact Phase 24 advanced the sea-trade substrate into a live runtime publication step: the shared logistics runtime can now derive sea-trade entrypoints from actual server-side `StorageArea` entities and ignores authored routes whose storage endpoints are not present.

## What changed

- Extended `BannerModLogisticsService` with a storage-availability-aware sea-trade entrypoint listing path so stale or unloaded route endpoints do not publish fake import/export handoffs.
- Added `BannerModLogisticsRuntime.listSeaTradeEntrypoints(Collection<StorageArea>)` so later treasury/trade slices can consume one server-authoritative read model built from live storage entities instead of hand-built route lists.
- Adapted the retained courier GameTest to assert the new live-storage projection path and to prove that a stale authored route does not surface as a sea-trade entrypoint.
- Added a focused logistics unit test covering the new missing-endpoint filter.

## Verification

- `./gradlew test --tests com.talhanation.bannermod.logistics.BannerModLogisticsServiceTest --console=plain`
- `./gradlew compileGameTestJava --console=plain`

## Notes

- This remains a read-model/runtime-publication slice only. It does not introduce ships, market simulation, cargo persistence, or cross-settlement economy authority.
