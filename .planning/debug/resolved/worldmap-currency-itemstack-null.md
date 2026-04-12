---
status: resolved
trigger: "Investigate issue: worldmap-currency-itemstack-null"
created: 2026-04-12T00:00:00Z
updated: 2026-04-12T00:20:00Z
---

## Current Focus

hypothesis: fixed null-safety gap by routing world map currency access through a defaulted client helper instead of dereferencing unsynchronized state directly
test: verify compile succeeds and request user confirmation in real multiplayer workflow
expecting: opening the world map from the keybinding no longer crashes before claim sync arrives
next_action: user should test the original multiplayer reproduction path and confirm whether the crash is gone

## Symptoms

expected: Pressing the world map key should open the world map UI without crashing, even if currency item data has not been initialized yet.
actual: The client crashes immediately when the world map screen/context menu is constructed from the keybinding.
errors: "java.lang.NullPointerException: Cannot invoke \"net.minecraft.world.item.ItemStack.m_41720_()\" because \"com.talhanation.recruits.client.ClientManager.currencyItemStack\" is null at com.talhanation.recruits.client.gui.worldmap.WorldMapContextMenu.<init>(WorldMapContextMenu.java:43), called from WorldMapScreen.<init> and KeyEvents.onKeyInput."
reproduction: Join a non-integrated multiplayer Forge server, press the recruits world map keybinding, observe immediate client crash.
started: Reported from current 1.20.1 BannerMod 1.14.3 client session; active regression.

## Eliminated

## Evidence

- timestamp: 2026-04-12T00:05:00Z
  checked: .planning/debug/knowledge-base.md
  found: No matching prior debug entry for world map or currency item null initialization.
  implication: Investigate as a new regression rather than applying an existing known fix.

- timestamp: 2026-04-12T00:05:00Z
  checked: codebase search for currencyItemStack and world map files
  found: WorldMapContextMenu and WorldMapScreen dereference ClientManager.currencyItemStack; ClientManager declares it static and clears it to null; MessageToClientUpdateClaims sets it from a server packet.
  implication: The crash likely occurs when the UI opens before the claim update packet has initialized client currency state.

- timestamp: 2026-04-12T00:12:00Z
  checked: recruits/src/main/java/com/talhanation/recruits/client/events/KeyEvents.java and recruits/src/main/java/com/talhanation/recruits/ClaimEvents.java
  found: The world map keybinding opens new WorldMapScreen() immediately on the client, while claim/currency sync is broadcast asynchronously on player join from the server via MessageToClientUpdateClaims.
  implication: On multiplayer, the UI can be constructed before claim sync completes, so null currency state is a valid transient condition that the UI must tolerate.

- timestamp: 2026-04-12T00:12:00Z
  checked: recruits/src/main/java/com/talhanation/recruits/client/gui/worldmap/WorldMapContextMenu.java and recruits/src/main/java/com/talhanation/recruits/client/gui/worldmap/WorldMapScreen.java
  found: WorldMapContextMenu constructor immediately calls ClientManager.currencyItemStack.getItem() for cost icons, and WorldMapScreen.canPlayerPay does the same for affordability checks, with no null/empty guard.
  implication: The reported NPE mechanism is fully explained by UI code assuming synchronized claim currency exists before the screen opens.

- timestamp: 2026-04-12T00:17:00Z
  checked: ./gradlew compileJava
  found: Build succeeded after updating ClientManager, WorldMapContextMenu, and WorldMapScreen; no new compilation errors were introduced.
  implication: The fix is syntactically valid and integrated cleanly into the merged workspace.

## Resolution

root_cause: World map UI code directly dereferences ClientManager.currencyItemStack even though client sync resets that field to null and only populates it later through MessageToClientUpdateClaims. Pressing the keybinding on multiplayer can therefore open the screen before currency sync arrives, causing a constructor-time NPE in WorldMapContextMenu and a later payment-check NPE in WorldMapScreen.
fix: Added a guarded ClientManager currency accessor that falls back to an emerald ItemStack when claim sync has not initialized currency yet, and updated WorldMapContextMenu plus WorldMapScreen.canPlayerPay to use that accessor instead of dereferencing currencyItemStack directly.
verification: Self-verified by successful root-project compile (`./gradlew compileJava`) after replacing world map currency dereferences with a defaulted client helper, then user-confirmed fixed in the original multiplayer workflow.
files_changed: [recruits/src/main/java/com/talhanation/recruits/client/ClientManager.java, recruits/src/main/java/com/talhanation/recruits/client/gui/worldmap/WorldMapContextMenu.java, recruits/src/main/java/com/talhanation/recruits/client/gui/worldmap/WorldMapScreen.java]
