package com.talhanation.bannermod.war.runtime;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RevoltOutcomePolicyTest {

    @Test
    void emptyObjectiveStaysPending() {
        assertEquals(RevoltState.PENDING, RevoltOutcomePolicy.evaluate(0, 0));
    }

    @Test
    void rebelOnlyPresenceSucceeds() {
        assertEquals(RevoltState.SUCCESS, RevoltOutcomePolicy.evaluate(1, 0));
        assertEquals(RevoltState.SUCCESS, RevoltOutcomePolicy.evaluate(50, 0));
    }

    @Test
    void anyOccupierPresenceFails() {
        assertEquals(RevoltState.FAILED, RevoltOutcomePolicy.evaluate(0, 1));
        assertEquals(RevoltState.FAILED, RevoltOutcomePolicy.evaluate(50, 1));
        assertEquals(RevoltState.FAILED, RevoltOutcomePolicy.evaluate(0, 50));
    }

    @Test
    void negativeCountsAreClampedToZero() {
        assertEquals(RevoltState.PENDING, RevoltOutcomePolicy.evaluate(-3, -2));
        assertEquals(RevoltState.SUCCESS, RevoltOutcomePolicy.evaluate(2, -1));
        assertEquals(RevoltState.FAILED, RevoltOutcomePolicy.evaluate(-1, 2));
    }
}
