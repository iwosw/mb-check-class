package com.talhanation.bannermod.entity.civilian;

import com.talhanation.bannermod.persistence.civilian.NeededItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;

final class WorkerSupplyRuntime {
    private final AbstractWorkerEntity worker;

    WorkerSupplyRuntime(AbstractWorkerEntity worker) {
        this.worker = worker;
    }

    boolean wantsToKeep(ItemStack itemStack) {
        FoodProperties food = itemStack.get(DataComponents.FOOD);
        return food != null && food.nutrition() > 4;
    }

    boolean wantsToPickUp(ItemStack itemStack) {
        if (this.wantsToKeep(itemStack)) {
            return true;
        }

        for (NeededItem neededItem : this.worker.neededItems) {
            if (neededItem.matches(itemStack)) {
                return true;
            }
        }

        return false;
    }

    boolean needsToGetToChest() {
        return this.worker.needsToGetFood() || this.needsToDeposit() || this.needsToGetItems();
    }

    boolean needsToDeposit() {
        if (this.worker.hasActiveCourierTask() && !this.worker.hasActiveCourierPickupPending() && this.worker.getActiveCourierCarriedCount() > 0) {
            return true;
        }

        return this.worker.forcedDeposit || this.worker.farmedItems > 128;
    }

    boolean needsToGetItems() {
        if (this.worker.hasActiveCourierPickupPending()) {
            return true;
        }

        return this.worker.neededItems.stream().anyMatch(neededItem -> neededItem.required);
    }

    void addNeededItem(NeededItem neededItem) {
        if (this.worker.neededItems.contains(neededItem)) {
            return;
        }

        this.worker.neededItems.add(neededItem);
    }

    void onItemStackAdded(ItemStack itemStack) {
        ItemStack copiedStack = itemStack.copy();
        for (NeededItem neededItem : this.worker.neededItems) {
            if (neededItem.matches(copiedStack)) {
                NeededItem.applyToNeededItems(copiedStack, this.worker.neededItems);
                break;
            }
        }

        if (!this.needsToGetItems()) {
            this.worker.clearPendingStorageComplaint();
        }
    }
}
