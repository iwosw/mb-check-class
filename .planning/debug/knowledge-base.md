# GSD Debug Knowledge Base

Resolved debug sessions. Used by `gsd-debugger` to surface known-pattern hypotheses at the start of new investigations.

---

## mixinminecraft-shouldentityappearglowing-client-crash — client crash caused by missing packaged recruits refmap
- **Date:** 2026-04-12
- **Error patterns:** InvalidInjectionException, shouldEntityAppearGlowing, No refMap loaded, Reference map mixins.recruits.refmap.json could not be read, client crashes during mod load
- **Root cause:** The merged root build generates mixins.recruits.refmap.json during compilation but does not package it into the final bannermod jars. At runtime Mixin logs "Reference map 'mixins.recruits.refmap.json' ... could not be read", so MixinMinecraft's named injector method cannot be remapped to the runtime Minecraft namespace and crashes while looking for shouldEntityAppearGlowing by the unmapped name.
- **Fix:** Updated the merged root build so jar and shadowJar explicitly include build/tmp/compileJava/mixins.recruits.refmap.json in the packaged artifact.
- **Files changed:** build.gradle
---
