package com.talhanation.bannermod.client.military.gui.worldmap;

import com.talhanation.bannermod.client.military.ClientManager;
import com.talhanation.bannermod.client.military.gui.diplomacy.DiplomacyEditScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

final class WorldMapGeneralMenuActions {
    private static final Component TEXT_DIPLOMACY = Component.translatable("gui.recruits.map.diplomacy");
    private static final Component TEXT_CENTER_MAP = Component.translatable("gui.recruits.map.center_map");

    private final WorldMapScreen screen;

    WorldMapGeneralMenuActions(WorldMapScreen screen) {
        this.screen = screen;
    }

    void addEntries(WorldMapContextMenu menu) {
        menu.addEntry(TEXT_DIPLOMACY.getString(),
                () -> ClientManager.ownFaction != null
                        && screen.selectedClaim != null
                        && screen.selectedClaim.getOwnerFaction() != null
                        && !ClientManager.ownFaction.getStringID().equals(screen.selectedClaim.getOwnerFactionStringID()),
                this::openDiplomacyEditor
        );

        menu.addEntry("Add Waypoint",
                () -> screen.getSelectedRoute() != null
                        && screen.canPlaceWaypointAt(screen.snapshotWorldX, screen.snapshotWorldZ),
                WorldMapScreen::addWaypointAtClicked
        );

        menu.addEntry("Remove Waypoint",
                () -> screen.getSelectedRoute() != null
                        && screen.isWaypointHoveredAt(menu.getSnapshotMouseX(), menu.getSnapshotMouseY()),
                (worldMapScreen) -> worldMapScreen.removeWaypointAt(menu.getSnapshotMouseX(), menu.getSnapshotMouseY())
        );

        menu.addEntry("Edit Waypoint",
                () -> screen.getSelectedRoute() != null
                        && screen.isWaypointHoveredAt(menu.getSnapshotMouseX(), menu.getSnapshotMouseY()),
                (worldMapScreen) -> worldMapScreen.openWaypointEditPopup(menu.getSnapshotMouseX(), menu.getSnapshotMouseY())
        );

        menu.addEntry(TEXT_CENTER_MAP.getString(), WorldMapScreen::centerOnPlayer);
    }

    private void openDiplomacyEditor(WorldMapScreen screen) {
        Minecraft.getInstance().setScreen(new DiplomacyEditScreen(screen, screen.selectedClaim.getOwnerFaction()));
    }
}
