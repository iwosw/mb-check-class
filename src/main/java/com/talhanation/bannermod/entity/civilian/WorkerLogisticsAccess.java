package com.talhanation.bannermod.entity.civilian;

import com.talhanation.bannermod.persistence.civilian.NeededItem;
import com.talhanation.bannermod.shared.logistics.BannerModCourierTask;
import com.talhanation.bannermod.shared.logistics.BannerModLogisticsBlockedReason;
import com.talhanation.bannermod.shared.logistics.BannerModSupplyStatus;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.UUID;

interface WorkerLogisticsAccess {
    private AbstractWorkerEntity worker() {
        return (AbstractWorkerEntity) this;
    }

    default boolean canAddItem(ItemStack itemToAdd) {
        return worker().inventoryService().canAddItem(itemToAdd);
    }

    default boolean wantsToKeep(ItemStack itemStack) {
        return worker().supplyRuntime().wantsToKeep(itemStack);
    }

    default boolean wantsToPickUpWorkerItem(ItemStack itemStack) {
        return worker().supplyRuntime().wantsToPickUp(itemStack);
    }

    default void pickUpWorkerItem(ItemEntity itemEntity) {
        worker().inventoryService().pickUpItem(itemEntity);
    }

    default ItemStack addItem(ItemStack itemStackToAdd) {
        return worker().inventoryService().addItem(itemStackToAdd);
    }

    default boolean needsToGetToChest() {
        return worker().supplyRuntime().needsToGetToChest();
    }

    default boolean needsToDeposit() {
        return worker().supplyRuntime().needsToDeposit();
    }

    default @Nullable ItemStack getMatchingItem(java.util.function.Predicate<ItemStack> predicate) {
        return worker().inventoryService().getMatchingItem(predicate);
    }

    default int countMatchingItems(java.util.function.Predicate<ItemStack> predicate) {
        return worker().inventoryService().countMatchingItems(predicate);
    }

    default int countMatchingStacks(java.util.function.Predicate<ItemStack> predicate) {
        return worker().inventoryService().countMatchingStacks(predicate);
    }

    default boolean needsToGetItems() {
        return worker().supplyRuntime().needsToGetItems();
    }

    default void addNeededItem(NeededItem neededItem) {
        worker().supplyRuntime().addNeededItem(neededItem);
    }

    default void requestRequiredItem(NeededItem neededItem, String reasonToken, Component message) {
        worker().courierService().requestRequiredItem(neededItem, reasonToken, message);
    }

    default void clearPendingStorageComplaint() {
        worker().courierService().clearPendingStorageComplaint();
    }

    default @Nullable WorkerStorageRequestState.PendingComplaint releasePendingStorageComplaint() {
        return worker().courierService().releasePendingStorageComplaint();
    }

    default @Nullable BannerModCourierTask getActiveCourierTask() {
        return worker().courierService().getActiveCourierTask();
    }

    default boolean hasActiveCourierTask() {
        return worker().courierService().hasActiveCourierTask();
    }

    default void setActiveCourierTask(@Nullable BannerModCourierTask activeCourierTask) {
        worker().courierService().setActiveCourierTask(activeCourierTask);
    }

    default void clearActiveCourierTask() {
        worker().courierService().clearActiveCourierTask();
    }

    default int getActiveCourierCarriedCount() {
        return worker().courierService().getActiveCourierCarriedCount();
    }

    default int getActiveCourierPickupMissingCount() {
        return worker().courierService().getActiveCourierPickupMissingCount();
    }

    default boolean hasActiveCourierPickupPending() {
        return worker().courierService().hasActiveCourierPickupPending();
    }

    default @Nullable UUID getActiveCourierTargetStorageAreaId() {
        return worker().courierService().getActiveCourierTargetStorageAreaId();
    }

    default void markActiveCourierPickupComplete() {
        worker().courierService().markActiveCourierPickupComplete();
    }

    default void completeActiveCourierDelivery() {
        worker().courierService().completeActiveCourierDelivery();
    }

    default void abandonActiveCourierTask(BannerModLogisticsBlockedReason reason, String message) {
        worker().courierService().abandonActiveCourierTask(reason, message);
    }

    default BannerModSupplyStatus.WorkerSupplyStatus getSupplyStatus() {
        return worker().courierService().getSupplyStatus();
    }

    default boolean hasPendingStorageComplaint() {
        return worker().courierService().hasPendingStorageComplaint();
    }

    default void onItemStackAdded(ItemStack itemStack) {
        worker().supplyRuntime().onItemStackAdded(itemStack);
    }

    default boolean hasFreeInvSlot() {
        return worker().inventoryService().hasFreeInvSlot();
    }
}
