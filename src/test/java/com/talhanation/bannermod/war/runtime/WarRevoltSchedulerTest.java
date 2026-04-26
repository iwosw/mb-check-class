package com.talhanation.bannermod.war.runtime;

import com.talhanation.bannermod.war.audit.WarAuditLogSavedData;
import com.talhanation.bannermod.war.registry.PoliticalRegistryRuntime;
import net.minecraft.world.level.ChunkPos;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WarRevoltSchedulerTest {

    private static WarOutcomeApplier applierFor(WarDeclarationRuntime declarations,
                                                OccupationRuntime occupations,
                                                WarAuditLogSavedData audit) {
        return new WarOutcomeApplier(
                declarations,
                new SiegeStandardRuntime(),
                audit,
                occupations,
                new DemilitarizationRuntime(),
                new PoliticalRegistryRuntime()
        );
    }

    @Test
    void closedWindowResolvesNothing() {
        RevoltRuntime revolts = new RevoltRuntime();
        OccupationRuntime occupations = new OccupationRuntime();
        OccupationRecord occupation = occupations.place(UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), List.of(new ChunkPos(0, 0)), 0L).orElseThrow();
        revolts.schedule(occupation.id(), UUID.randomUUID(), UUID.randomUUID(), 0L);

        int count = WarRevoltScheduler.tick(revolts,
                applierFor(new WarDeclarationRuntime(), occupations, new WarAuditLogSavedData()),
                100L,
                false);

        assertEquals(0, count);
        assertTrue(revolts.hasPendingRevoltFor(occupation.id()));
        assertTrue(occupations.byId(occupation.id()).isPresent());
    }

    @Test
    void notDueRevoltStaysPending() {
        RevoltRuntime revolts = new RevoltRuntime();
        OccupationRuntime occupations = new OccupationRuntime();
        OccupationRecord occupation = occupations.place(UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), List.of(new ChunkPos(0, 0)), 0L).orElseThrow();
        revolts.schedule(occupation.id(), UUID.randomUUID(), UUID.randomUUID(), 1000L);

        int count = WarRevoltScheduler.tick(revolts,
                applierFor(new WarDeclarationRuntime(), occupations, new WarAuditLogSavedData()),
                500L,
                true);

        assertEquals(0, count);
        assertTrue(revolts.hasPendingRevoltFor(occupation.id()));
    }

    @Test
    void dueRevoltResolvesSuccessAndRemovesOccupation() {
        RevoltRuntime revolts = new RevoltRuntime();
        OccupationRuntime occupations = new OccupationRuntime();
        OccupationRecord occupation = occupations.place(UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), List.of(new ChunkPos(0, 0)), 0L).orElseThrow();
        RevoltRecord revolt = revolts.schedule(occupation.id(), UUID.randomUUID(),
                UUID.randomUUID(), 100L).orElseThrow();
        WarAuditLogSavedData audit = new WarAuditLogSavedData();

        int count = WarRevoltScheduler.tick(revolts,
                applierFor(new WarDeclarationRuntime(), occupations, audit),
                200L,
                true);

        assertEquals(1, count);
        assertFalse(revolts.hasPendingRevoltFor(occupation.id()));
        assertEquals(RevoltState.SUCCESS, revolts.byId(revolt.id()).orElseThrow().state());
        assertTrue(occupations.byId(occupation.id()).isEmpty());
        assertEquals(1, audit.all().size());
        assertEquals("REVOLT_SUCCESS", audit.all().get(0).type());
    }

    @Test
    void resolvedRevoltsAreSkippedOnNextTick() {
        RevoltRuntime revolts = new RevoltRuntime();
        OccupationRuntime occupations = new OccupationRuntime();
        OccupationRecord occupation = occupations.place(UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), List.of(new ChunkPos(0, 0)), 0L).orElseThrow();
        revolts.schedule(occupation.id(), UUID.randomUUID(), UUID.randomUUID(), 0L);
        WarOutcomeApplier applier = applierFor(new WarDeclarationRuntime(), occupations, new WarAuditLogSavedData());

        WarRevoltScheduler.tick(revolts, applier, 10L, true);
        int second = WarRevoltScheduler.tick(revolts, applier, 20L, true);

        assertEquals(0, second);
    }
}
