package com.talhanation.bannermod.settlement.prefab.validation.impl;

import com.talhanation.bannermod.settlement.prefab.BuildingPrefab;
import com.talhanation.bannermod.settlement.prefab.impl.AnimalPenPrefab;
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
 * Rule set for a player-built animal pen (animal farmer).
 */
public final class AnimalPenValidator implements BuildingValidator {
    private static final int MIN_FOOTPRINT = 5;

    @Override
    public ResourceLocation prefabId() {
        return AnimalPenPrefab.ID;
    }

    @Override
    public ValidationResult validate(BuildingPrefab prefab, ServerLevel level, AABB bounds, BuildingInspectionView view) {
        List<ValidationIssue> issues = new ArrayList<>();

        if (view.width() < MIN_FOOTPRINT || view.depth() < MIN_FOOTPRINT) {
            issues.add(ValidationIssue.blocker("bannermod.prefab.validate.animal_pen.too_small",
                    view.width(), view.depth(), MIN_FOOTPRINT));
        }

        int hay = view.countBlock(Blocks.HAY_BLOCK);
        if (hay < 1) {
            issues.add(ValidationIssue.blocker("bannermod.prefab.validate.animal_pen.no_hay"));
        }

        if (view.countBlock(Blocks.CHEST) < 1) {
            issues.add(ValidationIssue.blocker("bannermod.prefab.validate.animal_pen.no_chest"));
        }

        int fenceLayerY = view.min().getY() + 1;
        double fenceCoverage = view.perimeterCoverage(level, fenceLayerY,
                state -> state.getBlock().defaultBlockState().is(BlockTags.FENCES));
        if (fenceCoverage < 0.5) {
            issues.add(ValidationIssue.blocker("bannermod.prefab.validate.animal_pen.unfenced",
                    Math.round(fenceCoverage * 100)));
        }

        if (view.waterCount() == 0) {
            issues.add(ValidationIssue.major("bannermod.prefab.validate.animal_pen.no_water"));
        }

        int footprint = Math.max(1, view.width() * view.depth());
        double roofCoverage = (double) view.countCoveredGroundCells(level) / footprint;
        if (roofCoverage < 0.1) {
            issues.add(ValidationIssue.minor("bannermod.prefab.validate.animal_pen.exposed",
                    Math.round(roofCoverage * 100)));
        }

        if (hay >= 3) {
            issues.add(ValidationIssue.info("bannermod.prefab.validate.animal_pen.well_stocked", hay));
        }

        int score = ArchitectureScorer.score(level, view);
        boolean passed = issues.stream().noneMatch(i -> i.severity() == ValidationIssue.Severity.BLOCKER);
        return new ValidationResult(passed, score, issues);
    }
}
