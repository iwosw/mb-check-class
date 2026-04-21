package com.talhanation.bannermod.ai.military;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Stage 4.B: phalanx cohesion bonus.
 */
class FormationCohesionTest {

    private static final double MAX = FormationCohesion.DEFAULT_MAX_DIST_SQR;

    @Test
    void cohesiveWhenTwoAlliesClose() {
        List<FormationCohesion.AllyObservation> allies = List.of(
                new FormationCohesion.AllyObservation(1.0, 0.0, CombatStance.LINE_HOLD),
                new FormationCohesion.AllyObservation(-1.0, 0.0, CombatStance.LINE_HOLD)
        );
        assertTrue(FormationCohesion.isCohesive(allies, CombatStance.LINE_HOLD, MAX));
    }

    @Test
    void notCohesiveInLooseStance() {
        List<FormationCohesion.AllyObservation> allies = List.of(
                new FormationCohesion.AllyObservation(1.0, 0.0, CombatStance.LOOSE),
                new FormationCohesion.AllyObservation(-1.0, 0.0, CombatStance.LOOSE)
        );
        assertFalse(FormationCohesion.isCohesive(allies, CombatStance.LOOSE, MAX));
    }

    @Test
    void shieldWallAlsoQualifies() {
        List<FormationCohesion.AllyObservation> allies = List.of(
                new FormationCohesion.AllyObservation(0.5, 0.5, CombatStance.SHIELD_WALL),
                new FormationCohesion.AllyObservation(-0.5, 0.5, CombatStance.SHIELD_WALL)
        );
        assertTrue(FormationCohesion.isCohesive(allies, CombatStance.SHIELD_WALL, MAX));
    }

    @Test
    void notCohesiveWhenOnlyOneAllyClose() {
        List<FormationCohesion.AllyObservation> allies = List.of(
                new FormationCohesion.AllyObservation(1.0, 0.0, CombatStance.LINE_HOLD),
                new FormationCohesion.AllyObservation(10.0, 0.0, CombatStance.LINE_HOLD) // too far
        );
        assertFalse(FormationCohesion.isCohesive(allies, CombatStance.LINE_HOLD, MAX));
    }

    @Test
    void notCohesiveWhenAlliesMismatchStance() {
        List<FormationCohesion.AllyObservation> allies = List.of(
                new FormationCohesion.AllyObservation(1.0, 0.0, CombatStance.SHIELD_WALL),
                new FormationCohesion.AllyObservation(-1.0, 0.0, CombatStance.LOOSE)
        );
        assertFalse(FormationCohesion.isCohesive(allies, CombatStance.LINE_HOLD, MAX));
    }

    @Test
    void emptyAllyListIsNotCohesive() {
        assertFalse(FormationCohesion.isCohesive(List.of(), CombatStance.LINE_HOLD, MAX));
    }

    @Test
    void nullAllyListIsNotCohesive() {
        assertFalse(FormationCohesion.isCohesive(null, CombatStance.LINE_HOLD, MAX));
    }

    @Test
    void nullStanceIsNotCohesive() {
        List<FormationCohesion.AllyObservation> allies = List.of(
                new FormationCohesion.AllyObservation(1.0, 0.0, CombatStance.LINE_HOLD),
                new FormationCohesion.AllyObservation(-1.0, 0.0, CombatStance.LINE_HOLD)
        );
        assertFalse(FormationCohesion.isCohesive(allies, null, MAX));
    }

    @Test
    void remainingFractionIsEightyFivePercent() {
        // Sanity-check: the constant is what we expect.
        org.junit.jupiter.api.Assertions.assertEquals(0.85f, FormationCohesion.COHESION_REMAINING, 1e-6f);
    }
}
