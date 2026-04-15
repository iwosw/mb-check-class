package com.talhanation.bannermod.ai.military.controller;

import com.talhanation.bannermod.util.NPCArmy;

public final class BattleTacticDecider {

    private BattleTacticDecider() {
    }

    public static Tactic decide(double distance, ArmySnapshot ownArmy, ArmySnapshot enemyArmy) {
        if (ownArmy.totalUnits() >= 2 * enemyArmy.totalUnits() || ownArmy.averageHealth() > 50) {
            return distance < 1000 ? Tactic.CHARGE : Tactic.ADVANCE;
        }

        if (ownArmy.averageMorale() > 70 && enemyArmy.averageMorale() < 30) {
            return Tactic.ADVANCE;
        }

        if (enemyArmy.totalUnits() >= 2 * ownArmy.totalUnits() || ownArmy.averageMorale() < 20 || enemyArmy.averageHealth() > 50) {
            return Tactic.RETREAT;
        }

        if (enemyArmy.rangedUnits() > ownArmy.cavalryUnits() + ownArmy.shieldUnits()) {
            return Tactic.RETREAT;
        }

        return distance < 1000 ? Tactic.DEFAULT_ATTACK : Tactic.ADVANCE;
    }

    public static ArmySnapshot snapshot(NPCArmy army) {
        return new ArmySnapshot(
                army.getTotalUnits(),
                army.getAverageHealth(),
                army.getAverageMorale(),
                army.getRanged().size(),
                army.getCavalry().size(),
                army.getShieldmen().size(),
                army.getAverageArmor()
        );
    }

    public record ArmySnapshot(
            int totalUnits,
            double averageHealth,
            double averageMorale,
            int rangedUnits,
            int cavalryUnits,
            int shieldUnits,
            double averageArmor
    ) {
    }

    public enum Tactic {
        CHARGE,
        ADVANCE,
        RETREAT,
        DEFAULT_ATTACK
    }
}
