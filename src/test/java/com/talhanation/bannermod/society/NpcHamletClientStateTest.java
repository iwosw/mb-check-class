package com.talhanation.bannermod.society;

import com.talhanation.bannermod.society.client.NpcHamletClientState;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NpcHamletClientStateTest {

    @Test
    void appliesSnapshotContractIntoClientMirror() {
        UUID claimId = UUID.fromString("00000000-0000-0000-0000-000000000601");
        UUID hamletId = UUID.fromString("00000000-0000-0000-0000-000000000602");
        UUID householdId = UUID.fromString("00000000-0000-0000-0000-000000000603");

        NpcHamletRecord record = NpcHamletRecord.create(
                hamletId,
                claimId,
                householdId,
                "Veresk",
                new BlockPos(90, 64, 120),
                UUID.fromString("00000000-0000-0000-0000-000000000604"),
                200L
        ).withStatus(NpcHamletStatus.REGISTERED, 220L);

        CompoundTag snapshot = NpcHamletSnapshotContract.encode(
                claimId,
                true,
                "",
                List.of(record)
        );

        NpcHamletClientState.clear();
        NpcHamletClientState.beginSync();
        NpcHamletClientState.applyFromNbt(snapshot);

        assertTrue(NpcHamletClientState.hasSnapshot());
        assertTrue(NpcHamletClientState.hasClaim());
        assertTrue(NpcHamletClientState.canManage());
        assertFalse(NpcHamletClientState.syncPending());
        assertEquals(claimId, NpcHamletClientState.claimUuid());
        assertEquals(1, NpcHamletClientState.hamlets().size());
        NpcHamletRecord mirrored = NpcHamletClientState.hamletById(hamletId);
        assertNotNull(mirrored);
        assertEquals("Veresk", mirrored.name());
        assertEquals(NpcHamletStatus.REGISTERED, mirrored.status());
    }
}
