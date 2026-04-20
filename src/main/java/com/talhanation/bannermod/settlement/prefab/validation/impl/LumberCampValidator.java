package com.talhanation.bannermod.settlement.prefab.validation.impl;

import com.talhanation.bannermod.settlement.prefab.BuildingPrefab;
import com.talhanation.bannermod.settlement.prefab.impl.LumberCampPrefab;
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
 * Rule set for a player-built lumber camp.
 *
 * <p>Hard requirements (BLOCKER): footprint ≥ 5×5, ≥1 storage (chest/barrel),
 * ≥1 crafting table, ≥12 log blocks in the structure.</p>
 * <p>Soft signals: roof coverage, presence of furnace, open-yard heuristics.</p>
 */
public final class LumberCampValidator implements BuildingValidator {
    private static final int MIN_FOOTPRINT = 5;
    private static final int MIN_LOGS = 12;

    @Override
    public ResourceLocation prefabId() {
        return LumberCampPrefab.ID;
    }

    @Override
    public ValidationResult validate(BuildingPrefab prefab, ServerLevel level, AABB bounds, BuildingInspectionView view) {
        List<ValidationIssue> issues = new ArrayList<>();

        if (view.width() < MIN_FOOTPRINT || view.depth() < MIN_FOOTPRINT) {
            issues.add(ValidationIssue.blocker("bannermod.prefab.validate.lumber_camp.too_small",
                    view.width(), view.depth(), MIN_FOOTPRINT));
        }

        int storage = view.countBlock(Blocks.CHEST) + view.countBlock(Blocks.BARREL);
        if (storage < 1) {
            issues.add(ValidationIssue.blocker("bannermod.prefab.validate.lumber_camp.no_chest"));
        }

        if (view.countBlock(Blocks.CRAFTING_TABLE) < 1) {
            issues.add(ValidationIssue.blocker("bannermod.prefab.validate.lumber_camp.no_crafting_table"));
        }

        int logs = view.countLogs();
        if (logs < MIN_LOGS) {
            issues.add(ValidationIssue.blocker("bannermod.prefab.validate.lumber_camp.not_enough_logs",
                    logs, MIN_LOGS));
        }

        int footprint = Math.max(1, view.width() * view.depth());
        double roofCoverage = (double) view.countCoveredGroundCells(level) / footprint;
        if (roofCoverage < 0.3) {
            issues.add(ValidationIssue.major("bannermod.prefab.validate.lumber_camp.no_roof",
                    Math.round(roofCoverage * 100)));
        }

        if (view.countBlock(Blocks.FURNACE) < 1) {
            issues.add(ValidationIssue.minor("bannermod.prefab.validate.lumber_camp.no_furnace"));
        }

        if (view.totalCells() > 0 && view.airCount() > view.totalCells() * 0.4) {
            issues.add(ValidationIssue.info("bannermod.prefab.validate.lumber_camp.open_yard"));
        }

        int score = ArchitectureScorer.score(level, view);
        boolean passed = issues.stream().noneMatch(i -> i.severity() == ValidationIssue.Severity.BLOCKER);
        return new ValidationResult(passed, score, issues);
    }
}
