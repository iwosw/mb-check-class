# 02-01 Summary

## Outcome

Added the shared worker-control foundation for Phase 2: a pure blocked/idle anti-spam seam and one generic recovery path across all worker types.

## Changes

- Added `WorkerControlStatus` as a small pure-Java helper that tracks blocked vs idle transitions by reason token.
- Added focused JUnit coverage for first-transition, duplicate suppression, kind/reason changes, and reset behavior.
- Extended `AbstractWorkerEntity` with shared `reportBlockedReason(...)`, `reportIdleReason(...)`, `clearWorkStatus()`, and `recoverControl(...)` APIs.
- Added per-worker recovery overrides so each worker clears its active assignment field during recovery.

## Verification

- `./gradlew test --tests com.talhanation.workers.entities.WorkerControlStatusTest`

## Notes

- Recovery remains soft-fail and chat-first, matching the existing control patterns used elsewhere in the mod.
