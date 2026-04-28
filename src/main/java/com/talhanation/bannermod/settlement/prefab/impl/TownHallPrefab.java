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
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

/**
 * Central settlement building used as a default claim bootstrap.
 * Designed as a compact "town hall" with indoor storage and work tables.
 */
public final class TownHallPrefab implements BuildingPrefab {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("bannermod", "town_hall");

    private static final int WIDTH = 13;
    private static final int HEIGHT = 9;
    private static final int DEPTH = 11;

    private static final BuildingPrefabDescriptor DESCRIPTOR = new BuildingPrefabDescriptor(
            ID, "bannermod.prefab.town_hall.name", "bannermod.prefab.town_hall.description",
            WIDTH, HEIGHT, DEPTH, BuildingPrefabProfession.NONE, "minecraft:bell");

    @Override public BuildingPrefabDescriptor descriptor() { return DESCRIPTOR; }

    @Override
    public CompoundTag buildStructureNBT(Direction facing) {
        BlockState stone = Blocks.STONE_BRICKS.defaultBlockState();
        BlockState planks = Blocks.SPRUCE_PLANKS.defaultBlockState();
        BlockState logs = Blocks.SPRUCE_LOG.defaultBlockState();
        BlockState glass = Blocks.GLASS_PANE.defaultBlockState();
        BlockState slab = Blocks.SPRUCE_SLAB.defaultBlockState();
        BlockState roofNorth = Blocks.SPRUCE_STAIRS.defaultBlockState()
                .setValue(net.minecraft.world.level.block.StairBlock.FACING, Direction.NORTH);
        BlockState roofSouth = Blocks.SPRUCE_STAIRS.defaultBlockState()
                .setValue(net.minecraft.world.level.block.StairBlock.FACING, Direction.SOUTH);
        BlockState roofWest = Blocks.SPRUCE_STAIRS.defaultBlockState()
                .setValue(net.minecraft.world.level.block.StairBlock.FACING, Direction.WEST);
        BlockState roofEast = Blocks.SPRUCE_STAIRS.defaultBlockState()
                .setValue(net.minecraft.world.level.block.StairBlock.FACING, Direction.EAST);
        BlockState doorLower = Blocks.SPRUCE_DOOR.defaultBlockState()
                .setValue(net.minecraft.world.level.block.DoorBlock.FACING, Direction.SOUTH)
                .setValue(net.minecraft.world.level.block.DoorBlock.HALF, DoubleBlockHalf.LOWER);
        BlockState doorUpper = Blocks.SPRUCE_DOOR.defaultBlockState()
                .setValue(net.minecraft.world.level.block.DoorBlock.FACING, Direction.SOUTH)
                .setValue(net.minecraft.world.level.block.DoorBlock.HALF, DoubleBlockHalf.UPPER);

        BuildingStructureNbtBuilder b = BuildingStructureNbtBuilder.of(WIDTH, HEIGHT, DEPTH, facing, "bannermod:town_hall");

        // Foundation and floor.
        b.fill(0, 0, 0, 12, 0, 10, stone);
        b.fill(1, 1, 1, 11, 1, 9, planks);

        // Outer frame/walls.
        for (int y = 2; y <= 5; y++) {
            b.rect(0, 0, 12, 10, y, logs);
        }
        for (int y = 2; y <= 4; y++) {
            b.block(6, y, 0, Blocks.AIR.defaultBlockState());
        }
        b.block(6, 2, 0, doorLower);
        b.block(6, 3, 0, doorUpper);

        // Windows and front bell marker.
        b.block(3, 3, 0, glass);
        b.block(9, 3, 0, glass);
        b.block(0, 3, 3, glass);
        b.block(0, 3, 7, glass);
        b.block(12, 3, 3, glass);
        b.block(12, 3, 7, glass);
        b.block(3, 3, 10, glass);
        b.block(9, 3, 10, glass);
        b.block(6, 2, 1, Blocks.BELL.defaultBlockState());

        // Interior utility.
        b.block(4, 2, 4, Blocks.CRAFTING_TABLE.defaultBlockState());
        b.block(8, 2, 4, Blocks.CARTOGRAPHY_TABLE.defaultBlockState());
        b.block(4, 2, 7, Blocks.CHEST.defaultBlockState());
        b.block(8, 2, 7, Blocks.CHEST.defaultBlockState());
        b.block(6, 2, 6, Blocks.LECTERN.defaultBlockState());
        b.block(2, 2, 5, Blocks.WHITE_BED.defaultBlockState());
        b.block(10, 2, 5, Blocks.RED_BED.defaultBlockState());

        // Roof.
        b.fill(1, 6, 1, 11, 6, 9, planks);
        b.fill(2, 7, 2, 10, 7, 8, slab);
        for (int x = 1; x <= 11; x++) {
            b.block(x, 6, 0, roofSouth);
            b.block(x, 6, 10, roofNorth);
        }
        for (int z = 1; z <= 9; z++) {
            b.block(0, 6, z, roofWest);
            b.block(12, 6, z, roofEast);
        }
        b.block(6, 8, 5, Blocks.LANTERN.defaultBlockState());

        return b.build();
    }
}
