package com.talhanation.bannermod.logistics;

import com.talhanation.bannerlord.entity.civilian.workarea.MarketArea;
import com.talhanation.bannerlord.entity.civilian.workarea.StorageArea;
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
        return com.talhanation.bannerlord.shared.logistics.BannerModUpkeepProviders.isValidBlockTarget(level, pos);
    }

    public static boolean isValidEntityTarget(@Nullable Entity entity) {
        return com.talhanation.bannerlord.shared.logistics.BannerModUpkeepProviders.isValidEntityTarget(entity);
    }

    @Nullable
    public static Container resolveBlockContainer(Level level, @Nullable BlockPos pos) {
        return com.talhanation.bannerlord.shared.logistics.BannerModUpkeepProviders.resolveBlockContainer(level, pos);
    }

    @Nullable
    public static Container resolveEntityContainer(@Nullable Entity entity, @Nullable BlockPos seekerPos) {
        return com.talhanation.bannerlord.shared.logistics.BannerModUpkeepProviders.resolveEntityContainer(entity, seekerPos);
    }
}
