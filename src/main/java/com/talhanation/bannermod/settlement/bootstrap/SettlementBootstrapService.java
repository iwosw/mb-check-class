package com.talhanation.bannermod.settlement.bootstrap;

import com.talhanation.bannermod.events.ClaimEvents;
import com.talhanation.bannermod.config.RecruitsServerConfig;
import com.talhanation.bannermod.persistence.military.RecruitsClaim;
import com.talhanation.bannermod.persistence.military.RecruitsPlayerInfo;
import com.talhanation.bannermod.war.WarRuntimeContext;
import com.talhanation.bannermod.war.registry.PoliticalEntityRecord;
import com.talhanation.bannermod.war.registry.PoliticalMembership;
import com.talhanation.bannermod.settlement.building.BuildingType;
import com.talhanation.bannermod.settlement.civilian.WorkerSettlementSpawnRules;
import com.talhanation.bannermod.settlement.civilian.WorkerSettlementSpawner;
import com.talhanation.bannermod.settlement.validation.BuildingValidationResult;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public final class SettlementBootstrapService {
    private static final List<WorkerSettlementSpawnRules.WorkerProfession> STARTER_PROFESSIONS = List.of(
            WorkerSettlementSpawnRules.WorkerProfession.FARMER,
            WorkerSettlementSpawnRules.WorkerProfession.MINER,
            WorkerSettlementSpawnRules.WorkerProfession.LUMBERJACK,
            WorkerSettlementSpawnRules.WorkerProfession.BUILDER
    );

    private SettlementBootstrapService() {
    }

    public static BootstrapResult bootstrapSettlement(ServerLevel level,
                                                      ServerPlayer player,
                                                      BuildingValidationResult fortValidationResult) {
        if (level == null || player == null || fortValidationResult == null) {
            return BootstrapResult.failure("Bootstrap request is missing required arguments.");
        }
        if (!fortValidationResult.valid() || fortValidationResult.type() != BuildingType.STARTER_FORT) {
            return BootstrapResult.failure("Settlement bootstrap requires a successful STARTER_FORT validation.");
        }
        if (fortValidationResult.snapshot() == null) {
            return BootstrapResult.failure("Fort validation snapshot is missing.");
        }

        BlockPos authorityPos = fortValidationResult.snapshot().anchorPos();
        RecruitsClaim claim = claimAt(authorityPos);
        if (claim != null && !playerOwnsClaim(player, claim)) {
            return BootstrapResult.failure("Settlement bootstrap requires a fort inside your faction claim.");
        }
        if (claim == null && isStarterTownTooCloseToSameNationTown(level, player, authorityPos)) {
            return BootstrapResult.failure("New town center is too close to another town in your nation.");
        }
        if (claim == null) {
            claim = createStarterClaim(level, player, authorityPos);
        }
        if (claim == null) {
            return BootstrapResult.failure("No claim found at fort authority position and automatic claim bootstrap failed.");
        }

        SettlementRegistryData registry = SettlementRegistryData.get(level);
        SettlementRecord existing = registry.getSettlementAt(new ChunkPos(authorityPos));
        if (existing != null) {
            return BootstrapResult.success("Settlement already exists at this authority position.", existing);
        }

        UUID settlementId = UUID.randomUUID();
        SettlementRecord settlement = new SettlementRecord(
                settlementId,
                player.getUUID(),
                claim.getOwnerPoliticalEntityId() == null ? null : claim.getOwnerPoliticalEntityId().toString(),
                claim.getUUID(),
                level.dimension(),
                authorityPos,
                authorityPos,
                UUID.randomUUID(),
                SettlementStatus.ACTIVE,
                level.getGameTime()
        );
        registry.put(settlement);

        int spawnedWorkers = spawnStarterCitizens(level, authorityPos, claim);
        return BootstrapResult.success(starterWorkerReadinessMessage(spawnedWorkers), settlement);
    }

    static String starterWorkerReadinessMessage(int spawnedWorkers) {
        return "Settlement bootstrapped. Starter workers spawned: " + spawnedWorkers
                + ". Ready: farmer has a starter crop area. Waiting: miner needs a mine, lumberjack needs a lumber camp, builder needs an architect workshop/build area. Free citizens spawn separately from worker jobs.";
    }

    public static BootstrapResult bootstrapSettlement(ServerLevel level,
                                                      Player player,
                                                      BuildingValidationResult fortValidationResult) {
        if (player instanceof ServerPlayer serverPlayer) {
            return bootstrapSettlement(level, serverPlayer, fortValidationResult);
        }
        if (level == null || player == null || fortValidationResult == null) {
            return BootstrapResult.failure("Bootstrap request is missing required arguments.");
        }
        if (!fortValidationResult.valid() || fortValidationResult.type() != BuildingType.STARTER_FORT) {
            return BootstrapResult.failure("Settlement bootstrap requires a successful STARTER_FORT validation.");
        }
        if (fortValidationResult.snapshot() == null) {
            return BootstrapResult.failure("Fort validation snapshot is missing.");
        }
        BlockPos authorityPos = fortValidationResult.snapshot().anchorPos();
        RecruitsClaim claim = claimAt(authorityPos);
        if (claim == null) {
            return BootstrapResult.failure("No claim found at fort authority position.");
        }
        if (!playerOwnsClaim(player, claim)) {
            return BootstrapResult.failure("Settlement bootstrap requires a fort inside your faction claim.");
        }
        SettlementRegistryData registry = SettlementRegistryData.get(level);
        SettlementRecord existing = registry.getSettlementAt(new ChunkPos(authorityPos));
        if (existing != null) {
            return BootstrapResult.success("Settlement already exists at this authority position.", existing);
        }
        UUID settlementId = UUID.randomUUID();
        SettlementRecord settlement = new SettlementRecord(
                settlementId,
                player.getUUID(),
                claim.getOwnerPoliticalEntityId() == null ? null : claim.getOwnerPoliticalEntityId().toString(),
                claim.getUUID(),
                level.dimension(),
                authorityPos,
                authorityPos,
                UUID.randomUUID(),
                SettlementStatus.ACTIVE,
                level.getGameTime()
        );
        registry.put(settlement);
        return BootstrapResult.success("Settlement bootstrapped in test/player mode. Starter citizens skipped.", settlement);
    }

    @Nullable
    private static RecruitsClaim claimAt(BlockPos anchorPos) {
        if (ClaimEvents.recruitsClaimManager == null) {
            return null;
        }
        return ClaimEvents.recruitsClaimManager.getClaim(new ChunkPos(anchorPos));
    }

    private static boolean playerOwnsClaim(Player player, RecruitsClaim claim) {
        if (player == null || claim == null) return false;
        if (claim.getPlayerInfo() != null && player.getUUID().equals(claim.getPlayerInfo().getUUID())) {
            return true;
        }
        UUID politicalEntityId = claim.getOwnerPoliticalEntityId();
        if (politicalEntityId == null || !(player.level() instanceof ServerLevel level)) return false;
        PoliticalEntityRecord owner = WarRuntimeContext.registry(level).byId(politicalEntityId).orElse(null);
        if (owner == null) return false;
        UUID playerUuid = player.getUUID();
        return playerUuid.equals(owner.leaderUuid()) || owner.coLeaderUuids().contains(playerUuid);
    }

    private static boolean isStarterTownTooCloseToSameNationTown(ServerLevel level, ServerPlayer player, BlockPos authorityPos) {
        if (ClaimEvents.recruitsClaimManager == null) {
            return false;
        }
        UUID politicalEntityId = PoliticalMembership.entityIdFor(WarRuntimeContext.registry(level), player.getUUID());
        if (politicalEntityId == null) {
            return false;
        }
        RecruitsClaim candidate = new RecruitsClaim(player.getName().getString(), politicalEntityId);
        ChunkPos center = new ChunkPos(authorityPos);
        candidate.addChunk(center);
        candidate.setCenter(center);
        return ClaimEvents.recruitsClaimManager.isTownTooCloseToSameNationTown(
                candidate,
                null,
                RecruitsServerConfig.TownMinCenterDistance.get());
    }

    @Nullable
    private static RecruitsClaim createStarterClaim(ServerLevel level, ServerPlayer player, BlockPos authorityPos) {
        if (ClaimEvents.recruitsClaimManager == null) {
            return null;
        }
        UUID politicalEntityId = PoliticalMembership.entityIdFor(WarRuntimeContext.registry(level), player.getUUID());
        if (politicalEntityId == null) {
            return null;
        }

        ChunkPos anchorChunk = new ChunkPos(authorityPos);
        RecruitsClaim existing = ClaimEvents.recruitsClaimManager.getClaim(anchorChunk);
        if (existing != null) {
            return existing;
        }

        RecruitsClaim claim = new RecruitsClaim(player.getName().getString(), politicalEntityId);
        claim.addChunk(anchorChunk);
        claim.setCenter(anchorChunk);
        claim.setPlayer(new RecruitsPlayerInfo(player.getUUID(), player.getName().getString()));
        if (ClaimEvents.recruitsClaimManager.isTownTooCloseToSameNationTown(
                claim,
                null,
                RecruitsServerConfig.TownMinCenterDistance.get())) {
            return null;
        }
        ClaimEvents.recruitsClaimManager.addOrUpdateClaim(level, claim);
        return claim;
    }

    private static int spawnStarterCitizens(ServerLevel level, BlockPos authorityPos, RecruitsClaim claim) {
        int spawned = 0;
        for (WorkerSettlementSpawnRules.WorkerProfession profession : STARTER_PROFESSIONS) {
            WorkerSettlementSpawnRules.Decision decision =
                    new WorkerSettlementSpawnRules.Decision(true, profession, null, 0L);
            if (WorkerSettlementSpawner.spawnClaimWorker(level, authorityPos, decision, claim) != null) {
                spawned++;
            }
        }
        return spawned;
    }
}
