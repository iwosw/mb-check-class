package com.talhanation.bannermod.entity.civilian;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

final class WorkerBlockBreakService {
    private final AbstractWorkerEntity worker;
    private int currentTimeBreak;
    private int breakingTime;
    private int previousTimeBreak;

    WorkerBlockBreakService(AbstractWorkerEntity worker) {
        this.worker = worker;
    }

    void mineBlock(BlockPos pos) {
        if (!this.worker.isAlive()) {
            return;
        }

        Level level = this.worker.getCommandSenderWorld();
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) {
            return;
        }

        if (state.getBlock() instanceof LeavesBlock && this.worker.getMainHandItem().getItem() instanceof ShearsItem) {
            this.shearLeaves(level, pos, state);
            return;
        }

        this.playHitSound(level, pos, state);
        if (this.breakingTime == 0) {
            this.breakingTime = (int) (state.getDestroySpeed(level, pos) * 30);
        }

        float destroySpeed = this.worker.getUseItem().getDestroySpeed(state) * 2;
        this.currentTimeBreak += (int) destroySpeed;

        int stage = (int) ((float) this.currentTimeBreak / this.breakingTime * 10);
        if (stage != this.previousTimeBreak) {
            level.destroyBlockProgress(1, pos, stage);
            this.previousTimeBreak = stage;
        }

        if (this.currentTimeBreak >= this.breakingTime) {
            level.destroyBlock(pos, true, this.worker);
            this.worker.damageMainHandItem();
            this.resetBreakProgress();
        }

        this.worker.swing(InteractionHand.MAIN_HAND);
    }

    static boolean isPosBroken(BlockPos pos, Level level, boolean allowWater) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir() || AbstractWorkerEntity.UNBREAKABLES.contains(state.getBlock())) {
            return true;
        }
        if (allowWater) {
            Fluid fluidState = level.getFluidState(pos).getType();
            return fluidState == Fluids.WATER || fluidState == Fluids.FLOWING_WATER;
        }
        return false;
    }

    private void shearLeaves(Level level, BlockPos pos, BlockState state) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        for (ItemStack drop : Block.getDrops(state, (ServerLevel) level, pos, blockEntity, this.worker, this.worker.getMainHandItem())) {
            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), drop);
        }

        level.destroyBlockProgress(1, pos, -1);
        state.onRemove(level, pos, Blocks.AIR.defaultBlockState(), false);
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        level.playSound(null, pos, SoundEvents.SHEEP_SHEAR, SoundSource.BLOCKS, 1.0F, 1.0F);
        this.resetBreakProgress();
    }

    private void playHitSound(Level level, BlockPos pos, BlockState state) {
        if (this.currentTimeBreak % 5 == 4) {
            level.playLocalSound(
                    pos.getX(),
                    pos.getY(),
                    pos.getZ(),
                    state.getSoundType().getHitSound(),
                    SoundSource.BLOCKS,
                    1F,
                    0.75F,
                    false
            );
        }
    }

    private void resetBreakProgress() {
        this.currentTimeBreak = 0;
        this.breakingTime = 0;
        this.previousTimeBreak = 0;
    }
}
