package com.talhanation.bannermod.events;

import com.talhanation.bannermod.config.WorkersServerConfig;
import com.talhanation.bannermod.entity.civilian.AbstractWorkerEntity;
import com.talhanation.bannermod.persistence.military.RecruitsClaim;
import com.talhanation.bannermod.settlement.civilian.WorkerSettlementSpawnRules;
import com.talhanation.bannermod.shared.settlement.BannerModSettlementBinding;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.Villager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

final class WorkerSettlementEventService {
    private static final Map<UUID, Long> CLAIM_WORKER_GROWTH_SPAWN_TIMES = new HashMap<>();

    private WorkerSettlementEventService() {
    }

    static void resetRuntimeState() {
        CLAIM_WORKER_GROWTH_SPAWN_TIMES.clear();
        WorkerSettlementSpawnRuntime.reset();
    }

    static void recordVillagerJoin(Villager villager) {
        WorkerSettlementSpawnRuntime.recordVillagerJoin(villager);
    }

    static void handleVillagerAdultTick(ServerLevel level, Villager villager) {
        WorkerSettlementSpawnRuntime.handleVillagerAdultTick(level, villager);
    }

    static AbstractWorkerEntity attemptBirthWorkerSpawn(ServerLevel level, Villager villager) {
        return WorkerSettlementSpawnRuntime.attemptBirthWorkerSpawn(level, villager);
    }

    static AbstractWorkerEntity attemptSettlementWorkerSpawn(ServerLevel level, Villager villager) {
        return WorkerSettlementSpawnRuntime.attemptSettlementWorkerSpawn(level, villager);
    }

    static void runClaimWorkerGrowthPass(ServerLevel level) {
        if (level == null || ClaimEvents.recruitsClaimManager == null) {
            return;
        }

        for (RecruitsClaim claim : ClaimEvents.recruitsClaimManager.getAllClaims()) {
            if (claim == null || claim.getOwnerFaction() == null) {
                continue;
            }
            attemptClaimWorkerGrowth(level, claim, claim.getOwnerFactionStringID(), level.getGameTime());
        }
    }

    static AbstractWorkerEntity attemptClaimWorkerGrowth(ServerLevel level,
                                                         RecruitsClaim claim,
                                                         BannerModSettlementBinding.Binding binding,
                                                         long gameTime,
                                                         WorkerSettlementSpawnRules.ClaimGrowthConfig config) {
        return WorkerSettlementClaimPolicy.attemptClaimWorkerGrowth(level, claim, binding, gameTime, config, CLAIM_WORKER_GROWTH_SPAWN_TIMES);
    }

    static AbstractWorkerEntity attemptClaimWorkerGrowth(ServerLevel level,
                                                         RecruitsClaim claim,
                                                         String settlementFactionId,
                                                         long gameTime) {
        return attemptClaimWorkerGrowth(level, claim, settlementFactionId, gameTime, WorkersServerConfig.claimWorkerGrowthConfig());
    }

    static AbstractWorkerEntity attemptClaimWorkerGrowth(ServerLevel level,
                                                         RecruitsClaim claim,
                                                         String settlementFactionId,
                                                         long gameTime,
                                                         WorkerSettlementSpawnRules.ClaimGrowthConfig config) {
        return attemptClaimWorkerGrowth(level, claim, WorkerSettlementClaimPolicy.resolveClaimGrowthBinding(claim, settlementFactionId), gameTime, config);
    }
}
