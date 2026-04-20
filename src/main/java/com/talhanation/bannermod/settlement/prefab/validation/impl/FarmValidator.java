package com.talhanation.bannermod.settlement.prefab.validation.impl;

import com.talhanation.bannermod.settlement.prefab.BuildingPrefab;
import com.talhanation.bannermod.settlement.prefab.impl.FarmPrefab;
import com.talhanation.bannermod.settlement.prefab.validation.ArchitectureScorer;
import com.talhanation.bannermod.settlement.prefab.validation.BuildingInspectionView;
import com.talhanation.bannermod.settlement.prefab.validation.BuildingValidator;
import com.talhanation.bannermod.settlement.prefab.validation.ValidationIssue;
import com.talhanation.bannermod.settlement.prefab.validation.ValidationResult;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

/**
 * Rule set for a player-built farm.
 *
 * <p>Hard requirements (BLOCKER):
 * <ul>
 *   <li>Footprint ≥ 5×5 tiles.</li>
 *   <li>≥ 9 farmland blocks (a 3×3 minimum field).</li>
 *   <li>≥ 1 water source touching the farmland layer.</li>
 * </ul></p>
 *
 * <p>Soft signals (MAJOR / MINOR / INFO):
 * <ul>
 *   <li>Fenced perimeter on the farmland layer (≥60% fence coverage).</li>
 *   <li>Presence of an attending structure (chest/furnace/crafting table) nearby counts as INFO.</li>
 * </ul></p>
 */
public final class FarmValidator implements BuildingValidator {
    private static final int MIN_FOOTPRINT = 5;
    private static final int MIN_FARMLAND = 9;

    @Override
    public ResourceLocation prefabId() {
        return FarmPrefab.ID;
    }

    @Override
    public ValidationResult validate(BuildingPrefab prefab, ServerLevel level, AABB bounds, BuildingInspectionView view) {
        List<ValidationIssue> issues = new ArrayList<>();

        if (view.width() < MIN_FOOTPRINT || view.depth() < MIN_FOOTPRINT) {
            issues.add(ValidationIssue.blocker("bannermod.prefab.validate.farm.too_small",
                    view.width(), view.depth(), MIN_FOOTPRINT));
        }

        int farmland = view.countBlock(Blocks.FARMLAND);
        if (farmland < MIN_FARMLAND) {
            issues.add(ValidationIssue.blocker("bannermod.prefab.validate.farm.not_enough_farmland",
                    farmland, MIN_FARMLAND));
        } else if (farmland < 16) {
            issues.add(ValidationIssue.minor("bannermod.prefab.validate.farm.small_field", farmland));
        }

        if (view.waterCount() == 0) {
            issues.add(ValidationIssue.blocker("bannermod.prefab.validate.farm.no_water"));
        }

        int fenceLayerY = view.min().getY() + 1;
        double fenceCoverage = view.perimeterCoverage(level, fenceLayerY,
                state -> state.getBlock().defaultBlockState().is(net.minecraft.tags.BlockTags.FENCES)
                        || state.getBlock() == Blocks.OAK_FENCE
                        || state.getBlock() == Blocks.SPRUCE_FENCE
                        || state.getBlock() == Blocks.BIRCH_FENCE);
        if (fenceCoverage < 0.6) {
            issues.add(ValidationIssue.major("bannermod.prefab.validate.farm.unfenced",
                    Math.round(fenceCoverage * 100)));
        } else {
            issues.add(ValidationIssue.info("bannermod.prefab.validate.farm.fenced",
                    Math.round(fenceCoverage * 100)));
        }

        if (view.countBlock(Blocks.CHEST) + view.countBlock(Blocks.BARREL) > 0) {
            issues.add(ValidationIssue.info("bannermod.prefab.validate.farm.has_storage"));
        }

        int score = ArchitectureScorer.score(level, view);
        boolean passed = issues.stream().noneMatch(i -> i.severity() == ValidationIssue.Severity.BLOCKER);
        return new ValidationResult(passed, score, issues);
    }
}
