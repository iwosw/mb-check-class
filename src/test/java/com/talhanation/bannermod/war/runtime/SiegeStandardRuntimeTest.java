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

    @Test
    void applyDamageDrainsControlPoolAndFlagsDestroyedOnce() {
        SiegeStandardRuntime runtime = new SiegeStandardRuntime();
        UUID warId = UUID.randomUUID();
        SiegeStandardRecord record = runtime.place(warId, UUID.randomUUID(),
                new BlockPos(0, 64, 0), 16, 0L).orElseThrow();
        int max = record.maxControlPool();

        com.talhanation.bannermod.combat.SiegeObjectivePolicy.DamageOutcome first =
                runtime.applyDamage(record.id(), 30).orElseThrow();
        assertEquals(max - 30, first.controlAfter());
        assertFalse(first.destroyed());

        // Second hit takes pool to zero — destroyed flag fires exactly here.
        com.talhanation.bannermod.combat.SiegeObjectivePolicy.DamageOutcome killing =
                runtime.applyDamage(record.id(), max).orElseThrow();
        assertEquals(0, killing.controlAfter());
        assertTrue(killing.destroyed());

        // Follow-up hit on a record that's already at zero must NOT re-fire destroyed,
        // so the audit log writes the destruction event exactly once even if the block
        // hangs around for another tick before the caller removes it.
        com.talhanation.bannermod.combat.SiegeObjectivePolicy.DamageOutcome cleanup =
                runtime.applyDamage(record.id(), 5).orElseThrow();
        assertEquals(0, cleanup.controlAfter());
        assertFalse(cleanup.destroyed());
    }

    @Test
    void byPosFindsRecordOrEmpty() {
        SiegeStandardRuntime runtime = new SiegeStandardRuntime();
        UUID warId = UUID.randomUUID();
        SiegeStandardRecord record = runtime.place(warId, UUID.randomUUID(),
                new BlockPos(5, 64, 5), 16, 0L).orElseThrow();
        assertEquals(record.id(), runtime.byPos(new BlockPos(5, 64, 5)).orElseThrow().id());
        assertTrue(runtime.byPos(new BlockPos(99, 64, 99)).isEmpty());
        assertTrue(runtime.byPos(null).isEmpty());
    }

    @Test
    void recordTagRoundTripPreservesControlPool() {
        SiegeStandardRecord original = new SiegeStandardRecord(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                new BlockPos(1, 64, 1), 16, 100L, 42, 100);
        SiegeStandardRecord restored = SiegeStandardRecord.fromTag(original.toTag());
        assertEquals(original, restored);
        assertEquals(42, restored.controlPool());
        assertEquals(100, restored.maxControlPool());
    }
}
