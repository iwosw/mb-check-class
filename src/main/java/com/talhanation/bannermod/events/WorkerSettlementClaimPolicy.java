package com.talhanation.bannermod.events;

import com.talhanation.bannermod.entity.civilian.AbstractWorkerEntity;
import com.talhanation.bannermod.persistence.military.RecruitsClaim;
import com.talhanation.bannermod.settlement.civilian.WorkerSettlementSpawnRules;
import com.talhanation.bannermod.settlement.civilian.WorkerSettlementSpawner;
import com.talhanation.bannermod.shared.settlement.BannerModSettlementBinding;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;

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
        long elapsedCooldownTicks = resolveClaimGrowthElapsedTicks(claim, gameTime, claimWorkerGrowthSpawnTimes);
        WorkerSettlementSpawnRules.Decision decision = WorkerSettlementSpawnRules.evaluateClaimWorkerGrowth(
                binding.status(),
                currentWorkerCount,
                elapsedCooldownTicks,
                config
        );
        WorkerSettlementSpawnRules.Decision deterministicDecision = applyClaimGrowthProfessionSeed(claim, currentWorkerCount, config, decision);
        if (!deterministicDecision.allowed()) {
            return null;
        }

        BlockPos spawnPos = resolveClaimGrowthSpawnPos(level, claim);
        AbstractWorkerEntity worker = WorkerSettlementSpawner.spawnClaimWorker(level, spawnPos, deterministicDecision, claim);
        if (worker != null) {
            claimWorkerGrowthSpawnTimes.put(claim.getUUID(), gameTime);
        }
        return worker;
    }

    static RecruitsClaim resolveClaim(BlockPos pos) {
        if (ClaimEvents.recruitsClaimManager == null) {
            return null;
        }
        return ClaimEvents.recruitsClaimManager.getClaim(new ChunkPos(pos));
    }

    static BannerModSettlementBinding.Binding resolveSettlementBinding(Villager villager, RecruitsClaim claim) {
        String factionId = claim.getOwnerFaction() != null ? claim.getOwnerFactionStringID() : null;
        if (villager.getTeam() != null) {
            factionId = villager.getTeam().getName();
        }
        return BannerModSettlementBinding.resolveSettlementStatus(ClaimEvents.recruitsClaimManager, villager.blockPosition(), factionId);
    }

    static BannerModSettlementBinding.Binding resolveClaimGrowthBinding(RecruitsClaim claim, String settlementFactionId) {
        ChunkPos anchorChunk = resolveClaimAnchorChunk(claim);
        return BannerModSettlementBinding.resolveSettlementStatus(claim, anchorChunk, settlementFactionId);
    }

    static <T extends Entity> int countEntitiesInClaim(ServerLevel level, RecruitsClaim claim, Class<T> entityType) {
        AABB claimBounds = getClaimBounds(level, claim);
        UUID leaderId = claim.getOwnerFaction() == null ? null : claim.getOwnerFaction().getTeamLeaderUUID();
        String factionId = claim.getOwnerFaction() == null ? null : claim.getOwnerFactionStringID();
        return level.getEntitiesOfClass(entityType, claimBounds, entity -> {
            if (!entity.isAlive() || !claim.containsChunk(entity.chunkPosition())) {
                return false;
            }
            if (!(entity instanceof AbstractWorkerEntity worker)) {
                return true;
            }

            boolean ownerMatch = leaderId != null && leaderId.equals(worker.getOwnerUUID());
            boolean teamMatch = factionId != null && worker.getTeam() != null && factionId.equals(worker.getTeam().getName());
            return ownerMatch || teamMatch;
        }).size();
    }

    private static long resolveClaimGrowthElapsedTicks(RecruitsClaim claim, long gameTime, Map<UUID, Long> claimWorkerGrowthSpawnTimes) {
        Long lastSpawnTime = claimWorkerGrowthSpawnTimes.get(claim.getUUID());
        if (lastSpawnTime == null) {
            return Long.MAX_VALUE;
        }
        return Math.max(0L, gameTime - lastSpawnTime);
    }

    private static WorkerSettlementSpawnRules.Decision applyClaimGrowthProfessionSeed(RecruitsClaim claim,
                                                                                       int currentWorkerCount,
                                                                                       WorkerSettlementSpawnRules.ClaimGrowthConfig config,
                                                                                       WorkerSettlementSpawnRules.Decision decision) {
        if (claim == null || config == null || decision == null || !decision.allowed() || config.allowedProfessions().isEmpty()) {
            return decision;
        }

        ChunkPos anchorChunk = resolveClaimAnchorChunk(claim);
        int professionIndex = Math.floorMod(anchorChunk.x * 31 + anchorChunk.z * 17 + currentWorkerCount, config.allowedProfessions().size());
        WorkerSettlementSpawnRules.WorkerProfession profession = config.allowedProfessions().get(professionIndex);
        return new WorkerSettlementSpawnRules.Decision(true, profession, null, decision.requiredCooldownTicks());
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
        int minChunkX = claim.getClaimedChunks().stream().mapToInt(chunkPos -> chunkPos.x).min().orElse(claim.getCenter().x);
        int maxChunkX = claim.getClaimedChunks().stream().mapToInt(chunkPos -> chunkPos.x).max().orElse(claim.getCenter().x);
        int minChunkZ = claim.getClaimedChunks().stream().mapToInt(chunkPos -> chunkPos.z).min().orElse(claim.getCenter().z);
        int maxChunkZ = claim.getClaimedChunks().stream().mapToInt(chunkPos -> chunkPos.z).max().orElse(claim.getCenter().z);
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
