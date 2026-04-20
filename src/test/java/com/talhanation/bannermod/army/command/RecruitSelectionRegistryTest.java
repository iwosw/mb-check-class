package com.talhanation.bannermod.army.command;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecruitSelectionRegistryTest {

    private static final UUID PLAYER_A = UUID.fromString("00000000-0000-0000-0000-0000000000a1");
    private static final UUID PLAYER_B = UUID.fromString("00000000-0000-0000-0000-0000000000a2");
    private static final UUID RECRUIT_1 = UUID.fromString("00000000-0000-0000-0000-0000000000b1");
    private static final UUID RECRUIT_2 = UUID.fromString("00000000-0000-0000-0000-0000000000b2");
    private static final UUID RECRUIT_3 = UUID.fromString("00000000-0000-0000-0000-0000000000b3");

    @BeforeEach
    void reset() {
        RecruitSelectionRegistry.instance().clearAllForTest();
    }

    @Test
    void nullArgsAreIgnored() {
        RecruitSelectionRegistry.instance().set(null, Set.of(RECRUIT_1));
        assertEquals(0, RecruitSelectionRegistry.instance().sizeFor(null));
        assertTrue(RecruitSelectionRegistry.instance().get(null).isEmpty());
    }

    @Test
    void setReplacesExistingSelection() {
        RecruitSelectionRegistry.instance().set(PLAYER_A, Set.of(RECRUIT_1, RECRUIT_2));
        assertEquals(2, RecruitSelectionRegistry.instance().sizeFor(PLAYER_A));

        RecruitSelectionRegistry.instance().set(PLAYER_A, Set.of(RECRUIT_3));
        assertEquals(1, RecruitSelectionRegistry.instance().sizeFor(PLAYER_A));
        assertTrue(RecruitSelectionRegistry.instance().get(PLAYER_A).contains(RECRUIT_3));
    }

    @Test
    void setWithEmptyClears() {
        RecruitSelectionRegistry.instance().set(PLAYER_A, Set.of(RECRUIT_1));
        RecruitSelectionRegistry.instance().set(PLAYER_A, Set.of());
        assertTrue(RecruitSelectionRegistry.instance().isEmpty(PLAYER_A));
    }

    @Test
    void addAndRemoveManageIndividualEntries() {
        RecruitSelectionRegistry.instance().add(PLAYER_A, RECRUIT_1);
        RecruitSelectionRegistry.instance().add(PLAYER_A, RECRUIT_2);
        assertEquals(2, RecruitSelectionRegistry.instance().sizeFor(PLAYER_A));

        RecruitSelectionRegistry.instance().remove(PLAYER_A, RECRUIT_1);
        assertEquals(1, RecruitSelectionRegistry.instance().sizeFor(PLAYER_A));
        assertFalse(RecruitSelectionRegistry.instance().get(PLAYER_A).contains(RECRUIT_1));
    }

    @Test
    void perPlayerIsolation() {
        RecruitSelectionRegistry.instance().set(PLAYER_A, Set.of(RECRUIT_1));
        RecruitSelectionRegistry.instance().set(PLAYER_B, Set.of(RECRUIT_2, RECRUIT_3));

        assertEquals(1, RecruitSelectionRegistry.instance().sizeFor(PLAYER_A));
        assertEquals(2, RecruitSelectionRegistry.instance().sizeFor(PLAYER_B));
    }

    @Test
    void clearRemovesForOnlyOnePlayer() {
        RecruitSelectionRegistry.instance().set(PLAYER_A, Set.of(RECRUIT_1));
        RecruitSelectionRegistry.instance().set(PLAYER_B, Set.of(RECRUIT_2));

        RecruitSelectionRegistry.instance().clear(PLAYER_A);

        assertEquals(0, RecruitSelectionRegistry.instance().sizeFor(PLAYER_A));
        assertEquals(1, RecruitSelectionRegistry.instance().sizeFor(PLAYER_B));
    }
}
