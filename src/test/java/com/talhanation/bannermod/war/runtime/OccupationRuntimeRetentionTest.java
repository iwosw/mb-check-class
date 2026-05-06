package com.talhanation.bannermod.war.runtime;

import com.talhanation.bannermod.war.retention.WarRetentionPolicy;
import net.minecraft.world.level.ChunkPos;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class OccupationRuntimeRetentionTest {

    @Test
    void placeBeyondCapEvictsOldestRecords() {
        OccupationRuntime runtime = new OccupationRuntime();
        UUID warId = UUID.randomUUID();
        for (int i = 0; i < 2000; i++) {
            runtime.place(warId, UUID.randomUUID(), UUID.randomUUID(),
                    List.of(new ChunkPos(i, i)), i);
        }
        assertEquals(WarRetentionPolicy.MAX_OCCUPATIONS, runtime.all().size());
        // Newest survived; oldest evicted.
        long oldestStarted = runtime.all().iterator().next().startedAtGameTime();
        assertEquals(2000L - WarRetentionPolicy.MAX_OCCUPATIONS, oldestStarted);
    }

    @Test
    void pruneResolvedDropsOldOccupationsForResolvedWars() {
        OccupationRuntime runtime = new OccupationRuntime();
        UUID resolved = UUID.randomUUID();
        UUID active = UUID.randomUUID();
        long retention = WarRetentionPolicy.resolvedWarRetentionTicks();
        long now = retention + 50_000L;

        OccupationRecord oldResolved = runtime.place(resolved,
                UUID.randomUUID(), UUID.randomUUID(),
                List.of(new ChunkPos(0, 0)), 0L).orElseThrow();
        OccupationRecord recentResolved = runtime.place(resolved,
                UUID.randomUUID(), UUID.randomUUID(),
                List.of(new ChunkPos(1, 1)), now - 10L).orElseThrow();
        OccupationRecord oldActive = runtime.place(active,
                UUID.randomUUID(), UUID.randomUUID(),
                List.of(new ChunkPos(2, 2)), 0L).orElseThrow();

        int removed = runtime.pruneResolved(List.of(resolved), now, retention);
        assertEquals(1, removed);
        assertFalse(runtime.byId(oldResolved.id()).isPresent());
        assertEquals(2, runtime.all().size());
        // Other two survive.
        assertEquals(true, runtime.byId(recentResolved.id()).isPresent());
        assertEquals(true, runtime.byId(oldActive.id()).isPresent());
    }
}
