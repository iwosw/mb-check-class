# Runtime Identity and Namespace Contract

## Status

- Active Phase 02 contract for runtime identity and namespace decisions D-01 through D-04.
- Applies to release-facing metadata, merge notes, and follow-up implementation planning.

## BannerMod-first public identity

- The merged runtime is publicly `BannerMod` first.
- The active shipped mod identity remains `modId="bannermod"` with `displayName="BannerMod"` in `recruits/src/main/resources/META-INF/mods.toml`.
- Any remaining Recruits-branded release wording in active metadata is transitional debt under D-02, not an accepted long-term identity.
- Downstream phases must not revive a second live `workers` runtime identity, artifact identity, or public branding track.

## Code-backed basis for this contract

- `recruits/src/main/resources/META-INF/mods.toml` is the release-facing metadata source for the merged runtime.
- `build.gradle` already packages Workers GUI textures into `assets/bannermod/textures/gui/workers` and Workers structures into `assets/bannermod/structures/workers`, proving the active runtime already targets BannerMod-owned resource paths.
- `workers/src/main/java/com/talhanation/workers/WorkersRuntime.java` defines `ACTIVE_ASSET_NAMESPACE = "bannermod"` and resolves merged GUI/structure resource locations through that namespace.

## Namespace end-state

- The end-state for Workers-owned GUI, structure, and language assets is full ownership under the active `bannermod` namespace.
- Preserved `assets/workers/**` content is migration input and compatibility residue, not the long-term namespace policy.
- Mixed namespace operation is therefore transitional: it may exist temporarily while files are re-homed safely, but it must not be documented as the desired steady state.

## Transitional boundaries

- This contract does not require immediate Java package renames, registry-id rewrites, or build-coordinate changes in Phase 02 Plan 01.
- Legacy `workers:*` handling that still exists must be framed as migration-only compatibility behavior.
- Follow-up implementation work may keep narrow migration seams while moving remaining asset/lang ownership toward BannerMod.

## Planning implications

- Merge notes and active metadata must describe one merged BannerMod runtime truthfully.
- Later phases should treat `assets/bannermod/**` as the destination namespace when deciding how to absorb preserved Workers resources.
- No plan should describe the preserved `workers` resource tree as justification for keeping a second live runtime identity.
