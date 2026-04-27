package com.talhanation.bannermod.combat;

import java.util.ArrayList;
import java.util.List;

/**
 * Pure resolution of squad morale from a {@link MoraleSnapshot}.
 *
 * <p>The policy aggregates "pressure points" from casualty fraction, outnumbered ratio,
 * isolation, and sustained-fire damage events; then applies "relief points" from commander
 * presence and nearby-ally support. The final state is decided by the net pressure level.
 *
 * <p>The policy intentionally exposes only static evaluation. The combat-AI hookup (rout
 * disengagement, shaken hit-rate dampening) is a follow-up slice and will consume the same
 * {@link MoraleAssessment}. Tunable thresholds are package-private constants so a future
 * Forge-config layer can override them without changing the core decision tree.
 */
public final class MoralePolicy {

    /** Casualty fraction at which the squad is treated as "heavy" losses. */
    public static final double CASUALTY_HEAVY_RATIO = 0.50D;

    /** Casualty fraction at which the squad is treated as "moderate" losses. */
    public static final double CASUALTY_MODERATE_RATIO = 0.25D;

    /** Hostile-to-survivors ratio at or above which the squad counts as "badly outnumbered". */
    public static final double OUTNUMBERED_3X_RATIO = 3.0D;

    /** Hostile-to-survivors ratio at or above which the squad counts as "outnumbered". */
    public static final double OUTNUMBERED_2X_RATIO = 2.0D;

    /** Recent-damage event count at or above which sustained-fire suppression triggers. */
    public static final int SUSTAINED_FIRE_THRESHOLD = 6;

    /** Nearby-ally count at or above which the support relief applies. */
    public static final int SUPPORT_ALLY_THRESHOLD = 3;

    /** Pressure threshold at which the squad routs (before commander relief). */
    private static final int ROUT_PRESSURE_THRESHOLD = 4;

    /** Pressure threshold at which the squad becomes shaken. */
    private static final int SHAKEN_PRESSURE_THRESHOLD = 2;

    private MoralePolicy() {
    }

    public static MoraleAssessment evaluate(MoraleSnapshot snapshot) {
        if (snapshot == null) {
            return new MoraleAssessment(MoraleState.STEADY, List.of());
        }

        List<String> reasons = new ArrayList<>();
        int pressure = 0;

        double casualtyRatio = snapshot.casualtyRatio();
        if (casualtyRatio >= CASUALTY_HEAVY_RATIO) {
            reasons.add("CASUALTIES_HEAVY");
            pressure += 3;
        } else if (casualtyRatio >= CASUALTY_MODERATE_RATIO) {
            reasons.add("CASUALTIES_MODERATE");
            pressure += 1;
        }

        int survivors = snapshot.survivors();
        if (survivors > 0) {
            double outnumberedRatio = (double) snapshot.hostileVisibleSize() / (double) survivors;
            if (outnumberedRatio >= OUTNUMBERED_3X_RATIO) {
                reasons.add("OUTNUMBERED_3X");
                pressure += 3;
            } else if (outnumberedRatio >= OUTNUMBERED_2X_RATIO) {
                reasons.add("OUTNUMBERED_2X");
                pressure += 1;
            }
        } else if (snapshot.hostileVisibleSize() > 0) {
            // Squad fully wiped but the snapshot still records visible hostiles. Treat as the
            // worst possible outnumbering so a follow-up tick reports a coherent ROUTED state.
            reasons.add("OUTNUMBERED_3X");
            pressure += 3;
        }

        if (snapshot.isolated()) {
            reasons.add("ISOLATED");
            pressure += 1;
        }

        if (snapshot.recentDamageEvents() >= SUSTAINED_FIRE_THRESHOLD) {
            reasons.add("SUSTAINED_FIRE");
            pressure += 1;
        }

        // Pre-relief decision.
        MoraleState preReliefState;
        if (pressure >= ROUT_PRESSURE_THRESHOLD) {
            preReliefState = MoraleState.ROUTED;
        } else if (pressure >= SHAKEN_PRESSURE_THRESHOLD) {
            preReliefState = MoraleState.SHAKEN;
        } else {
            preReliefState = MoraleState.STEADY;
        }

        // Apply relief: a present commander downgrades ROUTED -> SHAKEN; nearby allies
        // downgrade SHAKEN -> STEADY when no other pressure remains.
        MoraleState finalState = preReliefState;
        if (snapshot.commanderPresent()) {
            reasons.add("COMMANDER_PRESENT");
            if (finalState == MoraleState.ROUTED) {
                finalState = MoraleState.SHAKEN;
            }
        }
        if (snapshot.nearbyAllyCount() >= SUPPORT_ALLY_THRESHOLD) {
            reasons.add("ALLIES_NEARBY");
            if (finalState == MoraleState.SHAKEN && pressure <= SHAKEN_PRESSURE_THRESHOLD) {
                finalState = MoraleState.STEADY;
            }
        }

        return new MoraleAssessment(finalState, reasons);
    }
}
