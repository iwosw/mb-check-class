package com.talhanation.bannermod.client.military.gui.worldmap;

import net.minecraft.network.chat.Component;

final class WorldMapGeneralMenuActions {
    private static final Component TEXT_CENTER_MAP = Component.translatable("gui.recruits.map.center_map");
    private static final Component TEXT_ADD_WAYPOINT = Component.translatable("gui.recruits.map.route.add_waypoint");
    private static final Component TEXT_REMOVE_WAYPOINT = Component.translatable("gui.recruits.map.route.remove_waypoint");
    private static final Component TEXT_EDIT_WAYPOINT = Component.translatable("gui.recruits.map.route.edit_waypoint");
    private static final Component TEXT_WAYPOINT_NEEDS_ROUTE = Component.translatable("gui.recruits.map.route.disabled.no_route");
    private static final Component TEXT_WAYPOINT_NEEDS_EXPLORED = Component.translatable("gui.recruits.map.route.disabled.unexplored");

    private final WorldMapScreen screen;

    WorldMapGeneralMenuActions(WorldMapScreen screen) {
        this.screen = screen;
    }

    void addEntries(WorldMapContextMenu menu) {
        if (screen.getSelectedRoute() == null) {
            menu.addDisabledEntry(TEXT_ADD_WAYPOINT, TEXT_WAYPOINT_NEEDS_ROUTE, "route_waypoint_disabled");
        } else if (!screen.canPlaceWaypointAt(screen.snapshotWorldX, screen.snapshotWorldZ)) {
            menu.addDisabledEntry(TEXT_ADD_WAYPOINT, TEXT_WAYPOINT_NEEDS_EXPLORED, "route_waypoint_disabled");
        } else {
            menu.addEntry(TEXT_ADD_WAYPOINT, () -> true, WorldMapScreen::addWaypointAtClicked, "route_waypoint_add");
        }

        menu.addEntry(TEXT_REMOVE_WAYPOINT,
                () -> screen.getSelectedRoute() != null
                        && screen.isWaypointHoveredAt(menu.getSnapshotMouseX(), menu.getSnapshotMouseY()),
                (worldMapScreen) -> worldMapScreen.removeWaypointAt(menu.getSnapshotMouseX(), menu.getSnapshotMouseY()),
                "route_waypoint_remove"
        );

        menu.addEntry(TEXT_EDIT_WAYPOINT,
                () -> screen.getSelectedRoute() != null
                        && screen.isWaypointHoveredAt(menu.getSnapshotMouseX(), menu.getSnapshotMouseY()),
                (worldMapScreen) -> worldMapScreen.openWaypointEditPopup(menu.getSnapshotMouseX(), menu.getSnapshotMouseY()),
                "route_waypoint_edit"
        );

        menu.addEntry(TEXT_CENTER_MAP.getString(), WorldMapScreen::centerOnPlayer);
    }
}
