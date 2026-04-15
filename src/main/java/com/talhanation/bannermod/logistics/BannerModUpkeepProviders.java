package com.talhanation.bannermod.logistics;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * @deprecated Use {@link com.talhanation.bannermod.shared.logistics.BannerModUpkeepProviders} instead.
 * Forwarder retained for staged migration per Phase 21 D-05 -- legacy shared-package overlap is
 * documented in MERGE_NOTES.md and is intentionally NOT deduplicated during Phase 21.
 *
 * <p>All methods delegate to the canonical class. Do not add new members here.
 */
@Deprecated
public final class BannerModUpkeepProviders {

    private BannerModUpkeepProviders() {
    }

    public static boolean isValidBlockTarget(Level level, @Nullable BlockPos pos) {
        return com.talhanation.bannermod.shared.logistics.BannerModUpkeepProviders.isValidBlockTarget(level, pos);
    }

    public static boolean isValidEntityTarget(@Nullable Entity entity) {
        return com.talhanation.bannermod.shared.logistics.BannerModUpkeepProviders.isValidEntityTarget(entity);
    }

    @Nullable
    public static Container resolveBlockContainer(Level level, @Nullable BlockPos pos) {
        return com.talhanation.bannermod.shared.logistics.BannerModUpkeepProviders.resolveBlockContainer(level, pos);
    }

    @Nullable
    public static Container resolveEntityContainer(@Nullable Entity entity, @Nullable BlockPos seekerPos) {
        return com.talhanation.bannermod.shared.logistics.BannerModUpkeepProviders.resolveEntityContainer(entity, seekerPos);
    }
}
