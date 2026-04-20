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

    public static final ResourceLocation ID = new ResourceLocation("bannermod", "farm");

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
        BlockState log = Blocks.OAK_LOG.defaultBlockState();
        BlockState fence = Blocks.OAK_FENCE.defaultBlockState();

        BuildingStructureNbtBuilder b = BuildingStructureNbtBuilder.of(WIDTH, HEIGHT, DEPTH, facing, "bannermod:farm");

        b.fill(0, 0, 0, WIDTH - 1, 0, DEPTH - 1, dirt);

        b.rect(0, 0, WIDTH - 1, DEPTH - 1, 1, log);

        for (int x = 1; x <= WIDTH - 2; x++) {
            for (int z = 1; z <= DEPTH - 2; z++) {
                b.block(x, 1, z, farmland);
            }
        }

        b.block(WIDTH / 2, 1, DEPTH / 2, water);

        b.rect(0, 0, WIDTH - 1, DEPTH - 1, 2, fence);

        b.entity(
                ModEntityTypes.CROPAREA.get(),
                WIDTH / 2,
                1,
                DEPTH / 2,
                Direction.SOUTH,
                WIDTH - 2,
                2,
                DEPTH - 2
        );

        return b.build();
    }
}
