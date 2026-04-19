package com.talhanation.bannermod.client.military.gui.worldmap;

import com.talhanation.bannermod.persistence.military.RecruitsRoute;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;

import javax.annotation.Nullable;

final class WorldMapWaypointInteractionController {
    private final WorldMapScreen screen;
    private final WorldMapRouteSelectionUiController selectionUi;
    private final WorldMapRouteMutationController routeController;

    @Nullable
    private RecruitsRoute.Waypoint draggingWaypoint;
    private BlockPos dragOriginalPos;
    private boolean draggingWaypointActive;

    WorldMapWaypointInteractionController(WorldMapScreen screen,
                                          WorldMapRouteSelectionUiController selectionUi,
                                          WorldMapRouteMutationController routeController) {
        this.screen = screen;
        this.selectionUi = selectionUi;
        this.routeController = routeController;
    }

    void renderRouteOverlay(GuiGraphics guiGraphics, @Nullable RecruitsRoute selectedRoute, int mouseX, int mouseY) {
        if (selectedRoute == null) {
            return;
        }

        RouteRenderer.renderRoute(guiGraphics, selectedRoute, screen.offsetX, screen.offsetZ, WorldMapScreen.scale,
                draggingWaypoint, -1);
        if (draggingWaypointActive && draggingWaypoint != null) {
            RouteRenderer.renderDragGhost(guiGraphics, draggingWaypoint, mouseX, mouseY);
        }
    }

    boolean mouseClicked(double mouseX, double mouseY, int button) {
        RecruitsRoute selectedRoute = routeController.getSelectedRoute();
        if (button != 0 || selectedRoute == null) {
            return false;
        }

        RecruitsRoute.Waypoint waypoint = getWaypointAt(selectedRoute, mouseX, mouseY);
        if (waypoint == null) {
            return false;
        }

        draggingWaypoint = waypoint;
        dragOriginalPos = waypoint.getPosition();
        draggingWaypointActive = true;
        screen.clearHoveredAndSelectedChunk();
        return true;
    }

    boolean mouseReleased(int button) {
        if (button != 0 || !draggingWaypointActive || draggingWaypoint == null) {
            return false;
        }

        BlockPos finalPos = draggingWaypoint.getPosition();
        if (routeController.getSelectedRoute() != null && screen.canPlaceWaypointAt(finalPos.getX(), finalPos.getZ())) {
            routeController.saveSelectedRoute();
        } else if (dragOriginalPos != null) {
            draggingWaypoint.setPosition(dragOriginalPos);
        }

        draggingWaypoint = null;
        dragOriginalPos = null;
        draggingWaypointActive = false;
        return true;
    }

    boolean mouseDragged(double mouseX, double mouseY) {
        if (selectionUi.isAnyPopupVisible()) {
            return true;
        }
        if (!draggingWaypointActive || draggingWaypoint == null) {
            return false;
        }

        screen.clearHoveredAndSelectedChunk();
        int newWorldX = (int) Math.floor((mouseX - screen.offsetX) / WorldMapScreen.scale);
        int newWorldZ = (int) Math.floor((mouseY - screen.offsetZ) / WorldMapScreen.scale);
        draggingWaypoint.setPosition(new BlockPos(newWorldX, screen.resolveSurfaceY(newWorldX, newWorldZ), newWorldZ));
        return true;
    }

    void addWaypointAtClicked() {
        RecruitsRoute selectedRoute = routeController.getSelectedRoute();
        if (selectedRoute == null) {
            return;
        }

        BlockPos pos = screen.getClickedBlockPos();
        ChunkPos chunk = new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4);
        if (screen.getMinecraftInstance().level == null
                || screen.getMinecraftInstance().level.getChunkSource().getChunk(chunk.x, chunk.z, false) == null) {
            return;
        }

        String name = "WP " + (selectedRoute.getWaypoints().size() + 1);
        routeController.mutateSelectedRoute(route -> route.addWaypoint(new RecruitsRoute.Waypoint(name, pos, null)));
    }

    void openWaypointEditPopup(double mouseX, double mouseY) {
        RecruitsRoute selectedRoute = routeController.getSelectedRoute();
        RecruitsRoute.Waypoint waypoint = getWaypointAt(selectedRoute, mouseX, mouseY);
        if (waypoint == null) {
            return;
        }

        selectionUi.openWaypointEditPopup(waypoint);
        screen.closeContextMenu();
    }

    void removeWaypointAt(double mouseX, double mouseY) {
        RecruitsRoute selectedRoute = routeController.getSelectedRoute();
        if (selectedRoute == null) {
            return;
        }

        RecruitsRoute.Waypoint waypoint = getWaypointAt(selectedRoute, mouseX, mouseY);
        if (waypoint == null) {
            return;
        }

        routeController.mutateSelectedRoute(route -> route.removeWaypoint(waypoint));
    }

    boolean isWaypointHoveredAt(double mouseX, double mouseY) {
        return getWaypointAt(routeController.getSelectedRoute(), mouseX, mouseY) != null;
    }

    @Nullable
    private RecruitsRoute.Waypoint getWaypointAt(@Nullable RecruitsRoute selectedRoute, double mouseX, double mouseY) {
        if (selectedRoute == null) {
            return null;
        }

        return RouteRenderer.getWaypointAt(selectedRoute, mouseX, mouseY,
                screen.offsetX, screen.offsetZ, WorldMapScreen.scale);
    }
}
