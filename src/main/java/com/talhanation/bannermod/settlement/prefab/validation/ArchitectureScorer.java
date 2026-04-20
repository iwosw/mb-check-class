package com.talhanation.bannermod.settlement.prefab.validation;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;

/**
 * Heuristic 0..100 score estimating how architecturally interesting a player-built
 * structure is. The intent is not to police taste — just to distinguish a cardboard
 * dirt box from a structure the player actually put effort into.
 *
 * <p>Components (roughly):</p>
 * <ul>
 *   <li><b>Material diversity</b> (0..25): distinct block types in the volume, up to a cap.</li>
 *   <li><b>Decoration</b> (0..20): presence of stairs, slabs, beds, doors, lanterns, carpets, etc.</li>
 *   <li><b>Coverage</b> (0..15): fraction of ground cells that have a roof above them.</li>
 *   <li><b>Vertical variance</b> (0..15): how much the occupied-height varies between columns.</li>
 *   <li><b>Symmetry</b> (0..15): fraction of mirrored-XZ positions whose blocks match.</li>
 *   <li><b>Dirt/cobble spam penalty</b> (−0..20): if one cheap block dominates &gt;60% of non-air cells.</li>
 *   <li><b>Fill ratio</b> (0..10): rewards a healthy mix of solid and open interior (not 100% filled, not 95% empty).</li>
 * </ul>
 *
 * <p>Components are additive and clamped to [0, 100].</p>
 */
public final class ArchitectureScorer {
    private ArchitectureScorer() {
    }

    public static int score(ServerLevel level, BuildingInspectionView view) {
        int score = 0;
        score += diversityPoints(view);
        score += decorationPoints(view);
        score += coveragePoints(level, view);
        score += verticalVariancePoints(level, view);
        score += symmetryPoints(level, view);
        score += fillBalancePoints(view);
        score -= cheapBlockPenalty(view);
        return Math.max(0, Math.min(100, score));
    }

    private static int diversityPoints(BuildingInspectionView view) {
        int distinct = view.distinctBlockCount();
        if (distinct <= 1) {
            return 0;
        }
        return Math.min(25, distinct * 3);
    }

    private static int decorationPoints(BuildingInspectionView view) {
        int pts = 0;
        pts += Math.min(6, view.countStairs() / 2);
        pts += Math.min(4, view.countSlabs() / 2);
        pts += Math.min(3, view.countTag(BlockTags.DOORS));
        pts += Math.min(3, view.countTag(BlockTags.BEDS));
        pts += Math.min(4, view.countWhere(b -> b == net.minecraft.world.level.block.Blocks.LANTERN
                || b == net.minecraft.world.level.block.Blocks.SOUL_LANTERN
                || b == net.minecraft.world.level.block.Blocks.TORCH
                || b == net.minecraft.world.level.block.Blocks.WALL_TORCH));
        pts += Math.min(3, view.countWhere(b -> b == net.minecraft.world.level.block.Blocks.FLOWER_POT
                || b.defaultBlockState().is(BlockTags.FLOWERS)));
        pts += Math.min(3, view.countWhere(b -> b.defaultBlockState().is(BlockTags.WOOL_CARPETS)));
        pts += Math.min(3, view.countWhere(b -> b == net.minecraft.world.level.block.Blocks.GLASS_PANE
                || b == net.minecraft.world.level.block.Blocks.GLASS));
        return Math.min(20, pts);
    }

    private static int coveragePoints(ServerLevel level, BuildingInspectionView view) {
        int covered = view.countCoveredGroundCells(level);
        int floorCells = view.width() * view.depth();
        if (floorCells <= 0) {
            return 0;
        }
        double ratio = (double) covered / floorCells;
        return (int) Math.round(ratio * 15);
    }

    private static int verticalVariancePoints(ServerLevel level, BuildingInspectionView view) {
        int yMin = view.min().getY();
        int yMax = view.max().getY();
        int minTop = Integer.MAX_VALUE;
        int maxTop = Integer.MIN_VALUE;
        int columns = 0;
        for (int x = view.min().getX(); x <= view.max().getX(); x++) {
            for (int z = view.min().getZ(); z <= view.max().getZ(); z++) {
                int top = yMin - 1;
                for (int y = yMax; y >= yMin; y--) {
                    if (!level.getBlockState(new BlockPos(x, y, z)).isAir()) {
                        top = y;
                        break;
                    }
                }
                if (top >= yMin) {
                    minTop = Math.min(minTop, top);
                    maxTop = Math.max(maxTop, top);
                    columns++;
                }
            }
        }
        if (columns == 0 || minTop == Integer.MAX_VALUE) {
            return 0;
        }
        int range = maxTop - minTop;
        return Math.min(15, range * 3);
    }

    private static int symmetryPoints(ServerLevel level, BuildingInspectionView view) {
        int matches = 0;
        int pairs = 0;
        int xMin = view.min().getX();
        int xMax = view.max().getX();
        int yMin = view.min().getY();
        int yMax = view.max().getY();
        int zMin = view.min().getZ();
        int zMax = view.max().getZ();
        for (int x = xMin; x <= xMax; x++) {
            int mirrorX = xMax - (x - xMin);
            if (x >= mirrorX) break;
            for (int y = yMin; y <= yMax; y++) {
                for (int z = zMin; z <= zMax; z++) {
                    pairs++;
                    BlockState a = level.getBlockState(new BlockPos(x, y, z));
                    BlockState b = level.getBlockState(new BlockPos(mirrorX, y, z));
                    if (a.getBlock() == b.getBlock()) {
                        matches++;
                    }
                }
            }
        }
        if (pairs == 0) {
            return 0;
        }
        double ratio = (double) matches / pairs;
        return (int) Math.round(ratio * 15);
    }

    private static int fillBalancePoints(BuildingInspectionView view) {
        int total = view.totalCells();
        if (total == 0) return 0;
        double solidRatio = (double) view.solidCount() / total;
        double sweet = 0.4;
        double distance = Math.abs(solidRatio - sweet);
        return (int) Math.round(Math.max(0.0, (1.0 - distance * 2.0)) * 10.0);
    }

    private static int cheapBlockPenalty(BuildingInspectionView view) {
        int nonAir = view.totalCells() - view.airCount();
        if (nonAir == 0) {
            return 0;
        }
        int worst = 0;
        for (Map.Entry<Block, Integer> e : view.countsByBlock().entrySet()) {
            if (isCheap(e.getKey())) {
                worst = Math.max(worst, e.getValue());
            }
        }
        double ratio = (double) worst / nonAir;
        if (ratio <= 0.6) {
            return 0;
        }
        double excess = ratio - 0.6;
        return (int) Math.round(excess * 50.0);
    }

    private static boolean isCheap(Block block) {
        return block == net.minecraft.world.level.block.Blocks.DIRT
                || block == net.minecraft.world.level.block.Blocks.COBBLESTONE
                || block == net.minecraft.world.level.block.Blocks.GRAVEL
                || block == net.minecraft.world.level.block.Blocks.SAND;
    }
}
