package com.talhanation.bannermod.entity.civilian;

import com.talhanation.bannermod.persistence.civilian.NeededItem;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.function.Predicate;

final class WorkerInventoryService {
    private static final int WORKER_STORAGE_START_SLOT = 6;

    private final AbstractWorkerEntity worker;

    WorkerInventoryService(AbstractWorkerEntity worker) {
        this.worker = worker;
    }

    boolean canAddItem(ItemStack itemToAdd) {
        for (int i = WORKER_STORAGE_START_SLOT; i < this.getInventory().getContainerSize(); i++) {
            ItemStack itemStack = this.getInventory().getItem(i);
            if (itemStack.isEmpty()) {
                return true;
            }

            if (ItemStack.isSameItemSameComponents(itemStack, itemToAdd) && itemStack.getCount() < itemStack.getMaxStackSize()) {
                return true;
            }
        }

        return false;
    }

    void pickUpItem(ItemEntity itemEntity) {
        ItemStack itemStack = itemEntity.getItem();
        if (!this.worker.wantsToPickUp(itemStack) || !this.canAddItem(itemStack)) {
            return;
        }

        this.worker.onItemPickup(itemEntity);
        this.worker.take(itemEntity, itemStack.getCount());
        ItemStack leftover = this.addItem(itemStack);

        this.worker.farmedItems += itemStack.getCount() - leftover.getCount();
        NeededItem.applyToNeededItems(itemStack, this.worker.neededItems);
        if (!this.worker.needsToGetItems()) {
            this.worker.clearPendingStorageComplaint();
        }

        if (leftover.isEmpty()) {
            itemEntity.remove(Entity.RemovalReason.DISCARDED);
            return;
        }

        itemStack.setCount(leftover.getCount());
    }

    ItemStack addItem(ItemStack itemStackToAdd) {
        if (itemStackToAdd.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack itemStack = itemStackToAdd.copy();
        this.moveItemToOccupiedSlotsWithSameType(itemStack);
        if (itemStack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        this.moveItemToEmptySlots(itemStack);
        return itemStack.isEmpty() ? ItemStack.EMPTY : itemStack;
    }

    @Nullable
    ItemStack getMatchingItem(Predicate<ItemStack> predicate) {
        for (int i = 0; i < this.getInventory().getContainerSize(); i++) {
            ItemStack stack = this.getInventory().getItem(i);
            if (!stack.isEmpty() && predicate.test(stack)) {
                return stack;
            }
        }
        return null;
    }

    int countMatchingItems(Predicate<ItemStack> predicate) {
        int count = 0;

        for (int i = 0; i < this.getInventory().getContainerSize(); i++) {
            ItemStack stack = this.getInventory().getItem(i);
            if (!stack.isEmpty() && predicate.test(stack)) {
                count += stack.getCount();
            }
        }

        return count;
    }

    int countMatchingStacks(Predicate<ItemStack> predicate) {
        int count = 0;

        for (int i = 0; i < this.getInventory().getContainerSize(); i++) {
            ItemStack stack = this.getInventory().getItem(i);
            if (!stack.isEmpty() && predicate.test(stack)) {
                count++;
            }
        }

        return count;
    }

    boolean hasFreeInvSlot() {
        for (int i = WORKER_STORAGE_START_SLOT; i < this.getInventory().getContainerSize(); i++) {
            if (this.getInventory().getItem(i).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private void moveItemToOccupiedSlotsWithSameType(ItemStack itemStackToMove) {
        for (int i = WORKER_STORAGE_START_SLOT; i < this.getInventory().getContainerSize(); ++i) {
            ItemStack itemStack = this.getInventory().getItem(i);
            if (ItemStack.isSameItemSameComponents(itemStack, itemStackToMove)) {
                this.moveItemsBetweenStacks(itemStackToMove, itemStack);
                if (itemStackToMove.isEmpty()) {
                    return;
                }
            }
        }
    }

    private void moveItemToEmptySlots(ItemStack itemStack) {
        for (int i = WORKER_STORAGE_START_SLOT; i < this.getInventory().getContainerSize(); ++i) {
            if (this.getInventory().getItem(i).isEmpty()) {
                this.getInventory().setItem(i, itemStack.copyAndClear());
                return;
            }
        }
    }

    private void moveItemsBetweenStacks(ItemStack itemStackToMove, ItemStack targetStack) {
        int maxCount = Math.min(64, targetStack.getMaxStackSize());
        int transferCount = Math.min(itemStackToMove.getCount(), maxCount - targetStack.getCount());
        if (transferCount > 0) {
            targetStack.grow(transferCount);
            itemStackToMove.shrink(transferCount);
            this.getInventory().setChanged();
        }
    }

    private SimpleContainer getInventory() {
        return this.worker.getInventory();
    }
}
