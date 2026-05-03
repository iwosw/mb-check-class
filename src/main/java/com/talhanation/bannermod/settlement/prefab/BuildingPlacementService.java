package com.talhanation.bannermod.settlement.prefab;

import com.talhanation.bannermod.entity.military.RecruitPoliticalContext;
import com.talhanation.bannermod.persistence.military.RecruitsClaim;
import com.talhanation.bannermod.entity.civilian.workarea.BuildArea;
import com.talhanation.bannermod.registry.civilian.ModEntityTypes;
import com.talhanation.bannermod.settlement.onboarding.SettlementOnboardingGuide;
import com.talhanation.bannermod.shared.settlement.BannerModSettlementRefreshSupport;
import com.talhanation.bannermod.war.WarRuntimeContext;
import com.talhanation.bannermod.war.registry.PoliticalEntityRecord;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.Heightmap;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Server-side handler for "place this prefab here" requests.
 *
 * <p>Given a prefab id, world position, facing, and the requesting player, this service:</p>
 * <ol>
 *   <li>Looks up the prefab in {@link BuildingPrefabRegistry}.</li>
 *   <li>Spawns a {@link BuildArea} entity at the position, sized to the prefab's footprint,
 *       owned by the player and tagged with the player's faction team.</li>
 *   <li>Loads the prefab's STRUCTURE CompoundTag into the BuildArea via
 *       {@link BuildArea#setStructureNBT(CompoundTag)}.</li>
 *   <li>Calls {@link BuildArea#setStartBuild(boolean)} with {@code creative=false} so the
 *       builder worker must actually place blocks instead of teleporting them in.</li>
 *   <li>Refreshes the settlement snapshot so the new building is visible to the orchestrator
 *       on its next tick.</li>
 * </ol>
 *
 * <p>Completion of the physical build triggers
 * {@link BuildArea#spawnScannedEntities} which spawns the embedded work-area entity
 * (CropArea / LumberArea / MiningArea / ...) that the staffing hook then binds a worker to.</p>
 */
public final class BuildingPlacementService {
    private BuildingPlacementService() {
    }

    public enum Result {
        PLACED,
        UNKNOWN_PREFAB,
        INVALID_POSITION,
        NO_PLAYER
    }

    public static Result placeFor(@Nullable ServerPlayer player,
                                  ResourceLocation prefabId,
                                  BlockPos targetPos,
                                  Direction facing) {
        if (player == null) {
            return Result.NO_PLAYER;
        }
        if (!com.talhanation.bannermod.config.WorkersServerConfig.EnableBuildingPrefabs.get()) {
            // Prefab pipeline disabled — players are expected to build the structure
            // manually and then mark the work area with the surveyor (zone-then-mark
            // workflow). The surveyor's existing per-mode hint covers what each zone
            // needs.
            player.sendSystemMessage(Component.translatable("bannermod.prefab.disabled.use_surveyor")
                    .withStyle(ChatFormatting.YELLOW));
            return Result.UNKNOWN_PREFAB;
        }
        Objects.requireNonNull(prefabId, "prefabId");
        Objects.requireNonNull(targetPos, "targetPos");
        Direction actualFacing = facing == null ? player.getDirection() : facing;

        BuildingPrefabRegistry.instance().ensureDefaultsLoaded();
        Optional<BuildingPrefab> maybePrefab = BuildingPrefabRegistry.instance().lookup(prefabId);
        if (maybePrefab.isEmpty()) {
            player.sendSystemMessage(Component.translatable(
                    "bannermod.prefab.place.unknown", prefabId.toString()));
            return Result.UNKNOWN_PREFAB;
        }
        BuildingPrefab prefab = maybePrefab.get();

        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return Result.INVALID_POSITION;
        }

        BuildingPrefabDescriptor descriptor = prefab.descriptor();
        CompoundTag structure = prefab.buildStructureNBT(actualFacing);

        BuildArea buildArea = new BuildArea(ModEntityTypes.BUILDAREA.get(), serverLevel);
        buildArea.setWidthSize(descriptor.width());
        buildArea.setHeightSize(descriptor.height());
        buildArea.setDepthSize(descriptor.depth());
        buildArea.setFacing(actualFacing);
        BlockPos anchorPos = resolveLeveledAnchor(serverLevel, targetPos, descriptor.width(), descriptor.depth());
        buildArea.moveTo(anchorPos, 0, 0);
        buildArea.createArea();

        String teamId = player.getTeam() == null ? "" : player.getTeam().getName();
        UUID politicalEntityId = RecruitPoliticalContext.politicalEntityIdOf(player, WarRuntimeContext.registry(serverLevel));
        if (politicalEntityId != null) {
            teamId = politicalEntityId.toString();
        }
        buildArea.setTeamStringID(teamId);
        buildArea.setPlayerName(player.getName().getString());
        buildArea.setPlayerUUID(player.getUUID());
        buildArea.setCustomName(Component.literal(""));
        buildArea.setStructureNBT(structure);
        buildArea.setDone(false);

        serverLevel.addFreshEntity(buildArea);

        buildArea.setStartBuild(false);

        PrefabBuildAreaTracker.markPrefabBuildArea(buildArea.getUUID(), prefabId);

        BannerModSettlementRefreshSupport.refreshSnapshot(serverLevel, buildArea.blockPosition());

        player.sendSystemMessage(Component.translatable(
                "bannermod.prefab.place.ok", descriptor.displayKey()));
        player.sendSystemMessage(SettlementOnboardingGuide.placementHint(descriptor).withStyle(ChatFormatting.YELLOW));
        return Result.PLACED;
    }

    public static Result placeForClaim(ServerLevel serverLevel,
                                       RecruitsClaim claim,
                                       ResourceLocation prefabId,
                                       BlockPos targetPos,
                                       Direction facing) {
        if (serverLevel == null || claim == null) {
            return Result.INVALID_POSITION;
        }
        if (!com.talhanation.bannermod.config.WorkersServerConfig.EnableBuildingPrefabs.get()) {
            // Prefab pipeline disabled — settlement-project automation that wanted to
            // auto-place a building should treat this as a no-op and let the human
            // owner draw the zone manually with the surveyor.
            return Result.UNKNOWN_PREFAB;
        }
        Objects.requireNonNull(prefabId, "prefabId");
        Objects.requireNonNull(targetPos, "targetPos");
        Direction actualFacing = facing == null ? Direction.SOUTH : facing;

        BuildingPrefabRegistry.instance().ensureDefaultsLoaded();
        Optional<BuildingPrefab> maybePrefab = BuildingPrefabRegistry.instance().lookup(prefabId);
        if (maybePrefab.isEmpty()) {
            return Result.UNKNOWN_PREFAB;
        }

        PoliticalEntityRecord owner = claim.getOwnerPoliticalEntityId() == null
                ? null
                : WarRuntimeContext.registry(serverLevel).byId(claim.getOwnerPoliticalEntityId()).orElse(null);
        if (owner == null) {
            return Result.INVALID_POSITION;
        }

        BuildingPrefab prefab = maybePrefab.get();
        BuildingPrefabDescriptor descriptor = prefab.descriptor();
        CompoundTag structure = prefab.buildStructureNBT(actualFacing);

        BuildArea buildArea = new BuildArea(ModEntityTypes.BUILDAREA.get(), serverLevel);
        buildArea.setWidthSize(descriptor.width());
        buildArea.setHeightSize(descriptor.height());
        buildArea.setDepthSize(descriptor.depth());
        buildArea.setFacing(actualFacing);
        BlockPos anchorPos = resolveLeveledAnchor(serverLevel, targetPos, descriptor.width(), descriptor.depth(), actualFacing);
        buildArea.moveTo(anchorPos, 0, 0);
        buildArea.createArea();

        String teamId = owner.id().toString();
        buildArea.setTeamStringID(teamId);
        buildArea.setPlayerName(owner.name());
        if (owner.leaderUuid() != null) {
            buildArea.setPlayerUUID(owner.leaderUuid());
        }
        buildArea.setCustomName(Component.literal(""));
        buildArea.setStructureNBT(structure);
        buildArea.setDone(false);

        serverLevel.addFreshEntity(buildArea);
        buildArea.setStartBuild(false);
        PrefabBuildAreaTracker.markPrefabBuildArea(buildArea.getUUID(), prefabId);
        BannerModSettlementRefreshSupport.refreshSnapshot(serverLevel, buildArea.blockPosition());
        return Result.PLACED;
    }

    private static BlockPos resolveLeveledAnchor(ServerLevel level, BlockPos requestedPos, int width, int depth) {
        return resolveLeveledAnchor(level, requestedPos, width, depth, Direction.SOUTH);
    }

    private static BlockPos resolveLeveledAnchor(ServerLevel level, BlockPos requestedPos, int width, int depth, Direction facing) {
        if (level == null || requestedPos == null) {
            return requestedPos;
        }
        BlockPos end = switch (facing == null ? Direction.SOUTH : facing) {
            case NORTH -> requestedPos.offset(Math.max(0, width - 1), 0, -Math.max(0, depth - 1));
            case SOUTH -> requestedPos.offset(-Math.max(0, width - 1), 0, Math.max(0, depth - 1));
            case EAST -> requestedPos.offset(Math.max(0, depth - 1), 0, Math.max(0, width - 1));
            default -> requestedPos.offset(-Math.max(0, depth - 1), 0, -Math.max(0, width - 1));
        };
        int minX = Math.min(requestedPos.getX(), end.getX());
        int maxX = Math.max(requestedPos.getX(), end.getX());
        int minZ = Math.min(requestedPos.getZ(), end.getZ());
        int maxZ = Math.max(requestedPos.getZ(), end.getZ());

        int minY = Integer.MAX_VALUE;
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
                if (y < minY) {
                    minY = y;
                }
            }
        }
        if (minY == Integer.MAX_VALUE) {
            return requestedPos;
        }
        // BuildArea derives its block origin from Entity#getOnPos(), so place the marker at the
        // first free block above terrain. Placing it on the terrain block sinks the prefab by one.
        return new BlockPos(requestedPos.getX(), minY, requestedPos.getZ());
    }
}
