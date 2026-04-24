package com.talhanation.bannermod.army.command;

import com.talhanation.bannermod.ai.military.CombatStance;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CommandIntentShapeTest {

    @Test
    void movementReportsCorrectType() {
        CommandIntent intent = new CommandIntent.Movement(
                1L, CommandIntentPriority.NORMAL, false,
                2, 1, true, null);
        assertEquals(CommandIntentType.MOVEMENT, intent.type());
    }

    @Test
    void faceReportsCorrectType() {
        CommandIntent intent = new CommandIntent.Face(
                10L, CommandIntentPriority.LOW, false, 3, false);
        assertEquals(CommandIntentType.FACE, intent.type());
    }

    @Test
    void attackReportsCorrectType() {
        CommandIntent intent = new CommandIntent.Attack(
                20L, CommandIntentPriority.HIGH, false, UUID.randomUUID());
        assertEquals(CommandIntentType.ATTACK, intent.type());
    }

    @Test
    void strategicFireReportsCorrectType() {
        CommandIntent intent = new CommandIntent.StrategicFire(
                30L, CommandIntentPriority.NORMAL, false, UUID.randomUUID(), true);
        assertEquals(CommandIntentType.STRATEGIC_FIRE, intent.type());
    }

    @Test
    void aggroReportsCorrectType() {
        CommandIntent intent = new CommandIntent.Aggro(
                40L, CommandIntentPriority.NORMAL, false, 2, UUID.randomUUID(), true);
        assertEquals(CommandIntentType.AGGRO, intent.type());
    }

    @Test
    void combatStanceReportsCorrectType() {
        CommandIntent intent = new CommandIntent.CombatStanceChange(
                50L, CommandIntentPriority.NORMAL, false, CombatStance.SHIELD_WALL, UUID.randomUUID());
        assertEquals(CommandIntentType.COMBAT_STANCE, intent.type());
    }

    @Test
    void metaFieldsArePreservedOnAllVariants() {
        CommandIntent move = new CommandIntent.Movement(
                1L, CommandIntentPriority.IMMEDIATE, true, 6, 1, false, null);
        assertEquals(1L, move.issuedAtGameTime());
        assertEquals(CommandIntentPriority.IMMEDIATE, move.priority());
        assertEquals(true, move.queueMode());

        CommandIntent face = new CommandIntent.Face(
                2L, CommandIntentPriority.LOW, false, 3, false);
        assertEquals(CommandIntentPriority.LOW, face.priority());
    }
}
