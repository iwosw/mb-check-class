package com.talhanation.bannermod.events;

import com.talhanation.bannermod.persistence.military.RecruitsClaimManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

final class ClaimRuntimeService {
    void onServerStarting(ServerStartingEvent event) {
        ClaimEvents.server = event.getServer();
        ServerLevel level = ClaimEvents.server.overworld();

        ClaimEvents.recruitsClaimManager = new RecruitsClaimManager();
        ClaimEvents.recruitsClaimManager.load(level);
    }

    void onServerStopping(ServerStoppingEvent event) {
        ClaimEvents.recruitsClaimManager.save(ClaimEvents.server.overworld());
    }

    void onWorldSave(LevelEvent.Save event) {
        ClaimEvents.recruitsClaimManager.save(ClaimEvents.server.overworld());
    }

    void onPlayerJoin(EntityJoinLevelEvent event) {
        if(event.getLevel().isClientSide()) return;

        if(event.getEntity() instanceof ServerPlayer player){
            ClaimEvents.recruitsClaimManager.sendClaimsToPlayer(player);
        }
    }
}
