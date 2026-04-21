package com.talhanation.bannermod.ai.military;

import java.util.List;

/**
 * Stage 4.B: phalanx cohesion bonus.
 *
 * <p>A recruit is "cohesive" when it stands shoulder-to-shoulder with at least
 * {@link #MIN_ALLIES_FOR_COHESION} other cohort-mates, all within
 * {@code maxDistSqr} of the recruit's own position AND all sharing the recruit's
 * own {@link CombatStance}. When that condition holds the recruit benefits from
 * a 15% damage reduction ({@link #COHESION_REMAINING}).
 *
 * <p>Only {@link CombatStance#LINE_HOLD} and {@link CombatStance#SHIELD_WALL}
 * qualify — a LOOSE stance never triggers cohesion, per the stage spec.
 *
 * <p>Pure helper: no Minecraft types referenced. Callers supply pre-resolved
 * ally positions and stances so the helper stays unit-testable.
 */
public final class FormationCohesion {

    /** Fraction of damage REMAINING when the cohesion bonus applies (15% absorbed). */
    public static final float COHESION_REMAINING = 0.85f;
    /** Minimum OTHER cohort-mates within range and same stance required for cohesion. */
    public static final int MIN_ALLIES_FOR_COHESION = 2;
    /** Default squared distance (2 blocks): a little over an entity width. */
    public static final double DEFAULT_MAX_DIST_SQR = 2.0 * 2.0;

    private FormationCohesion() {
    }

    /**
     * A single allied cohort-mate observation as provided by the caller.
     *
     * @param dx       ally X minus self X
     * @param dz       ally Z minus self Z
     * @param stance   ally's current {@link CombatStance}
     */
    public record AllyObservation(double dx, double dz, CombatStance stance) {
    }

    /**
     * True iff at least {@link #MIN_ALLIES_FOR_COHESION} entries in {@code allies} are
     * within squared distance {@code maxDistSqr} AND share {@code selfStance}, AND
     * {@code selfStance} is one of {@link CombatStance#LINE_HOLD} or
     * {@link CombatStance#SHIELD_WALL}.
     */
    public static boolean isCohesive(List<AllyObservation> allies,
                                     CombatStance selfStance,
                                     double maxDistSqr) {
        if (selfStance == null) {
            return false;
        }
        if (selfStance != CombatStance.LINE_HOLD && selfStance != CombatStance.SHIELD_WALL) {
            return false;
        }
        if (allies == null || allies.isEmpty()) {
            return false;
        }
        int count = 0;
        for (AllyObservation ally : allies) {
            if (ally == null || ally.stance() != selfStance) {
                continue;
            }
            double distSqr = ally.dx() * ally.dx() + ally.dz() * ally.dz();
            if (distSqr <= maxDistSqr) {
                count++;
                if (count >= MIN_ALLIES_FOR_COHESION) {
                    return true;
                }
            }
        }
        return false;
    }
}
