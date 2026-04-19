package com.talhanation.bannermod.client.military.gui.worldmap;

import com.talhanation.bannermod.client.military.ClientManager;
import com.talhanation.bannermod.persistence.military.RecruitsRoute;

import javax.annotation.Nullable;
import java.util.function.Consumer;

final class WorldMapRouteMutationController {
    private final WorldMapScreen screen;

    @Nullable
    private RecruitsRoute selectedRoute;

    WorldMapRouteMutationController(WorldMapScreen screen) {
        this.screen = screen;
    }

    @Nullable
    RecruitsRoute getSelectedRoute() {
        return selectedRoute;
    }

    void setSelectedRoute(@Nullable RecruitsRoute route) {
        selectedRoute = route;
    }

    boolean hasSelectedRoute() {
        return selectedRoute != null;
    }

    void createRoute(String name) {
        RecruitsRoute newRoute = new RecruitsRoute(name);
        ClientManager.saveRoute(newRoute);
        selectedRoute = newRoute;
        screen.refreshRouteUI();
    }

    void renameRoute(RecruitsRoute route, String newName) {
        if (newName.equals(route.getName())) {
            return;
        }

        ClientManager.renameRoute(route, newName);
        screen.refreshRouteUI();
    }

    void deleteRoute(RecruitsRoute route) {
        ClientManager.deleteRoute(route);
        if (selectedRoute == route) {
            selectedRoute = null;
        }
        screen.refreshRouteUI();
    }

    boolean saveSelectedRoute() {
        if (selectedRoute == null) {
            return false;
        }

        ClientManager.saveRoute(selectedRoute);
        return true;
    }

    boolean mutateSelectedRoute(Consumer<RecruitsRoute> mutation) {
        if (selectedRoute == null) {
            return false;
        }

        mutation.accept(selectedRoute);
        ClientManager.saveRoute(selectedRoute);
        return true;
    }
}
