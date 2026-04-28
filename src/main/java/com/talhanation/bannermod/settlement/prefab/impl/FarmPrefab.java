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
 * Small procedural farm: 7×3×7 footprint, dirt floor, log border on the working layer,
 * a 5×5 farmland inner with a central water source. A {@code CropArea} entity sized to
 * the farmland interior is embedded so the completed BuildArea automatically spawns it
 * on completion, giving the auto-staffing hook a farmer-bindable work-area.
 */
public final class FarmPrefab implements BuildingPrefab {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("bannermod", "farm");

    private static final int WIDTH = 7;
    private static final int HEIGHT = 3;
    private static final int DEPTH = 7;

    private static final BuildingPrefabDescriptor DESCRIPTOR = new BuildingPrefabDescriptor(
            ID,
            "bannermod.prefab.farm.name",
            "bannermod.prefab.farm.description",
            WIDTH,
            HEIGHT,
            DEPTH,
            BuildingPrefabProfession.FARMER,
            "minecraft:wheat_seeds"
    );

    @Override
    public BuildingPrefabDescriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public CompoundTag buildStructureNBT(Direction facing) {
        BlockState dirt = Blocks.DIRT.defaultBlockState();
        BlockState farmland = Blocks.FARMLAND.defaultBlockState();
        BlockState water = Blocks.WATER.defaultBlockState();
        BlockState fence = Blocks.OAK_FENCE.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();

        BuildingStructureNbtBuilder b = BuildingStructureNbtBuilder.of(WIDTH, HEIGHT, DEPTH, facing, "bannermod:farm");

        // Foundation dirt across entire 7x7.
        b.fill(0, 0, 0, WIDTH - 1, 0, DEPTH - 1, dirt);

        // Farmland interior 5x5 at y=1 (the working layer — same as fence/gate layer).
        for (int x = 1; x <= WIDTH - 2; x++) {
            for (int z = 1; z <= DEPTH - 2; z++) {
                b.block(x, 1, z, farmland);
            }
        }

        // Water source at the centre of the farmland.
        b.block(WIDTH / 2, 1, DEPTH / 2, water);

        // Single fence perimeter at y=1 — shorter, no log layer underneath, with a proper
        // gate opening at the centre of the front edge (z=0) so the farmer can actually
        // walk in.
        b.rect(0, 0, WIDTH - 1, DEPTH - 1, 1, fence);
        // Refill the inner farmland because the fence rect overwrote the border cells.
        // (rect only writes the perimeter, so the inner farmland placed above is intact.)

        // Gate opening on the front edge at the middle column.
        b.block(WIDTH / 2, 1, 0, air);
        // Two-high clearance above the gate (y=2) — overwrite in case scanBreakArea would
        // leave a stray block there.
        b.block(WIDTH / 2, 2, 0, air);

        // Embedded CropArea.
        //
        // Positioning carefully: for scan-facing SOUTH, AbstractWorkAreaEntity.createArea
        // builds the AABB as x ∈ [start.x - (wa_width-1), start.x] and
        // z ∈ [start.z, start.z + (wa_depth-1)]. The entity must therefore sit at the
        // "back-right" corner of the target region from its own facing perspective.
        // For the farm's farmland interior (relative x=1..5, z=1..5, 5×5), the corner that
        // puts the AABB on top of the farmland is relative (5, 1, 1).
        b.entity(
                ModEntityTypes.CROPAREA.get(),
                WIDTH - 2,  // x = 5
                1,
                1,
                Direction.SOUTH,
                WIDTH - 2,  // wa_width = 5
                2,
                DEPTH - 2   // wa_depth = 5
        );

        return b.build();
    }
}
