package com.talhanation.bannermod.citizen;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

public record CitizenStateSnapshot(
        @Nullable UUID ownerUuid,
        @Nullable String teamId,
        int followState,
        boolean owned,
        boolean working,
        @Nullable UUID boundWorkAreaUuid,
        @Nullable Vec3 holdPos,
        @Nullable BlockPos movePos,
        CompoundTag inventoryData,
        Map<CitizenCore.RuntimeFlag, Boolean> runtimeFlags
) {
    public CitizenStateSnapshot {
        inventoryData = inventoryData == null ? new CompoundTag() : inventoryData.copy();
        EnumMap<CitizenCore.RuntimeFlag, Boolean> copiedFlags = new EnumMap<>(CitizenCore.RuntimeFlag.class);
        if (runtimeFlags != null) {
            copiedFlags.putAll(runtimeFlags);
        }
        for (CitizenCore.RuntimeFlag flag : CitizenCore.RuntimeFlag.values()) {
            copiedFlags.putIfAbsent(flag, Boolean.FALSE);
        }
        runtimeFlags = Collections.unmodifiableMap(copiedFlags);
    }

    public boolean runtimeFlag(CitizenCore.RuntimeFlag flag) {
        return this.runtimeFlags.getOrDefault(flag, false);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static CompoundTag copyInventory(SimpleContainer inventory) {
        CompoundTag inventoryData = new CompoundTag();
        ListTag items = new ListTag();
        for (int i = 0; i < inventory.getContainerSize(); ++i) {
            ItemStack itemStack = inventory.getItem(i);
            if (!itemStack.isEmpty()) {
                CompoundTag itemData = new CompoundTag();
                itemData.putByte("Slot", (byte) i);
                itemStack.save(itemData);
                items.add(itemData);
            }
        }
        inventoryData.put("Items", items);
        return inventoryData;
    }

    public static void restoreInventory(SimpleContainer inventory, CompoundTag inventoryData) {
        inventory.clearContent();
        ListTag items = inventoryData.getList("Items", 10);
        for (int i = 0; i < items.size(); ++i) {
            CompoundTag itemData = items.getCompound(i);
            int slot = itemData.getByte("Slot") & 255;
            if (slot < inventory.getContainerSize()) {
                inventory.setItem(slot, ItemStack.of(itemData));
            }
        }
        inventory.setChanged();
    }

    public static final class Builder {
        private UUID ownerUuid;
        private String teamId;
        private int followState;
        private boolean owned;
        private boolean working;
        private UUID boundWorkAreaUuid;
        private Vec3 holdPos;
        private BlockPos movePos;
        private CompoundTag inventoryData = new CompoundTag();
        private EnumMap<CitizenCore.RuntimeFlag, Boolean> runtimeFlags = new EnumMap<>(CitizenCore.RuntimeFlag.class);

        private Builder() {
        }

        public Builder ownerUuid(@Nullable UUID ownerUuid) {
            this.ownerUuid = ownerUuid;
            return this;
        }

        public Builder teamId(@Nullable String teamId) {
            this.teamId = teamId;
            return this;
        }

        public Builder followState(int followState) {
            this.followState = followState;
            return this;
        }

        public Builder owned(boolean owned) {
            this.owned = owned;
            return this;
        }

        public Builder working(boolean working) {
            this.working = working;
            return this;
        }

        public Builder boundWorkAreaUuid(@Nullable UUID boundWorkAreaUuid) {
            this.boundWorkAreaUuid = boundWorkAreaUuid;
            return this;
        }

        public Builder holdPos(@Nullable Vec3 holdPos) {
            this.holdPos = holdPos;
            return this;
        }

        public Builder movePos(@Nullable BlockPos movePos) {
            this.movePos = movePos;
            return this;
        }

        public Builder inventoryData(@Nullable CompoundTag inventoryData) {
            this.inventoryData = inventoryData == null ? new CompoundTag() : inventoryData.copy();
            return this;
        }

        public Builder runtimeFlag(CitizenCore.RuntimeFlag flag, boolean enabled) {
            this.runtimeFlags.put(flag, enabled);
            return this;
        }

        public Builder runtimeFlags(Map<CitizenCore.RuntimeFlag, Boolean> runtimeFlags) {
            this.runtimeFlags.clear();
            if (runtimeFlags != null) {
                this.runtimeFlags.putAll(runtimeFlags);
            }
            return this;
        }

        public CitizenStateSnapshot build() {
            return new CitizenStateSnapshot(
                    this.ownerUuid,
                    this.teamId,
                    this.followState,
                    this.owned,
                    this.working,
                    this.boundWorkAreaUuid,
                    this.holdPos,
                    this.movePos,
                    this.inventoryData,
                    this.runtimeFlags
            );
        }
    }
}
