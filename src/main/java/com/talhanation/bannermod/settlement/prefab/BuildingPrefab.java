package com.talhanation.bannermod.settlement.prefab;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

/**
 * A placeable, procedurally- or data-defined building.
 *
 * <p>Each implementation produces a {@code BuildArea} STRUCTURE CompoundTag on demand via
 * {@link #buildStructureNBT(Direction)}. The NBT is the same format the existing
 * {@code BuildArea.setStartBuild(...)} already consumes:</p>
 *
 * <pre>
 * {
 *   width: int,
 *   height: int,
 *   depth: int,
 *   facing: "south" | ...,
 *   blocks: [ { x, y, z, state: {...} } ],
 *   entities: [ { entity_type, x, y, z, facing, wa_width, wa_height, wa_depth } ]
 * }
 * </pre>
 *
 * <p>Embedding a work-area entity (CropArea, LumberArea, MiningArea, etc.) in
 * {@code entities} lets {@code BuildArea.spawnScannedEntities(...)} populate the
 * finished building with the right staffing target automatically.</p>
 */
public interface BuildingPrefab {
    BuildingPrefabDescriptor descriptor();

    /**
     * Convenience for {@code descriptor().id()}.
     */
    default ResourceLocation id() {
        return descriptor().id();
    }

    /**
     * Produce a BuildArea-compatible STRUCTURE CompoundTag for this prefab at the requested
     * facing. The returned tag is transient and safe to mutate further if needed.
     */
    CompoundTag buildStructureNBT(Direction facing);
}
