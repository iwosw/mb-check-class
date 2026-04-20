package com.talhanation.bannermod.settlement.prefab.validation.impl;

import com.talhanation.bannermod.settlement.prefab.BuildingPrefab;
import com.talhanation.bannermod.settlement.prefab.impl.PasturePrefab;
import com.talhanation.bannermod.settlement.prefab.validation.ArchitectureScorer;
import com.talhanation.bannermod.settlement.prefab.validation.BuildingInspectionView;
import com.talhanation.bannermod.settlement.prefab.validation.BuildingValidator;
import com.talhanation.bannermod.settlement.prefab.validation.ValidationIssue;
import com.talhanation.bannermod.settlement.prefab.validation.ValidationResult;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

/**
 * Rule set for a player-built pasture (shepherd).
 */
public final class PastureValidator implements BuildingValidator {
    private static final int MIN_FOOTPRINT = 7;
    private static final int MIN_GRASS = 20;

    @Override
    public ResourceLocation prefabId() {
        return PasturePrefab.ID;
    }

    @Override
    public ValidationResult validate(BuildingPrefab prefab, ServerLevel level, AABB bounds, BuildingInspectionView view) {
        List<ValidationIssue> issues = new ArrayList<>();

        if (view.width() < MIN_FOOTPRINT || view.depth() < MIN_FOOTPRINT) {
            issues.add(ValidationIssue.blocker("bannermod.prefab.validate.pasture.too_small",
                    view.width(), view.depth(), MIN_FOOTPRINT));
        }

        int fenceLayerY = view.min().getY() + 1;
        double fenceCoverage = view.perimeterCoverage(level, fenceLayerY,
                state -> state.getBlock().defaultBlockState().is(BlockTags.FENCES));
        if (fenceCoverage < 0.5) {
            issues.add(ValidationIssue.blocker("bannermod.prefab.validate.pasture.unfenced",
                    Math.round(fenceCoverage * 100)));
        }

        int grass = view.countBlock(Blocks.GRASS_BLOCK);
        if (grass < MIN_GRASS) {
            issues.add(ValidationIssue.blocker("bannermod.prefab.validate.pasture.not_enough_grass",
                    grass, MIN_GRASS));
        }

        if (fenceCoverage > 0.95) {
            issues.add(ValidationIssue.major("bannermod.prefab.validate.pasture.no_gate"));
        }

        int beds = view.countBeds();
        int planks = view.countPlanks();
        if (beds < 1 && planks < 4) {
            issues.add(ValidationIssue.minor("bannermod.prefab.validate.pasture.no_hut"));
        }

        if (grass > 40) {
            issues.add(ValidationIssue.info("bannermod.prefab.validate.pasture.lush", grass));
        }

        int score = ArchitectureScorer.score(level, view);
        boolean passed = issues.stream().noneMatch(i -> i.severity() == ValidationIssue.Severity.BLOCKER);
        return new ValidationResult(passed, score, issues);
    }
}
