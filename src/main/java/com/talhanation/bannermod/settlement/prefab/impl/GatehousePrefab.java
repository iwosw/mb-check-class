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
 * Compact fortified entrance prefab for players who want a real gatehouse without AW2.
 */
public final class GatehousePrefab implements BuildingPrefab {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("bannermod", "gatehouse");

    private static final int WIDTH = 11;
    private static final int HEIGHT = 8;
    private static final int DEPTH = 9;

    private static final BuildingPrefabDescriptor DESCRIPTOR = new BuildingPrefabDescriptor(
            ID, "bannermod.prefab.gatehouse.name", "bannermod.prefab.gatehouse.description",
            WIDTH, HEIGHT, DEPTH, BuildingPrefabProfession.NONE, "minecraft:oak_fence_gate");

    @Override
    public BuildingPrefabDescriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public CompoundTag buildStructureNBT(Direction facing) {
        BlockState stone = Blocks.STONE_BRICKS.defaultBlockState();
        BlockState stoneWall = Blocks.STONE_BRICK_WALL.defaultBlockState();
        BlockState logs = Blocks.SPRUCE_LOG.defaultBlockState();
        BlockState planks = Blocks.SPRUCE_PLANKS.defaultBlockState();
        BlockState slab = Blocks.SPRUCE_SLAB.defaultBlockState();
        BlockState bars = Blocks.IRON_BARS.defaultBlockState();
        BlockState lantern = Blocks.LANTERN.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState stairNorth = Blocks.SPRUCE_STAIRS.defaultBlockState()
                .setValue(net.minecraft.world.level.block.StairBlock.FACING, Direction.NORTH);
        BlockState stairSouth = Blocks.SPRUCE_STAIRS.defaultBlockState()
                .setValue(net.minecraft.world.level.block.StairBlock.FACING, Direction.SOUTH);

        BuildingStructureNbtBuilder b = BuildingStructureNbtBuilder.of(WIDTH, HEIGHT, DEPTH, facing, "bannermod:gatehouse");

        b.fill(0, 0, 0, 10, 0, 8, stone);
        b.fill(0, 1, 0, 2, 4, 8, stone);
        b.fill(8, 1, 0, 10, 4, 8, stone);
        b.fill(3, 1, 1, 7, 3, 7, air);
        b.fill(4, 1, 0, 6, 3, 8, air);

        for (int y = 1; y <= 4; y++) {
            b.block(3, y, 0, logs);
            b.block(7, y, 0, logs);
            b.block(3, y, 8, logs);
            b.block(7, y, 8, logs);
        }
        for (int x = 4; x <= 6; x++) {
            b.block(x, 4, 0, logs);
            b.block(x, 4, 8, logs);
        }
        for (int z = 1; z <= 7; z++) {
            b.block(3, 1, z, stone);
            b.block(3, 2, z, stone);
            b.block(3, 3, z, stone);
            b.block(7, 1, z, stone);
            b.block(7, 2, z, stone);
            b.block(7, 3, z, stone);
        }

        b.fill(3, 4, 1, 7, 4, 7, planks);
        b.fill(4, 5, 2, 6, 5, 6, slab);
        for (int x = 3; x <= 7; x++) {
            b.block(x, 5, 0, stairSouth);
            b.block(x, 5, 8, stairNorth);
        }
        for (int z = 1; z <= 7; z++) {
            b.block(0, 5, z, stoneWall);
            b.block(2, 5, z, stoneWall);
            b.block(8, 5, z, stoneWall);
            b.block(10, 5, z, stoneWall);
        }
        for (int x = 0; x <= 2; x++) {
            b.block(x, 5, 0, stoneWall);
            b.block(x, 5, 8, stoneWall);
            b.block(x, 5, 4, stoneWall);
            b.block(x, 6, 4, stoneWall);
            b.block(x + 8, 5, 0, stoneWall);
            b.block(x + 8, 5, 8, stoneWall);
            b.block(x + 8, 5, 4, stoneWall);
            b.block(x + 8, 6, 4, stoneWall);
        }

        b.block(1, 3, 2, bars);
        b.block(1, 3, 6, bars);
        b.block(9, 3, 2, bars);
        b.block(9, 3, 6, bars);
        b.block(5, 3, 2, lantern);
        b.block(5, 3, 6, lantern);

        return b.build();
    }
}
