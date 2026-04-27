package com.talhanation.bannermod.combat;

import java.util.List;

/**
 * Output of {@link MoralePolicy#evaluate(MoraleSnapshot)}: the resolved {@link MoraleState}
 * plus the ordered list of pressure / relief reason tokens that produced it. Reasons are
 * stable strings the future UI / audit log can format without re-running the policy.
 *
 * <p>Token vocabulary:
 *
 * <ul>
 *   <li>{@code CASUALTIES_HEAVY} — the squad lost &gt;= 50% of its starting strength.</li>
 *   <li>{@code CASUALTIES_MODERATE} — the squad lost &gt;= 25% of its starting strength.</li>
 *   <li>{@code OUTNUMBERED_3X} — visible hostiles outnumber survivors at least 3:1.</li>
 *   <li>{@code OUTNUMBERED_2X} — visible hostiles outnumber survivors at least 2:1.</li>
 *   <li>{@code ISOLATED} — squad has no nearby same-side support.</li>
 *   <li>{@code SUSTAINED_FIRE} — recent-damage event count above the suppression threshold.</li>
 *   <li>{@code COMMANDER_PRESENT} — a captain/commander is alive in aura range; downgrades
 *       ROUTED to SHAKEN. Always recorded when applicable so the UI can explain the relief.</li>
 *   <li>{@code ALLIES_NEARBY} — at least 3 friendly actors are nearby; downgrades SHAKEN to
 *       STEADY when no other pressure remains.</li>
 * </ul>
 */
public record MoraleAssessment(MoraleState state, List<String> reasons) {
    public MoraleAssessment {
        reasons = reasons == null ? List.of() : List.copyOf(reasons);
    }
}
