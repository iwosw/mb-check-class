package com.talhanation.bannermod.war.client;

import com.talhanation.bannermod.war.runtime.BattleWindowSchedule;
import com.talhanation.bannermod.war.runtime.OccupationRecord;
import com.talhanation.bannermod.war.runtime.RevoltRecord;
import com.talhanation.bannermod.war.runtime.RevoltState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ChunkPos;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WarClientStateTest {

    @AfterEach
    void clear() {
        WarClientState.clear();
    }

    @Test
    void encodeAndDecodePreservesRevoltsAndOccupations() {
        UUID warA = UUID.randomUUID();
        UUID warB = UUID.randomUUID();
        UUID occA = UUID.randomUUID();
        UUID occB = UUID.randomUUID();
        UUID rebelA = UUID.randomUUID();
        UUID occupierA = UUID.randomUUID();

        OccupationRecord occupationA = new OccupationRecord(occA, warA, occupierA, rebelA,
                List.of(new ChunkPos(3, 7)), 100L, 100L);
        OccupationRecord occupationB = new OccupationRecord(occB, warB, UUID.randomUUID(), UUID.randomUUID(),
                List.of(new ChunkPos(-2, 4)), 200L, 200L);
        RevoltRecord revoltA = new RevoltRecord(UUID.randomUUID(), occA, rebelA, occupierA,
                500L, 0L, RevoltState.PENDING);

        CompoundTag payload = WarClientState.encode(
                List.of(),
                List.of(),
                List.of(),
                new BattleWindowSchedule(List.of()),
                List.of(),
                List.of(occupationA, occupationB),
                List.of(revoltA)
        );

        WarClientState.applyFromNbt(payload);

        assertTrue(WarClientState.hasSnapshot());
        assertEquals(2, WarClientState.occupations().size());
        assertEquals(1, WarClientState.revolts().size());
        OccupationRecord roundTripped = WarClientState.occupationById(occA);
        assertNotNull(roundTripped);
        assertEquals(warA, roundTripped.warId());
        assertEquals(new ChunkPos(3, 7), roundTripped.chunks().get(0));
    }

    @Test
    void revoltsForWarFiltersByOccupationOwnership() {
        UUID warA = UUID.randomUUID();
        UUID warB = UUID.randomUUID();
        UUID occA = UUID.randomUUID();
        UUID occB = UUID.randomUUID();

        OccupationRecord occupationA = new OccupationRecord(occA, warA, UUID.randomUUID(), UUID.randomUUID(),
                List.of(new ChunkPos(0, 0)), 100L, 100L);
        OccupationRecord occupationB = new OccupationRecord(occB, warB, UUID.randomUUID(), UUID.randomUUID(),
                List.of(new ChunkPos(1, 1)), 100L, 100L);
        RevoltRecord revoltForA = new RevoltRecord(UUID.randomUUID(), occA, UUID.randomUUID(), UUID.randomUUID(),
                500L, 0L, RevoltState.PENDING);
        RevoltRecord revoltForB = new RevoltRecord(UUID.randomUUID(), occB, UUID.randomUUID(), UUID.randomUUID(),
                500L, 0L, RevoltState.PENDING);

        CompoundTag payload = WarClientState.encode(
                List.of(),
                List.of(),
                List.of(),
                new BattleWindowSchedule(List.of()),
                List.of(),
                List.of(occupationA, occupationB),
                List.of(revoltForA, revoltForB)
        );
        WarClientState.applyFromNbt(payload);

        List<RevoltRecord> warARevolts = WarClientState.revoltsForWar(warA);
        List<RevoltRecord> warBRevolts = WarClientState.revoltsForWar(warB);

        assertEquals(1, warARevolts.size());
        assertEquals(occA, warARevolts.get(0).occupationId());
        assertEquals(1, warBRevolts.size());
        assertEquals(occB, warBRevolts.get(0).occupationId());
    }

    @Test
    void occupationsForWarFiltersDirectlyByWarId() {
        UUID warA = UUID.randomUUID();
        UUID warB = UUID.randomUUID();
        OccupationRecord occupationA = new OccupationRecord(UUID.randomUUID(), warA, UUID.randomUUID(), UUID.randomUUID(),
                List.of(new ChunkPos(0, 0)), 100L, 140L);
        OccupationRecord occupationB = new OccupationRecord(UUID.randomUUID(), warB, UUID.randomUUID(), UUID.randomUUID(),
                List.of(new ChunkPos(1, 1)), 100L, 140L);

        WarClientState.applyFromNbt(WarClientState.encode(
                List.of(), List.of(), List.of(),
                new BattleWindowSchedule(List.of()),
                List.of(),
                List.of(occupationA, occupationB),
                List.of()
        ));

        List<OccupationRecord> warAOccupations = WarClientState.occupationsForWar(warA);

        assertEquals(1, warAOccupations.size());
        assertEquals(occupationA.id(), warAOccupations.get(0).id());
        assertTrue(WarClientState.occupationsForWar(null).isEmpty());
    }

    @Test
    void revoltsForUnknownWarIsEmpty() {
        WarClientState.applyFromNbt(WarClientState.encode(
                List.of(), List.of(), List.of(),
                new BattleWindowSchedule(List.of()),
                List.of(), List.of(), List.of()
        ));
        assertTrue(WarClientState.revoltsForWar(UUID.randomUUID()).isEmpty());
        assertTrue(WarClientState.revoltsForWar(null).isEmpty());
    }

    @Test
    void clearRemovesAllSnapshotState() {
        UUID warA = UUID.randomUUID();
        UUID occA = UUID.randomUUID();
        OccupationRecord occupationA = new OccupationRecord(occA, warA, UUID.randomUUID(), UUID.randomUUID(),
                List.of(new ChunkPos(0, 0)), 0L, 0L);
        RevoltRecord revolt = new RevoltRecord(UUID.randomUUID(), occA, UUID.randomUUID(), UUID.randomUUID(),
                0L, 0L, RevoltState.PENDING);
        WarClientState.applyFromNbt(WarClientState.encode(
                List.of(), List.of(), List.of(),
                new BattleWindowSchedule(List.of()),
                List.of(),
                List.of(occupationA),
                List.of(revolt)
        ));
        assertEquals(1, WarClientState.occupations().size());
        assertEquals(1, WarClientState.revolts().size());

        WarClientState.clear();

        assertFalse(WarClientState.hasSnapshot());
        assertTrue(WarClientState.occupations().isEmpty());
        assertTrue(WarClientState.revolts().isEmpty());
    }

    @Test
    void nullSnapshotClearsSyncReadyFlag() {
        WarClientState.applyFromNbt(new CompoundTag());
        assertTrue(WarClientState.hasSnapshot());

        WarClientState.applyFromNbt(null);

        assertFalse(WarClientState.hasSnapshot());
        assertTrue(WarClientState.entities().isEmpty());
        assertTrue(WarClientState.wars().isEmpty());
    }

    @Test
    void missingTagKeysFallBackToEmptyLists() {
        // Older server sends a snapshot without the new Occupations/Revolts keys; client must
        // tolerate that and report empty rather than NPE the War Room render path.
        CompoundTag minimal = new CompoundTag();
        WarClientState.applyFromNbt(minimal);

        assertTrue(WarClientState.occupations().isEmpty());
        assertTrue(WarClientState.revolts().isEmpty());
        assertTrue(WarClientState.revoltsForWar(UUID.randomUUID()).isEmpty());
    }

    @Test
    void clearAlsoResetsLastActionFeedback() {
        WarClientState.setLastActionFeedback(Component.literal("Updated"));

        WarClientState.clear();

        assertTrue(WarClientState.lastActionFeedback().getString().isBlank());
    }

    @Test
    void nullFeedbackNormalizesToEmptyComponent() {
        WarClientState.setLastActionFeedback(null);

        assertTrue(WarClientState.lastActionFeedback().getString().isBlank());
    }
}
