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
 * Merchant market stall — open-air pavilion with four corner pillars, a plank roof,
 * a central display counter and lanterns on top of each pillar. A {@code MarketArea}
 * is embedded so auto-staffing can bind a merchant.
 */
public final class MarketStallPrefab implements BuildingPrefab {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("bannermod", "market_stall");

    private static final int WIDTH = 7;
    private static final int HEIGHT = 5;
    private static final int DEPTH = 7;

    private static final BuildingPrefabDescriptor DESCRIPTOR = new BuildingPrefabDescriptor(
            ID, "bannermod.prefab.market_stall.name", "bannermod.prefab.market_stall.description",
            WIDTH, HEIGHT, DEPTH, BuildingPrefabProfession.MERCHANT, "minecraft:emerald");

    @Override public BuildingPrefabDescriptor descriptor() { return DESCRIPTOR; }

    @Override
    public CompoundTag buildStructureNBT(Direction facing) {
        // NOTE: 1.20.1 Blocks class has no STRIPPED_OAK_PLANKS constant — spec explicitly
        // permits falling back to OAK_PLANKS for the roof.
        BlockState planks = Blocks.OAK_PLANKS.defaultBlockState();
        BlockState log = Blocks.OAK_LOG.defaultBlockState();
        BlockState wool = Blocks.WHITE_WOOL.defaultBlockState();
        BlockState lantern = Blocks.LANTERN.defaultBlockState();

        BuildingStructureNbtBuilder b = BuildingStructureNbtBuilder.of(WIDTH, HEIGHT, DEPTH, facing, "bannermod:market_stall");

        // Floor — oak planks 7×7.
        b.fill(0, 0, 0, 6, 0, 6, planks);

        // Four corner pillars at y=1..3.
        for (int y = 1; y <= 3; y++) {
            b.block(0, y, 0, log);
            b.block(6, y, 0, log);
            b.block(0, y, 6, log);
            b.block(6, y, 6, log);
        }

        // Open-air roof at y=4 covering full 7×7.
        b.fill(0, 4, 0, 6, 4, 6, planks);

        // Merchant counter: central log with wool display pedestals around it.
        b.block(3, 1, 3, log);
        b.block(2, 1, 3, wool);
        b.block(4, 1, 3, wool);
        b.block(3, 1, 2, wool);
        b.block(3, 1, 4, wool);

        // Lanterns on top of each pillar (on top of the roof plank at that corner).
        // Roof at y=4 already occupies corners, so the lantern sits... the spec says "on top
        // of corner pillars at (0,4,0)" etc., overwriting the corner roof tile with a lantern.
        b.block(0, 4, 0, lantern);
        b.block(6, 4, 0, lantern);
        b.block(0, 4, 6, lantern);
        b.block(6, 4, 6, lantern);

        // MarketArea embedded at the counter.
        b.entity(
                ModEntityTypes.MARKETAREA.get(),
                3, 1, 3,
                Direction.SOUTH,
                5, 3, 5
        );

        return b.build();
    }
}
