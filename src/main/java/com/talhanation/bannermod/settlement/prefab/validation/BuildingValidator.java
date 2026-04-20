package com.talhanation.bannermod.settlement.prefab.validation;

import com.talhanation.bannermod.settlement.prefab.BuildingPrefab;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.AABB;

/**
 * Inspects a player-built structure within {@code bounds} and produces a
 * {@link ValidationResult}. Each prefab type has its own validator which knows the
 * feature checklist for that building kind.
 */
public interface BuildingValidator {
    /** Prefab id this validator covers. */
    ResourceLocation prefabId();

    /**
     * Perform validation.
     *
     * @param prefab the registered prefab (metadata only — useful for expected dimensions)
     * @param level  the server level
     * @param bounds bounding box the player marked; may be larger than the prefab's declared
     *               footprint, that's fine
     * @param view   pre-computed scan summary of the bounds
     */
    ValidationResult validate(BuildingPrefab prefab, ServerLevel level, AABB bounds, BuildingInspectionView view);
}
