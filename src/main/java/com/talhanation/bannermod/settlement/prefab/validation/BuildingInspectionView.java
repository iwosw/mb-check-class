package com.talhanation.bannermod.settlement.prefab.validation;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

/**
 * One-pass block-state summary over the player-built structure's bounding box. Validators
 * and the {@link ArchitectureScorer} both read from this view instead of re-iterating
 * the world, which keeps validation cheap.
 */
public final class BuildingInspectionView {
    private final AABB bounds;
    private final BlockPos min;
    private final BlockPos max;
    private final Map<Block, Integer> countsByBlock;
    private final int totalCells;
    private final int solidCount;
    private final int airCount;
    private final int waterCount;

    private BuildingInspectionView(AABB bounds,
                                   BlockPos min,
                                   BlockPos max,
                                   Map<Block, Integer> countsByBlock,
                                   int totalCells,
                                   int solidCount,
                                   int airCount,
                                   int waterCount) {
        this.bounds = bounds;
        this.min = min;
        this.max = max;
        this.countsByBlock = countsByBlock;
        this.totalCells = totalCells;
        this.solidCount = solidCount;
        this.airCount = airCount;
        this.waterCount = waterCount;
    }

    public static BuildingInspectionView scan(ServerLevel level, AABB bounds) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(bounds, "bounds");
        BlockPos min = BlockPos.containing(bounds.minX, bounds.minY, bounds.minZ);
        BlockPos max = BlockPos.containing(bounds.maxX - 1, bounds.maxY - 1, bounds.maxZ - 1);
        Map<Block, Integer> counts = new LinkedHashMap<>();
        int total = 0;
        int solid = 0;
        int air = 0;
        int water = 0;
        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            total++;
            BlockState state = level.getBlockState(pos);
            FluidState fluid = state.getFluidState();
            if (state.isAir()) {
                air++;
                continue;
            }
            counts.merge(state.getBlock(), 1, Integer::sum);
            if (fluid.is(net.minecraft.world.level.material.Fluids.WATER)) {
                water++;
            } else {
                solid++;
            }
        }
        return new BuildingInspectionView(bounds, min, max, new HashMap<>(counts), total, solid, air, water);
    }

    public AABB bounds() { return bounds; }
    public BlockPos min() { return min; }
    public BlockPos max() { return max; }
    public int totalCells() { return totalCells; }
    public int solidCount() { return solidCount; }
    public int airCount() { return airCount; }
    public int waterCount() { return waterCount; }
    public int distinctBlockCount() { return countsByBlock.size(); }

    public int countBlock(Block block) {
        return countsByBlock.getOrDefault(block, 0);
    }

    public int countTag(TagKey<Block> tag) {
        int sum = 0;
        for (Map.Entry<Block, Integer> e : countsByBlock.entrySet()) {
            if (e.getKey().defaultBlockState().is(tag)) {
                sum += e.getValue();
            }
        }
        return sum;
    }

    public int countWhere(Predicate<Block> predicate) {
        int sum = 0;
        for (Map.Entry<Block, Integer> e : countsByBlock.entrySet()) {
            if (predicate.test(e.getKey())) {
                sum += e.getValue();
            }
        }
        return sum;
    }

    public int countLogs() {
        return countTag(BlockTags.LOGS);
    }

    public int countPlanks() {
        return countTag(BlockTags.PLANKS);
    }

    public int countWool() {
        return countTag(BlockTags.WOOL);
    }

    public int countLeaves() {
        return countTag(BlockTags.LEAVES);
    }

    public int countBeds() {
        return countTag(BlockTags.BEDS);
    }

    public int countFences() {
        return countTag(BlockTags.FENCES);
    }

    public int countDoors() {
        return countTag(BlockTags.DOORS);
    }

    public int countStairs() {
        return countTag(BlockTags.STAIRS);
    }

    public int countSlabs() {
        return countTag(BlockTags.SLABS);
    }

    /** Count cells where the block above (inside bounds) is non-air — a crude "has a roof" estimate. */
    public int countCoveredGroundCells(ServerLevel level) {
        int covered = 0;
        for (int x = min.getX(); x <= max.getX(); x++) {
            for (int z = min.getZ(); z <= max.getZ(); z++) {
                int interiorY = min.getY() + 1;
                BlockPos floorPos = new BlockPos(x, interiorY, z);
                BlockState floor = level.getBlockState(floorPos);
                if (floor.isAir()) {
                    continue;
                }
                boolean hasRoofAbove = false;
                for (int y = interiorY + 1; y <= max.getY(); y++) {
                    if (!level.getBlockState(new BlockPos(x, y, z)).isAir()) {
                        hasRoofAbove = true;
                        break;
                    }
                }
                if (hasRoofAbove) {
                    covered++;
                }
            }
        }
        return covered;
    }

    /**
     * Scan the horizontal perimeter layer at {@code y} and return the fraction of cells whose
     * block satisfies {@code predicate}. Used to measure walls/fences.
     */
    public double perimeterCoverage(ServerLevel level, int y, Predicate<BlockState> predicate) {
        if (y < min.getY() || y > max.getY()) {
            return 0.0;
        }
        int perim = 0;
        int matching = 0;
        for (int x = min.getX(); x <= max.getX(); x++) {
            for (int z : new int[]{min.getZ(), max.getZ()}) {
                perim++;
                if (predicate.test(level.getBlockState(new BlockPos(x, y, z)))) {
                    matching++;
                }
            }
        }
        for (int z = min.getZ() + 1; z <= max.getZ() - 1; z++) {
            for (int x : new int[]{min.getX(), max.getX()}) {
                perim++;
                if (predicate.test(level.getBlockState(new BlockPos(x, y, z)))) {
                    matching++;
                }
            }
        }
        return perim == 0 ? 0.0 : (double) matching / perim;
    }

    /** Count blocks on a specific layer satisfying predicate. */
    public int countLayer(ServerLevel level, int y, Predicate<BlockState> predicate) {
        if (y < min.getY() || y > max.getY()) {
            return 0;
        }
        int n = 0;
        for (int x = min.getX(); x <= max.getX(); x++) {
            for (int z = min.getZ(); z <= max.getZ(); z++) {
                if (predicate.test(level.getBlockState(new BlockPos(x, y, z)))) {
                    n++;
                }
            }
        }
        return n;
    }

    /** Map of distinct block → count. Unmodifiable view. */
    public Map<Block, Integer> countsByBlock() {
        return java.util.Collections.unmodifiableMap(countsByBlock);
    }

    /** Stream of every block position in the bounds. */
    public Iterable<BlockPos> positions() {
        return BlockPos.betweenClosed(min, max);
    }

    public long positionStreamCount() {
        return StreamSupport.stream(positions().spliterator(), false).count();
    }

    public int width() { return max.getX() - min.getX() + 1; }
    public int height() { return max.getY() - min.getY() + 1; }
    public int depth() { return max.getZ() - min.getZ() + 1; }
}
