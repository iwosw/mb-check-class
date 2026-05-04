package com.talhanation.bannermod.society;

import com.talhanation.bannermod.events.ClaimEvents;
import com.talhanation.bannermod.persistence.military.RecruitsClaim;
import com.talhanation.bannermod.settlement.BannerModSettlementBuildingRecord;
import com.talhanation.bannermod.settlement.BannerModSettlementSnapshot;
import com.talhanation.bannermod.settlement.household.BannerModHomeAssignmentRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public final class NpcHousingPlotPlanner {
    private static final int RESERVED_PLOT_SPACING = 7;
    private static final int RESERVED_HOME_MATCH_RADIUS = 12;
    private static final int HAMLET_MIN_CHUNK_RADIUS = 3;
    private static final int HAMLET_MAX_CHUNK_RADIUS = 4;

    private NpcHousingPlotPlanner() {
    }

    public static NpcHousingRequestRecord ensureReservedPlot(ServerLevel level,
                                                             BannerModSettlementSnapshot snapshot,
                                                             NpcHousingRequestRecord request,
                                                             long gameTime) {
        if (level == null || snapshot == null || request == null || request.reservedPlotPos() != null) {
            return request;
        }
        NpcHouseholdRecord household = NpcHouseholdAccess.householdFor(level, request.householdId()).orElse(null);
        BlockPos reservedPlot = chooseReservedPlot(level, snapshot, request, household);
        return reservedPlot == null ? request : NpcHousingRequestAccess.reservePlot(level, request.householdId(), reservedPlot, gameTime);
    }

    public static @Nullable UUID findReservedHomeBuilding(BannerModSettlementSnapshot snapshot,
                                                          BannerModHomeAssignmentRuntime homeRuntime,
                                                          NpcHousingRequestRecord request) {
        if (snapshot == null || homeRuntime == null || request == null || request.reservedPlotPos() == null) {
            return null;
        }
        UUID best = null;
        double bestDistSqr = Double.POSITIVE_INFINITY;
        for (BannerModSettlementBuildingRecord building : snapshot.buildings()) {
            if (!isHousingCandidate(building)) {
                continue;
            }
            if (homeRuntime.assignmentsForBuilding(building.buildingUuid()).size() >= building.residentCapacity()) {
                continue;
            }
            double distSqr = building.originPos().distSqr(request.reservedPlotPos());
            if (distSqr > RESERVED_HOME_MATCH_RADIUS * RESERVED_HOME_MATCH_RADIUS) {
                continue;
            }
            if (distSqr < bestDistSqr) {
                bestDistSqr = distSqr;
                best = building.buildingUuid();
            }
        }
        return best;
    }

    public static @Nullable HousingPlotInfo nearestPlotInfo(ServerLevel level,
                                                            UUID claimUuid,
                                                            BlockPos pos,
                                                            double maxDistance) {
        if (level == null || claimUuid == null || pos == null) {
            return null;
        }
        NpcHousingRequestRecord bestRequest = null;
        double bestDistSqr = Math.max(1.0D, maxDistance * maxDistance);
        for (NpcHousingRequestRecord request : NpcHousingRequestSavedData.get(level).runtime().requestsForClaim(claimUuid)) {
            if (request == null || request.reservedPlotPos() == null) {
                continue;
            }
            double distSqr = request.reservedPlotPos().distSqr(pos);
            if (distSqr <= bestDistSqr) {
                bestDistSqr = distSqr;
                bestRequest = request;
            }
        }
        if (bestRequest == null) {
            return null;
        }
        NpcHouseholdRecord household = NpcHouseholdAccess.householdFor(level, bestRequest.householdId()).orElse(null);
        return new HousingPlotInfo(bestRequest, household, bestRequest.reservedPlotPos(), bestDistSqr);
    }

    private static @Nullable BlockPos chooseReservedPlot(ServerLevel level,
                                                         BannerModSettlementSnapshot snapshot,
                                                         NpcHousingRequestRecord request,
                                                         @Nullable NpcHouseholdRecord household) {
        RecruitsClaim claim = resolveClaim(snapshot.claimUuid());
        if (claim == null) {
            return null;
        }
        List<BlockPos> candidates = isHamletHousehold(household)
                ? candidateHamletPlots(level, claim, snapshot)
                : candidateFortPlots(level, claim, snapshot);
        if (candidates.isEmpty()) {
            candidates = candidateFortPlots(level, claim, snapshot);
        }
        if (candidates.isEmpty()) {
            return null;
        }
        List<BlockPos> occupied = occupiedOrigins(snapshot, request.householdId());
        List<BlockPos> reserved = reservedPlots(level, request.claimUuid(), request.householdId());
        BlockPos reference = referencePos(level, snapshot, request, household);
        return candidates.stream()
                .filter(candidate -> farEnough(candidate, occupied))
                .filter(candidate -> farEnough(candidate, reserved))
                .min(Comparator.comparingDouble(candidate -> score(candidate, reference, occupied, reserved)))
                .orElse(null);
    }

    private static double score(BlockPos candidate,
                                BlockPos reference,
                                List<BlockPos> occupied,
                                List<BlockPos> reserved) {
        double distanceToReference = candidate.distSqr(reference);
        double distanceToNeighborhood = nearestDistance(candidate, occupied, reserved);
        return distanceToReference - Math.min(distanceToNeighborhood, 64.0D);
    }

    private static double nearestDistance(BlockPos candidate, List<BlockPos> occupied, List<BlockPos> reserved) {
        double best = Double.POSITIVE_INFINITY;
        for (BlockPos pos : occupied) {
            best = Math.min(best, candidate.distSqr(pos));
        }
        for (BlockPos pos : reserved) {
            best = Math.min(best, candidate.distSqr(pos));
        }
        return Double.isInfinite(best) ? 64.0D : best;
    }

    private static boolean farEnough(BlockPos candidate, List<BlockPos> others) {
        for (BlockPos other : others) {
            if (other != null && candidate.closerThan(other, RESERVED_PLOT_SPACING)) {
                return false;
            }
        }
        return true;
    }

    private static List<BlockPos> occupiedOrigins(BannerModSettlementSnapshot snapshot, UUID requestingHouseholdId) {
        List<BlockPos> occupied = new ArrayList<>();
        if (snapshot == null) {
            return occupied;
        }
        for (BannerModSettlementBuildingRecord building : snapshot.buildings()) {
            if (isHousingCandidate(building)) {
                occupied.add(building.originPos());
            }
        }
        return occupied;
    }

    private static List<BlockPos> reservedPlots(ServerLevel level, UUID claimUuid, UUID requestingHouseholdId) {
        List<BlockPos> reserved = new ArrayList<>();
        for (NpcHousingRequestRecord request : NpcHousingRequestSavedData.get(level).runtime().requestsForClaim(claimUuid)) {
            if (request == null || request.reservedPlotPos() == null || request.householdId().equals(requestingHouseholdId)) {
                continue;
            }
            reserved.add(request.reservedPlotPos());
        }
        return reserved;
    }

    private static BlockPos referencePos(ServerLevel level,
                                         BannerModSettlementSnapshot snapshot,
                                         NpcHousingRequestRecord request,
                                         @Nullable NpcHouseholdRecord household) {
        if (household != null && household.homeBuildingUuid() != null) {
            for (BannerModSettlementBuildingRecord building : snapshot.buildings()) {
                if (building != null && household.homeBuildingUuid().equals(building.buildingUuid())) {
                    return building.originPos();
                }
            }
        }
        ChunkPos anchorChunk = snapshot.anchorChunk();
        return level.getHeightmapPos(
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                new BlockPos(anchorChunk.getMiddleBlockX(), level.getSeaLevel(), anchorChunk.getMiddleBlockZ())
        );
    }

    private static BlockPos surfacePos(ServerLevel level, ChunkPos chunk, int localX, int localZ) {
        return level.getHeightmapPos(
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                new BlockPos(chunk.getMinBlockX() + localX, level.getSeaLevel(), chunk.getMinBlockZ() + localZ)
        );
    }

    private static boolean isHousingCandidate(@Nullable BannerModSettlementBuildingRecord building) {
        if (building == null || building.buildingUuid() == null || building.residentCapacity() <= 0) {
            return false;
        }
        String buildingTypeId = building.buildingTypeId();
        if (buildingTypeId == null || buildingTypeId.isBlank()) {
            return false;
        }
        net.minecraft.resources.ResourceLocation typeId = net.minecraft.resources.ResourceLocation.tryParse(buildingTypeId);
        String path = typeId == null ? buildingTypeId.toLowerCase(java.util.Locale.ROOT) : typeId.getPath().toLowerCase(java.util.Locale.ROOT);
        return building != null
                && (path.contains("house") || path.contains("zemlyanka") || path.contains("hut"));
    }

    private static @Nullable RecruitsClaim resolveClaim(UUID claimUuid) {
        if (claimUuid == null || ClaimEvents.claimManager() == null) {
            return null;
        }
        for (RecruitsClaim claim : ClaimEvents.claimManager().getAllClaims()) {
            if (claim != null && claimUuid.equals(claim.getUUID())) {
                return claim;
            }
        }
        return null;
    }

    public record HousingPlotInfo(NpcHousingRequestRecord request,
                                  @Nullable NpcHouseholdRecord household,
                                  BlockPos plotPos,
                                  double distanceSqr) {
    }

    public static boolean isHamletPlot(BannerModSettlementSnapshot snapshot, @Nullable BlockPos plotPos) {
        if (snapshot == null || plotPos == null || snapshot.anchorChunk() == null) {
            return false;
        }
        ChunkPos plotChunk = new ChunkPos(plotPos);
        int distance = chunkDistance(snapshot.anchorChunk(), plotChunk);
        return distance >= HAMLET_MIN_CHUNK_RADIUS && distance <= HAMLET_MAX_CHUNK_RADIUS;
    }

    private static boolean isHamletHousehold(@Nullable NpcHouseholdRecord household) {
        return household != null
                && household.memberResidentUuids().size() >= 2
                && household.housingState() != NpcHouseholdHousingState.NORMAL;
    }

    private static List<BlockPos> candidateFortPlots(ServerLevel level,
                                                     RecruitsClaim claim,
                                                     BannerModSettlementSnapshot snapshot) {
        return candidatePlots(level, claim, snapshot == null ? claim.getCenter() : snapshot.anchorChunk(), false);
    }

    private static List<BlockPos> candidateHamletPlots(ServerLevel level,
                                                       RecruitsClaim claim,
                                                       BannerModSettlementSnapshot snapshot) {
        return candidatePlots(level, claim, snapshot == null ? null : snapshot.anchorChunk(), true);
    }

    private static List<BlockPos> candidatePlots(ServerLevel level,
                                                 RecruitsClaim claim,
                                                 @Nullable ChunkPos anchorChunk,
                                                 boolean hamletOnly) {
        List<BlockPos> candidates = new ArrayList<>();
        List<ChunkPos> claimedChunks = claim.getClaimedChunks();
        if (claimedChunks.isEmpty() && claim.getCenter() != null) {
            claimedChunks = List.of(claim.getCenter());
        }
        for (ChunkPos chunk : claimedChunks) {
            int distance = anchorChunk == null ? 0 : chunkDistance(anchorChunk, chunk);
            if (hamletOnly) {
                if (distance < HAMLET_MIN_CHUNK_RADIUS || distance > HAMLET_MAX_CHUNK_RADIUS) {
                    continue;
                }
            } else if (anchorChunk != null && distance >= HAMLET_MIN_CHUNK_RADIUS) {
                continue;
            }
            candidates.add(surfacePos(level, chunk, 4, 4));
            candidates.add(surfacePos(level, chunk, 11, 4));
            candidates.add(surfacePos(level, chunk, 4, 11));
            candidates.add(surfacePos(level, chunk, 11, 11));
        }
        return candidates;
    }

    private static int chunkDistance(ChunkPos anchor, ChunkPos other) {
        return Math.max(Math.abs(anchor.x - other.x), Math.abs(anchor.z - other.z));
    }
}
