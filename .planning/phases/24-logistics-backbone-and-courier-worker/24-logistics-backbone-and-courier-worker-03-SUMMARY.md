---
phase: 24-logistics-backbone-and-courier-worker
plan: 03
type: execute
completed: 2026-04-18T14:40:00Z
---

## Summary

Plan 24-03 is complete: workers can now carry an assigned courier task through the existing storage pickup/deposit goals, with source-to-destination routing based on reservation state and bounded failure cleanup when either endpoint is missing, empty, or full.

## What changed

- Added `CourierTaskFlow` as the narrow execution helper that decides whether a courier is still in pickup mode and which storage node is currently authoritative.
- Extended `AbstractWorkerEntity` with courier-task lifecycle helpers: pickup need sync, target-storage selection, pickup completion, delivery completion, and bounded task abandonment.
- Routed `AbstractChestGoal.scanAvailableStorageAreas()` through the active courier task so storage lookup follows the claimed source until pickup is complete, then the destination for delivery.
- Updated `GetNeededItemsFromStorage` and `DepositItemsToStorage` to complete or abandon active courier tasks through the shared blocked-reason vocabulary instead of looping indefinitely on the wrong endpoint.
- Added focused unit coverage for courier pickup-count and source/destination switching semantics.

## Verification

- `./gradlew compileJava --console=plain`
- `./gradlew test --tests com.talhanation.bannermod.ai.civilian.CourierTaskFlowTest --console=plain`
- `./gradlew test --tests com.talhanation.bannermod.logistics.* --console=plain`

## Notes

- This slice deliberately reuses the current worker storage goals rather than introducing a separate courier-only AI stack.
- Route claiming still enters through the existing shared logistics task object; route authoring and packet/UI surfaces remain in Plan 24-04.
