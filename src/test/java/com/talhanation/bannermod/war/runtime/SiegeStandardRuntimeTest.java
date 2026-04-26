package com.talhanation.bannermod.war.runtime;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SiegeStandardRuntimeTest {

    @Test
    void placeRejectsMissingArguments() {
        SiegeStandardRuntime runtime = new SiegeStandardRuntime();
        assertTrue(runtime.place(null, UUID.randomUUID(), new BlockPos(0, 0, 0), 32, 0L).isEmpty());
        assertTrue(runtime.place(UUID.randomUUID(), null, new BlockPos(0, 0, 0), 32, 0L).isEmpty());
        assertTrue(runtime.place(UUID.randomUUID(), UUID.randomUUID(), null, 32, 0L).isEmpty());
    }

    @Test
    void zoneContainmentRespectsRadius() {
        SiegeStandardRuntime runtime = new SiegeStandardRuntime();
        UUID warId = UUID.randomUUID();
        Optional<SiegeStandardRecord> placed = runtime.place(warId, UUID.randomUUID(),
                new BlockPos(0, 64, 0), 16, 0L);
        assertTrue(placed.isPresent());
        assertTrue(runtime.isInsideAnyZone(new BlockPos(10, 64, 10), Set.of(warId)));
        assertFalse(runtime.isInsideAnyZone(new BlockPos(100, 64, 100), Set.of(warId)));
    }

    @Test
    void zoneIgnoresInactiveWars() {
        SiegeStandardRuntime runtime = new SiegeStandardRuntime();
        UUID warId = UUID.randomUUID();
        runtime.place(warId, UUID.randomUUID(), new BlockPos(0, 64, 0), 32, 0L);
        assertFalse(runtime.isInsideAnyZone(new BlockPos(0, 64, 0), Set.of(UUID.randomUUID())));
    }

    @Test
    void removeClearsRecord() {
        SiegeStandardRuntime runtime = new SiegeStandardRuntime();
        UUID warId = UUID.randomUUID();
        SiegeStandardRecord record = runtime.place(warId, UUID.randomUUID(),
                new BlockPos(0, 64, 0), 32, 0L).orElseThrow();
        assertTrue(runtime.remove(record.id()));
        assertEquals(0, runtime.all().size());
    }
}
