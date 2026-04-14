package com.talhanation.bannermod.governance;

import com.talhanation.bannermod.logistics.BannerModSupplyStatus;
import com.talhanation.bannermod.settlement.BannerModSettlementBinding;
import com.talhanation.bannerlord.entity.shared.AbstractRecruitEntity;
import com.talhanation.recruits.world.RecruitsClaim;
import com.talhanation.recruits.world.RecruitsClaimManager;
import com.talhanation.bannerlord.entity.civilian.AbstractWorkerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public final class BannerModGovernorHeartbeat {

    private BannerModGovernorHeartbeat() {
    }

    public static HeartbeatReport evaluate(HeartbeatInput input) {
        EnumSet<BannerModGovernorIncident> incidents = EnumSet.noneOf(BannerModGovernorIncident.class);
        EnumSet<BannerModGovernorRecommendation> recommendations = EnumSet.noneOf(BannerModGovernorRecommendation.class);

        int citizens = Math.max(0, input.villagerCount()) + Math.max(0, input.workerCount());
        int taxesDue = 0;
        int taxesCollected = 0;

        switch (input.settlementStatus()) {
            case HOSTILE_CLAIM -> incidents.add(BannerModGovernorIncident.HOSTILE_CLAIM);
            case DEGRADED_MISMATCH -> incidents.add(BannerModGovernorIncident.DEGRADED_SETTLEMENT);
            case UNCLAIMED -> incidents.add(BannerModGovernorIncident.DEGRADED_SETTLEMENT);
            case FRIENDLY_CLAIM -> {
                taxesDue = citizens * 2;
                taxesCollected = taxesDue;
            }
        }

        if (input.underSiege()) {
            incidents.add(BannerModGovernorIncident.UNDER_SIEGE);
            taxesCollected = 0;
        }

        if (input.settlementStatus() != BannerModSettlementBinding.Status.FRIENDLY_CLAIM) {
            taxesCollected = 0;
        }

        if (input.workerCount() <= 0) {
            incidents.add(BannerModGovernorIncident.WORKER_SHORTAGE);
        }

        if (input.recruitCount() < Math.max(1, citizens / 2)) {
            recommendations.add(BannerModGovernorRecommendation.INCREASE_GARRISON);
            recommendations.add(BannerModGovernorRecommendation.STRENGTHEN_FORTIFICATIONS);
        }

        if (input.workerSupplyStatus() != null && input.workerSupplyStatus().blocked()) {
            incidents.add(BannerModGovernorIncident.SUPPLY_BLOCKED);
            recommendations.add(BannerModGovernorRecommendation.RELIEVE_SUPPLY_PRESSURE);
        }

        if (input.recruitSupplyStatus() != null && input.recruitSupplyStatus().blocked()) {
            incidents.add(BannerModGovernorIncident.RECRUIT_UPKEEP_BLOCKED);
            recommendations.add(BannerModGovernorRecommendation.RELIEVE_SUPPLY_PRESSURE);
        }

        if (recommendations.isEmpty()) {
            recommendations.add(BannerModGovernorRecommendation.HOLD_COURSE);
        }

        return new HeartbeatReport(
                citizens,
                taxesDue,
                taxesCollected,
                List.copyOf(incidents),
                List.copyOf(recommendations),
                input.gameTime(),
                taxesCollected > 0 ? input.gameTime() : input.snapshot().lastCollectionTick()
        );
    }

    public static List<String> incidentTokens(List<BannerModGovernorIncident> incidents) {
        List<String> tokens = new ArrayList<>();
        for (BannerModGovernorIncident incident : incidents) {
            tokens.add(incident.name().toLowerCase());
        }
        return tokens;
    }

    public static List<String> recommendationTokens(List<BannerModGovernorRecommendation> recommendations) {
        List<String> tokens = new ArrayList<>();
        for (BannerModGovernorRecommendation recommendation : recommendations) {
            tokens.add(recommendation.name().toLowerCase());
        }
        return tokens;
    }

    public static void runGovernedClaimHeartbeat(ServerLevel level, RecruitsClaimManager claimManager, BannerModGovernorManager governorManager) {
        if (level == null || claimManager == null || governorManager == null) {
            return;
        }

        for (BannerModGovernorSnapshot snapshot : governorManager.getAllSnapshots()) {
            if (snapshot == null || !snapshot.hasGovernor()) {
                continue;
            }

            RecruitsClaim claim = resolveClaim(claimManager, snapshot);
            BannerModSettlementBinding.Binding binding = claim == null
                    ? BannerModSettlementBinding.resolveSettlementStatus((RecruitsClaim) null, snapshot.anchorChunk(), snapshot.settlementFactionId())
                    : BannerModSettlementBinding.resolveSettlementStatus(claim, resolveAnchorChunk(claim, snapshot), snapshot.settlementFactionId());

            HeartbeatReport report = evaluate(new HeartbeatInput(
                    binding.status(),
                    claim != null && claim.isUnderSiege,
                    claim == null ? 0 : countEntitiesInClaim(level, claim, Villager.class),
                    claim == null ? 0 : countEntitiesInClaim(level, claim, AbstractWorkerEntity.class),
                    claim == null ? 0 : countEntitiesInClaim(level, claim, AbstractRecruitEntity.class),
                    new BannerModSupplyStatus.WorkerSupplyStatus(false, null, null),
                    new BannerModSupplyStatus.RecruitSupplyStatus(BannerModSupplyStatus.RecruitSupplyState.READY, false, false, false, null),
                    level.getGameTime(),
                    snapshot.lastHeartbeatTick(),
                    snapshot
            ));

            governorManager.putSnapshot(snapshot.withHeartbeatReport(
                    report.heartbeatTick(),
                    report.collectionTick(),
                    report.citizenCount(),
                    report.taxesDue(),
                    report.taxesCollected(),
                    incidentTokens(report.incidents()),
                    recommendationTokens(report.recommendations())
            ));
        }
    }

    public record HeartbeatInput(BannerModSettlementBinding.Status settlementStatus,
                                 boolean underSiege,
                                 int villagerCount,
                                 int workerCount,
                                 int recruitCount,
                                 BannerModSupplyStatus.WorkerSupplyStatus workerSupplyStatus,
                                 BannerModSupplyStatus.RecruitSupplyStatus recruitSupplyStatus,
                                 long gameTime,
                                 long previousHeartbeatTick,
                                 BannerModGovernorSnapshot snapshot) {
    }

    public record HeartbeatReport(int citizenCount,
                                  int taxesDue,
                                  int taxesCollected,
                                  List<BannerModGovernorIncident> incidents,
                                  List<BannerModGovernorRecommendation> recommendations,
                                  long heartbeatTick,
                                  long collectionTick) {
    }

    private static RecruitsClaim resolveClaim(RecruitsClaimManager claimManager, BannerModGovernorSnapshot snapshot) {
        RecruitsClaim claim = claimManager.getClaim(snapshot.anchorChunk());
        if (claim != null && claim.getUUID().equals(snapshot.claimUuid())) {
            return claim;
        }
        for (RecruitsClaim candidate : claimManager.getAllClaims()) {
            if (candidate != null && candidate.getUUID().equals(snapshot.claimUuid())) {
                return candidate;
            }
        }
        return claim;
    }

    private static ChunkPos resolveAnchorChunk(RecruitsClaim claim, BannerModGovernorSnapshot snapshot) {
        if (claim.getCenter() != null) {
            return claim.getCenter();
        }
        if (!claim.getClaimedChunks().isEmpty()) {
            return claim.getClaimedChunks().get(0);
        }
        return snapshot.anchorChunk();
    }

    private static <T extends Entity> int countEntitiesInClaim(ServerLevel level, RecruitsClaim claim, Class<T> entityClass) {
        return level.getEntitiesOfClass(entityClass, claimBounds(level, claim), entity -> entity.isAlive() && claim.containsChunk(entity.chunkPosition())).size();
    }

    private static AABB claimBounds(ServerLevel level, RecruitsClaim claim) {
        ChunkPos anchor = claim.getCenter() != null ? claim.getCenter() : new ChunkPos(0, 0);
        int minChunkX = claim.getClaimedChunks().stream().mapToInt(chunkPos -> chunkPos.x).min().orElse(anchor.x);
        int maxChunkX = claim.getClaimedChunks().stream().mapToInt(chunkPos -> chunkPos.x).max().orElse(anchor.x);
        int minChunkZ = claim.getClaimedChunks().stream().mapToInt(chunkPos -> chunkPos.z).min().orElse(anchor.z);
        int maxChunkZ = claim.getClaimedChunks().stream().mapToInt(chunkPos -> chunkPos.z).max().orElse(anchor.z);
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
