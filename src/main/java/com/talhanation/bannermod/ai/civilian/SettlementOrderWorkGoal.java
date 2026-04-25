package com.talhanation.bannermod.ai.civilian;

import com.talhanation.bannermod.entity.civilian.AbstractWorkerEntity;
import com.talhanation.bannermod.entity.civilian.workarea.BuildArea;
import com.talhanation.bannermod.persistence.civilian.BuildBlockParse;
import com.talhanation.bannermod.settlement.BannerModSettlementOrchestrator;
import com.talhanation.bannermod.settlement.workorder.SettlementWorkOrder;
import com.talhanation.bannermod.settlement.workorder.SettlementWorkOrderRuntime;
import com.talhanation.bannermod.settlement.workorder.SettlementWorkOrderType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.PlantType;

import java.util.EnumSet;
import java.util.Optional;

/**
 * Worker goal that consumes {@link SettlementWorkOrder} claims from the per-level
 * {@link SettlementWorkOrderRuntime} and executes them in-world.
 *
 * <p>Only simple, broadly applicable block actions are executed here. Construction placement
 * resolves its target state from the owning {@link BuildArea}; orders never guess a block-state.</p>
 *
 * <p>Priority: this goal is registered alongside the legacy {@code *WorkGoal} at priority 0.
 * Its {@link #canUse()} is only true while the runtime holds a claim for the worker, so it
 * preempts the legacy goal only when settlement demand is actively driving this worker.</p>
 */
public final class SettlementOrderWorkGoal extends Goal {

    private static final double REACH_THRESHOLD = 2.6D;
    private static final int MAX_PATH_TICKS = 20 * 30;
    private static final int MAX_ACTION_TICKS = 20 * 15;

    private final AbstractWorkerEntity worker;
    private SettlementWorkOrder activeOrder;
    private int pathTicks;
    private int actionTicks;
    private boolean attemptedExecution;

    public SettlementOrderWorkGoal(AbstractWorkerEntity worker) {
        this.worker = worker;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (worker.getCommandSenderWorld().isClientSide()) {
            return false;
        }
        if (!(worker.getCommandSenderWorld() instanceof ServerLevel level)) {
            return false;
        }
        SettlementWorkOrderRuntime runtime = BannerModSettlementOrchestrator.workOrderRuntime(level);
        if (runtime == null) {
            return false;
        }
        Optional<SettlementWorkOrder> claim = runtime.currentClaim(worker.getUUID());
        return claim.isPresent() && isExecutableOrder(claim.get());
    }

    @Override
    public boolean canContinueToUse() {
        if (activeOrder == null) {
            return false;
        }
        if (!(worker.getCommandSenderWorld() instanceof ServerLevel level)) {
            return false;
        }
        SettlementWorkOrderRuntime runtime = BannerModSettlementOrchestrator.workOrderRuntime(level);
        if (runtime == null) {
            return false;
        }
        return runtime.find(activeOrder.orderUuid()).isPresent();
    }

    @Override
    public void start() {
        super.start();
        if (!(worker.getCommandSenderWorld() instanceof ServerLevel level)) {
            return;
        }
        SettlementWorkOrderRuntime runtime = BannerModSettlementOrchestrator.workOrderRuntime(level);
        if (runtime == null) {
            return;
        }
        this.activeOrder = runtime.currentClaim(worker.getUUID()).orElse(null);
        this.pathTicks = 0;
        this.actionTicks = 0;
        this.attemptedExecution = false;
    }

    @Override
    public void stop() {
        super.stop();
        this.activeOrder = null;
        this.pathTicks = 0;
        this.actionTicks = 0;
        this.attemptedExecution = false;
        worker.getNavigation().stop();
    }

    @Override
    public void tick() {
        super.tick();
        if (activeOrder == null) {
            return;
        }
        if (!(worker.getCommandSenderWorld() instanceof ServerLevel level)) {
            return;
        }
        SettlementWorkOrderRuntime runtime = BannerModSettlementOrchestrator.workOrderRuntime(level);
        if (runtime == null) {
            return;
        }

        BlockPos target = activeOrder.targetPos();
        if (target == null) {
            completeActiveOrder(runtime, level);
            this.activeOrder = null;
            return;
        }

        double distance = worker.getHorizontalDistanceTo(target.getCenter());
        if (distance > REACH_THRESHOLD) {
            pathTicks++;
            if (pathTicks > MAX_PATH_TICKS) {
                runtime.release(activeOrder.orderUuid());
                this.activeOrder = null;
                return;
            }
            worker.getNavigation().moveTo(target.getX() + 0.5, target.getY(), target.getZ() + 0.5, 0.9D);
            worker.getLookControl().setLookAt(target.getCenter());
            return;
        }

        worker.getNavigation().stop();
        worker.getLookControl().setLookAt(target.getCenter());
        actionTicks++;
        if (actionTicks > MAX_ACTION_TICKS) {
            runtime.release(activeOrder.orderUuid());
            this.activeOrder = null;
            return;
        }

        executeAt(level, target, runtime);
    }

    private void executeAt(ServerLevel level, BlockPos target, SettlementWorkOrderRuntime runtime) {
        SettlementWorkOrderType type = activeOrder.type();
        switch (type) {
            case HARVEST_CROP,
                  BREAK_BLOCK,
                  MINE_BLOCK,
                  FELL_TREE -> {
                BlockState state = level.getBlockState(target);
                if (state.isAir() || AbstractWorkerEntity.isPosBroken(target, level, true)) {
                    completeActiveOrder(runtime, level);
                    this.activeOrder = null;
                    return;
                }
                if (!attemptedExecution) {
                    attemptedExecution = true;
                }
                worker.mineBlock(target);
                worker.swing(InteractionHand.MAIN_HAND);
            }
            case TILL_SOIL -> executeTillSoil(level, target, runtime);
            case PLANT_CROP -> executePlantCrop(level, target, runtime);
            case REPLANT_TREE -> executeReplantTree(level, target, runtime);
            case BUILD_BLOCK -> executeBuildBlock(level, target, runtime);
            default -> {
                // Placement-style or specialist types are left to legacy profession goals.
                runtime.release(activeOrder.orderUuid());
                this.activeOrder = null;
            }
        }
    }

    private static boolean isExecutableOrder(SettlementWorkOrder order) {
        if (order == null || order.targetPos() == null) {
            return false;
        }
        return switch (order.type()) {
            case HARVEST_CROP, BREAK_BLOCK, MINE_BLOCK, FELL_TREE, TILL_SOIL, PLANT_CROP, REPLANT_TREE, BUILD_BLOCK -> true;
            default -> false;
        };
    }

    private void executeBuildBlock(ServerLevel level, BlockPos target, SettlementWorkOrderRuntime runtime) {
        Entity buildingEntity = level.getEntity(activeOrder.buildingUuid());
        if (!(buildingEntity instanceof BuildArea buildArea) || !buildArea.isAlive()) {
            runtime.release(activeOrder.orderUuid());
            this.activeOrder = null;
            return;
        }

        BlockState buildingState = buildArea.getStateFromPos(target);
        if (buildingState == null) {
            completeActiveOrder(runtime, level);
            this.activeOrder = null;
            return;
        }

        BlockState levelState = level.getBlockState(target);
        if (buildArea.statesMatch(levelState, buildingState)) {
            buildArea.removeBuildBlockToPlace(target);
            buildArea.removeMultiBlockToPlace(target);
            completeActiveOrder(runtime, level);
            this.activeOrder = null;
            return;
        }
        if (!levelState.isAir() && !BuildArea.canDirectlyReplace(levelState, buildingState)) {
            runtime.release(activeOrder.orderUuid());
            this.activeOrder = null;
            return;
        }

        BuildBlockParse blockParse = BuildBlockParse.parseBlock(buildingState.getBlock());
        ItemStack buildingItem = worker.getMatchingItem(itemStack -> itemStack.is(blockParse.getItem()));
        if (buildingItem == null) {
            runtime.release(activeOrder.orderUuid());
            this.activeOrder = null;
            return;
        }
        if (!worker.getMainHandItem().is(buildingItem.getItem())) {
            worker.switchMainHandItem(itemStack -> itemStack.is(buildingItem.getItem()));
        }
        if (blockParse.wasParsed() && buildingItem.getItem() instanceof BlockItem blockItem) {
            buildingState = blockItem.getBlock().defaultBlockState();
        }

        BlockState secondaryState = buildArea.findPairedMultiBlockState(target);
        BlockPos secondaryPos = buildArea.findPairedMultiBlockPos(target);
        if (secondaryState != null && secondaryPos != null) {
            level.setBlock(target, buildingState, Block.UPDATE_CLIENTS);
            level.setBlock(secondaryPos, secondaryState, Block.UPDATE_ALL);
            level.blockUpdated(target, buildingState.getBlock());
            buildArea.removeMultiBlockToPlace(secondaryPos);
        } else {
            level.setBlockAndUpdate(target, buildingState);
            buildArea.removeMultiBlockToPlace(target);
        }

        level.playSound(null, target.getX(), target.getY(), target.getZ(), buildingState.getSoundType().getPlaceSound(), SoundSource.BLOCKS, 1.0F, 1.0F);
        worker.swing(InteractionHand.MAIN_HAND);
        buildingItem.shrink(1);
        buildArea.removeBuildBlockToPlace(target);
        completeActiveOrder(runtime, level);
        this.activeOrder = null;
    }

    private void executeTillSoil(ServerLevel level, BlockPos target, SettlementWorkOrderRuntime runtime) {
        BlockState state = level.getBlockState(target);
        if (state.getBlock() instanceof FarmBlock) {
            completeActiveOrder(runtime, level);
            this.activeOrder = null;
            return;
        }
        if (!(worker.getMainHandItem().getItem() instanceof HoeItem)) {
            runtime.release(activeOrder.orderUuid());
            this.activeOrder = null;
            return;
        }
        level.setBlock(target, Blocks.FARMLAND.defaultBlockState(), 3);
        level.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
        worker.damageMainHandItem();
        worker.swing(InteractionHand.MAIN_HAND);
        completeActiveOrder(runtime, level);
        this.activeOrder = null;
    }

    private void executePlantCrop(ServerLevel level, BlockPos target, SettlementWorkOrderRuntime runtime) {
        BlockState state = level.getBlockState(target);
        if (state.getBlock() instanceof CropBlock || state.getBlock() instanceof StemBlock) {
            completeActiveOrder(runtime, level);
            this.activeOrder = null;
            return;
        }
        ItemStack seedStack = worker.getMatchingItem(this::isCropSeed);
        if (seedStack == null) {
            runtime.release(activeOrder.orderUuid());
            this.activeOrder = null;
            return;
        }
        if (seedStack.getItem() instanceof BlockItem blockItem) {
            level.setBlockAndUpdate(target, blockItem.getBlock().defaultBlockState());
        } else if (seedStack.getItem() instanceof IPlantable plantable && plantable.getPlantType(level, target) == PlantType.CROP) {
            level.setBlock(target, plantable.getPlant(level, target), 3);
        } else {
            runtime.release(activeOrder.orderUuid());
            this.activeOrder = null;
            return;
        }
        level.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.CROP_PLANTED, SoundSource.BLOCKS, 1.0F, 1.0F);
        seedStack.shrink(1);
        worker.swing(InteractionHand.MAIN_HAND);
        completeActiveOrder(runtime, level);
        this.activeOrder = null;
    }

    private void executeReplantTree(ServerLevel level, BlockPos target, SettlementWorkOrderRuntime runtime) {
        if (!level.getBlockState(target).isAir()) {
            completeActiveOrder(runtime, level);
            this.activeOrder = null;
            return;
        }
        ItemStack saplingStack = worker.getMatchingItem(itemStack -> itemStack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof SaplingBlock);
        if (saplingStack == null || !(saplingStack.getItem() instanceof BlockItem blockItem)) {
            runtime.release(activeOrder.orderUuid());
            this.activeOrder = null;
            return;
        }
        level.setBlockAndUpdate(target, blockItem.getBlock().defaultBlockState());
        level.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.CROP_PLANTED, SoundSource.BLOCKS, 1.0F, 1.0F);
        saplingStack.shrink(1);
        worker.swing(InteractionHand.MAIN_HAND);
        completeActiveOrder(runtime, level);
        this.activeOrder = null;
    }

    private void completeActiveOrder(SettlementWorkOrderRuntime runtime, ServerLevel level) {
        runtime.complete(activeOrder.orderUuid(), level.getGameTime());
    }

    private boolean isCropSeed(ItemStack itemStack) {
        if (itemStack.getItem() instanceof BlockItem blockItem && blockItem.getBlock().defaultBlockState().getBlock() instanceof CropBlock) {
            return true;
        }
        return itemStack.getItem() instanceof IPlantable plantable && plantable.getPlantType(worker.getCommandSenderWorld(), worker.blockPosition()) == PlantType.CROP;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}
