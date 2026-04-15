package com.talhanation.recruits.entities.ai.controller;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BattleTacticDeciderTest {

    @Test
    void overwhelmingAdvantageChoosesCharge() {
        BattleTacticDecider.ArmySnapshot ownArmy = new BattleTacticDecider.ArmySnapshot(18, 65.0D, 82.0D, 4, 2, 3, 8.0D);
        BattleTacticDecider.ArmySnapshot enemyArmy = new BattleTacticDecider.ArmySnapshot(7, 22.0D, 28.0D, 1, 0, 1, 8.0D);

        BattleTacticDecider.Tactic tactic = BattleTacticDecider.decide(12.0D, ownArmy, enemyArmy);

        assertEquals(BattleTacticDecider.Tactic.CHARGE, tactic);
    }

    @Test
    void lowMoraleDisadvantageChoosesRetreat() {
        BattleTacticDecider.ArmySnapshot ownArmy = new BattleTacticDecider.ArmySnapshot(6, 18.0D, 15.0D, 1, 0, 0, 8.0D);
        BattleTacticDecider.ArmySnapshot enemyArmy = new BattleTacticDecider.ArmySnapshot(14, 36.0D, 61.0D, 2, 1, 2, 8.0D);

        BattleTacticDecider.Tactic tactic = BattleTacticDecider.decide(18.0D, ownArmy, enemyArmy);

        assertEquals(BattleTacticDecider.Tactic.RETREAT, tactic);
    }

    @Test
    void enemyRangedSuperiorityChoosesRetreatFallback() {
        BattleTacticDecider.ArmySnapshot ownArmy = new BattleTacticDecider.ArmySnapshot(10, 26.0D, 52.0D, 1, 1, 0, 8.0D);
        BattleTacticDecider.ArmySnapshot enemyArmy = new BattleTacticDecider.ArmySnapshot(10, 24.0D, 50.0D, 4, 0, 0, 8.0D);

        BattleTacticDecider.Tactic tactic = BattleTacticDecider.decide(22.0D, ownArmy, enemyArmy);

        assertEquals(BattleTacticDecider.Tactic.RETREAT, tactic);
    }
}
