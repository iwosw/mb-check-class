package com.talhanation.bannermod.settlement.prefab.validation.impl;

import com.talhanation.bannermod.settlement.prefab.BuildingPrefab;
import com.talhanation.bannermod.settlement.prefab.impl.MinePrefab;
import com.talhanation.bannermod.settlement.prefab.validation.ArchitectureScorer;
import com.talhanation.bannermod.settlement.prefab.validation.BuildingInspectionView;
import com.talhanation.bannermod.settlement.prefab.validation.BuildingValidator;
import com.talhanation.bannermod.settlement.prefab.validation.ValidationIssue;
import com.talhanation.bannermod.settlement.prefab.validation.ValidationResult;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Rule set for a player-built mine head.
 */
public final class MineValidator implements BuildingValidator {
    private static final int MIN_FOOTPRINT = 5;
    private static final int MIN_STONE = 40;

    private static final Predicate<Block> STONE_CLASS = b ->
            b == Blocks.COBBLESTONE
                    || b == Blocks.STONE
                    || b == Blocks.STONE_BRICKS
                    || b == Blocks.DEEPSLATE
                    || b == Blocks.SMOOTH_STONE;

    @Override
    public ResourceLocation prefabId() {
        return MinePrefab.ID;
    }

    @Override
    public ValidationResult validate(BuildingPrefab prefab, ServerLevel level, AABB bounds, BuildingInspectionView view) {
        List<ValidationIssue> issues = new ArrayList<>();

        if (view.width() < MIN_FOOTPRINT || view.depth() < MIN_FOOTPRINT) {
            issues.add(ValidationIssue.blocker("bannermod.prefab.validate.mine.too_small",
                    view.width(), view.depth(), MIN_FOOTPRINT));
        }

        if (view.countBlock(Blocks.CHEST) < 1) {
            issues.add(ValidationIssue.blocker("bannermod.prefab.validate.mine.no_chest"));
        }

        if (view.countBlock(Blocks.FURNACE) < 1) {
            issues.add(ValidationIssue.blocker("bannermod.prefab.validate.mine.no_furnace"));
        }

        int stone = view.countWhere(STONE_CLASS);
        if (stone < MIN_STONE) {
            issues.add(ValidationIssue.blocker("bannermod.prefab.validate.mine.not_enough_stone",
                    stone, MIN_STONE));
        }

        int footprint = Math.max(1, view.width() * view.depth());
        double roofCoverage = (double) view.countCoveredGroundCells(level) / footprint;
        int doors = view.countDoors();
        if (doors == 0 && roofCoverage > 0.95) {
            issues.add(ValidationIssue.major("bannermod.prefab.validate.mine.sealed_tomb"));
        }

        int lights = view.countBlock(Blocks.LANTERN)
                + view.countBlock(Blocks.TORCH)
                + view.countBlock(Blocks.WALL_TORCH)
                + view.countBlock(Blocks.SOUL_LANTERN)
                + view.countBlock(Blocks.SOUL_TORCH)
                + view.countBlock(Blocks.SOUL_WALL_TORCH);
        if (lights < 1) {
            issues.add(ValidationIssue.minor("bannermod.prefab.validate.mine.no_light"));
        }

        if (stone > 80) {
            issues.add(ValidationIssue.info("bannermod.prefab.validate.mine.fortified", stone));
        }

        int score = ArchitectureScorer.score(level, view);
        boolean passed = issues.stream().noneMatch(i -> i.severity() == ValidationIssue.Severity.BLOCKER);
        return new ValidationResult(passed, score, issues);
    }
}
