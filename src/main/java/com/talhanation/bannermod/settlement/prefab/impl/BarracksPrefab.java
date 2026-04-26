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
 * Military barracks. Auto-staffing spawns a swordsman recruit by default.
 * No embedded work-area entity — barracks uses the auto-staffing hook instead.
 */
public final class BarracksPrefab implements BuildingPrefab {
    public static final ResourceLocation ID = new ResourceLocation("bannermod", "barracks");

    private static final int WIDTH = 11;
    private static final int HEIGHT = 8;
    private static final int DEPTH = 9;

    private static final BuildingPrefabDescriptor DESCRIPTOR = new BuildingPrefabDescriptor(
            ID, "bannermod.prefab.barracks.name", "bannermod.prefab.barracks.description",
            WIDTH, HEIGHT, DEPTH, BuildingPrefabProfession.RECRUIT_SWORDSMAN, "minecraft:iron_sword");

    @Override public BuildingPrefabDescriptor descriptor() { return DESCRIPTOR; }

    @Override
    public CompoundTag buildStructureNBT(Direction facing) {
        BlockState planks = Blocks.SPRUCE_PLANKS.defaultBlockState();
        BlockState logs = Blocks.SPRUCE_LOG.defaultBlockState();
        BlockState stone = Blocks.STONE_BRICKS.defaultBlockState();
        BlockState cobble = Blocks.COBBLESTONE.defaultBlockState();
        BlockState glass = Blocks.GLASS_PANE.defaultBlockState();
        BlockState slab = Blocks.SPRUCE_SLAB.defaultBlockState();
        BlockState roofNorth = Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(net.minecraft.world.level.block.StairBlock.FACING, Direction.NORTH);
        BlockState roofSouth = Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(net.minecraft.world.level.block.StairBlock.FACING, Direction.SOUTH);
        BlockState bedWhite = Blocks.WHITE_BED.defaultBlockState();
        BlockState bedRed = Blocks.RED_BED.defaultBlockState();
        BlockState doorLower = Blocks.SPRUCE_DOOR.defaultBlockState()
                .setValue(net.minecraft.world.level.block.DoorBlock.FACING, Direction.SOUTH)
                .setValue(net.minecraft.world.level.block.DoorBlock.HALF, DoubleBlockHalf.LOWER);
        BlockState doorUpper = Blocks.SPRUCE_DOOR.defaultBlockState()
                .setValue(net.minecraft.world.level.block.DoorBlock.FACING, Direction.SOUTH)
                .setValue(net.minecraft.world.level.block.DoorBlock.HALF, DoubleBlockHalf.UPPER);
        BlockState chest = Blocks.CHEST.defaultBlockState();
        BlockState table = Blocks.CRAFTING_TABLE.defaultBlockState();
        BlockState lantern = Blocks.LANTERN.defaultBlockState();

        BuildingStructureNbtBuilder b = BuildingStructureNbtBuilder.of(WIDTH, HEIGHT, DEPTH, facing, "bannermod:barracks");

        // Foundation + floor (two-layer so the build never "hangs" visually).
        b.fill(0, 0, 0, 10, 0, 8, cobble);
        b.fill(1, 1, 1, 9, 1, 7, planks);

        // Timber frame walls with stone corners.
        for (int y = 2; y <= 4; y++) {
            b.rect(0, 0, 10, 8, y, logs);
        }
        for (int y = 1; y <= 3; y++) {
            b.block(0, y, 0, stone);
            b.block(10, y, 0, stone);
            b.block(0, y, 8, stone);
            b.block(10, y, 8, stone);
        }

        // Door and front stoop.
        b.block(5, 2, 0, doorLower);
        b.block(5, 3, 0, doorUpper);
        b.block(5, 1, 0, stone);

        // Windows.
        b.block(3, 3, 0, glass);
        b.block(7, 3, 0, glass);
        b.block(0, 3, 2, glass);
        b.block(0, 3, 6, glass);
        b.block(10, 3, 2, glass);
        b.block(10, 3, 6, glass);
        b.block(3, 3, 8, glass);
        b.block(7, 3, 8, glass);

        // Solid roof shell (no holes): full plank deck + decorative stair edges.
        b.fill(1, 5, 1, 9, 5, 7, planks);
        b.fill(2, 6, 2, 8, 6, 6, slab);
        for (int x = 1; x <= 9; x++) {
            b.block(x, 5, 0, roofSouth);
            b.block(x, 5, 8, roofNorth);
        }
        for (int z = 1; z <= 7; z++) {
            b.block(0, 5, z, roofSouth);
            b.block(10, 5, z, roofNorth);
        }

        // Proper sleeping rows.
        b.block(2, 2, 6, bedWhite);
        b.block(2, 2, 4, bedRed);
        b.block(8, 2, 6, bedWhite);
        b.block(8, 2, 4, bedRed);

        // Armory area and center table.
        b.block(5, 2, 4, table);
        b.block(4, 2, 2, chest);
        b.block(6, 2, 2, chest);
        b.block(5, 4, 4, lantern);

        // No embedded work-area entity — recruits are spawned via the auto-staffing hook.
        return b.build();
    }
}
