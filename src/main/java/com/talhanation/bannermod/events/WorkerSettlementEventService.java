package com.talhanation.bannermod.events;

import com.talhanation.bannermod.citizen.CitizenProfession;
import com.talhanation.bannermod.config.WorkersServerConfig;
import com.talhanation.bannermod.entity.civilian.AbstractWorkerEntity;
import com.talhanation.bannermod.persistence.military.RecruitsClaim;
import com.talhanation.bannermod.settlement.civilian.WorkerSettlementSpawnRules;
import com.talhanation.bannermod.settlement.civilian.WorkerSettlementSpawner;
import com.talhanation.bannermod.settlement.prefab.BuildingPlacementService;
import com.talhanation.bannermod.settlement.prefab.impl.BarracksPrefab;
import com.talhanation.bannermod.settlement.prefab.impl.StoragePrefab;
import com.talhanation.bannermod.shared.settlement.BannerModSettlementBinding;
import com.talhanation.bannermod.war.runtime.WarSiegeQueries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.entity.npc.Villager;
import com.talhanation.bannermod.entity.citizen.CitizenEntity;
import com.talhanation.bannermod.entity.citizen.CitizenIndex;
import com.talhanation.bannermod.registry.citizen.ModCitizenEntityTypes;
import com.talhanation.bannermod.war.WarRuntimeContext;
import com.talhanation.bannermod.war.registry.PoliticalEntityRecord;
import net.minecraft.world.scores.PlayerTeam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

final class WorkerSettlementEventService {
    private static final Map<UUID, Long> CLAIM_WORKER_GROWTH_SPAWN_TIMES = new HashMap<>();
    private static final Map<UUID, Boolean> CLAIM_SETTLEMENT_BOOTSTRAPPED = new HashMap<>();
    private static final int INITIAL_CITIZEN_COUNT = 4;

    private WorkerSettlementEventService() {
    }

    static void resetRuntimeState() {
        CLAIM_WORKER_GROWTH_SPAWN_TIMES.clear();
        CLAIM_SETTLEMENT_BOOTSTRAPPED.clear();
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
            if (claim == null || claim.getOwnerPoliticalEntityId() == null) {
                continue;
            }
            bootstrapClaimSettlement(level, claim);
            mobilizeCitizensIfClaimUnderSiege(level, claim);
            attemptClaimWorkerGrowth(level, claim, claim.getOwnerPoliticalEntityId().toString(), level.getGameTime());
        }
    }

    private static void mobilizeCitizensIfClaimUnderSiege(ServerLevel level, RecruitsClaim claim) {
        if (!WarSiegeQueries.isClaimUnderSiege(level, claim)) {
            return;
        }
        float chance = WorkersServerConfig.citizenMilitiaMobilizationChance();
        if (chance <= 0.0F) {
            return;
        }
        UUID politicalEntityId = claim.getOwnerPoliticalEntityId();
        if (politicalEntityId == null) {
            return;
        }
        PoliticalEntityRecord owner = WarRuntimeContext.registry(level).byId(politicalEntityId).orElse(null);
        if (owner == null) {
            return;
        }
        List<CitizenEntity> nearbyCitizens = CitizenIndex.instance().queryInClaim(level, claim).orElse(List.of());
        for (CitizenEntity citizen : nearbyCitizens) {
            if (!citizen.isAlive()) {
                continue;
            }
            if (citizen.activeProfession() != CitizenProfession.NONE) {
                continue;
            }
            UUID citizenOwner = citizen.getOwnerUUID();
            if (citizenOwner == null || !isPoliticalMember(owner, citizenOwner)) {
                continue;
            }
            if (citizen.getRandom().nextFloat() >= chance) {
                continue;
            }
            citizen.switchProfession(CitizenProfession.RECRUIT_SPEAR);
        }
    }

    private static boolean isPoliticalMember(PoliticalEntityRecord entity, UUID playerUuid) {
        if (entity == null || playerUuid == null) {
            return false;
        }
        if (playerUuid.equals(entity.leaderUuid())) {
            return true;
        }
        return entity.coLeaderUuids().contains(playerUuid);
    }

    private static void bootstrapClaimSettlement(ServerLevel level, RecruitsClaim claim) {
        UUID claimId = claim.getUUID();
        if (claimId == null || CLAIM_SETTLEMENT_BOOTSTRAPPED.containsKey(claimId)) {
            return;
        }

        BannerModSettlementBinding.Binding binding = WorkerSettlementClaimPolicy.resolveClaimGrowthBinding(claim, claim.getOwnerPoliticalEntityId().toString());
        if (!BannerModSettlementBinding.allowsSettlementOperation(binding)) {
            return;
        }

        int existingWorkers = WorkerSettlementClaimPolicy.countEntitiesInClaim(level, claim, AbstractWorkerEntity.class);
        if (existingWorkers > 0) {
            CLAIM_SETTLEMENT_BOOTSTRAPPED.put(claimId, true);
            return;
        }

        BlockPos anchorPos = resolveClaimAnchorPos(level, claim);
        placeInitialCentralBuilding(level, claim, anchorPos);
        spawnInitialWorkers(level, claim, anchorPos);
        CLAIM_SETTLEMENT_BOOTSTRAPPED.put(claimId, true);
    }

    private static void placeInitialCentralBuilding(ServerLevel level, RecruitsClaim claim, BlockPos anchorPos) {
        BuildingPlacementService.Result result = BuildingPlacementService.placeForClaim(
                level,
                claim,
                BarracksPrefab.ID,
                anchorPos,
                Direction.SOUTH
        );
        if (result == BuildingPlacementService.Result.UNKNOWN_PREFAB) {
            BuildingPlacementService.placeForClaim(level, claim, StoragePrefab.ID, anchorPos, Direction.SOUTH);
        }
    }

    private static void spawnInitialWorkers(ServerLevel level, RecruitsClaim claim, BlockPos anchorPos) {
        // Settlement bootstrap: one builder to construct the starter center.
        WorkerSettlementSpawnRules.Decision builderDecision = new WorkerSettlementSpawnRules.Decision(
                true,
                WorkerSettlementSpawnRules.WorkerProfession.BUILDER,
                null,
                0L
        );
        WorkerSettlementSpawner.spawnClaimWorker(level, anchorPos, builderDecision, claim);

        // Plus four generic citizens with no profession; they convert later by occupying jobs.
        for (int i = 0; i < INITIAL_CITIZEN_COUNT; i++) {
            BlockPos spawnPos = anchorPos.offset((i % 2) + 1, 0, (i / 2) + 1);
            spawnInitialCitizen(level, claim, spawnPos);
        }
    }

    private static void spawnInitialCitizen(ServerLevel level, RecruitsClaim claim, BlockPos spawnPos) {
        UUID politicalEntityId = claim.getOwnerPoliticalEntityId();
        if (politicalEntityId == null) {
            return;
        }
        PoliticalEntityRecord owner = WarRuntimeContext.registry(level).byId(politicalEntityId).orElse(null);
        if (owner == null) {
            return;
        }
        CitizenEntity citizen = ModCitizenEntityTypes.CITIZEN.get().create(level);
        if (citizen == null) {
            return;
        }
        citizen.moveTo(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D, 0.0F, 0.0F);
        citizen.setOwned(true);
        if (owner.leaderUuid() != null) {
            citizen.setOwnerUUID(java.util.Optional.of(owner.leaderUuid()));
        }
        level.addFreshEntity(citizen);
        PlayerTeam team = level.getScoreboard().getPlayerTeam(owner.name());
        if (team != null) {
            level.getScoreboard().addPlayerToTeam(citizen.getScoreboardName(), team);
        }
    }

    private static BlockPos resolveClaimAnchorPos(ServerLevel level, RecruitsClaim claim) {
        ChunkPos anchorChunk = claim.getCenter();
        if (anchorChunk == null && !claim.getClaimedChunks().isEmpty()) {
            anchorChunk = claim.getClaimedChunks().get(0);
        }
        if (anchorChunk == null) {
            return BlockPos.ZERO;
        }
        BlockPos chunkCenter = new BlockPos(anchorChunk.getMiddleBlockX(), level.getSeaLevel(), anchorChunk.getMiddleBlockZ());
        return level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, chunkCenter);
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
        if (level != null && settlementFactionId != null && !settlementFactionId.isBlank()) {
            settlementFactionId = WarRuntimeContext.registry(level)
                    .byName(settlementFactionId)
                    .map(PoliticalEntityRecord::id)
                    .map(UUID::toString)
                    .orElse(settlementFactionId);
        }
        return attemptClaimWorkerGrowth(level, claim, WorkerSettlementClaimPolicy.resolveClaimGrowthBinding(claim, settlementFactionId), gameTime, config);
    }
}
