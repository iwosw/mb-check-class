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
 * Procedural animal pen: 9×4×9 grass field with a 4×4×4 oak-log barn in one corner
 * (hay bales + chest inside) and a fenced outdoor enclosure with a central water
 * trough. An {@code AnimalPenArea} entity is embedded at the enclosure center.
 */
public final class AnimalPenPrefab implements BuildingPrefab {
    public static final ResourceLocation ID = new ResourceLocation("bannermod", "animal_pen");

    private static final int WIDTH = 9;
    private static final int HEIGHT = 4;
    private static final int DEPTH = 9;

    private static final BuildingPrefabDescriptor DESCRIPTOR = new BuildingPrefabDescriptor(
            ID, "bannermod.prefab.animal_pen.name", "bannermod.prefab.animal_pen.description",
            WIDTH, HEIGHT, DEPTH, BuildingPrefabProfession.ANIMAL_FARMER, "minecraft:egg");

    @Override public BuildingPrefabDescriptor descriptor() { return DESCRIPTOR; }

    @Override
    public CompoundTag buildStructureNBT(Direction facing) {
        BlockState grass = Blocks.GRASS_BLOCK.defaultBlockState();
        BlockState planks = Blocks.OAK_PLANKS.defaultBlockState();
        BlockState log = Blocks.OAK_LOG.defaultBlockState();
        BlockState fence = Blocks.OAK_FENCE.defaultBlockState();
        BlockState hay = Blocks.HAY_BLOCK.defaultBlockState();
        BlockState chest = Blocks.CHEST.defaultBlockState();
        BlockState water = Blocks.WATER.defaultBlockState();

        BuildingStructureNbtBuilder b = BuildingStructureNbtBuilder.of(WIDTH, HEIGHT, DEPTH, facing, "bannermod:animal_pen");

        // Grass floor across the full 9x9 footprint.
        b.fill(0, 0, 0, WIDTH - 1, 0, DEPTH - 1, grass);

        // Barn at corner (0..3, 0..3).
        // Interior floor: oak planks at y=0 (overrides grass inside the barn).
        b.fill(0, 0, 0, 3, 0, 3, planks);

        // Walls y=1..2 (hollow rectangle per layer) using oak logs.
        for (int y = 1; y <= 2; y++) {
            b.rect(0, 0, 3, 3, y, log);
        }

        // Roof planks at y=3 over the full 4x4 barn footprint.
        b.fill(0, 3, 0, 3, 3, 3, planks);

        // Interior furnishings: hay bales + chest.
        b.block(1, 1, 1, hay);
        b.block(2, 1, 1, hay);
        b.block(1, 1, 2, chest);

        // Fenced outdoor perimeter at y=1 around the full 9x9 border.
        b.rect(0, 0, WIDTH - 1, DEPTH - 1, 1, fence);

        // Gate opening on the front at (5,1,0).
        b.block(5, 1, 0, Blocks.AIR.defaultBlockState());

        // Clear any fence cells that overlap the barn walls so the barn walls (logs)
        // remain the actual structure on that edge. The rect above placed fences along
        // the full border including x=0..3 at z=0 and x=0 at z=0..3 where the barn
        // wall already lives; overwrite those cells back to oak log.
        for (int x = 0; x <= 3; x++) {
            b.block(x, 1, 0, log);
        }
        for (int z = 0; z <= 3; z++) {
            b.block(0, 1, z, log);
        }

        // Watering trough at the center of the outdoor pen.
        b.block(5, 1, 5, water);

        // Embedded AnimalPenArea entity centered on the trough.
        b.entity(
                ModEntityTypes.ANIMAL_PEN_AREA.get(),
                5, 1, 5,
                Direction.SOUTH,
                8, 3, 8
        );

        return b.build();
    }
}
