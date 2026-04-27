package com.talhanation.bannermod.combat;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Pure role-aware slot assignment for a mixed squad, plus the integrity-penalty math that
 * fires when a unit drifts out of its expected slot zone. Output feeds the upcoming combat-AI
 * formation goal that physically positions members; this layer just decides who goes where.
 *
 * <p>Slot rules (acceptance for COMBAT-003):
 *
 * <ul>
 *   <li>{@link CombatRole#INFANTRY} -> {@link FormationSlot#FRONT_RANK}.</li>
 *   <li>{@link CombatRole#PIKE} -> {@link FormationSlot#SUPPORT_RANK}. Pike sits behind
 *       the shield front so it can brace through gaps when cavalry charges in.</li>
 *   <li>{@link CombatRole#RANGED} -> {@link FormationSlot#REAR_RANK}.</li>
 *   <li>{@link CombatRole#CAVALRY} -> {@link FormationSlot#FLANK}.</li>
 * </ul>
 *
 * <p>Shield-wall pressure: the planner reports whether the front rank has at least
 * {@link #SHIELD_WALL_MIN_FRONT_RANK} infantry — that's the threshold at which the upcoming
 * combat-AI goal will switch the front rank from "hold" to "advance slowly". Below the
 * threshold the front rank holds.
 *
 * <p>Isolation penalty: a unit outside its expected slot's cohesion radius loses formation
 * benefits. The penalty is a multiplier (less than 1) applied to whatever bonus a future
 * combat-math layer wants to scale; we encode it as a pure function so tests cover the
 * thresholds without dragging in entity geometry.
 */
public final class FormationPlanner {

    /** Minimum front-rank headcount that enables shield-wall forward pressure. */
    public static final int SHIELD_WALL_MIN_FRONT_RANK = 3;

    /** Cohesion radius (blocks) inside which a unit is considered "in formation". */
    public static final double FORMATION_COHESION_RADIUS = 8.0D;

    /** Multiplier applied to a unit's bonuses while it is isolated. */
    public static final double ISOLATION_PENALTY_MULTIPLIER = 0.5D;

    private FormationPlanner() {
    }

    public static FormationSlot slotFor(CombatRole role) {
        if (role == null) return FormationSlot.UNASSIGNED;
        return switch (role) {
            case INFANTRY -> FormationSlot.FRONT_RANK;
            case PIKE -> FormationSlot.SUPPORT_RANK;
            case RANGED -> FormationSlot.REAR_RANK;
            case CAVALRY -> FormationSlot.FLANK;
        };
    }

    /**
     * Counts how many members of {@code roles} land in each {@link FormationSlot}. The
     * returned map is insertion-ordered for deterministic UI / audit, with all five slots
     * present even when their count is 0.
     */
    public static Map<FormationSlot, Integer> assign(List<CombatRole> roles) {
        Map<FormationSlot, Integer> counts = new LinkedHashMap<>();
        for (FormationSlot slot : FormationSlot.values()) {
            counts.put(slot, 0);
        }
        if (roles == null) return counts;
        for (CombatRole role : roles) {
            FormationSlot slot = slotFor(role);
            counts.put(slot, counts.get(slot) + 1);
        }
        return counts;
    }

    /**
     * @return {@code true} if the front rank has at least
     *         {@link #SHIELD_WALL_MIN_FRONT_RANK} members. Production AI uses this to
     *         toggle the "advance slowly" behaviour for the shield wall.
     */
    public static boolean shieldWallReady(Map<FormationSlot, Integer> assignment) {
        if (assignment == null) return false;
        Integer front = assignment.get(FormationSlot.FRONT_RANK);
        return front != null && front >= SHIELD_WALL_MIN_FRONT_RANK;
    }

    /**
     * Pure isolation check. {@code distanceFromSlotAnchor} is the 3D distance between the
     * unit and its assigned slot's anchor; pass {@link Double#POSITIVE_INFINITY} when the
     * caller could not locate an anchor (treated as fully isolated).
     */
    public static boolean isIsolated(double distanceFromSlotAnchor) {
        if (Double.isNaN(distanceFromSlotAnchor) || distanceFromSlotAnchor < 0.0D) {
            return true;
        }
        return distanceFromSlotAnchor > FORMATION_COHESION_RADIUS;
    }

    /**
     * Damage / morale bonus multiplier accounting for isolation. {@code 1.0} when the unit
     * is in formation, {@link #ISOLATION_PENALTY_MULTIPLIER} otherwise.
     */
    public static double cohesionMultiplier(double distanceFromSlotAnchor) {
        return isIsolated(distanceFromSlotAnchor) ? ISOLATION_PENALTY_MULTIPLIER : 1.0D;
    }

    /**
     * Convenience: returns a fixed-iteration-order EnumMap for callers who prefer working
     * with the enum-typed map shape directly. Defensive copy of {@link #assign(List)}.
     */
    public static Map<FormationSlot, Integer> toEnumMap(Map<FormationSlot, Integer> assignment) {
        Map<FormationSlot, Integer> result = new EnumMap<>(FormationSlot.class);
        if (assignment != null) {
            for (FormationSlot slot : FormationSlot.values()) {
                result.put(slot, assignment.getOrDefault(slot, 0));
            }
        } else {
            for (FormationSlot slot : FormationSlot.values()) {
                result.put(slot, 0);
            }
        }
        return result;
    }
}
