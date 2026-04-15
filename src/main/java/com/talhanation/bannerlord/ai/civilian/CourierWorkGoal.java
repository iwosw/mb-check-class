package com.talhanation.bannerlord.ai.civilian;

import com.talhanation.bannermod.logistics.BannerModCourierTask;
import com.talhanation.bannermod.logistics.BannerModLogisticsService;
import com.talhanation.bannerlord.entity.civilian.AbstractWorkerEntity;
import com.talhanation.bannerlord.entity.civilian.workarea.BuildArea;
import com.talhanation.bannerlord.entity.civilian.workarea.StorageArea;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.Optional;

public class CourierWorkGoal extends Goal {
    private final AbstractWorkerEntity worker;
    private BannerModCourierTask task;
    private BlockPos sourceChestPos;
    private Container sourceContainer;
    private int openTimer;
    private State state = State.IDLE;

    public CourierWorkGoal(AbstractWorkerEntity worker) {
        this.worker = worker;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!(this.worker.level() instanceof ServerLevel serverLevel)) {
            return false;
        }
        if (this.worker.needsToDeposit() || this.worker.needsToGetItems() || this.worker.getCurrentWorkArea() != null) {
            return false;
        }
        Optional<BannerModCourierTask> nextTask = BannerModLogisticsService.shared().selectBestTask(serverLevel, this.worker);
        if (nextTask.isEmpty()) {
            BannerModLogisticsService.shared().selectBlockedReason(serverLevel, this.worker)
                    .ifPresent(reason -> this.worker.reportBlockedReason(reason,
                            Component.literal(this.worker.getName().getString() + ": Logistics route is blocked.")));
            return false;
        }
        this.task = nextTask.get();
        return true;
    }

    @Override
    public void start() {
        this.state = State.MOVE_TO_SOURCE;
        this.openTimer = 0;
        this.sourceChestPos = null;
        this.sourceContainer = null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.task != null && this.state != State.DONE && this.state != State.BLOCKED;
    }

    @Override
    public void stop() {
        this.task = null;
        this.state = State.IDLE;
        this.sourceChestPos = null;
        this.sourceContainer = null;
        this.openTimer = 0;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (!(this.worker.level() instanceof ServerLevel serverLevel) || this.task == null) {
            return;
        }
        switch (this.state) {
            case MOVE_TO_SOURCE -> moveToSource(serverLevel);
            case PICKUP -> pickup(serverLevel);
            case MOVE_TO_DESTINATION -> moveToDestination();
            case DELIVER -> deliver(serverLevel);
            case BLOCKED -> this.worker.reportBlockedReason(this.task.blockedReasonToken(), Component.literal(this.worker.getName().getString() + ": Logistics task is blocked."));
            case DONE, IDLE -> {
            }
        }
    }

    private void moveToSource(ServerLevel level) {
        StorageArea storageArea = resolveStorage(level);
        if (storageArea == null) {
            this.state = State.BLOCKED;
            return;
        }
        if (this.sourceChestPos == null) {
            this.sourceChestPos = storageArea.findNearestContainerWithItem(this.worker.blockPosition(), this.task.itemId());
        }
        if (this.sourceChestPos == null) {
            this.state = State.BLOCKED;
            return;
        }
        if (moveTo(this.sourceChestPos)) {
            return;
        }
        this.sourceContainer = storageArea.getContainer(this.sourceChestPos);
        if (this.sourceContainer == null) {
            this.state = State.BLOCKED;
            return;
        }
        this.state = State.PICKUP;
    }

    private void pickup(ServerLevel level) {
        StorageArea storageArea = resolveStorage(level);
        if (storageArea == null || this.sourceContainer == null || this.sourceChestPos == null) {
            this.state = State.BLOCKED;
            return;
        }
        this.worker.getLookControl().setLookAt(this.sourceChestPos.getCenter());
        storageArea.scanStorageBlocks();
        if (this.openTimer++ < 10) {
            return;
        }
        this.openTimer = 0;
        int remaining = this.task.itemCount();
        for (int i = 0; i < this.sourceContainer.getContainerSize() && remaining > 0; i++) {
            ItemStack stack = this.sourceContainer.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            String itemId = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(stack.getItem()).toString();
            if (!this.task.itemId().equals(itemId)) {
                continue;
            }
            int toMove = Math.min(remaining, stack.getCount());
            ItemStack extracted = stack.split(toMove);
            ItemStack leftover = this.worker.getInventory().addItem(extracted);
            int inserted = toMove - leftover.getCount();
            if (!leftover.isEmpty()) {
                stack.grow(leftover.getCount());
            }
            remaining -= inserted;
        }
        if (remaining > 0) {
            this.state = State.BLOCKED;
            return;
        }
        BannerModLogisticsService.shared().markInTransit(this.task.reservationId(), level.getGameTime());
        this.state = State.MOVE_TO_DESTINATION;
    }

    private void moveToDestination() {
        if (moveTo(this.task.destinationPos())) {
            return;
        }
        this.state = State.DELIVER;
    }

    private void deliver(ServerLevel level) {
        BuildArea buildArea = resolveBuildArea(level);
        if (buildArea == null) {
            this.state = State.BLOCKED;
            return;
        }
        ItemStack carried = this.worker.getInventory().items.stream()
                .filter(stack -> !stack.isEmpty())
                .filter(stack -> this.task.itemId().equals(net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(stack.getItem()).toString()))
                .min(Comparator.comparingInt(ItemStack::getCount))
                .orElse(ItemStack.EMPTY);
        if (carried.isEmpty()) {
            this.state = State.BLOCKED;
            return;
        }
        ItemStack transfer = carried.split(Math.min(this.task.itemCount(), carried.getCount()));
        ItemStack leftover = buildArea.addToLogisticsBuffer(transfer);
        if (!leftover.isEmpty()) {
            carried.grow(leftover.getCount());
            this.state = State.BLOCKED;
            return;
        }
        BannerModLogisticsService.shared().release(this.task.reservationId());
        this.state = State.DONE;
    }

    private boolean moveTo(BlockPos pos) {
        if (pos == null) {
            return false;
        }
        if (pos.distSqr(this.worker.blockPosition()) < 9.0D) {
            this.worker.getNavigation().stop();
            return false;
        }
        this.worker.setFollowState(6);
        this.worker.getNavigation().moveTo(pos.getX(), pos.getY(), pos.getZ(), 0.9D);
        return true;
    }

    private StorageArea resolveStorage(ServerLevel level) {
        return level.getEntity(this.task.sourceEndpointId()) instanceof StorageArea storageArea ? storageArea : null;
    }

    private BuildArea resolveBuildArea(ServerLevel level) {
        return level.getEntity(this.task.destinationEndpointId()) instanceof BuildArea buildArea ? buildArea : null;
    }

    enum State {
        IDLE,
        MOVE_TO_SOURCE,
        PICKUP,
        MOVE_TO_DESTINATION,
        DELIVER,
        BLOCKED,
        DONE
    }
}
