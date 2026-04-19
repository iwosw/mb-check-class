package com.talhanation.bannermod.client.military.gui.worldmap;

import com.talhanation.bannermod.persistence.military.RecruitsRoute;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;

final class WorldMapRoutePopupController {
    private final WorldMapScreen screen;
    private final Player player;
    private final WorldMapRouteMutationController routeController;

    private RouteNamePopup routeNamePopup;
    private RouteEditPopup routeEditPopup;
    private WaypointEditPopup waypointEditPopup;

    WorldMapRoutePopupController(WorldMapScreen screen, Player player, WorldMapRouteMutationController routeController) {
        this.screen = screen;
        this.player = player;
        this.routeController = routeController;
    }

    void init() {
        routeNamePopup = new RouteNamePopup(screen, routeController);
        routeEditPopup = new RouteEditPopup(screen, player, routeController);
        waypointEditPopup = new WaypointEditPopup(screen, routeController);
    }

    void render(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (routeNamePopup.isVisible()) {
            routeNamePopup.render(guiGraphics, mouseX, mouseY);
        }
        if (routeEditPopup.isVisible()) {
            routeEditPopup.render(guiGraphics, mouseX, mouseY);
        }
        if (waypointEditPopup.isVisible()) {
            waypointEditPopup.render(guiGraphics, mouseX, mouseY);
        }
    }

    void tick() {
        routeNamePopup.tick();
        routeEditPopup.tick();
        waypointEditPopup.tick();
    }

    boolean isAnyVisible() {
        return routeNamePopup.isVisible() || routeEditPopup.isVisible() || waypointEditPopup.isVisible();
    }

    boolean mouseClicked(double mouseX, double mouseY) {
        if (routeNamePopup.isVisible()) {
            return routeNamePopup.mouseClicked(mouseX, mouseY);
        }
        if (routeEditPopup.isVisible()) {
            return routeEditPopup.mouseClicked(mouseX, mouseY);
        }
        if (waypointEditPopup.isVisible()) {
            return waypointEditPopup.mouseClicked(mouseX, mouseY);
        }
        return false;
    }

    boolean keyPressed(int keyCode) {
        if (waypointEditPopup.isVisible()) {
            return waypointEditPopup.keyPressed(keyCode);
        }
        if (routeEditPopup.isVisible()) {
            return routeEditPopup.keyPressed(keyCode);
        }
        if (routeNamePopup.isVisible()) {
            return routeNamePopup.keyPressed(keyCode);
        }
        return false;
    }

    boolean charTyped(char chr, int modifiers) {
        if (waypointEditPopup.isVisible()) {
            return waypointEditPopup.charTyped(chr);
        }
        if (routeEditPopup.isVisible()) {
            return routeEditPopup.charTyped(chr, modifiers);
        }
        if (routeNamePopup.isVisible()) {
            return routeNamePopup.charTyped(chr, modifiers);
        }
        return false;
    }

    void openRouteNamePopup() {
        routeNamePopup.open();
    }

    void openRouteEditPopup(RecruitsRoute route) {
        routeEditPopup.open(route);
    }

    void openWaypointEditPopup(RecruitsRoute.Waypoint waypoint) {
        waypointEditPopup.open(waypoint);
    }
}
