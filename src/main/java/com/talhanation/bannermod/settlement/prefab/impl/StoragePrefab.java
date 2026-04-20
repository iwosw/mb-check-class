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
 * Storage warehouse — log shell with a door opening, a flat plank roof, and a 3×3
 * grid of chests inside. Embeds a {@code StorageArea} so settlement systems can
 * discover it as a deposit target even though no worker is assigned.
 */
public final class StoragePrefab implements BuildingPrefab {
    public static final ResourceLocation ID = new ResourceLocation("bannermod", "storage");

    private static final int WIDTH = 7;
    private static final int HEIGHT = 5;
    private static final int DEPTH = 7;

    private static final BuildingPrefabDescriptor DESCRIPTOR = new BuildingPrefabDescriptor(
            ID, "bannermod.prefab.storage.name", "bannermod.prefab.storage.description",
            WIDTH, HEIGHT, DEPTH, BuildingPrefabProfession.NONE, "minecraft:chest");

    @Override public BuildingPrefabDescriptor descriptor() { return DESCRIPTOR; }

    @Override
    public CompoundTag buildStructureNBT(Direction facing) {
        BlockState planks = Blocks.OAK_PLANKS.defaultBlockState();
        BlockState log = Blocks.OAK_LOG.defaultBlockState();
        BlockState chest = Blocks.CHEST.defaultBlockState();

        BuildingStructureNbtBuilder b = BuildingStructureNbtBuilder.of(WIDTH, HEIGHT, DEPTH, facing, "bannermod:storage");

        // Floor 7×7.
        b.fill(0, 0, 0, 6, 0, 6, planks);

        // Log walls y=1..3 around perimeter.
        for (int y = 1; y <= 3; y++) {
            b.rect(0, 0, 6, 6, y, log);
        }

        // Cut door opening at (3, 1, 0) and (3, 2, 0) — overwrite with plank floor passage
        // but spec says "omit those two log blocks". We write OAK_PLANKS.defaultBlockState()
        // no — BuildArea interprets each entry as a block to place. "Omit" means we must not
        // emit a block entry for those coordinates at all. Since rect already emitted them,
        // we overwrite with AIR so the resulting state is an opening.
        b.block(3, 1, 0, Blocks.AIR.defaultBlockState());
        b.block(3, 2, 0, Blocks.AIR.defaultBlockState());

        // Flat plank roof at y=4.
        b.fill(0, 4, 0, 6, 4, 6, planks);

        // 3×3 chest grid at y=1.
        int[] xs = { 1, 3, 5 };
        int[] zs = { 1, 3, 5 };
        for (int cx : xs) {
            for (int cz : zs) {
                b.block(cx, 1, cz, chest);
            }
        }

        // Embedded StorageArea at center.
        b.entity(
                ModEntityTypes.STORAGEAREA.get(),
                3, 1, 3,
                Direction.SOUTH,
                7, 3, 7
        );

        return b.build();
    }
}
