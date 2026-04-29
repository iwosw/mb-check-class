package com.talhanation.bannermod.ai.civilian;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.PathfindingContext;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

public class RecruitsPathNodeEvaluator extends WalkNodeEvaluator {
    private int floorOffset;
    private PathNavigationRegion level;

    @Override
    public void prepare(PathNavigationRegion region, Mob mob) {
        super.prepare(region, mob);
        this.level = region;

        if (mob.isVehicle()) {
            this.entityHeight = Mth.floor(mob.getBbHeight() + (float) getEntityHeight());
        }

        applyWorkerPathMalus(mob);
    }

    public void setTarget(int x, int y, int z) {
    }

    public void setFloorOffset(int offset) {
        this.floorOffset = offset;
    }

    @Nullable
    @Override
    protected Node getNode(int x, int y, int z) {
        Node node = super.getNode(x, y, z);
        if (node == null) {
            return null;
        }

        PathType pathType = this.getPathTypeOfMob(new PathfindingContext(this.level, this.mob), x, y, z, this.mob);
        float malus = this.mob.getPathfindingMalus(pathType);
        if (malus < 0.0F) {
            return node;
        }

        node.type = pathType;
        node.costMalus = Math.max(node.costMalus, malus);

        BlockPos pos = new BlockPos(x, y, z);
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        BlockPos aboveEntityHeightPos = pos.above(getEntityHeight());

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            mutablePos.set(pos).move(direction);
            BlockState belowStateNeighbors = this.level.getBlockState(mutablePos.below());

            if (!belowStateNeighbors.is(Blocks.DIRT_PATH)) {
                node.costMalus += 2.0F;
                continue;
            }

            BlockState aboveLeavesCheck = this.level.getBlockState(aboveEntityHeightPos.relative(direction, 2));
            if (aboveLeavesCheck.is(BlockTags.LEAVES)) {
                node.costMalus = -1.0F;
                break;
            }
        }

        return node;
    }

    @Override
    protected double getFloorLevel(BlockPos pos) {
        if (this.canFloat() && this.level.getFluidState(pos).is(FluidTags.WATER)) {
            return (double) pos.getY() + 0.5D;
        }

        return getOffsetFloorLevel(this.level, pos);
    }

    private double getOffsetFloorLevel(BlockGetter level, BlockPos pos) {
        BlockPos blockPos = pos.below(this.floorOffset);
        VoxelShape voxelShape = level.getBlockState(blockPos).getCollisionShape(level, blockPos);
        return (double) blockPos.getY() + (voxelShape.isEmpty() ? 0.0D : voxelShape.max(Direction.Axis.Y));
    }

    private int getEntityHeight() {
        return this.mob.isVehicle() ? 2 : 1;
    }

    private static void applyWorkerPathMalus(Mob mob) {
        mob.setPathfindingMalus(PathType.WATER, 128.0F);
        mob.setPathfindingMalus(PathType.WATER_BORDER, 128.0F);
        mob.setPathfindingMalus(PathType.TRAPDOOR, -1.0F);
        mob.setPathfindingMalus(PathType.DAMAGE_FIRE, 32.0F);
        mob.setPathfindingMalus(PathType.DAMAGE_CAUTIOUS, 32.0F);
        mob.setPathfindingMalus(PathType.DANGER_POWDER_SNOW, -1.0F);
        mob.setPathfindingMalus(PathType.DOOR_WOOD_CLOSED, 0.0F);
        mob.setPathfindingMalus(PathType.FENCE, -1.0F);
        mob.setPathfindingMalus(PathType.LAVA, -1.0F);
        mob.setPathfindingMalus(PathType.LEAVES, -1.0F);
    }
}
