package com.talhanation.bannermod.client.military.gui.worldmap;

import com.talhanation.bannermod.persistence.military.RecruitsRoute;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorldMapRouteUiVerificationTest {
    private static final Path ROOT = Path.of("");

    @Test
    void routePrimitivesMapWorldCoordinatesAndHitTestWaypoints() {
        RecruitsRoute route = new RecruitsRoute("Patrol");
        RecruitsRoute.Waypoint first = waypoint("First", 16, 32);
        RecruitsRoute.Waypoint second = waypoint("Second", 80, 32);
        route.addWaypoint(first);
        route.addWaypoint(second);

        assertEquals(42, WorldMapRenderPrimitives.screenX(16, 10, 2));
        assertEquals(69, WorldMapRenderPrimitives.screenZ(32, 5, 2));

        assertSame(first, RouteRenderer.getWaypointAt(route, 44, 71, 10, 5, 2));
        assertSame(second, RouteRenderer.getWaypointAt(route, 170, 69, 10, 5, 2));
        assertNull(RouteRenderer.getWaypointAt(route, 120, 120, 10, 5, 2));
    }

    @Test
    void routeRendererComputesStableDragInsertPositions() {
        RecruitsRoute route = new RecruitsRoute("Patrol");
        RecruitsRoute.Waypoint first = waypoint("First", 0, 0);
        RecruitsRoute.Waypoint dragging = waypoint("Dragging", 100, 0);
        RecruitsRoute.Waypoint last = waypoint("Last", 200, 0);
        route.addWaypoint(first);
        route.addWaypoint(dragging);
        route.addWaypoint(last);

        assertEquals(0, RouteRenderer.computeInsertIndex(route, dragging, -5, 0, 0, 0, 1));
        assertEquals(1, RouteRenderer.computeInsertIndex(route, dragging, 100, 0, 0, 0, 1));
        assertEquals(2, RouteRenderer.computeInsertIndex(route, dragging, 205, 0, 0, 0, 1));
    }

    @Test
    void worldMapRouteUiStillWiresRouteDisplayPopupsScrollSelectionAndMoveDispatch() throws IOException {
        String screen = read("src/main/java/com/talhanation/bannermod/client/military/gui/worldmap/WorldMapScreen.java");
        String routeRenderer = read("src/main/java/com/talhanation/bannermod/client/military/gui/worldmap/RouteRenderer.java");
        String routeNamePopup = read("src/main/java/com/talhanation/bannermod/client/military/gui/worldmap/RouteNamePopup.java");
        String waypointPopup = read("src/main/java/com/talhanation/bannermod/client/military/gui/worldmap/WaypointEditPopup.java");
        String selectionUi = read("src/main/java/com/talhanation/bannermod/client/military/gui/worldmap/WorldMapRouteSelectionUiController.java");
        String formationActions = read("src/main/java/com/talhanation/bannermod/client/military/gui/worldmap/WorldMapFormationMenuActions.java");
        String moveOrder = read("src/main/java/com/talhanation/bannermod/network/messages/military/MessageFormationMapMoveOrder.java");

        assertTrue(routeRenderer.contains("WorldMapRenderPrimitives.solidLine"));
        assertTrue(routeRenderer.contains("WorldMapRenderPrimitives.dashedLine"));
        assertTrue(routeRenderer.contains("WorldMapRenderPrimitives.texturedIcon"));
        assertTrue(screen.contains("routeInteractionLayer.renderRouteOverlay(guiGraphics, mouseX, mouseY);"));

        assertTrue(routeNamePopup.contains("WorldMapRenderPrimitives.panel"));
        assertTrue(routeNamePopup.contains("routeController.createRoute(nameField.getValue().trim())"));
        assertTrue(routeNamePopup.contains("parent.setSelectedRoute(routeController.getSelectedRoute())"));
        assertTrue(waypointPopup.contains("WorldMapRenderPrimitives.panel"));
        assertTrue(waypointPopup.contains("routeController.saveSelectedRoute()"));
        assertTrue(waypointPopup.contains("actionType = RecruitsRoute.WaypointAction.Type.WAIT"));

        assertTrue(screen.contains("if (routeInteractionLayer.isAnyPopupVisible()) return true;"));
        assertTrue(screen.contains("double mouseWorldX = (mouseX - offsetX) / scale;"));
        assertTrue(screen.contains("offsetX = mouseX - mouseWorldX * scale;"));
        assertTrue(screen.contains("selectedFormationContactId = clickedContact.contactId();"));
        assertTrue(selectionUi.contains("screen.clearHoveredAndSelectedChunk();"));

        assertTrue(formationActions.contains("new MessageFormationMapMoveOrder("));
        assertTrue(moveOrder.contains("CommandEvents.getSavedFormation(sender)"));
        assertTrue(moveOrder.contains("new CommandIntent.Movement("));
        assertTrue(moveOrder.contains("MovementCommandState.MOVE_TO_POSITION"));
        assertTrue(moveOrder.contains("CommandIntentDispatcher.dispatch(sender, intent, recruits);"));
    }

    @Test
    void worldMapClaimWaypointAndRouteFlowDocsMatchShippedInteractions() throws IOException {
        String claimActions = read("src/main/java/com/talhanation/bannermod/client/military/gui/worldmap/WorldMapClaimMenuActions.java");
        String generalActions = read("src/main/java/com/talhanation/bannermod/client/military/gui/worldmap/WorldMapGeneralMenuActions.java");
        String routeEditPopup = read("src/main/java/com/talhanation/bannermod/client/military/gui/worldmap/RouteEditPopup.java");
        String waypointPopup = read("src/main/java/com/talhanation/bannermod/client/military/gui/worldmap/WaypointEditPopup.java");
        String waypointController = read("src/main/java/com/talhanation/bannermod/client/military/gui/worldmap/WorldMapWaypointInteractionController.java");
        String routeMutation = read("src/main/java/com/talhanation/bannermod/client/military/gui/worldmap/WorldMapRouteMutationController.java");
        String claimIntent = read("src/main/java/com/talhanation/bannermod/network/messages/military/MessageClaimIntent.java");
        String englishLang = read("src/main/resources/assets/bannermod/lang/en_us.json");
        String englishGuide = read("MULTIPLAYER_GUIDE_EN.md");
        String russianGuide = read("MULTIPLAYER_GUIDE_RU.md");
        String almanac = read("docs/BANNERMOD_ALMANAC.html");

        assertTrue(!claimActions.contains("TEXT_CLAIM_AREA"));
        assertTrue(claimActions.contains("new MessageClaimIntent(MessageClaimIntent.Action.REMOVE_CHUNK"));
        assertTrue(claimIntent.contains("applyServerSide(ServerPlayer player"));
        assertTrue(claimIntent.contains("Action.ADD_CHUNK"));
        assertTrue(claimIntent.contains("Action.REMOVE_CHUNK"));
        assertTrue(claimIntent.contains("Action.DELETE"));

        assertTrue(generalActions.contains("TEXT_WAYPOINT_NEEDS_ROUTE"));
        assertTrue(generalActions.contains("TEXT_WAYPOINT_NEEDS_EXPLORED"));
        assertTrue(englishLang.contains("left-drag a waypoint to move it"));
        assertTrue(routeEditPopup.contains("gui.recruits.map.route.edit.title"));
        assertTrue(waypointPopup.contains("gui.recruits.map.waypoint.action"));
        assertTrue(generalActions.contains("WorldMapScreen::addWaypointAtClicked"));
        assertTrue(waypointController.contains("route.addWaypoint(new RecruitsRoute.Waypoint"));
        assertTrue(waypointController.contains("route.removeWaypoint(waypoint)"));
        assertTrue(waypointController.contains("screen.canPlaceWaypointAt(finalPos.getX(), finalPos.getZ())"));
        assertTrue(routeMutation.contains("ClientManager.saveRoute"));

        assertTrue(englishGuide.contains("Left-drag a waypoint to move it"));
        assertTrue(russianGuide.contains("Точку можно перетаскивать ЛКМ"));
        assertTrue(almanac.contains("Left-drag a waypoint to move it"));
        assertTrue(almanac.contains("Точку можно перетаскивать левой кнопкой"));
    }

    private static RecruitsRoute.Waypoint waypoint(String name, int x, int z) {
        return new RecruitsRoute.Waypoint(name, new BlockPos(x, 64, z), null);
    }

    private static String read(String relativePath) throws IOException {
        return Files.readString(ROOT.resolve(relativePath));
    }
}
