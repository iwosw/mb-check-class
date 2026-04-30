package com.talhanation.bannermod.client.military.gui;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CriticalUiStateVerificationTest {
    private static final Path ROOT = Path.of("");

    @Test
    void keybindingsKeepCurrentWorkerMapAndWarRoomGating() throws IOException {
        String keyEvents = read("src/main/java/com/talhanation/bannermod/client/military/events/KeyEvents.java");

        assertTrue(keyEvents.contains("com.talhanation.bannermod.registry.civilian.ModShortcuts.COMMAND_SCREEN_KEY.consumeClick()"));
        assertTrue(keyEvents.contains("selectWorkerCommandCategory(clientPlayerEntity);"));
        assertTrue(keyEvents.contains("CommandEvents.openCommandScreen(clientPlayerEntity);"));
        assertTrue(keyEvents.contains("nbt.putInt(\"RecruitsCategory\", workerCategory);"));

        assertTrue(keyEvents.contains("minecraft.level != null && minecraft.level.dimension() == Level.OVERWORLD"));
        assertTrue(keyEvents.contains("new MessageRequestFormationMapSnapshot()"));
        assertTrue(keyEvents.contains("minecraft.setScreen(new WorldMapScreen());"));

        assertTrue(keyEvents.contains("ModShortcuts.WAR_ROOM_KEY.consumeClick()"));
        assertTrue(keyEvents.contains("minecraft.setScreen(new WarListScreen(null));"));
    }

    @Test
    void worldMapActionsKeepVisibleGatingAndSyncStateReasons() throws IOException {
        String generalActions = read("src/main/java/com/talhanation/bannermod/client/military/gui/worldmap/WorldMapGeneralMenuActions.java");
        String claimActions = read("src/main/java/com/talhanation/bannermod/client/military/gui/worldmap/WorldMapClaimMenuActions.java");
        String clientPlayerEvents = read("src/main/java/com/talhanation/bannermod/client/military/events/ClientPlayerEvents.java");
        String worldMapScreen = read("src/main/java/com/talhanation/bannermod/client/military/gui/worldmap/WorldMapScreen.java");

        assertTrue(generalActions.contains("if (screen.getSelectedRoute() == null)"));
        assertTrue(generalActions.contains("menu.addDisabledEntry(TEXT_ADD_WAYPOINT, TEXT_WAYPOINT_NEEDS_ROUTE, \"route_waypoint_disabled\")"));
        assertTrue(generalActions.contains("!screen.canPlaceWaypointAt(screen.snapshotWorldX, screen.snapshotWorldZ)"));
        assertTrue(generalActions.contains("menu.addDisabledEntry(TEXT_ADD_WAYPOINT, TEXT_WAYPOINT_NEEDS_EXPLORED, \"route_waypoint_disabled\")"));
        assertTrue(generalActions.contains("menu.addEntry(TEXT_ADD_WAYPOINT, () -> true, WorldMapScreen::addWaypointAtClicked, \"route_waypoint_add\")"));

        assertTrue(claimActions.contains("boolean claimsReady = ClientManager.hasClaimsSnapshot && !ClientManager.claimsSnapshotStale;"));
        assertTrue(claimActions.contains("Component claimChunkDisabledReason = !ClientManager.hasClaimsSnapshot ? TEXT_DISABLED_SYNC"));
        assertTrue(claimActions.contains(": ClientManager.claimsSnapshotStale ? TEXT_DISABLED_STALE"));
        assertTrue(claimActions.contains(": !canClaimChunk ? TEXT_DISABLED_UNCLAIMABLE"));
        assertTrue(claimActions.contains(": TEXT_DISABLED_NOT_LEADER;"));

        assertTrue(clientPlayerEvents.contains("updateMapTiles(!screen.isNavigatingMap());"));

        assertTrue(worldMapScreen.contains("if (!ClientManager.hasClaimsSnapshot) {"));
        assertTrue(worldMapScreen.contains("\"gui.bannermod.map.claim_state.waiting_sync\""));
        assertTrue(worldMapScreen.contains("} else if (ClientManager.claimsSnapshotStale) {"));
        assertTrue(worldMapScreen.contains("\"gui.bannermod.map.claim_state.stale\""));
        assertTrue(worldMapScreen.contains("} else if (ClientManager.recruitsClaims.isEmpty()) {"));
        assertTrue(worldMapScreen.contains("\"gui.bannermod.map.claim_state.empty\""));
    }

    @Test
    void warScreensKeepWaitingForSyncStateSelection() throws IOException {
        String warListScreen = read("src/main/java/com/talhanation/bannermod/client/military/gui/war/WarListScreen.java");
        String politicalEntityListScreen = read("src/main/java/com/talhanation/bannermod/client/military/gui/war/PoliticalEntityListScreen.java");

        assertTrue(warListScreen.contains("boolean hasSnapshot = WarClientState.hasSnapshot();"));
        assertTrue(warListScreen.contains("? \"gui.bannermod.war_list.empty\""));
        assertTrue(warListScreen.contains(": \"gui.bannermod.war_list.waiting_sync\"") );

        assertTrue(politicalEntityListScreen.contains("String empty = text(WarClientState.hasSnapshot()"));
        assertTrue(politicalEntityListScreen.contains("? \"gui.bannermod.states.empty\""));
        assertTrue(politicalEntityListScreen.contains(": \"gui.bannermod.states.waiting_sync\"") );
    }

    private static String read(String relativePath) throws IOException {
        return Files.readString(ROOT.resolve(relativePath));
    }
}
