package com.talhanation.bannermod.settlement.prefab.validation.impl;

import com.talhanation.bannermod.settlement.prefab.BuildingPrefab;
import com.talhanation.bannermod.settlement.prefab.impl.FishingDockPrefab;
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
 * Rule set for a player-built fishing dock.
 */
public final class FishingDockValidator implements BuildingValidator {
    private static final int MIN_DEPTH = 7;
    private static final int MIN_WATER = 8;

    @Override
    public ResourceLocation prefabId() {
        return FishingDockPrefab.ID;
    }

    @Override
    public ValidationResult validate(BuildingPrefab prefab, ServerLevel level, AABB bounds, BuildingInspectionView view) {
        List<ValidationIssue> issues = new ArrayList<>();

        if (view.depth() < MIN_DEPTH) {
            issues.add(ValidationIssue.blocker("bannermod.prefab.validate.fishing_dock.too_short",
                    view.depth(), MIN_DEPTH));
        }

        int planks = view.countPlanks();
        if (planks < 1) {
            issues.add(ValidationIssue.blocker("bannermod.prefab.validate.fishing_dock.no_planks"));
        }

        int storage = view.countBlock(Blocks.BARREL) + view.countBlock(Blocks.CHEST);
        if (storage < 1) {
            issues.add(ValidationIssue.blocker("bannermod.prefab.validate.fishing_dock.no_storage"));
        }

        int water = view.waterCount();
        if (water < MIN_WATER) {
            issues.add(ValidationIssue.blocker("bannermod.prefab.validate.fishing_dock.not_over_water",
                    water, MIN_WATER));
        }

        if (view.countFences() < 4) {
            issues.add(ValidationIssue.major("bannermod.prefab.validate.fishing_dock.no_railing",
                    view.countFences()));
        }

        if (view.countLogs() < 6) {
            issues.add(ValidationIssue.minor("bannermod.prefab.validate.fishing_dock.no_shelter",
                    view.countLogs()));
        }

        if (water >= 20) {
            issues.add(ValidationIssue.info("bannermod.prefab.validate.fishing_dock.good_vantage", water));
        }

        int score = ArchitectureScorer.score(level, view);
        boolean passed = issues.stream().noneMatch(i -> i.severity() == ValidationIssue.Severity.BLOCKER);
        return new ValidationResult(passed, score, issues);
    }
}
