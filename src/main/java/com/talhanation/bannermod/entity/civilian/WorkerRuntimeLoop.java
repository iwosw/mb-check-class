package com.talhanation.bannermod.entity.civilian;

import com.talhanation.bannermod.util.RuntimeProfilingCounters;
import com.talhanation.bannermod.entity.civilian.workarea.AbstractWorkAreaEntity;
import net.minecraft.world.entity.item.ItemEntity;

import java.util.List;

final class WorkerRuntimeLoop {
    private static final int LOOT_PICKUP_SCAN_INTERVAL_TICKS = 10;

    private WorkerRuntimeLoop() {
    }

    static void aiStep(AbstractWorkerEntity worker) {
        tickLootPickup(worker);
        releaseDistantWorkArea(worker);
    }

    private static void tickLootPickup(AbstractWorkerEntity worker) {
        if (worker.tickCount % LOOT_PICKUP_SCAN_INTERVAL_TICKS != 0) {
            return;
        }
        if (!worker.canPickUpLoot() || !worker.isAliveForLooting()) {
            return;
        }

        List<ItemEntity> nearbyItems = worker.getCommandSenderWorld().getEntitiesOfClass(
                ItemEntity.class,
                worker.getBoundingBox().inflate(5.5D, 5.5D, 5.5D)
        );
        RuntimeProfilingCounters.increment("worker.loot_pickup.scans");
        RuntimeProfilingCounters.add("worker.loot_pickup.items_seen", nearbyItems.size());

        for (ItemEntity itemEntity : nearbyItems) {
            if (!itemEntity.isRemoved()
                    && !itemEntity.getItem().isEmpty()
                    && !itemEntity.hasPickUpDelay()
                    && worker.wantsToPickUp(itemEntity.getItem())) {
                worker.pickUpItem(itemEntity);
            }
        }
    }

    private static void releaseDistantWorkArea(AbstractWorkerEntity worker) {
        if (worker.tickCount % 20 != 0) {
            return;
        }

        AbstractWorkAreaEntity workArea = worker.getCurrentWorkArea();
        if (workArea == null) {
            return;
        }

        double distance = worker.getHorizontalDistanceTo(workArea.position());
        if (distance >= 1000) {
            workArea.setBeingWorkedOn(false);
        }
    }
}
