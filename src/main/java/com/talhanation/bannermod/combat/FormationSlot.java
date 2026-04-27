package com.talhanation.bannermod.combat;

/**
 * Layered formation rank a unit is assigned to by {@link FormationPlanner}.
 *
 * <ul>
 *   <li>{@link #FRONT_RANK} — shield wall / heavy infantry. Absorbs the engagement front.</li>
 *   <li>{@link #SUPPORT_RANK} — second-line infantry / pike. Steps up to fill front-rank
 *       gaps and braces against cavalry.</li>
 *   <li>{@link #REAR_RANK} — ranged backline. Fires through the firing lane gaps.</li>
 *   <li>{@link #FLANK} — cavalry / harassers. Skirts the formation edges.</li>
 *   <li>{@link #UNASSIGNED} — fallback when the role does not map to a slot (e.g. a
 *       neutral mob slipped into the squad list); the planner never produces this for
 *       known {@link CombatRole}s.</li>
 * </ul>
 */
public enum FormationSlot {
    FRONT_RANK,
    SUPPORT_RANK,
    REAR_RANK,
    FLANK,
    UNASSIGNED
}
