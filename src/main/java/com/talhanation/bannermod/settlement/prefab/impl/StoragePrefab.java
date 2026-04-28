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
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

/**
 * Storage warehouse — log shell with a door opening, a flat plank roof, and a 3×3
 * grid of chests inside. Embeds a {@code StorageArea} so settlement systems can
 * discover it as a deposit target even though no worker is assigned.
 */
public final class StoragePrefab implements BuildingPrefab {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("bannermod", "storage");

    private static final int WIDTH = 9;
    private static final int HEIGHT = 7;
    private static final int DEPTH = 9;

    private static final BuildingPrefabDescriptor DESCRIPTOR = new BuildingPrefabDescriptor(
            ID, "bannermod.prefab.storage.name", "bannermod.prefab.storage.description",
            WIDTH, HEIGHT, DEPTH, BuildingPrefabProfession.NONE, "minecraft:chest");

    @Override public BuildingPrefabDescriptor descriptor() { return DESCRIPTOR; }

    @Override
    public CompoundTag buildStructureNBT(Direction facing) {
        BlockState planks = Blocks.OAK_PLANKS.defaultBlockState();
        BlockState log = Blocks.STRIPPED_OAK_LOG.defaultBlockState();
        BlockState stone = Blocks.STONE_BRICKS.defaultBlockState();
        BlockState chest = Blocks.CHEST.defaultBlockState();
        BlockState stairSouth = Blocks.OAK_STAIRS.defaultBlockState().setValue(net.minecraft.world.level.block.StairBlock.FACING, Direction.SOUTH);
        BlockState stairNorth = Blocks.OAK_STAIRS.defaultBlockState().setValue(net.minecraft.world.level.block.StairBlock.FACING, Direction.NORTH);
        BlockState slab = Blocks.OAK_SLAB.defaultBlockState();
        BlockState glass = Blocks.GLASS_PANE.defaultBlockState();
        BlockState doorLower = Blocks.OAK_DOOR.defaultBlockState()
                .setValue(net.minecraft.world.level.block.DoorBlock.FACING, Direction.SOUTH)
                .setValue(net.minecraft.world.level.block.DoorBlock.HALF, DoubleBlockHalf.LOWER);
        BlockState doorUpper = Blocks.OAK_DOOR.defaultBlockState()
                .setValue(net.minecraft.world.level.block.DoorBlock.FACING, Direction.SOUTH)
                .setValue(net.minecraft.world.level.block.DoorBlock.HALF, DoubleBlockHalf.UPPER);

        BuildingStructureNbtBuilder b = BuildingStructureNbtBuilder.of(WIDTH, HEIGHT, DEPTH, facing, "bannermod:storage");

        // Foundation + floor.
        b.fill(0, 0, 0, 8, 0, 8, stone);
        b.fill(1, 1, 1, 7, 1, 7, planks);

        // Log walls.
        for (int y = 2; y <= 4; y++) {
            b.rect(0, 0, 8, 8, y, log);
        }

        // Door opening + proper door blocks.
        b.block(4, 2, 0, doorLower);
        b.block(4, 3, 0, doorUpper);

        // Windows.
        b.block(2, 3, 0, glass);
        b.block(6, 3, 0, glass);
        b.block(0, 3, 2, glass);
        b.block(0, 3, 6, glass);
        b.block(8, 3, 2, glass);
        b.block(8, 3, 6, glass);

        // Solid roof core with stair trim.
        b.fill(1, 5, 1, 7, 5, 7, planks);
        b.fill(2, 6, 2, 6, 6, 6, slab);
        for (int x = 1; x <= 7; x++) {
            b.block(x, 5, 0, stairSouth);
            b.block(x, 5, 8, stairNorth);
        }
        for (int z = 1; z <= 7; z++) {
            b.block(0, 5, z, stairSouth);
            b.block(8, 5, z, stairNorth);
        }

        // Chest rows with walkable aisle.
        int[] xs = { 2, 6 };
        int[] zs = { 2, 4, 6 };
        for (int cx : xs) {
            for (int cz : zs) {
                b.block(cx, 2, cz, chest);
            }
        }

        // Embedded StorageArea at center.
        b.entity(
                ModEntityTypes.STORAGEAREA.get(),
                4, 2, 4,
                Direction.SOUTH,
                9, 4, 9
        );

        return b.build();
    }
}
