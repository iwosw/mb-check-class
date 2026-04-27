package com.talhanation.bannermod.combat;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommanderAuraPolicyTest {

    private static final UUID FRIENDLY = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID HOSTILE = UUID.fromString("00000000-0000-0000-0000-000000000002");

    @Test
    void sameFactionCommanderInsideRadiusActivatesAura() {
        CommanderAura captain = CommanderAura.at(FRIENDLY, 0, 0, 0);

        assertTrue(CommanderAuraPolicy.isAuraActive(List.of(captain), FRIENDLY, 8, 0, 6));
    }

    @Test
    void sameFactionCommanderOutsideRadiusDoesNotActivate() {
        CommanderAura captain = CommanderAura.at(FRIENDLY, 0, 0, 0);

        // sqrt(20*20 + 0 + 0) = 20 > default 16
        assertFalse(CommanderAuraPolicy.isAuraActive(List.of(captain), FRIENDLY, 20, 0, 0));
    }

    @Test
    void hostileCommanderNeverProjectsAuraToFriendlySquad() {
        CommanderAura enemy = CommanderAura.at(HOSTILE, 0, 0, 0);

        assertFalse(CommanderAuraPolicy.isAuraActive(List.of(enemy), FRIENDLY, 0, 0, 0));
    }

    @Test
    void neutralCommanderWithoutPoliticalEntityNeverProjects() {
        CommanderAura neutral = CommanderAura.at(null, 0, 0, 0);

        assertFalse(CommanderAuraPolicy.isAuraActive(List.of(neutral), FRIENDLY, 0, 0, 0));
    }

    @Test
    void neutralSquadWithoutPoliticalEntityIsNeverBuffed() {
        CommanderAura captain = CommanderAura.at(FRIENDLY, 0, 0, 0);

        assertFalse(CommanderAuraPolicy.isAuraActive(List.of(captain), null, 0, 0, 0));
    }

    @Test
    void multiCommanderListReturnsTrueWhenAnyMatches() {
        CommanderAura far = CommanderAura.at(FRIENDLY, 100, 0, 0);
        CommanderAura near = CommanderAura.at(FRIENDLY, 5, 0, 0);
        CommanderAura enemy = CommanderAura.at(HOSTILE, 0, 0, 0);

        assertTrue(CommanderAuraPolicy.isAuraActive(List.of(far, enemy, near), FRIENDLY, 0, 0, 0));
    }

    @Test
    void emptyOrNullCommanderListIsInert() {
        assertFalse(CommanderAuraPolicy.isAuraActive(List.of(), FRIENDLY, 0, 0, 0));
        assertFalse(CommanderAuraPolicy.isAuraActive(null, FRIENDLY, 0, 0, 0));
    }

    @Test
    void exactRadiusBoundaryIsInclusive() {
        CommanderAura captain = CommanderAura.at(FRIENDLY, 0, 0, 0);

        // Exactly on the radius boundary should still count (squared <= squared).
        assertTrue(CommanderAuraPolicy.isAuraActive(captain, FRIENDLY,
                CommanderAuraPolicy.DEFAULT_AURA_RADIUS_BLOCKS, 0, 0,
                CommanderAuraPolicy.DEFAULT_AURA_RADIUS_BLOCKS));
    }

    @Test
    void distanceCheckIsFullThreeDimensional() {
        CommanderAura captain = CommanderAura.at(FRIENDLY, 0, 0, 0);

        // Pythagoras across all three axes: sqrt(8*8 + 8*8 + 8*8) ~= 13.86 < 16.
        assertTrue(CommanderAuraPolicy.isAuraActive(captain, FRIENDLY, 8, 8, 8,
                CommanderAuraPolicy.DEFAULT_AURA_RADIUS_BLOCKS));

        // sqrt(10*10 + 10*10 + 10*10) ~= 17.32 > 16.
        assertFalse(CommanderAuraPolicy.isAuraActive(captain, FRIENDLY, 10, 10, 10,
                CommanderAuraPolicy.DEFAULT_AURA_RADIUS_BLOCKS));
    }

    @Test
    void zeroOrNegativeRadiusOnlyCoversTheCommanderSquare() {
        CommanderAura captain = CommanderAura.at(FRIENDLY, 0, 0, 0);

        assertTrue(CommanderAuraPolicy.isAuraActive(captain, FRIENDLY, 0, 0, 0, 0.0D));
        assertFalse(CommanderAuraPolicy.isAuraActive(captain, FRIENDLY, 0.5, 0, 0, 0.0D));
        // Negative radius clamped to zero.
        assertTrue(CommanderAuraPolicy.isAuraActive(captain, FRIENDLY, 0, 0, 0, -5.0D));
        assertFalse(CommanderAuraPolicy.isAuraActive(captain, FRIENDLY, 0.5, 0, 0, -5.0D));
    }
}
