package com.talhanation.bannermod.settlement.prefab;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Objects;

/**
 * Helper that builds a BuildArea-compatible STRUCTURE CompoundTag programmatically.
 *
 * <p>The tag format is the same one consumed by {@code BuildArea.setStartBuild(...)} and
 * {@code BuildArea.spawnScannedEntities(...)}:</p>
 *
 * <pre>
 * root = {
 *   name: "prefab:farm",
 *   width: W, height: H, depth: D,
 *   facing: "south",
 *   blocks: [ { x, y, z, state: {...} } ],
 *   entities: [ { entity_type, x, y, z, facing, wa_width, wa_height, wa_depth } ]
 * }
 * </pre>
 *
 * <p>Coordinate convention: (x, y, z) are <em>relative</em> indices inside the prefab's
 * bounding box. BuildArea rotates them at placement time to match the player's chosen
 * facing.</p>
 */
public final class BuildingStructureNbtBuilder {
    private final CompoundTag root = new CompoundTag();
    private final ListTag blocks = new ListTag();
    private final ListTag entities = new ListTag();

    private BuildingStructureNbtBuilder(int width, int height, int depth, Direction facing, String name) {
        this.root.putInt("width", width);
        this.root.putInt("height", height);
        this.root.putInt("depth", depth);
        this.root.putString("facing", Objects.requireNonNullElse(facing, Direction.SOUTH).getName());
        if (name != null && !name.isBlank()) {
            this.root.putString("name", name);
        }
        this.root.put("blocks", this.blocks);
        this.root.put("entities", this.entities);
    }

    public static BuildingStructureNbtBuilder of(int width, int height, int depth, Direction facing) {
        return new BuildingStructureNbtBuilder(width, height, depth, facing, null);
    }

    public static BuildingStructureNbtBuilder of(int width, int height, int depth, Direction facing, String name) {
        return new BuildingStructureNbtBuilder(width, height, depth, facing, name);
    }

    public BuildingStructureNbtBuilder block(int x, int y, int z, BlockState state) {
        Objects.requireNonNull(state, "state");
        CompoundTag tag = new CompoundTag();
        tag.putInt("x", x);
        tag.putInt("y", y);
        tag.putInt("z", z);
        tag.put("state", NbtUtils.writeBlockState(state));
        this.blocks.add(tag);
        return this;
    }

    public BuildingStructureNbtBuilder block(BlockPos relativePos, BlockState state) {
        Objects.requireNonNull(relativePos, "relativePos");
        return block(relativePos.getX(), relativePos.getY(), relativePos.getZ(), state);
    }

    /** Fill a rectangular cuboid with the same block state. Inclusive on all bounds. */
    public BuildingStructureNbtBuilder fill(int x0, int y0, int z0, int x1, int y1, int z1, BlockState state) {
        int xmin = Math.min(x0, x1);
        int ymin = Math.min(y0, y1);
        int zmin = Math.min(z0, z1);
        int xmax = Math.max(x0, x1);
        int ymax = Math.max(y0, y1);
        int zmax = Math.max(z0, z1);
        for (int x = xmin; x <= xmax; x++) {
            for (int y = ymin; y <= ymax; y++) {
                for (int z = zmin; z <= zmax; z++) {
                    block(x, y, z, state);
                }
            }
        }
        return this;
    }

    /** Draw a hollow rectangle at the given y. */
    public BuildingStructureNbtBuilder rect(int x0, int z0, int x1, int z1, int y, BlockState state) {
        int xmin = Math.min(x0, x1);
        int zmin = Math.min(z0, z1);
        int xmax = Math.max(x0, x1);
        int zmax = Math.max(z0, z1);
        for (int x = xmin; x <= xmax; x++) {
            block(x, y, zmin, state);
            block(x, y, zmax, state);
        }
        for (int z = zmin + 1; z < zmax; z++) {
            block(xmin, y, z, state);
            block(xmax, y, z, state);
        }
        return this;
    }

    /** Embed a work-area entity that BuildArea will spawn on completion. */
    public BuildingStructureNbtBuilder entity(EntityType<?> type,
                                              int x, int y, int z,
                                              Direction scanFacing,
                                              int waWidth, int waHeight, int waDepth) {
        Objects.requireNonNull(type, "type");
        CompoundTag tag = new CompoundTag();
        tag.putString("entity_type",
                Objects.requireNonNull(BuiltInRegistries.ENTITY_TYPE.getKey(type)).toString());
        tag.putInt("x", x);
        tag.putInt("y", y);
        tag.putInt("z", z);
        tag.putInt("facing", Objects.requireNonNullElse(scanFacing, Direction.SOUTH).get2DDataValue());
        tag.putInt("wa_width", waWidth);
        tag.putInt("wa_height", waHeight);
        tag.putInt("wa_depth", waDepth);
        this.entities.add(tag);
        return this;
    }

    public CompoundTag build() {
        return root.copy();
    }
}
