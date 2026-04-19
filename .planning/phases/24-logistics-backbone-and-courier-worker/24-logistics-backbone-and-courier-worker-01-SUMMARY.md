---
phase: 24-logistics-backbone-and-courier-worker
plan: 01
type: execute
completed: 2026-04-18T14:10:00Z
---

## Summary

Plan 24-01 is complete: the shared logistics contract layer now exists under `com.talhanation.bannermod.shared.logistics` with explicit route, node, item-filter, reservation, priority, blocked-reason, and courier-task vocabulary.

## What changed

- Added the core shared logistics records and enums for authored storage-node routing and lightweight reservation intent.
- Kept reservations item-intent shaped (`filter` + `reservedCount`) instead of slot-lock shaped.
- Added focused unit coverage for reservation expiry and filter semantics.

## Verification

- `./gradlew compileJava --console=plain`
- `./gradlew test --tests com.talhanation.bannermod.logistics.* --console=plain`

## Notes

- The original filter tests relied on full Minecraft bootstrap through `Items`/`ItemStack`, which is not safe in the plain JUnit environment used here. The final verification path uses `ResourceLocation`-level filter matching instead, via `BannerModLogisticsItemFilter.matchesItemId(...)`, while preserving the runtime `ItemStack` matcher used by production code.
