package com.talhanation.bannermod.ai.military;

/**
 * Pure helper for Step 1.B formation-aware combat leash.
 *
 * <p>Decides whether a recruit may engage (or keep engaging) a target given how
 * far the target is from the recruit's formation slot and the active stance.
 *
 * <p>Framework-free so unit tests do not need a Minecraft runtime.
 */
public final class CombatLeashPolicy {

    /** Pre-Stage-1 leash (13 blocks) when the recruit is in formation and stance is LOOSE. */
    public static final double LOOSE_FORMATION_LEASH_SQR = 169.0D;
    /** Pre-Stage-1 leash (20 blocks) when the recruit is NOT in formation. */
    public static final double FREE_ROAM_LEASH_SQR = 400.0D;
    /** Step 1.B leash (5 blocks) for LINE_HOLD inside formation. */
    public static final double LINE_HOLD_FORMATION_LEASH_SQR = 25.0D;
    /** Step 1.B leash (3 blocks) for SHIELD_WALL inside formation. */
    public static final double SHIELD_WALL_FORMATION_LEASH_SQR = 9.0D;

    private CombatLeashPolicy() {
    }

    /**
     * Returns the maximum squared distance the target may be from the recruit's hold-pos
     * for engagement to be permitted.
     */
    public static double maxEngageDistanceSqr(CombatStance stance, boolean inFormation) {
        if (!inFormation) {
            return FREE_ROAM_LEASH_SQR;
        }
        if (stance == null) {
            return LOOSE_FORMATION_LEASH_SQR;
        }
        switch (stance) {
            case LINE_HOLD:
                return LINE_HOLD_FORMATION_LEASH_SQR;
            case SHIELD_WALL:
                return SHIELD_WALL_FORMATION_LEASH_SQR;
            case LOOSE:
            default:
                return LOOSE_FORMATION_LEASH_SQR;
        }
    }

    /**
     * Decide whether the recruit is allowed to attack / keep attacking given where the
     * target is relative to the recruit's formation hold position.
     *
     * @param targetDistSqrToHoldPos squared distance from the current target to the
     *                               recruit's hold-pos (0 if no hold-pos / not holding).
     * @param holdPosActive          true if the recruit has a hold-pos and should-hold-pos.
     * @param inFormation            true if the recruit is currently in formation.
     * @param stance                 current combat stance.
     */
    public static boolean canEngage(double targetDistSqrToHoldPos,
                                    boolean holdPosActive,
                                    boolean inFormation,
                                    CombatStance stance) {
        if (!holdPosActive) {
            return true;
        }
        return targetDistSqrToHoldPos < maxEngageDistanceSqr(stance, inFormation);
    }

    /**
     * Decide whether the recruit should break engagement because it has drifted too far
     * from its hold-pos. Used from {@code canContinueToUse} to yank a recruit back into
     * formation once it has drifted past the stance-appropriate leash while chasing.
     */
    public static boolean hasDriftedOffLeash(double recruitDistSqrToHoldPos,
                                             boolean holdPosActive,
                                             boolean inFormation,
                                             CombatStance stance) {
        if (!holdPosActive || !inFormation) {
            return false;
        }
        if (stance == null || stance == CombatStance.LOOSE) {
            return false;
        }
        return recruitDistSqrToHoldPos > maxEngageDistanceSqr(stance, true);
    }
}
