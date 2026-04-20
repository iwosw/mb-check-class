package com.talhanation.bannermod.settlement.prefab;

import com.talhanation.bannermod.entity.civilian.workarea.BuildArea;
import com.talhanation.bannermod.registry.civilian.ModEntityTypes;
import com.talhanation.bannermod.shared.settlement.BannerModSettlementRefreshSupport;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;

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
        buildArea.moveTo(targetPos.above(), 0, 0);
        buildArea.createArea();

        String teamId = player.getTeam() == null ? "" : player.getTeam().getName();
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
        return Result.PLACED;
    }
}
