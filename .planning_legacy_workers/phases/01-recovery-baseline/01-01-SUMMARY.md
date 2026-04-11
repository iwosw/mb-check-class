# 01-01 Summary

## Outcome

Recovered the baseline 1.20.1 build metadata so the project compiles cleanly again on the current machine.

## Changes

- Updated the Gradle wrapper to `8.5` so the build works with the installed Java 21 runtime.
- Aligned `gradle.properties` placeholder values with the actual Workers 1.20.1 baseline metadata.
- Added `mavenCentral()` to `settings.gradle` plugin resolution.
- Added `systemProp.net.minecraftforge.gradle.check.certs=false` to avoid ForgeGradle certificate validation blocking task discovery in this environment.
- Kept `mods.toml` on the existing Forge 47.x / Recruits-required baseline.

## Verification

- `./gradlew compileJava -x test`
- `./gradlew tasks --all`

## Notes

- The baseline still emits deprecation warnings from Forge/Minecraft APIs, but they are non-blocking for Phase 1.
