## Deferred out-of-scope issues

- `./gradlew compileJava test --console=plain` still fails in pre-existing root test sources outside Phase 23-04 governance scope.
- Current failures are the known 39 compile errors in `src/test/java/com/talhanation/bannermod/**` and `src/test/java/com/talhanation/workers/**` (missing legacy runtime symbols and settlement type mismatches).
- Governance UI/runtime changes for Plan 23-04 compile successfully with `./gradlew compileJava --console=plain`.
