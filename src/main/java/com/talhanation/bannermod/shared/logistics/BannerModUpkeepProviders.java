package com.talhanation.bannermod.shared.logistics;

import com.talhanation.bannermod.entity.civilian.workarea.MarketArea;
import com.talhanation.bannermod.entity.civilian.workarea.StorageArea;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

public final class BannerModUpkeepProviders {

    private BannerModUpkeepProviders() {
    }

    public static boolean isValidBlockTarget(Level level, @Nullable BlockPos pos) {
        return resolveBlockContainer(level, pos) != null;
    }

    public static boolean isValidEntityTarget(@Nullable Entity entity) {
        return resolveEntityContainer(entity, entity == null ? null : entity.blockPosition()) != null;
    }

    @Nullable
    public static Container resolveBlockContainer(Level level, @Nullable BlockPos pos) {
        if (level == null || pos == null) {
            return null;
        }
        BlockEntity entity = level.getBlockEntity(pos);
        BlockState blockState = level.getBlockState(pos);
        if (blockState.getBlock() instanceof ChestBlock chestBlock) {
            return ChestBlock.getContainer(chestBlock, blockState, level, pos, false);
        }
        if (entity instanceof Container containerEntity) {
            return containerEntity;
        }
        return null;
    }

    @Nullable
    public static Container resolveEntityContainer(@Nullable Entity entity, @Nullable BlockPos seekerPos) {
        if (entity == null) {
            return null;
        }
        if (entity instanceof AbstractHorse horse) {
            return horse.inventory;
        }
        if (entity instanceof InventoryCarrier carrier) {
            return carrier.getInventory();
        }
        if (entity instanceof Container containerEntity) {
            return containerEntity;
        }
        if (entity instanceof StorageArea storageArea) {
            storageArea.scanStorageBlocks();
            return combine(storageArea.storageMap, seekerPos);
        }
        if (entity instanceof MarketArea marketArea) {
            marketArea.scanContainers();
            return combine(marketArea.containerMap, seekerPos);
        }
        return null;
    }

    @Nullable
    private static Container combine(Map<BlockPos, Container> containers, @Nullable BlockPos seekerPos) {
        if (containers == null || containers.isEmpty()) {
            return null;
        }
        List<Container> ordered = containers.entrySet().stream()
                .sorted(Comparator.comparingDouble(entry -> seekerPos == null ? 0.0D : entry.getKey().distSqr(seekerPos)))
                .map(Map.Entry::getValue)
                .distinct()
                .toList();
        return BannerModCombinedContainer.of(ordered);
    }
}
