package com.talhanation.bannermod.events;

import com.talhanation.bannermod.ai.pathfinding.AsyncPathProcessor;
import com.talhanation.bannermod.ai.pathfinding.async.TrueAsyncPathfindingRuntime;
import com.talhanation.bannermod.events.runtime.RecruitWorldLifecycleService;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

public class RecruitLifecycleEvents {
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        RecruitEvents.server = event.getServer();
        RecruitWorldLifecycleService.RecruitManagers managers = RecruitWorldLifecycleService.initializeManagers(RecruitEvents.server);
        RecruitEvents.recruitsPlayerUnitManager = managers.playerUnitManager();
        RecruitEvents.recruitsGroupsManager = managers.groupsManager();
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        // ServerStartedEvent runs after levels exist, so the async path runtime can start safely.
        AsyncPathProcessor.start();
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        RecruitWorldLifecycleService.saveManagers(
                RecruitEvents.server,
                RecruitEvents.recruitsPlayerUnitManager,
                RecruitEvents.recruitsGroupsManager
        );

        AsyncPathProcessor.shutdown();
        TrueAsyncPathfindingRuntime.instance().shutdown();
    }

    @SubscribeEvent
    public void onWorldSave(LevelEvent.Save event) {
        RecruitWorldLifecycleService.saveManagers(
                RecruitEvents.server,
                RecruitEvents.recruitsPlayerUnitManager,
                RecruitEvents.recruitsGroupsManager
        );
    }

    @SubscribeEvent
    public void onPlayerJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) {
            return;
        }

        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            RecruitWorldLifecycleService.syncPlayerJoin(
                    serverPlayer,
                    RecruitEvents.recruitsPlayerUnitManager,
                    RecruitEvents.recruitsGroupsManager
            );
            RecruitGovernorWorkflow.syncGovernorSnapshotsOnLogin(serverPlayer);
        }
    }

    @SubscribeEvent
    public void onTeleportEvent(EntityTeleportEvent event) {
        RecruitWorldLifecycleService.teleportFollowingRecruits(event);
    }

    @SubscribeEvent
    public void onServerTick(LevelTickEvent.Post event) {
        RecruitWorldLifecycleService.tickLevel(event, RecruitEvents.RECRUIT_PATROL, RecruitEvents.PILLAGER_PATROL);
    }

    @SubscribeEvent
    public void onHorseJoinWorld(EntityJoinLevelEvent event) {
        RecruitWorldLifecycleService.ensureHorseGoal(event.getEntity());
    }
}
