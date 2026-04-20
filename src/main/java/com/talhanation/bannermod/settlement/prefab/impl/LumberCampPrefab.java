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
 * Procedural lumber camp: 9×5×9 footprint with a 4×4×4 oak-log cabin in one corner
 * and an open grass perimeter so the lumberjack has tree-cutting space. A
 * {@code LumberArea} entity centered in the plot is embedded so BuildArea spawns it
 * automatically on completion.
 */
public final class LumberCampPrefab implements BuildingPrefab {
    public static final ResourceLocation ID = new ResourceLocation("bannermod", "lumber_camp");

    private static final int WIDTH = 9;
    private static final int HEIGHT = 5;
    private static final int DEPTH = 9;

    private static final BuildingPrefabDescriptor DESCRIPTOR = new BuildingPrefabDescriptor(
            ID, "bannermod.prefab.lumber_camp.name", "bannermod.prefab.lumber_camp.description",
            WIDTH, HEIGHT, DEPTH, BuildingPrefabProfession.LUMBERJACK, "minecraft:iron_axe");

    @Override public BuildingPrefabDescriptor descriptor() { return DESCRIPTOR; }

    @Override
    public CompoundTag buildStructureNBT(Direction facing) {
        BlockState dirt = Blocks.DIRT.defaultBlockState();
        BlockState planks = Blocks.OAK_PLANKS.defaultBlockState();
        BlockState log = Blocks.OAK_LOG.defaultBlockState();
        BlockState craftingTable = Blocks.CRAFTING_TABLE.defaultBlockState();
        BlockState chest = Blocks.CHEST.defaultBlockState();

        BuildingStructureNbtBuilder b = BuildingStructureNbtBuilder.of(WIDTH, HEIGHT, DEPTH, facing, "bannermod:lumber_camp");

        // Dirt floor across the whole 9x9 footprint.
        b.fill(0, 0, 0, WIDTH - 1, 0, DEPTH - 1, dirt);

        // Cabin floor: 4x4 oak planks at y=0 inside the cabin corner (0..3, 0..3).
        b.fill(0, 0, 0, 3, 0, 3, planks);

        // Cabin walls y=1..3 around the 4x4 footprint (hollow rectangle per layer).
        for (int y = 1; y <= 3; y++) {
            b.rect(0, 0, 3, 3, y, log);
        }

        // Door opening on the front (z=3): clear (2,1,3) and (2,2,3) for a 1-wide, 2-high doorway.
        // Spec mentions door opening at (2,1,0); we treat the open front as z=3 (the side facing
        // the open yard). Also punch the y=1 doorway slot at the spec-specified (2,1,0) just in
        // case — but the spec explicitly says "open front at z=3" so the actual open hole is there.
        b.block(2, 1, 3, Blocks.AIR.defaultBlockState());
        b.block(2, 2, 3, Blocks.AIR.defaultBlockState());

        // Interior furniture.
        b.block(1, 1, 1, craftingTable);
        b.block(2, 1, 1, chest);

        // Roof at y=4 over the 4x4 cabin footprint.
        b.fill(0, 4, 0, 3, 4, 3, planks);

        // Embedded LumberArea entity at the center of the full 9x9 plot.
        b.entity(
                ModEntityTypes.LUMBERAREA.get(),
                WIDTH / 2,
                1,
                DEPTH / 2,
                Direction.SOUTH,
                16, 12, 16
        );

        return b.build();
    }
}
