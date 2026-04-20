package com.talhanation.bannermod.settlement.prefab.validation.impl;

import com.talhanation.bannermod.settlement.prefab.BuildingPrefab;
import com.talhanation.bannermod.settlement.prefab.impl.StoragePrefab;
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
 * Rule set for a player-built storage / warehouse.
 */
public final class StorageValidator implements BuildingValidator {
    private static final int MIN_FOOTPRINT = 5;
    private static final int MIN_STORAGE = 6;

    @Override
    public ResourceLocation prefabId() {
        return StoragePrefab.ID;
    }

    @Override
    public ValidationResult validate(BuildingPrefab prefab, ServerLevel level, AABB bounds, BuildingInspectionView view) {
        List<ValidationIssue> issues = new ArrayList<>();

        if (view.width() < MIN_FOOTPRINT || view.depth() < MIN_FOOTPRINT) {
            issues.add(ValidationIssue.blocker("bannermod.prefab.validate.storage.too_small",
                    view.width(), view.depth(), MIN_FOOTPRINT));
        }

        int chests = view.countBlock(Blocks.CHEST);
        int barrels = view.countBlock(Blocks.BARREL);
        int trapped = view.countBlock(Blocks.TRAPPED_CHEST);
        int total = chests + barrels + trapped;
        if (total < MIN_STORAGE) {
            issues.add(ValidationIssue.blocker("bannermod.prefab.validate.storage.not_enough_chests",
                    total, MIN_STORAGE));
        }

        int footprint = Math.max(1, view.width() * view.depth());
        double roofCoverage = (double) view.countCoveredGroundCells(level) / footprint;
        if (roofCoverage < 0.7) {
            issues.add(ValidationIssue.blocker("bannermod.prefab.validate.storage.no_roof",
                    Math.round(roofCoverage * 100)));
        }

        int wallLayerY = view.min().getY() + 1;
        double wallCoverage = view.perimeterCoverage(level, wallLayerY,
                state -> state.getBlock().defaultBlockState().is(BlockTags.LOGS)
                        || state.getBlock().defaultBlockState().is(BlockTags.PLANKS));
        if (wallCoverage > 0.95 && view.countDoors() < 1) {
            issues.add(ValidationIssue.major("bannermod.prefab.validate.storage.no_door"));
        }

        int kinds = 0;
        if (chests > 0) kinds++;
        if (barrels > 0) kinds++;
        if (trapped > 0) kinds++;
        if (kinds <= 1) {
            issues.add(ValidationIssue.minor("bannermod.prefab.validate.storage.monotonous"));
        }

        if (total >= 12) {
            issues.add(ValidationIssue.info("bannermod.prefab.validate.storage.bulk_capacity", total));
        }

        int score = ArchitectureScorer.score(level, view);
        boolean passed = issues.stream().noneMatch(i -> i.severity() == ValidationIssue.Severity.BLOCKER);
        return new ValidationResult(passed, score, issues);
    }
}
