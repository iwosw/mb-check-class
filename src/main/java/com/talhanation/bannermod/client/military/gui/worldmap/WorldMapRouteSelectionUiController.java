package com.talhanation.bannermod.client.military.gui.worldmap;

import com.talhanation.bannermod.persistence.military.RecruitsRoute;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.List;

final class WorldMapRouteSelectionUiController {
    private final WorldMapScreen screen;
    private final WorldMapRouteMutationController routeController;
    private final WorldMapRouteToolbar toolbar;
    private final WorldMapRoutePopupController popupController;
    private boolean claimTransparency;

    WorldMapRouteSelectionUiController(WorldMapScreen screen, Player player,
                                       WorldMapRouteMutationController routeController) {
        this.screen = screen;
        this.routeController = routeController;
        this.toolbar = new WorldMapRouteToolbar(screen);
        this.popupController = new WorldMapRoutePopupController(screen, player, routeController);
    }

    void init() {
        popupController.init();
    }

    void refresh(List<RecruitsRoute> options) {
        toolbar.refresh(options, routeController.getSelectedRoute(), this::setSelectedRoute);
    }

    @Nullable
    RecruitsRoute getSelectedRoute() {
        return routeController.getSelectedRoute();
    }

    void setSelectedRoute(@Nullable RecruitsRoute route) {
        applySelectedRoute(route);
    }

    boolean hasSelectedRoute() {
        return routeController.hasSelectedRoute();
    }

    boolean isClaimTransparencyEnabled() {
        return claimTransparency;
    }

    void renderToolbar(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        toolbar.render(guiGraphics, mouseX, mouseY, partialTicks, routeController.getSelectedRoute(), claimTransparency);
    }

    void renderPopups(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        popupController.render(guiGraphics, mouseX, mouseY);
    }

    void tick() {
        popupController.tick();
    }

    boolean isAnyPopupVisible() {
        return popupController.isAnyVisible();
    }

    boolean isUiHovered(double mouseX, double mouseY) {
        return isAnyPopupVisible() || toolbar.isUiHovered(mouseX, mouseY);
    }

    void mouseMoved(double mouseX, double mouseY) {
        toolbar.mouseMoved(mouseX, mouseY);
    }

    boolean mouseClicked(double mouseX, double mouseY) {
        if (popupController.mouseClicked(mouseX, mouseY)) {
            return true;
        }

        if (toolbar.isAddButtonHovered(mouseX, mouseY)) {
            openAddRoutePopup();
            return true;
        }

        RecruitsRoute selectedRoute = routeController.getSelectedRoute();
        if (selectedRoute != null && toolbar.isEditButtonHovered(mouseX, mouseY)) {
            screen.clearHoveredAndSelectedChunk();
            popupController.openRouteEditPopup(selectedRoute);
            return true;
        }

        if (selectedRoute != null && toolbar.isTransparencyButtonHovered(mouseX, mouseY)) {
            claimTransparency = !claimTransparency;
            return true;
        }

        if (toolbar.clickDropdown(mouseX, mouseY)) {
            screen.clearHoveredAndSelectedChunk();
            return true;
        }

        return false;
    }

    boolean keyPressed(int keyCode) {
        return popupController.keyPressed(keyCode);
    }

    boolean charTyped(char chr, int modifiers) {
        return popupController.charTyped(chr, modifiers);
    }

    void openAddRoutePopup() {
        screen.clearHoveredAndSelectedChunk();
        popupController.openRouteNamePopup();
        screen.closeContextMenu();
    }

    void openWaypointEditPopup(RecruitsRoute.Waypoint waypoint) {
        popupController.openWaypointEditPopup(waypoint);
    }

    private void applySelectedRoute(@Nullable RecruitsRoute route) {
        routeController.setSelectedRoute(route);
        if (route == null) {
            claimTransparency = false;
        }
    }
}
