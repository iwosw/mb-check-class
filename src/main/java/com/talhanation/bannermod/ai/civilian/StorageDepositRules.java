package com.talhanation.bannermod.ai.civilian;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class StorageDepositRules {

    public static DepositResult depositAll(Container inventory, Container storage, Predicate<ItemStack> wantsToKeep) {
        List<SlotState> inventoryStates = new ArrayList<>();
        List<SlotState> storageStates = new ArrayList<>();
        List<ItemStack> inventoryTemplates = new ArrayList<>();
        List<ItemStack> storageTemplates = new ArrayList<>();

        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            inventoryStates.add(SlotState.fromStack(stack, wantsToKeep.test(stack)));
            inventoryTemplates.add(stack.copy());
        }

        for (int i = 0; i < storage.getContainerSize(); i++) {
            ItemStack stack = storage.getItem(i);
            storageStates.add(SlotState.fromStack(stack, false));
            storageTemplates.add(stack.copy());
        }

        DepositResult result = depositAll(inventoryStates, storageStates);

        for (int i = 0; i < inventoryStates.size(); i++) {
            inventory.setItem(i, inventoryStates.get(i).toStack(inventoryTemplates.get(i)));
        }

        for (int i = 0; i < storageStates.size(); i++) {
            ItemStack template = storageTemplates.get(i);
            if (template.isEmpty()) {
                template = storageStates.get(i).template;
            }
            storage.setItem(i, storageStates.get(i).toStack(template));
        }

        inventory.setChanged();
        storage.setChanged();
        return result;
    }

    public static boolean hasDepositableItems(Container inventory, Predicate<ItemStack> wantsToKeep) {
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty() && !wantsToKeep.test(stack)) {
                return true;
            }
        }

        return false;
    }

    static DepositResult depositAll(List<SlotState> inventoryStates, List<SlotState> storageStates) {
        boolean movedAnyItems = false;

        for (SlotState inventoryState : inventoryStates) {
            if (inventoryState.isEmpty() || inventoryState.reserved) {
                continue;
            }

            int originalCount = inventoryState.count;
            deposit(inventoryState, storageStates);
            if (inventoryState.count != originalCount) {
                movedAnyItems = true;
            }
        }

        return new DepositResult(movedAnyItems, hasDepositableItems(inventoryStates));
    }

    static boolean hasDepositableItems(List<SlotState> inventoryStates) {
        for (SlotState inventoryState : inventoryStates) {
            if (!inventoryState.isEmpty() && !inventoryState.reserved) {
                return true;
            }
        }

        return false;
    }

    private static void deposit(SlotState inventoryState, List<SlotState> storageStates) {
        for (SlotState storageState : storageStates) {
            if (!storageState.canMerge(inventoryState)) {
                continue;
            }

            int amountToDeposit = Math.min(inventoryState.count, storageState.maxStackSize - storageState.count);
            if (amountToDeposit <= 0) {
                continue;
            }

            storageState.count += amountToDeposit;
            inventoryState.count -= amountToDeposit;
            if (inventoryState.count == 0) {
                return;
            }
        }

        for (SlotState storageState : storageStates) {
            if (!storageState.isEmpty()) {
                continue;
            }

            storageState.copyFrom(inventoryState);
            inventoryState.count = 0;
            return;
        }
    }

    public record DepositResult(boolean movedAnyItems, boolean hasDepositableItemsRemaining) {
    }

    static class SlotState {
        private final boolean reserved;
        private String key;
        private int count;
        private int maxStackSize;
        private ItemStack template;

        private SlotState(String key, int count, int maxStackSize, boolean reserved, ItemStack template) {
            this.key = key;
            this.count = count;
            this.maxStackSize = maxStackSize;
            this.reserved = reserved;
            this.template = template;
        }

        static SlotState fromStack(ItemStack stack, boolean reserved) {
            if (stack.isEmpty()) {
                return new SlotState(null, 0, 64, reserved, ItemStack.EMPTY);
            }

            return new SlotState(stackKey(stack), stack.getCount(), stack.getMaxStackSize(), reserved, stack.copy());
        }

        static SlotState forTests(String key, int count, int maxStackSize, boolean reserved) {
            return new SlotState(key, count, maxStackSize, reserved, null);
        }

        boolean isEmpty() {
            return this.key == null || this.count <= 0;
        }

        boolean canMerge(SlotState other) {
            return !this.isEmpty() && !other.isEmpty() && Objects.equals(this.key, other.key);
        }

        void copyFrom(SlotState other) {
            this.key = other.key;
            this.count = other.count;
            this.maxStackSize = other.maxStackSize;
            this.template = other.template == null ? null : other.template.copy();
        }

        int count() {
            return this.count;
        }

        String key() {
            return this.key;
        }

        ItemStack toStack(ItemStack existingTemplate) {
            if (this.isEmpty()) {
                return ItemStack.EMPTY;
            }

            ItemStack base = existingTemplate != null && !existingTemplate.isEmpty() ? existingTemplate.copy() : this.template.copy();
            base.setCount(this.count);
            return base;
        }

        private static String stackKey(ItemStack stack) {
            return BuiltInRegistries.ITEM.getKey(stack.getItem()) + "|" + Objects.toString(stack.getTag(), "");
        }
    }
}
