package com.talhanation.bannermod.citizen;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public final class CitizenPersistenceBridge {

    private CitizenPersistenceBridge() {
    }

    public static CitizenStateSnapshot fromRecruitLegacy(CompoundTag legacy) {
        return fromLegacy(legacy);
    }

    public static CitizenStateSnapshot fromWorkerLegacy(CompoundTag legacy) {
        return fromLegacy(legacy);
    }

    public static CompoundTag writeRecruitLegacy(CitizenStateSnapshot snapshot, CompoundTag target) {
        writeSharedLegacy(snapshot, target);
        return target;
    }

    public static CompoundTag writeWorkerLegacy(CitizenStateSnapshot snapshot, CompoundTag target) {
        writeSharedLegacy(snapshot, target);
        if (snapshot.boundWorkAreaUuid() != null) {
            target.putUUID("boundWorkArea", snapshot.boundWorkAreaUuid());
        }
        return target;
    }

    private static CitizenStateSnapshot fromLegacy(CompoundTag legacy) {
        CitizenStateSnapshot.Builder builder = CitizenStateSnapshot.builder()
                .ownerUuid(readUuid(legacy, "OwnerUUID"))
                .followState(legacy.getInt("FollowState"))
                .owned(legacy.getBoolean("isOwned"))
                .boundWorkAreaUuid(readUuid(legacy, "boundWorkArea"))
                .inventoryData(copyInventoryData(legacy));

        if (legacy.contains("HoldPosX") && legacy.contains("HoldPosY") && legacy.contains("HoldPosZ")) {
            builder.holdPos(new Vec3(
                    legacy.getDouble("HoldPosX"),
                    legacy.getDouble("HoldPosY"),
                    legacy.getDouble("HoldPosZ")
            ));
        }

        if (legacy.contains("MovePosX") && legacy.contains("MovePosY") && legacy.contains("MovePosZ")) {
            builder.movePos(new BlockPos(
                    legacy.getInt("MovePosX"),
                    legacy.getInt("MovePosY"),
                    legacy.getInt("MovePosZ")
            ));
        }

        for (CitizenCore.RuntimeFlag flag : CitizenCore.RuntimeFlag.values()) {
            builder.runtimeFlag(flag, readFlag(legacy, flag));
        }

        CitizenStateSnapshot snapshot = builder.build();
        if (snapshot.followState() == 6) {
            builder.working(true);
            snapshot = builder.build();
        }
        return snapshot;
    }

    private static void writeSharedLegacy(CitizenStateSnapshot snapshot, CompoundTag target) {
        if (snapshot.ownerUuid() != null) {
            target.putUUID("OwnerUUID", snapshot.ownerUuid());
        }
        target.putInt("FollowState", snapshot.followState());
        target.putBoolean("isOwned", snapshot.owned());

        writeFlag(target, "ShouldFollow", snapshot.runtimeFlag(CitizenCore.RuntimeFlag.SHOULD_FOLLOW));
        writeFlag(target, "ShouldHoldPos", snapshot.runtimeFlag(CitizenCore.RuntimeFlag.SHOULD_HOLD_POS));
        writeFlag(target, "ShouldMovePos", snapshot.runtimeFlag(CitizenCore.RuntimeFlag.SHOULD_MOVE_POS));
        writeFlag(target, "ShouldProtect", snapshot.runtimeFlag(CitizenCore.RuntimeFlag.SHOULD_PROTECT));
        writeFlag(target, "ShouldMount", snapshot.runtimeFlag(CitizenCore.RuntimeFlag.SHOULD_MOUNT));
        writeFlag(target, "ShouldBlock", snapshot.runtimeFlag(CitizenCore.RuntimeFlag.SHOULD_BLOCK));
        writeFlag(target, "Listen", snapshot.runtimeFlag(CitizenCore.RuntimeFlag.LISTEN));
        writeFlag(target, "isFollowing", snapshot.runtimeFlag(CitizenCore.RuntimeFlag.IS_FOLLOWING));
        writeFlag(target, "ShouldRest", snapshot.runtimeFlag(CitizenCore.RuntimeFlag.SHOULD_REST));
        writeFlag(target, "ShouldRanged", snapshot.runtimeFlag(CitizenCore.RuntimeFlag.SHOULD_RANGED));
        writeFlag(target, "isInFormation", snapshot.runtimeFlag(CitizenCore.RuntimeFlag.IS_IN_FORMATION));

        if (snapshot.holdPos() != null) {
            target.putDouble("HoldPosX", snapshot.holdPos().x());
            target.putDouble("HoldPosY", snapshot.holdPos().y());
            target.putDouble("HoldPosZ", snapshot.holdPos().z());
        }

        if (snapshot.movePos() != null) {
            target.putInt("MovePosX", snapshot.movePos().getX());
            target.putInt("MovePosY", snapshot.movePos().getY());
            target.putInt("MovePosZ", snapshot.movePos().getZ());
        }

        copyInventoryData(snapshot.inventoryData(), target);
    }

    private static UUID readUuid(CompoundTag legacy, String key) {
        return legacy.contains(key) ? legacy.getUUID(key) : null;
    }

    private static boolean readFlag(CompoundTag legacy, CitizenCore.RuntimeFlag flag) {
        return switch (flag) {
            case SHOULD_FOLLOW -> legacy.getBoolean("ShouldFollow");
            case SHOULD_HOLD_POS -> legacy.getBoolean("ShouldHoldPos");
            case SHOULD_MOVE_POS -> legacy.getBoolean("ShouldMovePos");
            case SHOULD_PROTECT -> legacy.getBoolean("ShouldProtect");
            case SHOULD_MOUNT -> legacy.getBoolean("ShouldMount");
            case SHOULD_BLOCK -> legacy.getBoolean("ShouldBlock");
            case LISTEN -> legacy.getBoolean("Listen");
            case IS_FOLLOWING -> legacy.getBoolean("isFollowing");
            case SHOULD_REST -> legacy.getBoolean("ShouldRest");
            case SHOULD_RANGED -> legacy.getBoolean("ShouldRanged");
            case IS_IN_FORMATION -> legacy.getBoolean("isInFormation");
        };
    }

    private static void writeFlag(CompoundTag target, String key, boolean value) {
        target.putBoolean(key, value);
    }

    private static CompoundTag copyInventoryData(CompoundTag legacy) {
        CompoundTag inventory = new CompoundTag();
        if (legacy.contains("Items")) {
            inventory.put("Items", legacy.getList("Items", 10).copy());
        }
        if (legacy.contains("ArmorItems")) {
            inventory.put("ArmorItems", legacy.getList("ArmorItems", 10).copy());
        }
        if (legacy.contains("HandItems")) {
            inventory.put("HandItems", legacy.getList("HandItems", 10).copy());
        }
        if (legacy.contains("BeforeItemSlot")) {
            inventory.putInt("BeforeItemSlot", legacy.getInt("BeforeItemSlot"));
        }
        return inventory;
    }

    private static void copyInventoryData(CompoundTag inventory, CompoundTag target) {
        if (inventory.contains("Items")) {
            target.put("Items", inventory.getList("Items", 10).copy());
        }
        if (inventory.contains("ArmorItems")) {
            target.put("ArmorItems", inventory.getList("ArmorItems", 10).copy());
        }
        if (inventory.contains("HandItems")) {
            target.put("HandItems", inventory.getList("HandItems", 10).copy());
        }
        if (inventory.contains("BeforeItemSlot")) {
            target.putInt("BeforeItemSlot", inventory.getInt("BeforeItemSlot"));
        }
    }
}
