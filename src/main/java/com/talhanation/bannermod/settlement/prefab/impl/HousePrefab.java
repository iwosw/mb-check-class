package com.talhanation.bannermod.settlement.prefab.impl;

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
 * Small house. No embedded work-area — this is a home. Auto-staffing treats
 * {@link BuildingPrefabProfession#NONE} as "no worker needed".
 */
public final class HousePrefab implements BuildingPrefab {
    public static final ResourceLocation ID = new ResourceLocation("bannermod", "house");

    private static final int WIDTH = 7;
    private static final int HEIGHT = 5;
    private static final int DEPTH = 7;

    private static final BuildingPrefabDescriptor DESCRIPTOR = new BuildingPrefabDescriptor(
            ID, "bannermod.prefab.house.name", "bannermod.prefab.house.description",
            WIDTH, HEIGHT, DEPTH, BuildingPrefabProfession.NONE, "minecraft:oak_door");

    @Override public BuildingPrefabDescriptor descriptor() { return DESCRIPTOR; }

    @Override
    public CompoundTag buildStructureNBT(Direction facing) {
        BlockState planks = Blocks.OAK_PLANKS.defaultBlockState();
        BlockState log = Blocks.OAK_LOG.defaultBlockState();
        BlockState glass = Blocks.GLASS_PANE.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState bed = Blocks.RED_BED.defaultBlockState();
        BlockState table = Blocks.CRAFTING_TABLE.defaultBlockState();
        BlockState furnace = Blocks.FURNACE.defaultBlockState();

        BuildingStructureNbtBuilder b = BuildingStructureNbtBuilder.of(WIDTH, HEIGHT, DEPTH, facing, "bannermod:house");

        // Floor 7×7.
        b.fill(0, 0, 0, 6, 0, 6, planks);

        // Log walls y=1..3 around perimeter.
        for (int y = 1; y <= 3; y++) {
            b.rect(0, 0, 6, 6, y, log);
        }

        // Door opening — overwrite the two log blocks with AIR so there's a passable door gap.
        b.block(3, 1, 0, air);
        b.block(3, 2, 0, air);

        // Windows — overwrite wall logs with glass panes.
        b.block(1, 2, 0, glass);
        b.block(5, 2, 0, glass);
        b.block(0, 2, 3, glass);
        b.block(6, 2, 3, glass);

        // Flat plank roof at y=4.
        b.fill(0, 4, 0, 6, 4, 6, planks);

        // Interior — simplification: place the bed head block; MC will merge at load time.
        b.block(1, 1, 5, bed);

        // Crafting table and furnace.
        b.block(5, 1, 5, table);
        b.block(5, 1, 1, furnace);

        // No embedded work-area entity — homes aren't workplaces.
        return b.build();
    }
}
