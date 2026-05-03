package com.talhanation.bannermod.events;

import com.talhanation.bannermod.entity.civilian.AbstractWorkerEntity;
import com.talhanation.bannermod.entity.civilian.AnimalFarmerEntity;
import com.talhanation.bannermod.entity.civilian.BuilderEntity;
import com.talhanation.bannermod.entity.civilian.FarmerEntity;
import com.talhanation.bannermod.entity.civilian.FishermanEntity;
import com.talhanation.bannermod.entity.civilian.LumberjackEntity;
import com.talhanation.bannermod.entity.civilian.MerchantEntity;
import com.talhanation.bannermod.entity.civilian.MinerEntity;
import com.talhanation.bannermod.entity.civilian.WorkerIndex;
import com.talhanation.bannermod.entity.military.RecruitPoliticalContext;
import com.talhanation.bannermod.persistence.military.RecruitsClaim;
import com.talhanation.bannermod.settlement.BannerModSettlementBuildingCategory;
import com.talhanation.bannermod.settlement.BannerModSettlementBuildingRecord;
import com.talhanation.bannermod.settlement.BannerModSettlementManager;
import com.talhanation.bannermod.settlement.BannerModSettlementSnapshot;
import com.talhanation.bannermod.settlement.civilian.WorkerSettlementSpawnRules;
import com.talhanation.bannermod.settlement.civilian.WorkerSettlementSpawner;
import com.talhanation.bannermod.settlement.household.BannerModHomeAssignmentRuntime;
import com.talhanation.bannermod.settlement.household.BannerModHomeAssignmentSavedData;
import com.talhanation.bannermod.settlement.household.HomePreference;
import com.talhanation.bannermod.shared.settlement.BannerModSettlementBinding;
import com.talhanation.bannermod.util.RuntimeProfilingCounters;
import com.talhanation.bannermod.war.WarRuntimeContext;
import com.talhanation.bannermod.war.registry.PoliticalEntityRecord;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

final class WorkerSettlementClaimPolicy {
    private WorkerSettlementClaimPolicy() {
    }

    static AbstractWorkerEntity attemptClaimWorkerGrowth(ServerLevel level,
                                                         RecruitsClaim claim,
                                                         BannerModSettlementBinding.Binding binding,
                                                         long gameTime,
                                                         WorkerSettlementSpawnRules.ClaimGrowthConfig config,
                                                         Map<UUID, Long> claimWorkerGrowthSpawnTimes) {
        if (level == null || claim == null || binding == null) {
            return null;
        }

        int currentWorkerCount = countEntitiesInClaim(level, claim, AbstractWorkerEntity.class);
        Map<WorkerSettlementSpawnRules.WorkerProfession, Integer> currentByProfession =
                countWorkersByProfession(level, claim);
        long elapsedCooldownTicks = resolveClaimGrowthElapsedTicks(claim, gameTime, claimWorkerGrowthSpawnTimes);
        WorkerSettlementSpawnRules.Decision decision = WorkerSettlementSpawnRules.evaluateClaimWorkerGrowth(
                binding.status(),
                currentWorkerCount,
                elapsedCooldownTicks,
                config,
                currentByProfession,
                housingSlackForClaim(level, claim)
        );
        if (!decision.allowed()) {
            return null;
        }

        BlockPos spawnPos = resolveClaimGrowthSpawnPos(level, claim);
        AbstractWorkerEntity worker = WorkerSettlementSpawner.spawnClaimWorker(level, spawnPos, decision, claim);
        if (worker != null) {
            claimWorkerGrowthSpawnTimes.put(claim.getUUID(), gameTime);
            assignHomeIfAvailable(level, claim, worker.getUUID(), gameTime);
        }
        return worker;
    }

    static RecruitsClaim resolveClaim(BlockPos pos) {
        if (ClaimEvents.claimManager() == null) {
            return null;
        }
        return ClaimEvents.claimManager().getClaim(new ChunkPos(pos));
    }

    static BannerModSettlementBinding.Binding resolveSettlementBinding(Villager villager, RecruitsClaim claim) {
        String factionId = claim.getOwnerPoliticalEntityId() != null ? claim.getOwnerPoliticalEntityId().toString() : null;
        if (villager.getTeam() != null) {
            String teamName = villager.getTeam().getName();
            if (villager.level() instanceof ServerLevel serverLevel) {
                factionId = WarRuntimeContext.registry(serverLevel)
                        .byName(teamName)
                        .map(PoliticalEntityRecord::id)
                        .map(UUID::toString)
                        .orElse(teamName);
            } else {
                factionId = teamName;
            }
        }
        return BannerModSettlementBinding.resolveSettlementStatus(ClaimEvents.claimManager(), villager.blockPosition(), factionId);
    }

    static BannerModSettlementBinding.Binding resolveClaimGrowthBinding(RecruitsClaim claim, String settlementFactionId) {
        ChunkPos anchorChunk = resolveClaimAnchorChunk(claim);
        return BannerModSettlementBinding.resolveSettlementStatus(claim, anchorChunk, settlementFactionId);
    }

    static <T extends Entity> int countEntitiesInClaim(ServerLevel level, RecruitsClaim claim, Class<T> entityType) {
        if (entityType == AbstractWorkerEntity.class) {
            ClaimOwnerKey ownerKey = resolveClaimOwnerKey(level, claim);
            return WorkerIndex.instance()
                    .queryInClaim(level, claim)
                    .map(workers -> (int) workers.stream().filter(worker -> workerMatchesClaimOwner(level, worker, ownerKey)).count())
                    .orElseGet(() -> {
                        RuntimeProfilingCounters.increment("worker.index.fallback_scans");
                        return countEntitiesInClaimByScan(level, claim, entityType, ownerKey);
                    });
        }
        return countEntitiesInClaimByScan(level, claim, entityType, ClaimOwnerKey.EMPTY);
    }

    private static <T extends Entity> int countEntitiesInClaimByScan(ServerLevel level, RecruitsClaim claim, Class<T> entityType, ClaimOwnerKey ownerKey) {
        if (level == null || claim == null || entityType == null || claim.getClaimedChunks().isEmpty()) {
            return 0;
        }
        AABB claimBounds = getClaimBounds(level, claim);
        return level.getEntitiesOfClass(entityType, claimBounds, entity -> {
            if (!entity.isAlive() || !claim.containsChunk(entity.chunkPosition())) {
                return false;
            }
            if (!(entity instanceof AbstractWorkerEntity worker)) {
                return true;
            }

            return workerMatchesClaimOwner(level, worker, ownerKey);
        }).size();
    }

    private static ClaimOwnerKey resolveClaimOwnerKey(ServerLevel level, RecruitsClaim claim) {
        UUID politicalEntityId = claim.getOwnerPoliticalEntityId();
        if (politicalEntityId == null) return ClaimOwnerKey.EMPTY;
        PoliticalEntityRecord owner = WarRuntimeContext.registry(level).byId(politicalEntityId).orElse(null);
        if (owner == null) return new ClaimOwnerKey(null, java.util.Set.of(), politicalEntityId.toString());
        java.util.Set<UUID> ownerUuids = new java.util.HashSet<>();
        if (owner.leaderUuid() != null) ownerUuids.add(owner.leaderUuid());
        ownerUuids.addAll(owner.coLeaderUuids());
        return new ClaimOwnerKey(owner.leaderUuid(), ownerUuids, politicalEntityId.toString());
    }

    private static boolean workerMatchesClaimOwner(ServerLevel level, AbstractWorkerEntity worker, ClaimOwnerKey ownerKey) {
        UUID workerOwner = worker.getOwnerUUID();
        boolean ownerMatch = workerOwner != null && ownerKey.ownerUuids().contains(workerOwner);
        if (ownerMatch || ownerKey.factionId() == null) {
            return ownerMatch;
        }
        UUID workerPoliticalEntityId = RecruitPoliticalContext.politicalEntityIdOf(worker, WarRuntimeContext.registry(level));
        return workerPoliticalEntityId != null && ownerKey.factionId().equals(workerPoliticalEntityId.toString());
    }

    private record ClaimOwnerKey(@Nullable UUID leaderId, java.util.Set<UUID> ownerUuids, @Nullable String factionId) {
        private static final ClaimOwnerKey EMPTY = new ClaimOwnerKey(null, java.util.Set.of(), null);
    }

    private static long resolveClaimGrowthElapsedTicks(RecruitsClaim claim, long gameTime, Map<UUID, Long> claimWorkerGrowthSpawnTimes) {
        Long lastSpawnTime = claimWorkerGrowthSpawnTimes.get(claim.getUUID());
        if (lastSpawnTime == null) {
            return Long.MAX_VALUE;
        }
        return Math.max(0L, gameTime - lastSpawnTime);
    }

    static Map<WorkerSettlementSpawnRules.WorkerProfession, Integer> countWorkersByProfession(ServerLevel level, RecruitsClaim claim) {
        Map<WorkerSettlementSpawnRules.WorkerProfession, Integer> counts =
                new EnumMap<>(WorkerSettlementSpawnRules.WorkerProfession.class);
        if (level == null || claim == null || claim.getClaimedChunks().isEmpty()) {
            return counts;
        }
        ClaimOwnerKey ownerKey = resolveClaimOwnerKey(level, claim);
        AABB claimBounds = getClaimBounds(level, claim);
        for (AbstractWorkerEntity worker : level.getEntitiesOfClass(AbstractWorkerEntity.class, claimBounds, w -> w.isAlive() && claim.containsChunk(w.chunkPosition()) && workerMatchesClaimOwner(level, w, ownerKey))) {
            WorkerSettlementSpawnRules.WorkerProfession profession = professionOf(worker);
            if (profession != null) {
                counts.merge(profession, 1, Integer::sum);
            }
        }
        return counts;
    }

    /**
     * Free housing capacity for {@code claim}: sum of residentCapacity over the
     * claim's HOUSING/GENERAL buildings minus their current home assignments.
     * Returns 0 when no snapshot is available — the spawn rules treat that as
     * "no slack" and deny when {@code requireHousing} is on.
     */
    static int housingSlackForClaim(ServerLevel level, RecruitsClaim claim) {
        if (level == null || claim == null) {
            return 0;
        }
        BannerModSettlementSnapshot snapshot = BannerModSettlementManager.get(level).getSnapshot(claim.getUUID());
        if (snapshot == null) {
            return 0;
        }
        BannerModHomeAssignmentRuntime runtime = BannerModHomeAssignmentSavedData.get(level).runtime();
        int slack = 0;
        for (BannerModSettlementBuildingRecord building : snapshot.buildings()) {
            if (!isHousingCategory(building.buildingCategory())) {
                continue;
            }
            int capacity = building.residentCapacity();
            if (capacity <= 0) {
                continue;
            }
            int used = runtime.assignmentsForBuilding(building.buildingUuid()).size();
            slack += Math.max(0, capacity - used);
        }
        return slack;
    }

    /**
     * Bind a freshly spawned worker to a home in the same claim, if any free
     * slot exists. Silent no-op when housing is exhausted — the spawn rules
     * already gate on {@link #housingSlackForClaim} so this is a best-effort
     * assignment for the path where housing is not strictly required.
     */
    static void assignHomeIfAvailable(ServerLevel level, RecruitsClaim claim, UUID residentUuid, long gameTime) {
        if (level == null || claim == null || residentUuid == null) {
            return;
        }
        BannerModSettlementSnapshot snapshot = BannerModSettlementManager.get(level).getSnapshot(claim.getUUID());
        if (snapshot == null) {
            return;
        }
        BannerModHomeAssignmentRuntime runtime = BannerModHomeAssignmentSavedData.get(level).runtime();
        for (BannerModSettlementBuildingRecord building : snapshot.buildings()) {
            if (!isHousingCategory(building.buildingCategory())) {
                continue;
            }
            int capacity = building.residentCapacity();
            if (capacity <= 0) {
                continue;
            }
            int used = runtime.assignmentsForBuilding(building.buildingUuid()).size();
            if (used >= capacity) {
                continue;
            }
            HomePreference preference = used == 0 ? HomePreference.ASSIGNED : HomePreference.SHARED;
            runtime.assign(residentUuid, building.buildingUuid(), preference, gameTime);
            return;
        }
    }

    private static boolean isHousingCategory(BannerModSettlementBuildingCategory category) {
        return category == BannerModSettlementBuildingCategory.GENERAL;
    }

    @Nullable
    private static WorkerSettlementSpawnRules.WorkerProfession professionOf(AbstractWorkerEntity worker) {
        if (worker instanceof FarmerEntity) return WorkerSettlementSpawnRules.WorkerProfession.FARMER;
        if (worker instanceof LumberjackEntity) return WorkerSettlementSpawnRules.WorkerProfession.LUMBERJACK;
        if (worker instanceof MinerEntity) return WorkerSettlementSpawnRules.WorkerProfession.MINER;
        if (worker instanceof BuilderEntity) return WorkerSettlementSpawnRules.WorkerProfession.BUILDER;
        if (worker instanceof MerchantEntity) return WorkerSettlementSpawnRules.WorkerProfession.MERCHANT;
        if (worker instanceof FishermanEntity) return WorkerSettlementSpawnRules.WorkerProfession.FISHERMAN;
        if (worker instanceof AnimalFarmerEntity) return WorkerSettlementSpawnRules.WorkerProfession.ANIMAL_FARMER;
        return null;
    }

    private static ChunkPos resolveClaimAnchorChunk(RecruitsClaim claim) {
        if (claim.getCenter() != null) {
            return claim.getCenter();
        }
        if (!claim.getClaimedChunks().isEmpty()) {
            return claim.getClaimedChunks().get(0);
        }
        return new ChunkPos(0, 0);
    }

    private static BlockPos resolveClaimGrowthSpawnPos(ServerLevel level, RecruitsClaim claim) {
        ChunkPos anchorChunk = resolveClaimAnchorChunk(claim);
        BlockPos chunkCenter = new BlockPos(anchorChunk.getMiddleBlockX(), level.getSeaLevel(), anchorChunk.getMiddleBlockZ());
        return level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, chunkCenter);
    }

    private static AABB getClaimBounds(ServerLevel level, RecruitsClaim claim) {
        ChunkPos anchorChunk = resolveClaimAnchorChunk(claim);
        int minChunkX = claim.getClaimedChunks().stream().mapToInt(chunkPos -> chunkPos.x).min().orElse(anchorChunk.x);
        int maxChunkX = claim.getClaimedChunks().stream().mapToInt(chunkPos -> chunkPos.x).max().orElse(anchorChunk.x);
        int minChunkZ = claim.getClaimedChunks().stream().mapToInt(chunkPos -> chunkPos.z).min().orElse(anchorChunk.z);
        int maxChunkZ = claim.getClaimedChunks().stream().mapToInt(chunkPos -> chunkPos.z).max().orElse(anchorChunk.z);
        return new AABB(
                minChunkX * 16.0D,
                level.getMinBuildHeight(),
                minChunkZ * 16.0D,
                (maxChunkX + 1) * 16.0D,
                level.getMaxBuildHeight(),
                (maxChunkZ + 1) * 16.0D
        );
    }
}
