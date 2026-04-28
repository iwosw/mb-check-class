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
 * Procedural pasture: 11×3×11 fenced field with a small 4×3×4 shepherd hut in one
 * corner and a centered {@code AnimalPenArea} work-area entity. The fence perimeter
 * has a gate opening on the front edge at (5,1,0).
 */
public final class PasturePrefab implements BuildingPrefab {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("bannermod", "pasture");

    private static final int WIDTH = 11;
    private static final int HEIGHT = 3;
    private static final int DEPTH = 11;

    private static final BuildingPrefabDescriptor DESCRIPTOR = new BuildingPrefabDescriptor(
            ID, "bannermod.prefab.pasture.name", "bannermod.prefab.pasture.description",
            WIDTH, HEIGHT, DEPTH, BuildingPrefabProfession.SHEPHERD, "minecraft:shears");

    @Override public BuildingPrefabDescriptor descriptor() { return DESCRIPTOR; }

    @Override
    public CompoundTag buildStructureNBT(Direction facing) {
        BlockState grass = Blocks.GRASS_BLOCK.defaultBlockState();
        BlockState fence = Blocks.OAK_FENCE.defaultBlockState();
        BlockState planks = Blocks.OAK_PLANKS.defaultBlockState();
        BlockState log = Blocks.OAK_LOG.defaultBlockState();

        BuildingStructureNbtBuilder b = BuildingStructureNbtBuilder.of(WIDTH, HEIGHT, DEPTH, facing, "bannermod:pasture");

        // Grass floor across the full 11x11 footprint.
        b.fill(0, 0, 0, WIDTH - 1, 0, DEPTH - 1, grass);

        // Fenced perimeter at y=1.
        b.rect(0, 0, WIDTH - 1, DEPTH - 1, 1, fence);

        // Gate opening at (5,1,0) — carve the fence back to air.
        b.block(5, 1, 0, Blocks.AIR.defaultBlockState());

        // Shepherd hut at corner (0..3, 0..3).
        // Interior floor planks at y=0 (override grass inside the hut).
        b.fill(0, 0, 0, 3, 0, 3, planks);

        // Walls y=1..2 (hollow rectangle per layer).
        for (int y = 1; y <= 2; y++) {
            b.rect(0, 0, 3, 3, y, log);
        }

        // Open front at z=3: clear the doorway slots.
        b.block(2, 1, 3, Blocks.AIR.defaultBlockState());
        b.block(2, 2, 3, Blocks.AIR.defaultBlockState());

        // Roof planks at y=2 filling the hut footprint (caps the hut).
        // Spec says "y=2: oak_planks roof" — we fill the top layer (y=2 interior) with planks.
        // The rect call above already placed logs on the border of y=2; now fill the inner
        // cells with planks to form a flat roof.
        b.fill(1, 2, 1, 2, 2, 2, planks);

        // Embedded AnimalPenArea entity centered in the pasture.
        b.entity(
                ModEntityTypes.ANIMAL_PEN_AREA.get(),
                5, 1, 5,
                Direction.SOUTH,
                12, 4, 12
        );

        return b.build();
    }
}
