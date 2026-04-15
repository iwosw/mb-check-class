package com.talhanation.workers.entities.ai;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnimalFarmerLoopProgressTest {

    @Test
    void prefersBreedingWhenReadyAndPairsExist() {
        AnimalFarmerLoopProgress.Decision decision = AnimalFarmerLoopProgress.selectNextAction(true, true, 4, true, 1, false, true, 8, 4);

        assertEquals(AnimalFarmerLoopProgress.Action.PREPARE_BREED, decision.action());
        assertTrue(decision.keepCurrentPen());
    }

    @Test
    void fallsThroughToSpecialThenSlaughterAndSupportsDepositWaits() {
        AnimalFarmerLoopProgress.Decision specialDecision = AnimalFarmerLoopProgress.selectNextAction(true, false, 4, true, 1, false, true, 8, 4);
        assertEquals(AnimalFarmerLoopProgress.Action.PREPARE_SPECIAL_TASK, specialDecision.action());

        AnimalFarmerLoopProgress.Decision slaughterDecision = AnimalFarmerLoopProgress.selectNextAction(false, false, 0, false, 0, false, true, 8, 4);
        assertEquals(AnimalFarmerLoopProgress.Action.PREPARE_SLAUGHTER, slaughterDecision.action());

        AnimalFarmerLoopProgress.Decision waitingForDeposit = AnimalFarmerLoopProgress.waitForDeposit(AnimalFarmerLoopProgress.Action.PREPARE_SPECIAL_TASK);
        assertTrue(waitingForDeposit.isWaiting());
        assertTrue(waitingForDeposit.keepCurrentPen());
        assertEquals(AnimalFarmerLoopProgress.Action.PREPARE_SPECIAL_TASK, waitingForDeposit.resumeAction());
    }

    @Test
    void finishesWhenNoPenWorkRemains() {
        AnimalFarmerLoopProgress.Decision decision = AnimalFarmerLoopProgress.selectNextAction(false, false, 0, false, 0, false, false, 0, 4);

        assertTrue(decision.isFinished());
        assertFalse(decision.keepCurrentPen());
    }
}
