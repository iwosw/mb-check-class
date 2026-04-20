package com.talhanation.bannermod.settlement.prefab.validation.impl;

import com.talhanation.bannermod.settlement.prefab.BuildingPrefab;
import com.talhanation.bannermod.settlement.prefab.impl.HousePrefab;
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
 * Rule set for a player-built house / home.
 */
public final class HouseValidator implements BuildingValidator {
    private static final int MIN_FOOTPRINT = 5;

    @Override
    public ResourceLocation prefabId() {
        return HousePrefab.ID;
    }

    @Override
    public ValidationResult validate(BuildingPrefab prefab, ServerLevel level, AABB bounds, BuildingInspectionView view) {
        List<ValidationIssue> issues = new ArrayList<>();

        if (view.width() < MIN_FOOTPRINT || view.depth() < MIN_FOOTPRINT) {
            issues.add(ValidationIssue.blocker("bannermod.prefab.validate.house.too_small",
                    view.width(), view.depth(), MIN_FOOTPRINT));
        }

        int beds = view.countBeds();
        if (beds < 1) {
            issues.add(ValidationIssue.blocker("bannermod.prefab.validate.house.no_bed"));
        }

        int wallLayerY = view.min().getY() + 1;
        double wallCoverage = view.perimeterCoverage(level, wallLayerY,
                state -> !state.isAir());
        int doors = view.countDoors();
        if (wallCoverage >= 0.95 && doors < 1) {
            issues.add(ValidationIssue.blocker("bannermod.prefab.validate.house.sealed"));
        }

        int footprint = Math.max(1, view.width() * view.depth());
        double roofCoverage = (double) view.countCoveredGroundCells(level) / footprint;
        if (roofCoverage < 0.6) {
            issues.add(ValidationIssue.blocker("bannermod.prefab.validate.house.no_roof",
                    Math.round(roofCoverage * 100)));
        }

        int glass = view.countWhere(b -> b == Blocks.GLASS || b == Blocks.GLASS_PANE);
        if (glass < 1) {
            issues.add(ValidationIssue.major("bannermod.prefab.validate.house.no_windows"));
        }

        int lights = view.countBlock(Blocks.LANTERN)
                + view.countBlock(Blocks.TORCH)
                + view.countBlock(Blocks.WALL_TORCH)
                + view.countBlock(Blocks.SOUL_LANTERN)
                + view.countBlock(Blocks.SOUL_TORCH)
                + view.countBlock(Blocks.SOUL_WALL_TORCH);
        if (lights < 1) {
            issues.add(ValidationIssue.minor("bannermod.prefab.validate.house.no_light"));
        }

        int decor = view.countStairs() + view.countSlabs() + view.countDoors();
        if (decor >= 5) {
            issues.add(ValidationIssue.info("bannermod.prefab.validate.house.cozy", decor));
        }

        int score = ArchitectureScorer.score(level, view);
        boolean passed = issues.stream().noneMatch(i -> i.severity() == ValidationIssue.Severity.BLOCKER);
        return new ValidationResult(passed, score, issues);
    }
}
