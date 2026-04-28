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
 * Procedural mine: 7×4×7 cobblestone pad with a 4×4×4 stone-brick hut at one corner
 * (chest + furnace inside, doorway cut at the front) and an open 4×7-ish mining yard.
 * A {@code MiningArea} entity is embedded in the open area so BuildArea spawns the
 * work-area automatically on completion.
 */
public final class MinePrefab implements BuildingPrefab {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("bannermod", "mine");

    private static final int WIDTH = 7;
    private static final int HEIGHT = 4;
    private static final int DEPTH = 7;

    private static final BuildingPrefabDescriptor DESCRIPTOR = new BuildingPrefabDescriptor(
            ID, "bannermod.prefab.mine.name", "bannermod.prefab.mine.description",
            WIDTH, HEIGHT, DEPTH, BuildingPrefabProfession.MINER, "minecraft:iron_pickaxe");

    @Override public BuildingPrefabDescriptor descriptor() { return DESCRIPTOR; }

    @Override
    public CompoundTag buildStructureNBT(Direction facing) {
        BlockState cobble = Blocks.COBBLESTONE.defaultBlockState();
        BlockState stoneBricks = Blocks.STONE_BRICKS.defaultBlockState();
        BlockState chest = Blocks.CHEST.defaultBlockState();
        BlockState furnace = Blocks.FURNACE.defaultBlockState();

        BuildingStructureNbtBuilder b = BuildingStructureNbtBuilder.of(WIDTH, HEIGHT, DEPTH, facing, "bannermod:mine");

        // Cobblestone pad across the full 7x7 footprint.
        b.fill(0, 0, 0, WIDTH - 1, 0, DEPTH - 1, cobble);

        // Stone-brick hut walls y=1..2 around 4x4 footprint (hollow rect per layer).
        for (int y = 1; y <= 2; y++) {
            b.rect(0, 0, 3, 3, y, stoneBricks);
        }

        // Roof at y=3 over the 4x4 hut footprint.
        b.fill(0, 3, 0, 3, 3, 3, stoneBricks);

        // Interior: chest and furnace.
        b.block(1, 1, 1, chest);
        b.block(2, 1, 1, furnace);

        // Entrance opening at (2,1,0) and (2,2,0): carve the wall blocks back to air.
        b.block(2, 1, 0, Blocks.AIR.defaultBlockState());
        b.block(2, 2, 0, Blocks.AIR.defaultBlockState());

        // Embedded MiningArea entity in the open area (outside the hut).
        b.entity(
                ModEntityTypes.MININGAREA.get(),
                5, 0, 5,
                Direction.SOUTH,
                8, 6, 8
        );

        return b.build();
    }
}
