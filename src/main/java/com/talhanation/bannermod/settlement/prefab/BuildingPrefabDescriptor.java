package com.talhanation.bannermod.settlement.prefab;

import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

/**
 * Descriptive metadata for a {@link BuildingPrefab} — what it is and how it should be
 * shown to the player in the placement GUI.
 */
public record BuildingPrefabDescriptor(
        ResourceLocation id,
        String displayKey,
        String descriptionKey,
        int width,
        int height,
        int depth,
        BuildingPrefabProfession profession,
        String iconItemId
) {
    public BuildingPrefabDescriptor {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(displayKey, "displayKey");
        Objects.requireNonNull(descriptionKey, "descriptionKey");
        Objects.requireNonNull(profession, "profession");
        if (width <= 0 || height <= 0 || depth <= 0) {
            throw new IllegalArgumentException("prefab dimensions must be positive: "
                    + width + "x" + height + "x" + depth);
        }
    }
}
