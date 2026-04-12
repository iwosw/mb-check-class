---
status: resolved
trigger: "Investigate issue: mixinminecraft-shouldentityappearglowing-client-crash"
created: 2026-04-12T00:00:00Z
updated: 2026-04-12T00:40:00Z
---

## Current Focus

hypothesis: confirmed — the missing packaged recruits refmap caused the client mixin injection failure
test: user needs to run the rebuilt bannermod jar in the real client modpack and confirm the startup no longer crashes at MixinMinecraft.shouldEntityAppearGlowing injection
expecting: BannerMod loads without the "Reference map 'mixins.recruits.refmap.json' ... could not be read" warning or the InvalidInjectionException for injectShouldEntityAppearGlowing
next_action: wait for human verification in the Prism/Forge client environment

## Symptoms

expected: The client should start normally with BannerMod and the provided mod set; at minimum BannerMod should initialize on the client without mixin injection failures.
actual: The client crashes during mod load before reaching the main menu.
errors: InvalidInjectionException Critical injection failure: @Inject annotation on injectShouldEntityAppearGlowing could not find any targets matching 'shouldEntityAppearGlowing' in net.minecraft.client.Minecraft. No refMap loaded.
reproduction: Reproduce by launching the modded client with the provided mod set. Workspace side likely needs code inspection and local validation of the mixin target/method mapping rather than full external Prism reproduction. Prioritize confirming whether the injected method name/signature is wrong for the built artifact or missing due to mapping/refmap packaging.
started: Never worked in this client path.

## Eliminated

## Evidence

- timestamp: 2026-04-12T00:05:00Z
  checked: .planning/debug/knowledge-base.md
  found: Knowledge base file does not exist yet.
  implication: No prior resolved debug patterns available; investigate from first principles.

- timestamp: 2026-04-12T00:05:00Z
  checked: mixin file discovery
  found: Relevant files located at recruits/src/main/java/com/talhanation/recruits/mixin/MixinMinecraft.java and recruits/src/main/resources/mixins.recruits.json.
  implication: The failing injection originates from the recruits runtime code/resources in this merged workspace.

- timestamp: 2026-04-12T00:10:00Z
  checked: recruits/src/main/java/com/talhanation/recruits/mixin/MixinMinecraft.java
  found: The mixin injects with @Inject(method = "shouldEntityAppearGlowing", at = @At("HEAD"), cancellable = true) into net.minecraft.client.Minecraft.
  implication: The injector depends on method-name remapping or matching to locate the runtime target.

- timestamp: 2026-04-12T00:10:00Z
  checked: recruits/src/main/resources/mixins.recruits.json
  found: The config declares refmap "mixins.recruits.refmap.json" and includes MixinMinecraft as a client mixin.
  implication: If that refmap is absent or not loaded, named-method injection against obfuscated runtime names can fail exactly as reported.

- timestamp: 2026-04-12T00:10:00Z
  checked: build.gradle mixin setup
  found: The merged root build config adds sourceSets.main refmaps for both recruits and workers and registers mixins.recruits.json plus mixins.workers.json; recruits/build.gradle separately also adds mixins.recruits.refmap.json.
  implication: Build-time refmap generation is intended, so the next question is whether the shipped artifact/resource path actually contains the recruits refmap and config under the expected names.

- timestamp: 2026-04-12T00:15:00Z
  checked: Prism/Forge/Mixin log excerpt
  found: The runtime explicitly warns "Reference map 'mixins.recruits.refmap.json' for mixins.recruits.json could not be read" immediately before the fatal InvalidInjectionException on injectShouldEntityAppearGlowing.
  implication: The crash mechanism is consistent with refmap-loading failure rather than a generic unrelated mod conflict.

- timestamp: 2026-04-12T00:15:00Z
  checked: repository-wide file search for mixins.recruits.refmap.json
  found: No existing source or built file named mixins.recruits.refmap.json was present in the workspace at inspection time.
  implication: Unless build generation creates and packages it later, the shipped artifact will lack the refmap resource that the mixin config expects.

- timestamp: 2026-04-12T00:20:00Z
  checked: ./gradlew assemble
  found: The merged root project builds successfully and produces build/libs/bannermod-1.20.1-1.14.3-all.jar and build/libs/bannermod-1.20.1-1.14.3.jar.
  implication: The issue is not a compile-time mixin definition failure; it is likely in packaging/runtime consumption.

- timestamp: 2026-04-12T00:20:00Z
  checked: build/resources/main and build/tmp/compileJava
  found: build/resources/main contains mixins.recruits.json but not mixins.recruits.refmap.json, while build/tmp/compileJava/mixins.recruits.refmap.json exists.
  implication: The refmap is generated during compilation but is not copied into the processed resources output that feeds the final artifact.

- timestamp: 2026-04-12T00:25:00Z
  checked: built jar contents after assemble
  found: Both build/libs/bannermod-1.20.1-1.14.3.jar and build/libs/bannermod-1.20.1-1.14.3-all.jar contain mixins.recruits.json and mixins.workers.refmap.json, but do not contain mixins.recruits.refmap.json.
  implication: The shipped BannerMod artifact is definitively missing the recruits refmap that its mixin config references, which directly explains the runtime warning and injection failure.

- timestamp: 2026-04-12T00:30:00Z
  checked: first packaging fix attempt
  found: Rebuilding failed because jar already contained mixins.workers.refmap.json, so copying all generated *.refmap.json files created a duplicate-entry error for workers.
  implication: Workers refmap packaging already works; the minimal fix should only add the missing recruits refmap.

- timestamp: 2026-04-12T00:35:00Z
  checked: rebuilt jars after targeted build.gradle fix
  found: ./gradlew assemble succeeds, and both build/libs/bannermod-1.20.1-1.14.3.jar and build/libs/bannermod-1.20.1-1.14.3-all.jar now contain mixins.recruits.refmap.json alongside both mixin configs and the workers refmap.
  implication: The shipped artifact now includes the refmap resource that the failing client mixin config requires for runtime remapping.

## Resolution

root_cause: The merged root build generates mixins.recruits.refmap.json during compilation but does not package it into the final bannermod jars. At runtime Mixin logs "Reference map 'mixins.recruits.refmap.json' ... could not be read", so MixinMinecraft's named injector method cannot be remapped to the runtime Minecraft namespace and crashes while looking for shouldEntityAppearGlowing by the unmapped name.
fix: Updated the merged root build so jar and shadowJar explicitly include build/tmp/compileJava/mixins.recruits.refmap.json in the packaged artifact.
verification: Verified with ./gradlew assemble and direct jar inspection that both bannermod output jars now include mixins.recruits.refmap.json; user then confirmed the rebuilt jar fixes the real Prism/Forge client startup crash in the target modpack.
files_changed: [build.gradle]
