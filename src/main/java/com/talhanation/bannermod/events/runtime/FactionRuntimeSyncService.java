package com.talhanation.bannermod.events.runtime;

import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import com.talhanation.bannermod.entity.military.RecruitIndex;
import com.talhanation.bannermod.events.ClaimEvents;
import com.talhanation.bannermod.events.FactionEvents;
import com.talhanation.bannermod.util.RuntimeProfilingCounters;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import java.util.List;

final class FactionRuntimeSyncService {
    private FactionRuntimeSyncService() {
    }

    static void save(ServerLevel level) {
        FactionEvents.recruitsFactionManager.save(level);
    }

    static void saveOverworld(MinecraftServer server) {
        FactionEvents.recruitsFactionManager.save(server.overworld());
    }

    static void broadcastFactionPlayers(ServerLevel level, String teamName) {
        FactionEvents.recruitsFactionManager.broadcastToFactionPlayers(teamName, level);
    }

    static void broadcastClaims(ServerLevel level) {
        ClaimEvents.recruitsClaimManager.broadcastClaimsToAll(level);
    }

    static void markRecruitsNeedingTeamUpdate(ServerLevel level) {
        List<AbstractRecruitEntity> recruitList = RecruitIndex.instance().all(level, false);
        if (recruitList == null) {
            RuntimeProfilingCounters.increment("recruit.index.unavailable");
            save(level);
            return;
        }
        for (AbstractRecruitEntity recruit : recruitList) {
            recruit.needsTeamUpdate = true;
        }
        save(level);
    }
}
