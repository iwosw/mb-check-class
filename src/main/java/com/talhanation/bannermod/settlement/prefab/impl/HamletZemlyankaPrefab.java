package com.talhanation.bannermod.settlement.prefab.impl;

import com.talhanation.bannermod.bootstrap.WorkersRuntime;
import com.talhanation.bannermod.persistence.civilian.StructureTemplateLoader;
import com.talhanation.bannermod.settlement.prefab.BuildingPrefab;
import com.talhanation.bannermod.settlement.prefab.BuildingPrefabDescriptor;
import com.talhanation.bannermod.settlement.prefab.BuildingPrefabProfession;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.io.InputStream;

public final class HamletZemlyankaPrefab implements BuildingPrefab {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("bannermod", "hamlet_zemlyanka");
    private static final String TEMPLATE_RESOURCE = "assets/bannermod/structures/zemlyanka.nbt";
    private static final CompoundTag TEMPLATE = loadTemplate();
    private static final int SIDE_PADDING = 3;
    private static final int FRONT_PADDING = 4;
    private static final int BACK_PADDING = 2;
    private static final int LOT_WIDTH = TEMPLATE.getInt("width") + SIDE_PADDING * 2;
    private static final int LOT_DEPTH = TEMPLATE.getInt("depth") + FRONT_PADDING + BACK_PADDING;
    private static final int LOT_HEIGHT = Math.max(TEMPLATE.getInt("height"), 4);
    private static final int HOUSE_OFFSET_X = SIDE_PADDING;
    private static final int HOUSE_OFFSET_Z = BACK_PADDING;
    private static final BuildingPrefabDescriptor DESCRIPTOR = new BuildingPrefabDescriptor(
            ID,
            "bannermod.prefab.hamlet_zemlyanka.name",
            "bannermod.prefab.hamlet_zemlyanka.description",
            LOT_WIDTH,
            LOT_HEIGHT,
            LOT_DEPTH,
            BuildingPrefabProfession.NONE,
            "minecraft:oak_door"
    );

    @Override
    public BuildingPrefabDescriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public CompoundTag buildStructureNBT(Direction facing) {
        CompoundTag structure = TEMPLATE.copy();
        structure.putInt("width", LOT_WIDTH);
        structure.putInt("height", LOT_HEIGHT);
        structure.putInt("depth", LOT_DEPTH);
        ListTag shifted = new ListTag();
        for (Tag tag : structure.getList("blocks", Tag.TAG_COMPOUND)) {
            CompoundTag block = ((CompoundTag) tag).copy();
            block.putInt("x", block.getInt("x") + HOUSE_OFFSET_X);
            block.putInt("z", block.getInt("z") + HOUSE_OFFSET_Z);
            shifted.add(block);
        }
        structure.put("blocks", shifted);
        appendFencedLot(structure.getList("blocks", Tag.TAG_COMPOUND));
        return structure;
    }

    private static void appendFencedLot(ListTag blocks) {
        BlockState fence = Blocks.OAK_FENCE.defaultBlockState();
        BlockState gate = Blocks.OAK_FENCE_GATE.defaultBlockState();
        BlockState coarseDirt = Blocks.COARSE_DIRT.defaultBlockState();
        int gateX = LOT_WIDTH / 2;

        for (int x = 0; x < LOT_WIDTH; x++) {
            addBlock(blocks, x, 0, 0, coarseDirt);
            addBlock(blocks, x, 0, LOT_DEPTH - 1, coarseDirt);
            if (x != gateX && x != gateX - 1) {
                addBlock(blocks, x, 1, 0, fence);
            }
            addBlock(blocks, x, 1, LOT_DEPTH - 1, fence);
        }
        addBlock(blocks, gateX, 1, 0, gate);
        addBlock(blocks, gateX - 1, 1, 0, gate);

        for (int z = 1; z < LOT_DEPTH - 1; z++) {
            addBlock(blocks, 0, 0, z, coarseDirt);
            addBlock(blocks, LOT_WIDTH - 1, 0, z, coarseDirt);
            addBlock(blocks, 0, 1, z, fence);
            addBlock(blocks, LOT_WIDTH - 1, 1, z, fence);
        }

        int penStartX = LOT_WIDTH - 4;
        int penEndX = LOT_WIDTH - 2;
        int penStartZ = 2;
        int penEndZ = Math.min(LOT_DEPTH - 3, HOUSE_OFFSET_Z + Math.max(3, TEMPLATE.getInt("depth") / 2));
        for (int z = penStartZ; z <= penEndZ; z++) {
            addBlock(blocks, penStartX, 1, z, fence);
        }
        for (int x = penStartX; x <= penEndX; x++) {
            addBlock(blocks, x, 1, penStartZ, fence);
            addBlock(blocks, x, 1, penEndZ, fence);
        }
    }

    private static void addBlock(ListTag blocks, int x, int y, int z, BlockState state) {
        CompoundTag block = new CompoundTag();
        block.putInt("x", x);
        block.putInt("y", y);
        block.putInt("z", z);
        block.put("state", NbtUtils.writeBlockState(state));
        block.putString("block", BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString());
        blocks.add(block);
    }

    private static CompoundTag loadTemplate() {
        try (InputStream input = HamletZemlyankaPrefab.class.getClassLoader().getResourceAsStream(TEMPLATE_RESOURCE)) {
            CompoundTag template = StructureTemplateLoader.loadTemplate(input, "zemlyanka.nbt", WorkersRuntime::migrateStructureNbt);
            if (template == null || !template.contains("blocks")) {
                throw new IllegalStateException("Failed to load hamlet template from " + TEMPLATE_RESOURCE);
            }
            return template;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load hamlet template from " + TEMPLATE_RESOURCE, e);
        }
    }
}
