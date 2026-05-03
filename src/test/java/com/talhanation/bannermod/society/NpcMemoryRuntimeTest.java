package com.talhanation.bannermod.society;

import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NpcMemoryRuntimeTest {

    @Test
    void mergesDuplicateMemoryKeysInsteadOfGrowingUnbounded() {
        NpcMemoryRuntime runtime = new NpcMemoryRuntime();
        UUID residentId = UUID.fromString("00000000-0000-0000-0000-000000000101");

        runtime.remember(NpcMemoryRecord.create(
                residentId,
                NpcSocialMemoryType.ASSAULTED_BY_PLAYER,
                NpcSocialMemoryScope.PERSONAL,
                UUID.fromString("00000000-0000-0000-0000-000000000201"),
                40,
                100L,
                200L
        ), 100L);
        runtime.remember(NpcMemoryRecord.create(
                residentId,
                NpcSocialMemoryType.ASSAULTED_BY_PLAYER,
                NpcSocialMemoryScope.PERSONAL,
                UUID.fromString("00000000-0000-0000-0000-000000000201"),
                72,
                2600L,
                5200L
        ), 2600L);

        List<NpcMemoryRecord> stored = runtime.memoriesFor(residentId, 2600L);
        assertEquals(1, stored.size());
        assertEquals(72, stored.getFirst().intensity());
        assertEquals(5200L, stored.getFirst().expiresGameTime());
    }

    @Test
    void moveResidentTransfersMemoryOwnership() {
        NpcMemoryRuntime runtime = new NpcMemoryRuntime();
        UUID oldResidentId = UUID.fromString("00000000-0000-0000-0000-000000000102");
        UUID newResidentId = UUID.fromString("00000000-0000-0000-0000-000000000103");

        runtime.remember(NpcMemoryRecord.create(
                oldResidentId,
                NpcSocialMemoryType.PROTECTED_BY_PLAYER,
                NpcSocialMemoryScope.FAMILY,
                UUID.fromString("00000000-0000-0000-0000-000000000202"),
                55,
                50L,
                500L
        ), 50L);
        runtime.moveResident(oldResidentId, newResidentId, 80L);

        assertTrue(runtime.memoriesFor(oldResidentId, 80L).isEmpty());
        List<NpcMemoryRecord> moved = runtime.memoriesFor(newResidentId, 80L);
        assertEquals(1, moved.size());
        assertEquals(newResidentId, moved.getFirst().residentUuid());
    }

    @Test
    void saveLoadRoundTripPreservesActiveMemories() {
        NpcMemoryRuntime runtime = new NpcMemoryRuntime();
        UUID residentId = UUID.fromString("00000000-0000-0000-0000-000000000104");

        runtime.remember(NpcMemoryRecord.create(
                residentId,
                NpcSocialMemoryType.HOMELESS,
                NpcSocialMemoryScope.HOUSEHOLD,
                null,
                64,
                200L,
                1200L
        ), 200L);

        CompoundTag tag = runtime.toTag();
        NpcMemoryRuntime restored = NpcMemoryRuntime.fromTag(tag);

        List<NpcMemoryRecord> restoredMemories = restored.memoriesFor(residentId, 300L);
        assertEquals(1, restoredMemories.size());
        assertEquals(NpcSocialMemoryType.HOMELESS, restoredMemories.getFirst().type());
        assertEquals(64, restoredMemories.getFirst().intensity());
    }
}
