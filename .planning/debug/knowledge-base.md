# Debug Knowledge Base

Resolved debug sessions. A reference dump of known-pattern hypotheses for new investigations.

---

## mixinminecraft-shouldentityappearglowing-client-crash — client crash caused by missing packaged recruits refmap
- **Date:** 2026-04-12
- **Error patterns:** InvalidInjectionException, shouldEntityAppearGlowing, No refMap loaded, Reference map mixins.recruits.refmap.json could not be read, client crashes during mod load
- **Root cause:** The merged root build generates mixins.recruits.refmap.json during compilation but does not package it into the final bannermod jars. At runtime Mixin logs "Reference map 'mixins.recruits.refmap.json' ... could not be read", so MixinMinecraft's named injector method cannot be remapped to the runtime Minecraft namespace and crashes while looking for shouldEntityAppearGlowing by the unmapped name.
- **Fix:** Updated the merged root build so jar and shadowJar explicitly include build/tmp/compileJava/mixins.recruits.refmap.json in the packaged artifact.
- **Files changed:** build.gradle
---

## formation-collision-nearest-free-slot-fallback — blocked formation recruits now retarget to nearby open slots
- **Date:** 2026-04-12
- **Error patterns:** formation collision, nearest free slot fallback, recruits keep pushing, blocked formation slot, runtime behavior bug
- **Root cause:** Formation slot assignment was effectively static. Once a recruit entered hold-position formation movement, RecruitHoldPosGoal only kept pathing back to the same holdPos and never reacted to navigation-stuck or collision states, so blocked recruits kept pushing at occupied paths without releasing or reassigning their slot.
- **Fix:** Added a runtime formation fallback helper that picks the nearest currently unoccupied formation slot, swaps slot claims between recruits so the blocked recruit releases its original slot, and invoked it from RecruitHoldPosGoal when navigation reports collision or stuck behavior.
- **Files changed:** recruits/src/main/java/com/talhanation/recruits/util/FormationUtils.java, recruits/src/main/java/com/talhanation/recruits/entities/ai/RecruitHoldPosGoal.java, recruits/src/test/java/com/talhanation/recruits/util/FormationUtilsTest.java
---

## worldmap-currency-itemstack-null — world map tolerates unsynced client currency state
- **Date:** 2026-04-12
- **Error patterns:** NullPointerException, currencyItemStack is null, WorldMapContextMenu, WorldMapScreen, keybinding world map crash, multiplayer claim sync
- **Root cause:** World map UI code directly dereferenced ClientManager.currencyItemStack even though client sync resets that field to null and only populates it later through MessageToClientUpdateClaims. Pressing the keybinding on multiplayer can therefore open the screen before currency sync arrives, causing a constructor-time NPE in WorldMapContextMenu and a later payment-check NPE in WorldMapScreen.
- **Fix:** Added a guarded ClientManager currency accessor that falls back to an emerald ItemStack when claim sync has not initialized currency yet, and updated WorldMapContextMenu plus WorldMapScreen.canPlayerPay to use that accessor instead of dereferencing currencyItemStack directly.
- **Files changed:** recruits/src/main/java/com/talhanation/recruits/client/ClientManager.java, recruits/src/main/java/com/talhanation/recruits/client/gui/worldmap/WorldMapContextMenu.java, recruits/src/main/java/com/talhanation/recruits/client/gui/worldmap/WorldMapScreen.java
---

## multiplayer-recruit-currency-and-farmer-claim-idle — legacy military config sync and claim-grown farmer crop-area seeding
- **Date:** 2026-04-13
- **Error patterns:** multiplayer emerald currency, RecruitCurrency golden_ingot ignored, faction creation still uses emerald, recruit hire still uses emerald, worker hire still uses emerald, UI icons still show emerald, farmer idle in claim, farmer_no_area, prepared field ignored
- **Root cause:** The merged runtime switched military config ownership from `bannermod-server.toml` to `bannermod-military.toml` but only migrated once when the new file was absent, so later legacy-file edits were ignored and multiplayer currency sync stayed on emerald. Separately, claim worker growth spawned farmers without any authored or auto-created `CropArea`, while farmer AI only works against crop-area entities, leaving prepared-field claim farmers permanently idle.
- **Fix:** Added startup forward-sync from a newer legacy military config into the active military config file, and taught claim-grown farmers to seed/bind a crop area from a prepared field during claim worker spawn. Added regression coverage for both paths.
- **Files changed:** src/main/java/com/talhanation/bannermod/config/BannerModConfigFiles.java, src/test/java/com/talhanation/bannermod/BannerModConfigFilesTest.java, workers/src/main/java/com/talhanation/workers/settlement/WorkerSettlementSpawner.java, src/gametest/java/com/talhanation/bannermod/BannerModClaimWorkerGrowthGameTests.java
---

## outsider-claim-interaction-bypass — outsiders can no longer interact inside чужие claims
- **Date:** 2026-04-13
- **Error patterns:** outsider claim interaction bypass, чужой клейм, water bucket in чужом клейме, lava bucket in чужом клейме, usable blocks inside claim, outsider attack inside claim, no errors
- **Root cause:** ClaimEvents only denied a narrow subset of RightClickBlock targets and never applied claim-aware checks to entity interaction or direct attack events, so outsiders could still use generic usable blocks, bucket/liquid click paths, and entity actions inside чужие claims.
- **Fix:** Replaced the whitelist-style block interaction gate with a unified outsider block interaction check, added RightClickItem plus entity interact/attack claim guards, and expanded claim protection gametests to cover generic use, bucket click, and attack denial paths.
- **Files changed:** recruits/src/main/java/com/talhanation/recruits/ClaimEvents.java, src/gametest/java/com/talhanation/bannermod/BannerModClaimProtectionGameTests.java
---

## upkeep-storage-and-provider-sourcing — upkeep now resolves settlement storage providers
- **Date:** 2026-04-14
- **Error patterns:** workers ignore storage chest, upkeep command gray on chest hover, recruits fail to source upkeep, workers fail to source upkeep, no visible errors
- **Root cause:** Recruit/worker upkeep logic only understood direct Container targets, while settlement storage providers are represented as StorageArea/MarketArea entities that wrap multiple scanned chests. The command UI rejected those entities on hover, and the upkeep goals could not resolve them into consumable containers, so workers/recruits could not source food or payment through the intended settlement provider path.
- **Fix:** Add a shared upkeep-provider bridge that recognizes StorageArea/MarketArea entities and exposes their scanned chests as one combined Container, then reuse that bridge in the command-screen upkeep target validation and upkeep goals.
- **Files changed:** src/main/java/com/talhanation/bannermod/logistics/BannerModCombinedContainer.java, src/main/java/com/talhanation/bannermod/logistics/BannerModUpkeepProviders.java, recruits/src/main/java/com/talhanation/recruits/client/gui/commandscreen/OtherCategory.java, recruits/src/main/java/com/talhanation/recruits/entities/ai/RecruitUpkeepPosGoal.java, recruits/src/main/java/com/talhanation/recruits/entities/ai/RecruitUpkeepEntityGoal.java
---
