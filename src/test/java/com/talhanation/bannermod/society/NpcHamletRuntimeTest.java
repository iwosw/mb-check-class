package com.talhanation.bannermod.society;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NpcHamletRuntimeTest {

    @Test
    void nearbyRemoteHouseholdsClusterIntoOneHamlet() {
        NpcHamletRuntime runtime = new NpcHamletRuntime();
        UUID claimId = UUID.fromString("00000000-0000-0000-0000-000000000301");
        UUID firstHousehold = UUID.fromString("00000000-0000-0000-0000-000000000401");
        UUID secondHousehold = UUID.fromString("00000000-0000-0000-0000-000000000402");

        NpcHamletRecord created = runtime.reconcileHousehold(
                claimId,
                firstHousehold,
                UUID.fromString("00000000-0000-0000-0000-000000000501"),
                new BlockPos(100, 64, 100),
                200L
        );
        NpcHamletRecord joined = runtime.reconcileHousehold(
                claimId,
                secondHousehold,
                UUID.fromString("00000000-0000-0000-0000-000000000502"),
                new BlockPos(112, 64, 104),
                220L
        );

        assertNotNull(created);
        assertNotNull(joined);
        assertEquals(created.hamletId(), joined.hamletId());
        assertEquals(2, joined.householdCount());
        assertTrue(runtime.hamletForHousehold(secondHousehold).isPresent());
    }

    @Test
    void renameAndSaveLoadPreserveHamletIdentity() {
        NpcHamletRuntime runtime = new NpcHamletRuntime();
        UUID claimId = UUID.fromString("00000000-0000-0000-0000-000000000302");
        UUID householdId = UUID.fromString("00000000-0000-0000-0000-000000000403");

        NpcHamletRecord created = runtime.reconcileHousehold(
                claimId,
                householdId,
                UUID.fromString("00000000-0000-0000-0000-000000000503"),
                new BlockPos(140, 65, 180),
                300L
        );
        NpcHamletRecord renamed = runtime.rename(created.hamletId(), "Berezki", 320L);
        NpcHamletRecord registered = runtime.register(created.hamletId(), 340L);

        CompoundTag tag = runtime.toTag();
        NpcHamletRuntime restored = NpcHamletRuntime.fromTag(tag);
        NpcHamletRecord loaded = restored.hamletFor(created.hamletId()).orElseThrow();

        assertEquals("Berezki", renamed.name());
        assertEquals(NpcHamletStatus.REGISTERED, registered.status());
        assertEquals("Berezki", loaded.name());
        assertEquals(NpcHamletStatus.REGISTERED, loaded.status());
        assertEquals(1, loaded.householdCount());
    }

    @Test
    void removingLastHouseholdLeavesAbandonedHamletRecord() {
        NpcHamletRuntime runtime = new NpcHamletRuntime();
        UUID claimId = UUID.fromString("00000000-0000-0000-0000-000000000303");
        UUID householdId = UUID.fromString("00000000-0000-0000-0000-000000000404");

        NpcHamletRecord created = runtime.reconcileHousehold(
                claimId,
                householdId,
                UUID.fromString("00000000-0000-0000-0000-000000000504"),
                new BlockPos(180, 70, 220),
                400L
        );
        NpcHamletRecord removed = runtime.removeHousehold(householdId, 440L);

        assertNotNull(removed);
        assertEquals(NpcHamletStatus.ABANDONED, removed.status());
        assertTrue(runtime.hamletFor(created.hamletId()).isPresent());
        assertTrue(runtime.hamletForHousehold(householdId).isEmpty());
    }

    @Test
    void cooldownAndNameValidationAreEnforced() {
        NpcHamletRuntime runtime = new NpcHamletRuntime();
        UUID claimId = UUID.fromString("00000000-0000-0000-0000-000000000304");
        UUID householdId = UUID.fromString("00000000-0000-0000-0000-000000000405");
        UUID secondHousehold = UUID.fromString("00000000-0000-0000-0000-000000000406");

        NpcHamletRecord first = runtime.reconcileHousehold(
                claimId,
                householdId,
                UUID.fromString("00000000-0000-0000-0000-000000000505"),
                new BlockPos(220, 68, 260),
                500L
        );
        NpcHamletRecord second = runtime.reconcileHousehold(
                claimId,
                secondHousehold,
                UUID.fromString("00000000-0000-0000-0000-000000000506"),
                new BlockPos(320, 68, 360),
                520L
        );

        runtime.rename(first.hamletId(), "Veresk", 540L);
        assertTrue(runtime.noteHostileAction(first.hamletId(), 600L, 1200L));
        assertFalse(runtime.noteHostileAction(first.hamletId(), 900L, 1200L));
        assertTrue(runtime.noteHostileAction(first.hamletId(), 1900L, 1200L));
        assertThrows(IllegalArgumentException.class, () -> runtime.rename(second.hamletId(), "Veresk", 560L));
        assertThrows(IllegalArgumentException.class, () -> runtime.rename(second.hamletId(), "  ", 560L));
    }
}
