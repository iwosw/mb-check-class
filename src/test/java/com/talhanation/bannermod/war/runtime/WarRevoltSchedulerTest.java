package com.talhanation.bannermod.war.runtime;

import com.talhanation.bannermod.war.audit.WarAuditEntry;
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

    private static ObjectivePresenceProbe fixedProbe(int rebels, int occupiers) {
        return (chunk, rebelId, occupierId) -> new ObjectivePresenceProbe.PresenceCounts(rebels, occupiers);
    }

    @Test
    void closedWindowResolvesNothing() {
        RevoltRuntime revolts = new RevoltRuntime();
        OccupationRuntime occupations = new OccupationRuntime();
        OccupationRecord occupation = occupations.place(UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), List.of(new ChunkPos(0, 0)), 0L).orElseThrow();
        revolts.schedule(occupation.id(), UUID.randomUUID(), UUID.randomUUID(), 0L);

        int count = WarRevoltScheduler.tick(revolts,
                occupations,
                applierFor(new WarDeclarationRuntime(), occupations, new WarAuditLogSavedData()),
                fixedProbe(5, 0),
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
                occupations,
                applierFor(new WarDeclarationRuntime(), occupations, new WarAuditLogSavedData()),
                fixedProbe(5, 0),
                500L,
                true);

        assertEquals(0, count);
        assertTrue(revolts.hasPendingRevoltFor(occupation.id()));
    }

    @Test
    void emptyObjectiveLeavesRevoltPendingAndOccupationInPlace() {
        RevoltRuntime revolts = new RevoltRuntime();
        OccupationRuntime occupations = new OccupationRuntime();
        OccupationRecord occupation = occupations.place(UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), List.of(new ChunkPos(0, 0)), 0L).orElseThrow();
        RevoltRecord revolt = revolts.schedule(occupation.id(), UUID.randomUUID(),
                UUID.randomUUID(), 100L).orElseThrow();
        WarAuditLogSavedData audit = new WarAuditLogSavedData();

        int count = WarRevoltScheduler.tick(revolts,
                occupations,
                applierFor(new WarDeclarationRuntime(), occupations, audit),
                fixedProbe(0, 0),
                200L,
                true);

        assertEquals(0, count);
        assertTrue(revolts.hasPendingRevoltFor(occupation.id()));
        assertEquals(RevoltState.PENDING, revolts.byId(revolt.id()).orElseThrow().state());
        assertTrue(occupations.byId(occupation.id()).isPresent());
        assertTrue(audit.all().isEmpty());
    }

    @Test
    void rebelControlledObjectiveSucceedsAndRemovesOccupation() {
        RevoltRuntime revolts = new RevoltRuntime();
        OccupationRuntime occupations = new OccupationRuntime();
        OccupationRecord occupation = occupations.place(UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), List.of(new ChunkPos(0, 0)), 0L).orElseThrow();
        RevoltRecord revolt = revolts.schedule(occupation.id(), UUID.randomUUID(),
                UUID.randomUUID(), 100L).orElseThrow();
        WarAuditLogSavedData audit = new WarAuditLogSavedData();

        int count = WarRevoltScheduler.tick(revolts,
                occupations,
                applierFor(new WarDeclarationRuntime(), occupations, audit),
                fixedProbe(3, 0),
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
    void occupierDefenseFailsTheRevoltAndKeepsTheOccupation() {
        RevoltRuntime revolts = new RevoltRuntime();
        OccupationRuntime occupations = new OccupationRuntime();
        OccupationRecord occupation = occupations.place(UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), List.of(new ChunkPos(0, 0)), 0L).orElseThrow();
        RevoltRecord revolt = revolts.schedule(occupation.id(), UUID.randomUUID(),
                UUID.randomUUID(), 100L).orElseThrow();
        WarAuditLogSavedData audit = new WarAuditLogSavedData();

        int count = WarRevoltScheduler.tick(revolts,
                occupations,
                applierFor(new WarDeclarationRuntime(), occupations, audit),
                fixedProbe(0, 1),
                200L,
                true);

        assertEquals(1, count);
        assertFalse(revolts.hasPendingRevoltFor(occupation.id()));
        assertEquals(RevoltState.FAILED, revolts.byId(revolt.id()).orElseThrow().state());
        assertTrue(occupations.byId(occupation.id()).isPresent());
        assertEquals(1, audit.all().size());
        WarAuditEntry entry = audit.all().get(0);
        assertEquals("REVOLT_FAILED", entry.type());
        assertTrue(entry.detail().contains("occupierPresence=1"));
        assertTrue(entry.detail().contains("rebelPresence=0"));
    }

    @Test
    void contestedObjectiveFailsTheRevoltOnDefenderTiebreak() {
        RevoltRuntime revolts = new RevoltRuntime();
        OccupationRuntime occupations = new OccupationRuntime();
        OccupationRecord occupation = occupations.place(UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), List.of(new ChunkPos(0, 0)), 0L).orElseThrow();
        RevoltRecord revolt = revolts.schedule(occupation.id(), UUID.randomUUID(),
                UUID.randomUUID(), 100L).orElseThrow();
        WarAuditLogSavedData audit = new WarAuditLogSavedData();

        int count = WarRevoltScheduler.tick(revolts,
                occupations,
                applierFor(new WarDeclarationRuntime(), occupations, audit),
                fixedProbe(5, 1),
                200L,
                true);

        assertEquals(1, count);
        assertEquals(RevoltState.FAILED, revolts.byId(revolt.id()).orElseThrow().state());
        assertTrue(occupations.byId(occupation.id()).isPresent());
        assertEquals("REVOLT_FAILED", audit.all().get(0).type());
    }

    @Test
    void revoltAgainstAlreadyRemovedOccupationFailsWithDedicatedAudit() {
        RevoltRuntime revolts = new RevoltRuntime();
        OccupationRuntime occupations = new OccupationRuntime();
        OccupationRecord occupation = occupations.place(UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), List.of(new ChunkPos(0, 0)), 0L).orElseThrow();
        RevoltRecord revolt = revolts.schedule(occupation.id(), UUID.randomUUID(),
                UUID.randomUUID(), 100L).orElseThrow();
        WarAuditLogSavedData audit = new WarAuditLogSavedData();
        // External admin override removed the occupation between schedule and resolution.
        occupations.remove(occupation.id());

        int count = WarRevoltScheduler.tick(revolts,
                occupations,
                applierFor(new WarDeclarationRuntime(), occupations, audit),
                fixedProbe(5, 0),
                200L,
                true);

        assertEquals(1, count);
        assertEquals(RevoltState.FAILED, revolts.byId(revolt.id()).orElseThrow().state());
        assertEquals(1, audit.all().size());
        assertEquals("REVOLT_FAILED_NO_OCCUPATION", audit.all().get(0).type());
    }

    @Test
    void resolvedRevoltsAreSkippedOnNextTick() {
        RevoltRuntime revolts = new RevoltRuntime();
        OccupationRuntime occupations = new OccupationRuntime();
        OccupationRecord occupation = occupations.place(UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), List.of(new ChunkPos(0, 0)), 0L).orElseThrow();
        revolts.schedule(occupation.id(), UUID.randomUUID(), UUID.randomUUID(), 0L);
        WarOutcomeApplier applier = applierFor(new WarDeclarationRuntime(), occupations, new WarAuditLogSavedData());

        WarRevoltScheduler.tick(revolts, occupations, applier, fixedProbe(3, 0), 10L, true);
        int second = WarRevoltScheduler.tick(revolts, occupations, applier, fixedProbe(3, 0), 20L, true);

        assertEquals(0, second);
    }

    @Test
    void nullProbeShortCircuitsToZeroResolutions() {
        RevoltRuntime revolts = new RevoltRuntime();
        OccupationRuntime occupations = new OccupationRuntime();
        OccupationRecord occupation = occupations.place(UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), List.of(new ChunkPos(0, 0)), 0L).orElseThrow();
        revolts.schedule(occupation.id(), UUID.randomUUID(), UUID.randomUUID(), 0L);

        int count = WarRevoltScheduler.tick(revolts,
                occupations,
                applierFor(new WarDeclarationRuntime(), occupations, new WarAuditLogSavedData()),
                null,
                100L,
                true);

        assertEquals(0, count);
        assertTrue(revolts.hasPendingRevoltFor(occupation.id()));
    }
}
