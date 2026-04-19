package com.talhanation.bannermod.ai.civilian;

import com.talhanation.bannermod.entity.civilian.AbstractWorkerEntity;
import com.talhanation.bannermod.settlement.BannerModSettlementOrchestrator;
import com.talhanation.bannermod.settlement.workorder.SettlementWorkOrder;
import com.talhanation.bannermod.settlement.workorder.SettlementWorkOrderRuntime;
import com.talhanation.bannermod.settlement.workorder.SettlementWorkOrderType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.state.BlockState;

import java.util.EnumSet;
import java.util.Optional;

/**
 * Worker goal that consumes {@link SettlementWorkOrder} claims from the per-level
 * {@link SettlementWorkOrderRuntime} and executes them in-world.
 *
 * <p>Only a subset of order types are executed here — the ones whose action is simple and
 * broadly applicable across worker kinds (break a block, mine a block). Placement-style
 * orders (plant, build, replant) are left to the legacy profession goals for now; this goal
 * releases them back so the zone-driven path can pick them up.</p>
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
            runtime.complete(activeOrder.orderUuid());
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
                    runtime.complete(activeOrder.orderUuid());
                    this.activeOrder = null;
                    return;
                }
                if (!attemptedExecution) {
                    attemptedExecution = true;
                }
                worker.mineBlock(target);
                worker.swing(InteractionHand.MAIN_HAND);
            }
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
            case HARVEST_CROP, BREAK_BLOCK, MINE_BLOCK, FELL_TREE -> true;
            default -> false;
        };
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}
