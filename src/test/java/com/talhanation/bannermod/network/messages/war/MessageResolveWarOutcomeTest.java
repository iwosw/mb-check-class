package com.talhanation.bannermod.network.messages.war;

import com.talhanation.bannermod.war.runtime.WarGoalType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MessageResolveWarOutcomeTest {

    @Test
    void tributeSelectionOnlySupportsTributeWars() {
        assertTrue(MessageResolveWarOutcome.isOutcomeSupportedForGoal(
                MessageResolveWarOutcome.Action.TRIBUTE,
                WarGoalType.TRIBUTE));
        assertFalse(MessageResolveWarOutcome.isOutcomeSupportedForGoal(
                MessageResolveWarOutcome.Action.TRIBUTE,
                WarGoalType.OCCUPATION));
    }

    @Test
    void remainingSelectionsRequireMatchingWarGoal() {
        assertTrue(MessageResolveWarOutcome.isOutcomeSupportedForGoal(
                MessageResolveWarOutcome.Action.OCCUPY,
                WarGoalType.OCCUPATION));
        assertTrue(MessageResolveWarOutcome.isOutcomeSupportedForGoal(
                MessageResolveWarOutcome.Action.ANNEX,
                WarGoalType.ANNEX_LIMITED_CHUNKS));
        assertFalse(MessageResolveWarOutcome.isOutcomeSupportedForGoal(
                MessageResolveWarOutcome.Action.OCCUPY,
                WarGoalType.ANNEX_LIMITED_CHUNKS));
    }
}
