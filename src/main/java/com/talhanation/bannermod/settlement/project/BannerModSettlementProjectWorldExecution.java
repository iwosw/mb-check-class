package com.talhanation.bannermod.settlement.project;

import com.talhanation.bannermod.entity.civilian.workarea.BuildArea;
import com.talhanation.bannermod.events.ClaimEvents;
import com.talhanation.bannermod.persistence.military.RecruitsClaim;
import com.talhanation.bannermod.settlement.BannerModSettlementBuildingProfileSeed;
import com.talhanation.bannermod.settlement.growth.PendingProject;
import com.talhanation.bannermod.settlement.growth.ProjectBlocker;
import com.talhanation.bannermod.settlement.prefab.BuildingPlacementService;
import com.talhanation.bannermod.settlement.prefab.impl.BarracksPrefab;
import com.talhanation.bannermod.settlement.prefab.impl.FarmPrefab;
import com.talhanation.bannermod.settlement.prefab.impl.HousePrefab;
import com.talhanation.bannermod.settlement.prefab.impl.LumberCampPrefab;
import com.talhanation.bannermod.settlement.prefab.impl.MarketStallPrefab;
import com.talhanation.bannermod.settlement.prefab.impl.StoragePrefab;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.List;
import java.util.UUID;

final class BannerModSettlementProjectWorldExecution {

    private static final int PLACEMENT_SPACING_BLOCKS = 24;

    private BannerModSettlementProjectWorldExecution() {
    }

    static boolean ensureExecutableTarget(ServerLevel level, UUID claimUuid, PendingProject project) {
        if (level == null
                || claimUuid == null
                || project == null
                || ClaimEvents.recruitsClaimManager == null
                || (project.blockerReason() != ProjectBlocker.NONE && project.blockerReason() != ProjectBlocker.NO_SITE)) {
            return false;
        }
        RecruitsClaim claim = resolveClaim(claimUuid);
        if (claim == null) {
            return false;
        }
        List<BuildArea> buildAreas = BannerModBuildAreaProjectBridge.collectBuildAreas(level, claim);
        if (buildAreas.stream().anyMatch(buildArea -> buildArea != null && buildArea.isAlive() && !buildArea.isDone())) {
            return false;
        }
        return BuildingPlacementService.placeForClaim(
                level,
                claim,
                prefabIdFor(project.profileSeed()),
                choosePlacementPos(level, claim, buildAreas.size()),
                Direction.SOUTH
        ) == BuildingPlacementService.Result.PLACED;
    }

    private static RecruitsClaim resolveClaim(UUID claimUuid) {
        for (RecruitsClaim claim : ClaimEvents.recruitsClaimManager.getAllClaims()) {
            if (claim != null && claimUuid.equals(claim.getUUID())) {
                return claim;
            }
        }
        return null;
    }

    private static ResourceLocation prefabIdFor(BannerModSettlementBuildingProfileSeed profileSeed) {
        return switch (profileSeed == null ? BannerModSettlementBuildingProfileSeed.GENERAL : profileSeed) {
            case FOOD_PRODUCTION -> FarmPrefab.ID;
            case MATERIAL_PRODUCTION -> LumberCampPrefab.ID;
            case STORAGE -> StoragePrefab.ID;
            case MARKET -> MarketStallPrefab.ID;
            case CONSTRUCTION -> BarracksPrefab.ID;
            case GENERAL -> HousePrefab.ID;
        };
    }

    private static BlockPos choosePlacementPos(ServerLevel level, RecruitsClaim claim, int existingBuildAreaCount) {
        ChunkPos anchorChunk = claim.getCenter();
        if (anchorChunk == null && !claim.getClaimedChunks().isEmpty()) {
            anchorChunk = claim.getClaimedChunks().get(0);
        }
        if (anchorChunk == null) {
            return BlockPos.ZERO;
        }

        BlockPos anchorPos = level.getHeightmapPos(
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                new BlockPos(anchorChunk.getMiddleBlockX(), level.getSeaLevel(), anchorChunk.getMiddleBlockZ())
        );
        int ring = existingBuildAreaCount / 4 + 1;
        int lane = existingBuildAreaCount % 4;
        int distance = ring * PLACEMENT_SPACING_BLOCKS;
        return switch (lane) {
            case 0 -> anchorPos.offset(distance, 0, 0);
            case 1 -> anchorPos.offset(-distance, 0, 0);
            case 2 -> anchorPos.offset(0, 0, distance);
            default -> anchorPos.offset(0, 0, -distance);
        };
    }
}
