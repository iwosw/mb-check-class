package com.talhanation.bannermod.client.military;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.entity.military.AbstractLeaderEntity;
import com.talhanation.bannermod.network.messages.military.MessagePatrolLeaderSetRoute;
import com.talhanation.bannermod.persistence.military.RecruitsRoute;
import net.minecraft.core.BlockPos;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class PatrolRouteAssignmentController {

    public void loadClientRoutes() {
        ClientManager.loadRoutes();
    }

    public List<RecruitsRoute> getRouteOptions() {
        List<RecruitsRoute> routeOptions = new ArrayList<>();
        routeOptions.add(null);
        routeOptions.addAll(ClientManager.getRoutesList());
        return routeOptions;
    }

    @Nullable
    public RecruitsRoute getAssignedRoute(AbstractLeaderEntity leader) {
        UUID routeId = leader.getRouteID();
        return routeId == null ? null : ClientManager.routesMap.get(routeId.toString());
    }

    public void sendRouteAssignment(UUID leaderId, @Nullable RecruitsRoute route) {
        BannerModMain.SIMPLE_CHANNEL.sendToServer(buildRouteAssignment(leaderId, route));
    }

    public MessagePatrolLeaderSetRoute buildRouteAssignment(UUID leaderId, @Nullable RecruitsRoute route) {
        RoutePayload payload = RoutePayload.fromRoute(route);
        return new MessagePatrolLeaderSetRoute(leaderId, payload.routeId(), payload.waypoints(), payload.waitSeconds());
    }

    private record RoutePayload(@Nullable UUID routeId, List<BlockPos> waypoints, List<Integer> waitSeconds) {
        static RoutePayload fromRoute(@Nullable RecruitsRoute route) {
            if (route == null) {
                return new RoutePayload(null, List.of(), List.of());
            }

            List<BlockPos> positions = new ArrayList<>();
            List<Integer> waits = new ArrayList<>();
            for (RecruitsRoute.Waypoint waypoint : route.getWaypoints()) {
                positions.add(waypoint.getPosition());
                waits.add(getWaitSeconds(waypoint));
            }

            return new RoutePayload(route.getId(), positions, waits);
        }

        private static int getWaitSeconds(RecruitsRoute.Waypoint waypoint) {
            RecruitsRoute.WaypointAction action = waypoint.getAction();
            if (action == null || action.getType() != RecruitsRoute.WaypointAction.Type.WAIT) {
                return 0;
            }
            return action.getWaitSeconds();
        }
    }
}
