package com.talhanation.bannermod.citizen;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CitizenPersistenceBridgeTest {

    @Test
    void recruitLegacyKeysHydrateIntoOneCitizenSnapshotWithoutDroppingInventory() {
        UUID ownerId = UUID.randomUUID();
        CompoundTag legacy = new CompoundTag();
        legacy.putUUID("OwnerUUID", ownerId);
        legacy.putInt("FollowState", 2);
        legacy.putBoolean("isOwned", true);
        legacy.putBoolean("ShouldHoldPos", true);
        legacy.putDouble("HoldPosX", 1.5D);
        legacy.putDouble("HoldPosY", 64.0D);
        legacy.putDouble("HoldPosZ", -2.5D);
        legacy.put("Items", createItemsTag(0, "minecraft:iron_sword", 1));

        CitizenStateSnapshot snapshot = CitizenPersistenceBridge.fromRecruitLegacy(legacy);

        assertEquals(ownerId, snapshot.ownerUuid());
        assertEquals(2, snapshot.followState());
        assertTrue(snapshot.owned());
        assertEquals(new Vec3(1.5D, 64.0D, -2.5D), snapshot.holdPos());
        assertEquals(1, snapshot.inventoryData().getList("Items", 10).size());
    }

    @Test
    void workerLegacyKeysHydrateIntoSameCitizenSnapshotShape() {
        UUID ownerId = UUID.randomUUID();
        UUID boundWorkAreaId = UUID.randomUUID();
        CompoundTag legacy = new CompoundTag();
        legacy.putUUID("OwnerUUID", ownerId);
        legacy.putInt("FollowState", 6);
        legacy.putBoolean("isOwned", true);
        legacy.putUUID("boundWorkArea", boundWorkAreaId);
        legacy.putBoolean("ShouldMovePos", true);
        legacy.putInt("MovePosX", 12);
        legacy.putInt("MovePosY", 70);
        legacy.putInt("MovePosZ", 4);

        CitizenStateSnapshot snapshot = CitizenPersistenceBridge.fromWorkerLegacy(legacy);

        assertEquals(ownerId, snapshot.ownerUuid());
        assertEquals(6, snapshot.followState());
        assertTrue(snapshot.owned());
        assertTrue(snapshot.working());
        assertEquals(boundWorkAreaId, snapshot.boundWorkAreaUuid());
        assertEquals(new BlockPos(12, 70, 4), snapshot.movePos());
    }

    @Test
    void writingSnapshotBackPreservesLegacyKeyNames() {
        UUID ownerId = UUID.randomUUID();
        UUID boundWorkAreaId = UUID.randomUUID();
        CitizenStateSnapshot snapshot = CitizenStateSnapshot.builder()
                .ownerUuid(ownerId)
                .followState(6)
                .owned(true)
                .working(true)
                .holdPos(new Vec3(2.0D, 65.0D, 3.0D))
                .movePos(new BlockPos(8, 66, 9))
                .boundWorkAreaUuid(boundWorkAreaId)
                .runtimeFlag(CitizenCore.RuntimeFlag.SHOULD_HOLD_POS, true)
                .runtimeFlag(CitizenCore.RuntimeFlag.SHOULD_MOVE_POS, true)
                .inventoryData(new CompoundTag())
                .build();

        CompoundTag recruitLegacy = CitizenPersistenceBridge.writeRecruitLegacy(snapshot, new CompoundTag());
        CompoundTag workerLegacy = CitizenPersistenceBridge.writeWorkerLegacy(snapshot, new CompoundTag());

        assertEquals(ownerId, recruitLegacy.getUUID("OwnerUUID"));
        assertEquals(6, recruitLegacy.getInt("FollowState"));
        assertTrue(recruitLegacy.getBoolean("ShouldHoldPos"));
        assertEquals(2.0D, recruitLegacy.getDouble("HoldPosX"));

        assertEquals(ownerId, workerLegacy.getUUID("OwnerUUID"));
        assertEquals(boundWorkAreaId, workerLegacy.getUUID("boundWorkArea"));
        assertTrue(workerLegacy.getBoolean("ShouldMovePos"));
        assertEquals(8, workerLegacy.getInt("MovePosX"));
    }

    private static ListTag createItemsTag(int slot, String itemId, int count) {
        CompoundTag itemData = new CompoundTag();
        itemData.putByte("Slot", (byte) slot);
        itemData.putString("id", itemId);
        itemData.putByte("Count", (byte) count);
        ListTag items = new ListTag();
        items.add(itemData);
        return items;
    }
}
