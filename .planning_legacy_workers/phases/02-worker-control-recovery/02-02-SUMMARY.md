# 02-02 Summary

## Outcome

Wired the new generic recovery action into the existing worker command screen without adding a new control surface.

## Changes

- Added `MessageRecoverWorkerControl` as a server-authoritative recovery packet for selected worker UUIDs.
- Registered the recovery packet in `WorkersMain`.
- Added a `Recover Workers` action to `WorkerCommandScreen` that flattens active group members and sends the packet.
- Added localized label and tooltip strings for the recovery action in the existing language files.

## Verification

- `./gradlew compileJava -x test`

## Notes

- Recovery requests remain ownership-gated on the server and report a visible result when no controlled workers can be recovered.
