package com.talhanation.bannermod.events;

import com.talhanation.bannermod.config.WorkersServerConfig;
import com.talhanation.bannermod.entity.civilian.AbstractWorkerEntity;
import com.talhanation.bannermod.persistence.military.RecruitsClaim;
import com.talhanation.bannermod.settlement.civilian.WorkerSettlementSpawnRules;
import com.talhanation.bannermod.settlement.civilian.WorkerSettlementSpawner;
import com.talhanation.bannermod.shared.settlement.BannerModSettlementBinding;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.Villager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

final class WorkerSettlementEventService {
    private static final Map<UUID, Long> SETTLEMENT_SPAWN_COOLDOWNS = new HashMap<>();
    private static final Map<UUID, Long> CLAIM_WORKER_GROWTH_SPAWN_TIMES = new HashMap<>();
    private static final Set<UUID> KNOWN_ADULT_VILLAGERS = new HashSet<>();

    private WorkerSettlementEventService() {
    }

    static void resetRuntimeState() {
        SETTLEMENT_SPAWN_COOLDOWNS.clear();
        CLAIM_WORKER_GROWTH_SPAWN_TIMES.clear();
        KNOWN_ADULT_VILLAGERS.clear();
    }

    static void recordVillagerJoin(Villager villager) {
        if (!villager.isBaby()) {
            KNOWN_ADULT_VILLAGERS.add(villager.getUUID());
        }
    }

    static void handleVillagerAdultTick(ServerLevel level, Villager villager) {
        if (KNOWN_ADULT_VILLAGERS.add(villager.getUUID())) {
            attemptBirthWorkerSpawn(level, villager);
        }
    }

    static AbstractWorkerEntity attemptBirthWorkerSpawn(ServerLevel level, Villager villager) {
        RecruitsClaim claim = WorkerSettlementClaimPolicy.resolveClaim(villager.blockPosition());
        if (claim == null) {
            return null;
        }

        int villagerCount = Math.max(1, WorkerSettlementClaimPolicy.countEntitiesInClaim(level, claim, Villager.class));
        WorkerSettlementSpawnRules.Decision decision = WorkerSettlementSpawnRules.evaluateBirth(
                WorkerSettlementClaimPolicy.resolveSettlementBinding(villager, claim),
                villagerCount,
                WorkerSettlementClaimPolicy.countEntitiesInClaim(level, claim, AbstractWorkerEntity.class),
                false,
                WorkersServerConfig.workerBirthRuleConfig()
        );
        return WorkerSettlementSpawner.spawnWorkerFromVillager(level, villager, decision, claim);
    }

    static AbstractWorkerEntity attemptSettlementWorkerSpawn(ServerLevel level, Villager villager) {
        RecruitsClaim claim = WorkerSettlementClaimPolicy.resolveClaim(villager.blockPosition());
        if (claim == null) {
            return null;
        }

        long now = level.getGameTime();
        WorkerSettlementSpawnRules.Decision decision = WorkerSettlementSpawnRules.evaluateSettlementSpawn(
                WorkerSettlementClaimPolicy.resolveSettlementBinding(villager, claim),
                WorkerSettlementClaimPolicy.countEntitiesInClaim(level, claim, Villager.class),
                WorkerSettlementClaimPolicy.countEntitiesInClaim(level, claim, AbstractWorkerEntity.class),
                isSettlementSpawnOnCooldown(claim, now),
                WorkersServerConfig.workerSettlementSpawnRuleConfig()
        );

        AbstractWorkerEntity worker = WorkerSettlementSpawner.spawnWorkerFromVillager(level, villager, decision, claim);
        if (worker != null) {
            long cooldownTicks = WorkersServerConfig.settlementSpawnCooldownTicks();
            if (cooldownTicks > 0L) {
                SETTLEMENT_SPAWN_COOLDOWNS.put(claim.getUUID(), now + cooldownTicks);
            }
        }
        return worker;
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

    private static boolean isSettlementSpawnOnCooldown(RecruitsClaim claim, long gameTime) {
        Long cooldownUntil = SETTLEMENT_SPAWN_COOLDOWNS.get(claim.getUUID());
        return cooldownUntil != null && cooldownUntil > gameTime;
    }
}
