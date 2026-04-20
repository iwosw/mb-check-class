package com.talhanation.bannermod.settlement.prefab.validation.impl;

import com.talhanation.bannermod.settlement.prefab.BuildingPrefab;
import com.talhanation.bannermod.settlement.prefab.impl.BarracksPrefab;
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
 * Rule set for a player-built military barracks.
 */
public final class BarracksValidator implements BuildingValidator {
    private static final int MIN_FOOTPRINT = 7;
    private static final int MIN_STONE = 30;

    private static final Predicate<Block> STONE_CLASS = b ->
            b == Blocks.COBBLESTONE
                    || b == Blocks.STONE
                    || b == Blocks.STONE_BRICKS
                    || b == Blocks.DEEPSLATE
                    || b == Blocks.SMOOTH_STONE;

    @Override
    public ResourceLocation prefabId() {
        return BarracksPrefab.ID;
    }

    @Override
    public ValidationResult validate(BuildingPrefab prefab, ServerLevel level, AABB bounds, BuildingInspectionView view) {
        List<ValidationIssue> issues = new ArrayList<>();

        if (view.width() < MIN_FOOTPRINT || view.depth() < MIN_FOOTPRINT) {
            issues.add(ValidationIssue.blocker("bannermod.prefab.validate.barracks.too_small",
                    view.width(), view.depth(), MIN_FOOTPRINT));
        }

        int beds = view.countBeds();
        if (beds < 2) {
            issues.add(ValidationIssue.blocker("bannermod.prefab.validate.barracks.not_enough_beds",
                    beds, 2));
        }

        if (view.countBlock(Blocks.CHEST) < 1) {
            issues.add(ValidationIssue.blocker("bannermod.prefab.validate.barracks.no_chest"));
        }

        int stone = view.countWhere(STONE_CLASS);
        if (stone < MIN_STONE) {
            issues.add(ValidationIssue.blocker("bannermod.prefab.validate.barracks.not_enough_stone",
                    stone, MIN_STONE));
        }

        int wallLayerY = view.min().getY() + 1;
        double wallCoverage = view.perimeterCoverage(level, wallLayerY,
                state -> !state.isAir());
        if (wallCoverage > 0.95 && view.countDoors() < 1) {
            issues.add(ValidationIssue.major("bannermod.prefab.validate.barracks.sealed"));
        }

        int lights = view.countBlock(Blocks.LANTERN)
                + view.countBlock(Blocks.TORCH)
                + view.countBlock(Blocks.WALL_TORCH)
                + view.countBlock(Blocks.SOUL_LANTERN)
                + view.countBlock(Blocks.SOUL_TORCH)
                + view.countBlock(Blocks.SOUL_WALL_TORCH);
        if (lights < 1) {
            issues.add(ValidationIssue.minor("bannermod.prefab.validate.barracks.no_light"));
        }

        int walls = view.countWhere(b -> b == Blocks.COBBLESTONE_WALL || b == Blocks.STONE_BRICK_WALL);
        if (walls >= 2) {
            issues.add(ValidationIssue.info("bannermod.prefab.validate.barracks.fortified", walls));
        }

        int score = ArchitectureScorer.score(level, view);
        boolean passed = issues.stream().noneMatch(i -> i.severity() == ValidationIssue.Severity.BLOCKER);
        return new ValidationResult(passed, score, issues);
    }
}
