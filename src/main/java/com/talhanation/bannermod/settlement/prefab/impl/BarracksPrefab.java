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
 * Military barracks. Auto-staffing spawns a swordsman recruit by default.
 * No embedded work-area entity — barracks uses the auto-staffing hook instead.
 */
public final class BarracksPrefab implements BuildingPrefab {
    public static final ResourceLocation ID = new ResourceLocation("bannermod", "barracks");

    private static final int WIDTH = 9;
    private static final int HEIGHT = 5;
    private static final int DEPTH = 9;

    private static final BuildingPrefabDescriptor DESCRIPTOR = new BuildingPrefabDescriptor(
            ID, "bannermod.prefab.barracks.name", "bannermod.prefab.barracks.description",
            WIDTH, HEIGHT, DEPTH, BuildingPrefabProfession.RECRUIT_SWORDSMAN, "minecraft:iron_sword");

    @Override public BuildingPrefabDescriptor descriptor() { return DESCRIPTOR; }

    @Override
    public CompoundTag buildStructureNBT(Direction facing) {
        BlockState bricks = Blocks.STONE_BRICKS.defaultBlockState();
        BlockState wall = Blocks.COBBLESTONE_WALL.defaultBlockState();
        BlockState bed = Blocks.RED_BED.defaultBlockState();
        BlockState chest = Blocks.CHEST.defaultBlockState();
        BlockState table = Blocks.CRAFTING_TABLE.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();

        BuildingStructureNbtBuilder b = BuildingStructureNbtBuilder.of(WIDTH, HEIGHT, DEPTH, facing, "bannermod:barracks");

        // Floor 9×9 stone bricks.
        b.fill(0, 0, 0, 8, 0, 8, bricks);

        // Stone brick walls y=1..3 around perimeter.
        for (int y = 1; y <= 3; y++) {
            b.rect(0, 0, 8, 8, y, bricks);
        }

        // Door opening at (4, 1, 0) and (4, 2, 0) — overwrite with AIR for a passable gap.
        b.block(4, 1, 0, air);
        b.block(4, 2, 0, air);

        // Flat stone brick roof at y=4.
        b.fill(0, 4, 0, 8, 4, 8, bricks);

        // Arrow slits — cobblestone walls at listed positions (overwrite the stone brick).
        b.block(2, 3, 0, wall);
        b.block(6, 3, 0, wall);
        b.block(0, 3, 2, wall);
        b.block(0, 3, 6, wall);
        b.block(8, 3, 2, wall);
        b.block(8, 3, 6, wall);

        // Three beds along the back wall (x=2, 4, 6 at z=7).
        b.block(2, 1, 7, bed);
        b.block(4, 1, 7, bed);
        b.block(6, 1, 7, bed);

        // Armory chest and crafting table.
        b.block(4, 1, 4, chest);
        b.block(6, 1, 4, table);

        // No embedded work-area entity — recruits are spawned via the auto-staffing hook.
        return b.build();
    }
}
