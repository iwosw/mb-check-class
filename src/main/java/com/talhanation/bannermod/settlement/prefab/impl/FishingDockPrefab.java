package com.talhanation.bannermod.settlement.prefab.impl;

import com.talhanation.bannermod.registry.civilian.ModEntityTypes;
import com.talhanation.bannermod.settlement.prefab.BuildingPrefab;
import com.talhanation.bannermod.settlement.prefab.BuildingPrefabDescriptor;
import com.talhanation.bannermod.settlement.prefab.BuildingPrefabProfession;
import com.talhanation.bannermod.settlement.prefab.BuildingStructureNbtBuilder;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Fisherman's dock — a small hut on shore with a plank dock extending over water and
 * a {@code FishingArea} embedded at the end of the dock so auto-staffing can bind a
 * fisherman to the water tiles.
 */
public final class FishingDockPrefab implements BuildingPrefab {
    public static final ResourceLocation ID = new ResourceLocation("bannermod", "fishing_dock");

    private static final int WIDTH = 7;
    private static final int HEIGHT = 3;
    private static final int DEPTH = 11;

    private static final BuildingPrefabDescriptor DESCRIPTOR = new BuildingPrefabDescriptor(
            ID, "bannermod.prefab.fishing_dock.name", "bannermod.prefab.fishing_dock.description",
            WIDTH, HEIGHT, DEPTH, BuildingPrefabProfession.FISHERMAN, "minecraft:fishing_rod");

    @Override public BuildingPrefabDescriptor descriptor() { return DESCRIPTOR; }

    @Override
    public CompoundTag buildStructureNBT(Direction facing) {
        BlockState planks = Blocks.OAK_PLANKS.defaultBlockState();
        BlockState log = Blocks.OAK_LOG.defaultBlockState();
        BlockState fence = Blocks.OAK_FENCE.defaultBlockState();
        BlockState barrel = Blocks.BARREL.defaultBlockState();

        BuildingStructureNbtBuilder b = BuildingStructureNbtBuilder.of(WIDTH, HEIGHT, DEPTH, facing, "bannermod:fishing_dock");

        // Land floor — oak planks across full 7×7 land portion.
        b.fill(0, 0, 0, 6, 0, 6, planks);

        // Fisherman hut at corner (0..2, 1..2, 0..2).
        // Walls: oak_log ring on y=1 and y=2 around the 3×3 footprint,
        // but leave the front open at (1, 1, 2) and (1, 2, 2).
        for (int y = 1; y <= 2; y++) {
            // back wall z=0
            b.block(0, y, 0, log);
            b.block(1, y, 0, log);
            b.block(2, y, 0, log);
            // side wall x=0
            b.block(0, y, 1, log);
            b.block(0, y, 2, log);
            // side wall x=2
            b.block(2, y, 1, log);
            b.block(2, y, 2, log);
            // front wall z=2 — corners only; center (1, y, 2) is the open doorway.
            // (no blocks placed at (1, y, 2))
        }

        // Hut roof — oak_planks lid over the 3×3 at y=3? Hut height only 3 (y=0..2),
        // so the roof sits at y=2 covering the top. Place a planks lid above walls at y=2? The
        // walls already occupy y=1..2; the roof caps at the top of y=2, so we put roof planks
        // at y=2 only over interior (1, 2, 1) to finish it? Simpler: roof covers entire 3×3 at y=2
        // interior — but that would overwrite the wall tops. Keep roof minimal at y=2 interior.
        b.block(1, 2, 1, planks);

        // Barrel inside hut.
        b.block(1, 1, 1, barrel);

        // Dock planks over water: z=7..10 at y=0, 3-wide centered (x=2..4).
        b.fill(2, 0, 7, 4, 0, 10, planks);

        // Fence posts along dock edges at z=7..10 on sides x=2 and x=4, y=1.
        for (int z = 7; z <= 10; z++) {
            b.block(2, 1, z, fence);
            b.block(4, 1, z, fence);
        }

        // Embedded FishingArea at end of dock, over water.
        b.entity(
                ModEntityTypes.FISHINGAREA.get(),
                3, 0, 10,
                Direction.SOUTH,
                10, 3, 10
        );

        return b.build();
    }
}
