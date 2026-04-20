package com.talhanation.bannermod.settlement.prefab.validation.impl;

import com.talhanation.bannermod.settlement.prefab.BuildingPrefab;
import com.talhanation.bannermod.settlement.prefab.impl.MarketStallPrefab;
import com.talhanation.bannermod.settlement.prefab.validation.ArchitectureScorer;
import com.talhanation.bannermod.settlement.prefab.validation.BuildingInspectionView;
import com.talhanation.bannermod.settlement.prefab.validation.BuildingValidator;
import com.talhanation.bannermod.settlement.prefab.validation.ValidationIssue;
import com.talhanation.bannermod.settlement.prefab.validation.ValidationResult;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

/**
 * Rule set for a player-built market stall.
 */
public final class MarketStallValidator implements BuildingValidator {
    private static final int MIN_FOOTPRINT = 5;

    @Override
    public ResourceLocation prefabId() {
        return MarketStallPrefab.ID;
    }

    @Override
    public ValidationResult validate(BuildingPrefab prefab, ServerLevel level, AABB bounds, BuildingInspectionView view) {
        List<ValidationIssue> issues = new ArrayList<>();

        if (view.width() < MIN_FOOTPRINT || view.depth() < MIN_FOOTPRINT) {
            issues.add(ValidationIssue.blocker("bannermod.prefab.validate.market_stall.too_small",
                    view.width(), view.depth(), MIN_FOOTPRINT));
        }

        BlockPos min = view.min();
        BlockPos max = view.max();
        int pillarY = min.getY() + 1;
        BlockPos c1 = new BlockPos(min.getX(), pillarY, min.getZ());
        BlockPos c2 = new BlockPos(max.getX(), pillarY, min.getZ());
        BlockPos c3 = new BlockPos(min.getX(), pillarY, max.getZ());
        BlockPos c4 = new BlockPos(max.getX(), pillarY, max.getZ());
        boolean cornersOk = !level.getBlockState(c1).isAir()
                && !level.getBlockState(c2).isAir()
                && !level.getBlockState(c3).isAir()
                && !level.getBlockState(c4).isAir();
        if (view.countLogs() < 4 || !cornersOk) {
            issues.add(ValidationIssue.blocker("bannermod.prefab.validate.market_stall.no_pillars",
                    view.countLogs()));
        }

        int footprint = Math.max(1, view.width() * view.depth());
        double roofCoverage = (double) view.countCoveredGroundCells(level) / footprint;
        if (roofCoverage < 0.5) {
            issues.add(ValidationIssue.blocker("bannermod.prefab.validate.market_stall.no_roof",
                    Math.round(roofCoverage * 100)));
        }

        int wool = view.countTag(BlockTags.WOOL);
        int centerX = (min.getX() + max.getX()) / 2;
        int centerZ = (min.getZ() + max.getZ()) / 2;
        BlockPos centerPos = new BlockPos(centerX, pillarY, centerZ);
        boolean centerFilled = !level.getBlockState(centerPos).isAir();
        if (wool < 1 && !centerFilled) {
            issues.add(ValidationIssue.major("bannermod.prefab.validate.market_stall.no_counter"));
        }

        int lights = view.countBlock(Blocks.LANTERN)
                + view.countBlock(Blocks.TORCH)
                + view.countBlock(Blocks.WALL_TORCH)
                + view.countBlock(Blocks.SOUL_LANTERN)
                + view.countBlock(Blocks.SOUL_TORCH)
                + view.countBlock(Blocks.SOUL_WALL_TORCH);
        if (lights < 1) {
            issues.add(ValidationIssue.minor("bannermod.prefab.validate.market_stall.no_light"));
        }

        if (view.totalCells() > 0 && view.airCount() > view.totalCells() * 0.6) {
            issues.add(ValidationIssue.info("bannermod.prefab.validate.market_stall.open_pavilion"));
        }

        int score = ArchitectureScorer.score(level, view);
        boolean passed = issues.stream().noneMatch(i -> i.severity() == ValidationIssue.Severity.BLOCKER);
        return new ValidationResult(passed, score, issues);
    }
}
