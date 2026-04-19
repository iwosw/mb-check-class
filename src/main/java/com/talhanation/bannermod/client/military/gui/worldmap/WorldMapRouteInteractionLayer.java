package com.talhanation.bannermod.client.military.gui.worldmap;

import com.talhanation.bannermod.client.military.PatrolRouteAssignmentController;
import com.talhanation.bannermod.persistence.military.RecruitsRoute;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.List;

final class WorldMapRouteInteractionLayer {
    private final WorldMapRouteMutationController routeController;
    private final PatrolRouteAssignmentController routeAssignments;
    private final WorldMapRouteSelectionUiController selectionUi;
    private final WorldMapWaypointInteractionController waypointController;

    WorldMapRouteInteractionLayer(WorldMapScreen screen, Player player) {
        this.routeController = new WorldMapRouteMutationController(screen);
        this.routeAssignments = new PatrolRouteAssignmentController();
        this.selectionUi = new WorldMapRouteSelectionUiController(screen, player, routeController);
        this.waypointController = new WorldMapWaypointInteractionController(screen, selectionUi, routeController);
    }

    void init() {
        routeAssignments.loadClientRoutes();
        refreshUI();
        selectionUi.init();
    }

    void refreshUI() {
        List<RecruitsRoute> options = routeAssignments.getRouteOptions();
        selectionUi.refresh(options);
    }

    @Nullable
    RecruitsRoute getSelectedRoute() {
        return selectionUi.getSelectedRoute();
    }

    void setSelectedRoute(@Nullable RecruitsRoute route) {
        selectionUi.setSelectedRoute(route);
    }

    boolean isClaimTransparencyEnabled() {
        return selectionUi.isClaimTransparencyEnabled();
    }

    boolean hasSelectedRoute() {
        return selectionUi.hasSelectedRoute();
    }

    void renderRouteOverlay(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        waypointController.renderRouteOverlay(guiGraphics, routeController.getSelectedRoute(), mouseX, mouseY);
    }

    void renderUi(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        selectionUi.renderToolbar(guiGraphics, mouseX, mouseY, partialTicks);
    }

    void renderPopups(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        selectionUi.renderPopups(guiGraphics, mouseX, mouseY);
    }

    void tick() {
        selectionUi.tick();
    }

    boolean isAnyPopupVisible() {
        return selectionUi.isAnyPopupVisible();
    }

    boolean isUiHovered(double mouseX, double mouseY) {
        return selectionUi.isUiHovered(mouseX, mouseY);
    }

    void mouseMoved(double mouseX, double mouseY) {
        selectionUi.mouseMoved(mouseX, mouseY);
    }

    boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (selectionUi.mouseClicked(mouseX, mouseY)) {
            return true;
        }

        if (waypointController.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        return false;
    }

    boolean mouseReleased(double mouseX, double mouseY, int button) {
        return waypointController.mouseReleased(button);
    }

    boolean mouseDragged(double mouseX, double mouseY) {
        return waypointController.mouseDragged(mouseX, mouseY);
    }

    boolean keyPressed(int keyCode) {
        return selectionUi.keyPressed(keyCode);
    }

    boolean charTyped(char chr, int modifiers) {
        return selectionUi.charTyped(chr, modifiers);
    }

    void addWaypointAtClicked() {
        waypointController.addWaypointAtClicked();
    }

    void openWaypointEditPopup(double mouseX, double mouseY) {
        waypointController.openWaypointEditPopup(mouseX, mouseY);
    }

    void removeWaypointAt(double mouseX, double mouseY) {
        waypointController.removeWaypointAt(mouseX, mouseY);
    }

    boolean isWaypointHoveredAt(double mouseX, double mouseY) {
        return waypointController.isWaypointHoveredAt(mouseX, mouseY);
    }

    void openAddRoutePopup() {
        selectionUi.openAddRoutePopup();
    }
}
